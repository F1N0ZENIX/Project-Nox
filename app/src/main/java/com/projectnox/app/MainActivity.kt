package com.projectnox.app

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import kotlin.math.abs

class MainActivity : Activity() {
    private val black = Color.rgb(0, 0, 0)
    private val card = Color.rgb(18, 18, 18)
    private val raised = Color.rgb(31, 31, 31)
    private val white = Color.WHITE
    private val grey = Color.rgb(160, 160, 160)
    private val red = Color.rgb(255, 55, 55)
    private lateinit var root: LinearLayout
    private var controlsVisible = true
    private var pointerSpeed = 50
    private var pointerX = 0f
    private var pointerY = 0f
    private var downX = 0f
    private var downY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private lateinit var pointerLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = black
        window.navigationBarColor = black
        show("home")
    }

    private fun show(page: String) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(black)
            setPadding(dp(20), dp(10), dp(20), dp(14))
        }
        setContentView(root)
        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        val brand = label("N O X", 20f, white, true).apply { letterSpacing = 0.14f }
        header.addView(brand, LinearLayout.LayoutParams(0, dp(48), 1f))
        header.addView(btn("⚙", false) { show("settings") }, LinearLayout.LayoutParams(dp(54), dp(44)))
        root.addView(header)
        val line = View(this).apply { setBackgroundColor(Color.rgb(48, 48, 48)) }
        root.addView(line, LinearLayout.LayoutParams(-1, dp(1)))
        val scroll = ScrollView(this)
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(18), 0, dp(14))
        }
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        when (page) {
            "home" -> home(body)
            "settings" -> settings(body)
            "controls" -> controls(body)
            "display" -> display(body)
            "session" -> session(body)
            "about" -> about(body)
        }
        root.addView(label("PROJECT NOX  •  EARLY BUILD", 10f, grey), LinearLayout.LayoutParams(-1, dp(22)))
    }

    private fun home(body: LinearLayout) {
        body.addView(label("YOUR LINUX.\nYOUR POCKET.", 31f, white, true))
        body.addView(space(8))
        body.addView(label("Ubuntu ARM64  ·  XFCE  ·  No root", 14f, grey))
        body.addView(space(22))
        val status = column(card, 16)
        status.addView(label("●  ENVIRONMENT NOT INSTALLED", 12f, red, true))
        status.addView(space(8))
        status.addView(label("The app foundation is ready. The Ubuntu runtime and desktop connection are the next stage.", 14f, grey))
        body.addView(status)
        body.addView(space(14))
        body.addView(btn("INSTALL UBUNTU", true) { toast("Ubuntu installer is planned for the next milestone.") })
        body.addView(space(9))
        body.addView(btn("OPEN TOUCH CONTROLS", false) { show("controls") })
        body.addView(space(9))
        body.addView(btn("DESKTOP SETTINGS", false) { show("settings") })
        body.addView(space(22))
        body.addView(label("MADE FOR TOUCH", 12f, white, true))
        body.addView(space(7))
        body.addView(label("A pocket-sized Linux desktop with an AMOLED-black, monochrome interface and dot-inspired details.", 14f, grey))
    }

    private fun settings(body: LinearLayout) {
        body.addView(label("Settings", 30f, white, true))
        body.addView(space(5))
        body.addView(label("Make your desktop feel right.", 14f, grey))
        body.addView(space(20))
        body.addView(menu("◉", "Touch controls", "Touchpad, mouse buttons and keyboard") { show("controls") })
        body.addView(menu("▣", "Display", "Scaling, orientation and pointer visibility") { show("display") })
        body.addView(menu("◌", "Session", "Start, pause and stop the desktop") { show("session") })
        body.addView(menu("i", "About Project Nox", "Version and development status") { show("about") })
        body.addView(space(18))
        body.addView(label("INTERFACE", 11f, grey, true))
        body.addView(space(6))
        body.addView(label("AMOLED black  /  Monochrome  /  Dot-inspired", 13f, grey))
        body.addView(space(18))
        body.addView(btn("← BACK TO HOME", false) { show("home") })
    }

    private fun controls(body: LinearLayout) {
        body.addView(label("Touch controls", 28f, white, true))
        body.addView(space(5))
        body.addView(label("Use the touch area like a laptop trackpad.", 14f, grey))
        body.addView(space(14))
        body.addView(btn(if (controlsVisible) "HIDE ON-SCREEN CONTROLS" else "SHOW ON-SCREEN CONTROLS", false) {
            controlsVisible = !controlsVisible
            show("controls")
        })
        if (controlsVisible) {
            body.addView(space(10))
            val pad = column(raised, 14).apply { gravity = Gravity.CENTER }
            pad.addView(label("TOUCHPAD", 11f, grey, true))
            pad.addView(space(10))
            pointerLabel = label("●  Touch and move", 15f, white).apply { gravity = Gravity.CENTER }
            pad.addView(pointerLabel, LinearLayout.LayoutParams(-1, dp(100)))
            pad.setOnTouchListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.x; downY = event.y
                        lastX = event.x; lastY = event.y
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        pointerX += (event.x - lastX) * (pointerSpeed / 50f)
                        pointerY += (event.y - lastY) * (pointerSpeed / 50f)
                        lastX = event.x; lastY = event.y
                        pointerLabel.text = "X " + pointerX.toInt() + "     Y " + pointerY.toInt()
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (abs(event.x - downX) < dp(8) && abs(event.y - downY) < dp(8)) {
                            pointerLabel.text = "●  Tap detected"
                        }
                        true
                    }
                    else -> true
                }
            }
            body.addView(pad, LinearLayout.LayoutParams(-1, dp(184)))
            body.addView(space(9))
            val clicks = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            clicks.addView(btn("◀  LEFT CLICK", false) { pointerLabel.text = "Left click selected" }, LinearLayout.LayoutParams(0, dp(48), 1f))
            clicks.addView(spaceW(8))
            clicks.addView(btn("RIGHT CLICK  ▶", false) { pointerLabel.text = "Right click selected" }, LinearLayout.LayoutParams(0, dp(48), 1f))
            body.addView(clicks)
            body.addView(space(13))
            body.addView(label("POINTER SPEED  ·  " + pointerSpeed + "%", 12f, grey, true))
            val speed = SeekBar(this).apply {
                max = 100
                progress = pointerSpeed
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                        pointerSpeed = value.coerceAtLeast(10)
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) { show("controls") }
                })
            }
            body.addView(speed)
            body.addView(space(8))
            body.addView(btn("⌨  OPEN ANDROID KEYBOARD", false) { openKeyboard() })
        }
        body.addView(space(18))
        body.addView(btn("⚙  CONTROL SETTINGS", false) { show("settings") })
        body.addView(space(8))
        body.addView(btn("← BACK", false) { show("home") })
    }

    private fun display(body: LinearLayout) {
        body.addView(label("Display", 28f, white, true))
        body.addView(space(8))
        body.addView(label("These options will be wired to the Linux display backend.", 14f, grey))
        body.addView(space(18))
        body.addView(menu("↔", "Fit to screen", "Scale the desktop to your phone") { toast("Display option queued for runtime integration.") })
        body.addView(menu("⤢", "Landscape mode", "Use a wider desktop layout") { toast("Orientation control is planned.") })
        body.addView(menu("◉", "Cursor visibility", "Adjust pointer size and contrast") { toast("Cursor settings are planned.") })
        body.addView(space(16))
        body.addView(btn("← BACK TO SETTINGS", false) { show("settings") })
    }

    private fun session(body: LinearLayout) {
        body.addView(label("Session", 28f, white, true))
        body.addView(space(8))
        body.addView(label("Quick actions for your Linux desktop.", 14f, grey))
        body.addView(space(18))
        body.addView(btn("START DESKTOP", true) { toast("Linux runtime is not connected yet.") })
        body.addView(space(9))
        body.addView(btn("PAUSE SESSION", false) { toast("Session controls will be enabled after runtime integration.") })
        body.addView(space(9))
        body.addView(btn("STOP SESSION", false) { toast("No Linux session is running.") })
        body.addView(space(18))
        body.addView(label("The session menu will grow into separate Display, Controls, Linux and Session sections, inspired by handheld desktop launchers.", 14f, grey))
        body.addView(space(16))
        body.addView(btn("← BACK TO SETTINGS", false) { show("settings") })
    }

    private fun about(body: LinearLayout) {
        body.addView(label("Project Nox", 30f, white, true))
        body.addView(space(8))
        body.addView(label("Version 0.1.0 · Initial UI foundation", 14f, grey))
        body.addView(space(18))
        body.addView(label("GOAL", 11f, grey, true))
        body.addView(label("A Linux desktop on Android without root, using Ubuntu ARM64 and a clean Nothing OS-inspired interface.", 15f, white))
        body.addView(space(18))
        body.addView(label("STATUS", 11f, grey, true))
        body.addView(label("Touch controls currently demonstrate gestures inside the app. Ubuntu/PRoot installation, graphical output, audio and real mouse/keyboard forwarding still need implementation.", 14f, grey))
        body.addView(space(18))
        body.addView(btn("← BACK TO SETTINGS", false) { show("settings") })
    }

    private fun menu(icon: String, title: String, subtitle: String, action: () -> Unit): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(card, 14)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        row.addView(label(icon, 22f, white, true).apply { gravity = Gravity.CENTER }, LinearLayout.LayoutParams(dp(42), dp(48)))
        val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        texts.addView(label(title, 15f, white, true))
        texts.addView(space(3))
        texts.addView(label(subtitle, 12f, grey))
        row.addView(texts, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(label("›", 24f, grey))
        row.layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(9) }
        return row
    }

    private fun column(color: Int, padding: Int) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = rounded(color, 16)
        setPadding(dp(padding), dp(padding), dp(padding), dp(padding))
    }

    private fun label(value: String, size: Float, color: Int, bold: Boolean = false) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        includeFontPadding = true
    }

    private fun btn(value: String, primary: Boolean, action: () -> Unit): Button = Button(this).apply {
        text = value
        textSize = 12f
        isAllCaps = false
        setTextColor(if (primary) black else white)
        background = rounded(if (primary) white else raised, 12)
        minHeight = dp(46)
        minimumHeight = dp(46)
        setPadding(dp(10), dp(8), dp(10), dp(8))
        setOnClickListener { action() }
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun space(height: Int) = View(this).apply { layoutParams = LinearLayout.LayoutParams(1, dp(height)) }
    private fun spaceW(width: Int) = View(this).apply { layoutParams = LinearLayout.LayoutParams(dp(width), 1) }

    private fun openKeyboard() {
        val input = EditText(this).apply {
            hint = "Keyboard test"
            setTextColor(white)
            setHintTextColor(grey)
            background = rounded(raised, 10)
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }
        root.addView(input, 1, LinearLayout.LayoutParams(-1, dp(48)))
        input.requestFocus()
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
        toast("Android keyboard opened. Forwarding to Linux comes later.")
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
