package com.projectnox.app

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.graphics.drawable.GradientDrawable
import android.text.method.ScrollingMovementMethod
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import java.io.BufferedWriter
import java.io.OutputStreamWriter

class UbuntuTerminalActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private val outputBuffer = StringBuilder()
    private var outputView: TextView? = null
    private var inputWriter: BufferedWriter? = null
    private var shell: Process? = null
    private lateinit var terminalRoot: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        terminalRoot = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            setPadding(dp(16), dp(8), dp(16), dp(8))
        }
        setContentView(terminalRoot)
        ViewCompat.setOnApplyWindowInsetsListener(terminalRoot) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.displayCutout())
            view.setPadding(dp(16), bars.top + dp(8), dp(16), bars.bottom + dp(8))
            insets
        }
        ViewCompat.requestApplyInsets(terminalRoot)
        buildUi()
        startShell()
    }

    private fun buildUi() {
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val title = TextView(this).apply { text = "UBUNTU / ARM64"; textSize = 17f; setTextColor(Color.WHITE); typeface = Typeface.create("sans-serif-medium", Typeface.BOLD) }
        header.addView(title, LinearLayout.LayoutParams(0, dp(46), 1f))
        header.addView(button("CLOSE", false) { finish() }, LinearLayout.LayoutParams(dp(84), dp(42)))
        terminalRoot.addView(header)
        val line = View(this).apply { setBackgroundColor(Color.rgb(48, 48, 48)) }
        terminalRoot.addView(line, LinearLayout.LayoutParams(-1, dp(1)))
        terminalRoot.addView(TextView(this).apply { text = "Real Linux shell · Ubuntu Base · no root"; textSize = 11f; setTextColor(Color.rgb(155, 155, 155)); setPadding(0, dp(8), 0, dp(8)) })
        val scroll = ScrollView(this).apply {
            background = rounded(Color.rgb(8, 8, 8), 12)
            isFillViewport = false
            isVerticalScrollBarEnabled = true
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }
        val text = TextView(this).apply {
            textSize = 12f
            setTextColor(Color.rgb(225, 225, 225))
            typeface = Typeface.MONOSPACE
            movementMethod = ScrollingMovementMethod()
            setTextIsSelectable(true)
            text = "Nox terminal starting…\n"
        }
        outputView = text
        scroll.addView(text)
        terminalRoot.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val command = EditText(this).apply {
            hint = "Enter Linux command…"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            textSize = 13f
            isSingleLine = true
            imeOptions = EditorInfo.IME_ACTION_SEND
            background = rounded(Color.rgb(29, 29, 29), 10)
            setPadding(dp(12), dp(8), dp(12), dp(8))
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                    sendCommand(this.text.toString())
                    this.text.clear()
                    true
                } else false
            }
        }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        row.addView(command, LinearLayout.LayoutParams(0, dp(50), 1f))
        row.addView(View(this), LinearLayout.LayoutParams(dp(8), 1))
        row.addView(button("RUN", true) { sendCommand(command.text.toString()); command.text.clear() }, LinearLayout.LayoutParams(dp(78), dp(50)))
        terminalRoot.addView(row)
        terminalRoot.addView(button("RESTART SHELL", false) { stopShell(); startShell() })
        terminalRoot.addView(button("CLEAR OUTPUT", false) { outputBuffer.clear(); outputView?.text = "" })
    }

    private fun startShell() {
        if (!UbuntuRuntime.isInstalled(this)) {
            appendOutput("Ubuntu is not installed. Return to Project Nox and complete offline setup.\n")
            return
        }
        try {
            shell = UbuntuRuntime.startShell(this)
            inputWriter = BufferedWriter(OutputStreamWriter(shell!!.outputStream))
            appendOutput("\n[Nox] Ubuntu shell started. Try: id, uname -a, apt-get update\n")
            val active = shell!!
            Thread {
                try {
                    active.inputStream.bufferedReader().use { reader ->
                        val buffer = CharArray(1024)
                        while (true) {
                            val count = reader.read(buffer)
                            if (count < 0) break
                            appendOutput(String(buffer, 0, count))
                        }
                    }
                } catch (e: Exception) {
                    appendOutput("\n[Read error: " + (e.message ?: "unknown") + "]\n")
                }
                appendOutput("\n[Ubuntu shell exited]\n")
                inputWriter = null
                shell = null
            }.start()
        } catch (e: Exception) {
            appendOutput("\n[Could not start Ubuntu: " + (e.message ?: e.javaClass.simpleName) + "]\n")
            Toast.makeText(this, "Falha ao iniciar o Ubuntu. Veja a mensagem no terminal.", Toast.LENGTH_LONG).show()
        }
    }

    private fun sendCommand(raw: String) {
        val command = raw.trimEnd()
        if (command.isEmpty()) return
        try {
            val writer = inputWriter ?: run { appendOutput("\n[Shell indisponível. Reinicie a sessão.]\n"); return }
            writer.write(command)
            writer.newLine()
            writer.flush()
        } catch (e: Exception) {
            appendOutput("\n[Command error: " + (e.message ?: "unknown") + "]\n")
        }
    }

    private fun appendOutput(value: String) {
        handler.post {
            outputBuffer.append(value)
            if (outputBuffer.length > 24000) outputBuffer.delete(0, outputBuffer.length - 24000)
            outputView?.text = outputBuffer.toString()
            val parent = outputView?.parent
            if (parent is ScrollView) parent.post { parent.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private fun stopShell() {
        try { inputWriter?.write("exit\n"); inputWriter?.flush() } catch (_: Exception) {}
        try { shell?.destroy() } catch (_: Exception) {}
        inputWriter = null
        shell = null
    }

    override fun onDestroy() {
        stopShell()
        super.onDestroy()
    }

    private fun button(textValue: String, primary: Boolean, action: () -> Unit): Button = Button(this).apply {
        text = textValue
        textSize = 11f
        isAllCaps = false
        setTextColor(if (primary) Color.BLACK else Color.WHITE)
        background = rounded(if (primary) Color.WHITE else Color.rgb(29, 29, 29), 10)
        minHeight = dp(42)
        minimumHeight = dp(42)
        setPadding(dp(8), dp(5), dp(8), dp(5))
        setOnClickListener { action() }
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}