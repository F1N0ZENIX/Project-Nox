package com.projectnox.app

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.WindowCompat
import java.io.BufferedWriter
import java.io.OutputStreamWriter

class DesktopActivity : Activity() {
    private lateinit var log: TextView
    private lateinit var web: WebView
    private lateinit var content: LinearLayout
    private var shell: Process? = null
    private var writer: BufferedWriter? = null
    private val black = Color.BLACK
    private val white = Color.WHITE
    private val grey = Color.rgb(165, 165, 165)
    private val setupCommand = """
        set -e
        export DEBIAN_FRONTEND=noninteractive
        echo '[Nox] Updating Ubuntu package lists…'
        apt-get update
        echo '[Nox] Installing XFCE, TigerVNC and noVNC…'
        apt-get install -y xfce4 dbus-x11 tigervnc-standalone-server novnc websockify
        mkdir -p /root/.vnc
        printf '#!/bin/sh\nunset SESSION_MANAGER\nunset DBUS_SESSION_BUS_ADDRESS\nexec dbus-launch --exit-with-session startxfce4\n' > /root/.vnc/xstartup
        chmod +x /root/.vnc/xstartup
        (vncserver -kill :1 >/dev/null 2>&1 || true)
        vncserver :1 -localhost yes -geometry 1280x720 -depth 24 -SecurityTypes None
        (pkill -f 'websockify.*6080' >/dev/null 2>&1 || true)
        nohup websockify --web=/usr/share/novnc 127.0.0.1:6080 127.0.0.1:5901 >/tmp/nox-websockify.log 2>&1 &
        sleep 2
        echo NOX_DESKTOP_READY
    """.trimIndent().replace("
", "; ")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = black
        window.navigationBarColor = black
        buildUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(black); setPadding(dp(16), dp(12), dp(16), dp(8)) }
        setContentView(root)
        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        header.addView(TextView(this).apply { text = "NOX / DESKTOP"; textSize = 19f; setTextColor(white); letterSpacing = .12f }, LinearLayout.LayoutParams(0, dp(48), 1f))
        header.addView(button("CLOSE") { finish() }, LinearLayout.LayoutParams(dp(82), dp(42)))
        root.addView(header)
        root.addView(TextView(this).apply { text = "XFCE WORKSPACE  ·  UBUNTU ARM64"; textSize = 10f; setTextColor(grey); setPadding(0, dp(4), 0, dp(12)) })
        web = WebView(this).apply {
            setBackgroundColor(Color.rgb(8,8,8))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
        }
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(black) }
        val scroll = ScrollView(this)
        val panel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(16), dp(14), dp(16)); background = android.graphics.drawable.GradientDrawable().apply { setColor(Color.rgb(17,17,17)); cornerRadius = dp(16).toFloat() } }
        panel.addView(TextView(this).apply { text = "YOUR LINUX.\nYOUR SPACE."; textSize = 29f; setTextColor(white); typeface = android.graphics.Typeface.create("sans-serif-medium", 1) })
        panel.addView(TextView(this).apply { text = "\nPrepare a real XFCE desktop inside Ubuntu, then open it here through noVNC. First launch downloads the desktop packages."; textSize = 14f; setTextColor(grey) })
        panel.addView(button("INSTALL & START DESKTOP") { installDesktop() })
        panel.addView(TextView(this).apply { text = "\nRequirements: working internet and several GB of free space. Package installation can take a while."; textSize = 12f; setTextColor(grey) })
        log = TextView(this).apply { text = "Ready. The desktop has not been installed yet."; textSize = 12f; setTextColor(Color.rgb(220,220,220)); typeface = android.graphics.Typeface.MONOSPACE; setPadding(0, dp(14), 0, dp(6)) }
        panel.addView(log)
        scroll.addView(panel)
        content.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(content, LinearLayout.LayoutParams(-1, 0, 1f))
    }

    private fun installDesktop() {
        if (!UbuntuRuntime.isInstalled(this)) { Toast.makeText(this, "Instale o Ubuntu primeiro.", Toast.LENGTH_LONG).show(); return }
        log.text = "Starting Ubuntu shell…"
        Thread {
            try {
                val p = UbuntuRuntime.startShell(this)
                shell = p
                val out = BufferedWriter(OutputStreamWriter(p.outputStream))
                writer = out
                out.write("export DEBIAN_FRONTEND=noninteractive\n")
                out.write("apt-get update && apt-get install -y xfce4 dbus-x11 tigervnc-standalone-server novnc websockify\n")
                out.write("mkdir -p /root/.vnc\n")
                out.write("printf '#!/bin/sh\\\\nunset SESSION_MANAGER\\\\nunset DBUS_SESSION_BUS_ADDRESS\\\\nexec dbus-launch --exit-with-session startxfce4\\\\n' > /root/.vnc/xstartup\n")
                out.write("chmod +x /root/.vnc/xstartup\n")
                out.write("(vncserver -kill :1 >/dev/null 2>&1 || true)\n")
                out.write("vncserver :1 -localhost yes -geometry 1280x720 -depth 24 -SecurityTypes None\n")
                out.write("nohup websockify --web=/usr/share/novnc 127.0.0.1:6080 127.0.0.1:5901 >/tmp/nox-websockify.log 2>&1 &\n")
                out.write("sleep 2; echo NOX_DESKTOP_READY\n")
                out.flush()
                val reader = p.inputStream.bufferedReader()
                while (true) {
                    val line = reader.readLine() ?: break
                    runOnUiThread { log.append("\n$line"); if (log.lineCount > 120) log.text = log.text.takeLast(8000) }
                    if (line.contains("NOX_DESKTOP_READY")) {
                        runOnUiThread { openDesktop() }
                    }
                }
                runOnUiThread {
                    if (p.exitValueOrNull() != 0) log.append("\nShell ended. Check package errors above, then retry.")
                }
            } catch (e: Exception) { runOnUiThread { log.append("\nError: ${e.message}") } }
        }.start()
        Toast.makeText(this, "O terminal ficará ocupado enquanto o desktop é preparado.", Toast.LENGTH_LONG).show()
    }

    private fun openDesktop() {
        content.removeAllViews()
        content.addView(web, ViewGroup.LayoutParams(-1, -1))
        web.loadUrl("http://127.0.0.1:6080/vnc.html?autoconnect=true&resize=remote")
        log.text = "Opening local desktop…"
    }

    private fun button(title: String, action: () -> Unit) = Button(this).apply {
        text = title; isAllCaps = false; textSize = 11f; setTextColor(if (title.contains("INSTALL")) black else white)
        background = android.graphics.drawable.GradientDrawable().apply { setColor(if (title.contains("INSTALL")) white else Color.rgb(29,29,29)); cornerRadius = dp(12).toFloat() }
        setOnClickListener { action() }
    }
    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()
    private fun Process.exitValueOrNull(): Int? = try { exitValue() } catch (_: IllegalThreadStateException) { null }
    override fun onDestroy() { try { web.destroy() } catch (_: Exception) {}; try { writer?.write("exit\n"); writer?.flush() } catch (_: Exception) {}; try { shell?.destroy() } catch (_: Exception) {}; super.onDestroy() }
}
