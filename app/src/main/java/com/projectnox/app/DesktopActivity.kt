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
    private var desktopOpened = false
    private val black = Color.BLACK
    private val white = Color.WHITE
    private val grey = Color.rgb(165, 165, 165)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = black
        window.navigationBarColor = black
        buildUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(black)
            setPadding(dp(16), dp(12), dp(16), dp(8))
        }
        setContentView(root)
        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        header.addView(TextView(this).apply {
            text = "NOX / DESKTOP"
            textSize = 19f
            setTextColor(white)
            letterSpacing = .12f
        }, LinearLayout.LayoutParams(0, dp(48), 1f))
        header.addView(button("CLOSE") { finish() }, LinearLayout.LayoutParams(dp(82), dp(42)))
        root.addView(header)
        root.addView(TextView(this).apply {
            text = "LIGHTWEIGHT XFCE  ·  UBUNTU ARM64"
            textSize = 10f
            setTextColor(grey)
            setPadding(0, dp(4), 0, dp(12))
        })

        web = WebView(this).apply {
            setBackgroundColor(Color.rgb(8, 8, 8))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView,
                    request: android.webkit.WebResourceRequest,
                    error: android.webkit.WebResourceError
                ) {
                    if (request.isForMainFrame) runOnUiThread {
                        if (desktopOpened) {
                            content.removeAllViews()
                            content.addView(makeErrorPanel("noVNC could not load. Check that the desktop services are running, then close and retry."))
                        }
                    }
                }
            }
            webChromeClient = WebChromeClient()
        }
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(black)
        }
        val scroll = ScrollView(this)
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(16), dp(14), dp(16))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.rgb(17, 17, 17))
                cornerRadius = dp(16).toFloat()
            }
        }
        panel.addView(TextView(this).apply {
            text = "YOUR LINUX.\nYOUR SPACE."
            textSize = 29f
            setTextColor(white)
            typeface = android.graphics.Typeface.create("sans-serif-medium", 1)
        })
        panel.addView(TextView(this).apply {
            text = "\nInstall a minimal XFCE workspace with no recommended packages. The first setup needs internet; later launches reuse the installed packages."
            textSize = 14f
            setTextColor(grey)
        })
        panel.addView(button("INSTALL & START DESKTOP") { installDesktop() })
        panel.addView(TextView(this).apply {
            text = "\nLightweight 1024 × 600 desktop, 16-bit color. Keep a few GB free for Ubuntu packages."
            textSize = 12f
            setTextColor(grey)
        })
        log = TextView(this).apply {
            text = "Ready. The desktop has not been installed yet."
            textSize = 12f
            setTextColor(Color.rgb(220, 220, 220))
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(0, dp(14), 0, dp(6))
        }
        panel.addView(log)
        scroll.addView(panel)
        content.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(content, LinearLayout.LayoutParams(-1, 0, 1f))
    }

    private fun installDesktop() {
        if (!UbuntuRuntime.isInstalled(this)) {
            Toast.makeText(this, "Instale o Ubuntu primeiro.", Toast.LENGTH_LONG).show()
            return
        }
        desktopOpened = false
        log.text = "Checking desktop packages…"
        Thread {
            try {
                val p = UbuntuRuntime.startShell(this)
                shell = p
                val out = BufferedWriter(OutputStreamWriter(p.outputStream))
                writer = out
                val commands = listOf(
                    "export DEBIAN_FRONTEND=noninteractive",
                    "if ! command -v startxfce4 >/dev/null 2>&1 || ! command -v vncserver >/dev/null 2>&1 || ! command -v websockify >/dev/null 2>&1; then apt-get update && apt-get install -y --no-install-recommends xfce4-session xfce4-panel xfwm4 xfdesktop4 xfce4-settings xfconf thunar xfce4-terminal dbus-x11 dbus tigervnc-standalone-server novnc websockify xterm; fi",
                    "if ! command -v startxfce4 >/dev/null 2>&1 || ! command -v vncserver >/dev/null 2>&1 || ! command -v websockify >/dev/null 2>&1; then echo NOX_INSTALL_FAILED; else echo NOX_PACKAGES_READY; fi",
                    "mkdir -p /root/.vnc",
                    "cat > /root/.vnc/xstartup <<'NOX_XSTARTUP'",
                    "#!/bin/sh",
                    "unset SESSION_MANAGER",
                    "unset DBUS_SESSION_BUS_ADDRESS",
                    "export XDG_RUNTIME_DIR=/tmp/nox-runtime",
                    "mkdir -p /tmp/nox-runtime",
                    "chmod 700 /tmp/nox-runtime",
                    "exec dbus-launch --exit-with-session startxfce4",
                    "NOX_XSTARTUP",
                    "chmod +x /root/.vnc/xstartup",
                    "(vncserver -kill :1 >/dev/null 2>&1 || true)",
                    "vncserver :1 -localhost yes -geometry 1024x600 -depth 16 -SecurityTypes None -xstartup /root/.vnc/xstartup",
                    "sleep 4",
                    "if ! pgrep -f 'xfce4-session|xfwm4' >/dev/null 2>&1; then echo NOX_XFCE_FAILED; else echo NOX_XFCE_READY; fi",
                    "nohup websockify --web=/usr/share/novnc 127.0.0.1:6080 127.0.0.1:5901 >/tmp/nox-websockify.log 2>&1 &",
                    "sleep 1",
                    "if ! pgrep -f 'websockify.*6080' >/dev/null 2>&1; then echo NOX_WEBSOCKIFY_FAILED; else echo NOX_DESKTOP_READY; fi"
                )
                commands.forEach { out.write(it + "\n") }
                out.flush()
                val reader = p.inputStream.bufferedReader()
                while (true) {
                    val line = reader.readLine() ?: break
                    runOnUiThread {
                        log.append("\n$line")
                        if (log.lineCount > 120) log.text = log.text.takeLast(8000)
                        when {
                            line.contains("NOX_INSTALL_FAILED") -> log.append("\nPackage install did not complete. Check internet and retry.")
                            line.contains("NOX_XFCE_FAILED") -> log.append("\nXFCE did not start. VNC log: /root/.vnc/")
                            line.contains("NOX_WEBSOCKIFY_FAILED") -> log.append("\nnoVNC WebSocket service did not start.")
                            line.contains("NOX_DESKTOP_READY") -> openDesktop()
                        }
                    }
                }
                runOnUiThread {
                    if (!desktopOpened) log.append("\nLinux shell ended before the desktop was ready. Review the error above and retry.")
                }
            } catch (e: Exception) {
                runOnUiThread { log.append("\nError: ${e.message}") }
            }
        }.start()
        Toast.makeText(this, "Preparing lightweight XFCE…", Toast.LENGTH_LONG).show()
    }

    private fun openDesktop() {
        if (desktopOpened) return
        desktopOpened = true
        content.removeAllViews()
        content.addView(web, ViewGroup.LayoutParams(-1, -1))
        web.loadUrl("http://127.0.0.1:6080/vnc.html?autoconnect=true&resize=remote&reconnect=true")
    }

    private fun makeErrorPanel(message: String): ViewGroup {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            setBackgroundColor(black)
            addView(TextView(this@DesktopActivity).apply {
                text = "DESKTOP NOT READY"
                textSize = 20f
                setTextColor(white)
            })
            addView(TextView(this@DesktopActivity).apply {
                text = message
                textSize = 14f
                setTextColor(grey)
                setPadding(0, dp(12), 0, dp(16))
            })
            addView(button("BACK / RETRY") {
                desktopOpened = false
                buildUi()
            })
        }
    }

    private fun button(title: String, action: () -> Unit) = Button(this).apply {
        text = title
        isAllCaps = false
        textSize = 11f
        setTextColor(if (title.contains("INSTALL")) black else white)
        background = android.graphics.drawable.GradientDrawable().apply {
            setColor(if (title.contains("INSTALL")) white else Color.rgb(29, 29, 29))
            cornerRadius = dp(12).toFloat()
        }
        setOnClickListener { action() }
    }

    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        try { web.destroy() } catch (_: Exception) {}
        try { writer?.write("exit\n"); writer?.flush() } catch (_: Exception) {}
        try { shell?.destroy() } catch (_: Exception) {}
        super.onDestroy()
    }
}
