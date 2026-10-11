package com.projectnox.app

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
import kotlin.math.abs

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("nox_prefs", Context.MODE_PRIVATE) }
    private val black = Color.rgb(0, 0, 0)
    private val card = Color.rgb(17, 17, 17)
    private val raised = Color.rgb(29, 29, 29)
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
        // Draw edge-to-edge, then explicitly keep the app content below system bars/cutouts.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = black
        window.navigationBarColor = black
        window.decorView.systemUiVisibility = 0
        controlsVisible = prefs.getBoolean("controls_visible", true)
        pointerSpeed = prefs.getInt("pointer_speed", 50)
        show("home")
    }

    private fun show(page: String) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(black)
            setPadding(dp(18), dp(8), dp(18), dp(8))
        }
        setContentView(root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.statusBars() or
                    WindowInsetsCompat.Type.navigationBars() or
                    WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(dp(18), bars.top + dp(8), dp(18), bars.bottom + dp(8))
            insets
        }
        ViewCompat.requestApplyInsets(root)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }
        brand.addView(label("PROJECT NOX", 17f, white, true).apply { letterSpacing = 0.12f })
        brand.addView(label("LINUX ON THE GO", 9f, grey, true).apply { letterSpacing = 0.18f })
        header.addView(brand, LinearLayout.LayoutParams(0, dp(50), 1f))
        header.addView(btn("⚙", false) { show("settings") }, LinearLayout.LayoutParams(dp(48), dp(44)))
        root.addView(header)

        val line = View(this).apply { setBackgroundColor(Color.rgb(45, 45, 45)) }
        root.addView(line, LinearLayout.LayoutParams(-1, dp(1)))

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            setPadding(0, dp(8), 0, dp(8))
        }
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(10), 0, dp(16))
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
        root.addView(label("●  PROJECT NOX  /  EARLY ACCESS", 9f, grey), LinearLayout.LayoutParams(-1, dp(22)))
    }

    private fun home(body: LinearLayout) {
        body.addView(label("YOUR LINUX.\nYOUR POCKET.", 32f, white, true))
        body.addView(space(8))
        body.addView(label("Ubuntu ARM64  ·  XFCE  ·  No root", 13f, grey))
        body.addView(space(20))
        val status = column(card, 16)
        val statusTitle = label("●  APP READY · LINUX NOT INSTALLED", 11f, red, true)
        status.addView(statusTitle)
        status.addView(space(8))
        status.addView(label("The interface is running. The Ubuntu environment and real desktop streaming still need to be added before this app can launch a Linux desktop.", 13f, grey))
        body.addView(status)
        body.addView(space(14))
        body.addView(btn("SET UP LINUX", true) { show("session") })
        body.addView(space(8))
        body.addView(btn("TOUCH CONTROLS", false) { show("controls") })
        body.addView(space(8))
        body.addView(btn("DESKTOP SETTINGS", false) { show("settings") })
        body.addView(space(22))
        body.addView(label("BUILT FOR YOUR PHONE", 11f, white, true))
        body.addView(space(7))
        body.addView(label("AMOLED black, crisp monochrome details, comfortable touch targets and a settings menu designed for small screens.", 13f, grey))
        body.addView(space(18))
        val note = column(card, 14)
        note.addView(label("NEXT MILESTONE", 10f, grey, true))
        note.addView(space(5))
        note.addView(label("Connect the Ubuntu ARM64 userspace, graphical desktop and input forwarding.", 13f, white))
        body.addView(note)
    }

    private fun settings(body: LinearLayout) {
        body.addView(label("Settings", 29f, white, true))
        body.addView(space(4))
        body.addView(label("Tune your Nox experience.", 13f, grey))
        body.addView(space(18))
        body.addView(menu("◉", "Touch controls", "Touchpad, click buttons and keyboard") { show("controls") })
        body.addView(menu("▣", "Display", "Orientation and screen layout") { show("display") })
        body.addView(menu("◌", "Session", "Linux setup and desktop status") { show("session") })
        body.addView(menu("i", "About Project Nox", "Version, goals and current status") { show("about") })
        body.addView(space(14))
        body.addView(label("VISUAL IDENTITY", 10f, grey, true))
        body.addView(space(5))
        body.addView(label("AMOLED BLACK   /   MONOCHROME   /   DOT INSPIRED", 11f, white))
        body.addView(space(18))
        body.addView(btn("←  HOME", false) { show("home") })
    }

    private fun controls(body: LinearLayout) {
        body.addView(label("Touch controls", 27f, white, true))
        body.addView(space(4))
        body.addView(label("A trackpad-style area for testing touch gestures.", 13f, grey))
        body.addView(space(14))
        body.addView(btn(if (controlsVisible) "HIDE TOUCHPAD" else "SHOW TOUCHPAD", false) {
            controlsVisible = !controlsVisible
            prefs.edit().putBoolean("controls_visible", controlsVisible).apply()
            show("controls")
        })
        if (controlsVisible) {
            body.addView(space(10))
            val pad = column(raised, 14).apply { gravity = Gravity.CENTER }
            pad.addView(label("TOUCHPAD", 10f, grey, true))
            body.addView(space(8))
            pointerLabel = label("Touch and move here", 14f, white).apply { gravity = Gravity.CENTER }
            pad.addView(pointerLabel, LinearLayout.LayoutParams(-1, dp(94)))
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
                        pointerLabel.text = "X  ${pointerX.toInt()}     Y  ${pointerY.toInt()}"
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (abs(event.x - downX) < dp(8) && abs(event.y - downY) < dp(8)) {
                            pointerLabel.text = "●  TAP DETECTED"
                        }
                        true
                    }
                    else -> true
                }
            }
            body.addView(pad, LinearLayout.LayoutParams(-1, dp(158)))
            body.addView(space(8))
            val clicks = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            clicks.addView(btn("◀  LEFT CLICK", false) { pointerLabel.text = "LEFT CLICK · TEST" }, LinearLayout.LayoutParams(0, dp(46), 1f))
            clicks.addView(spaceW(8))
            clicks.addView(btn("RIGHT CLICK  ▶", false) { pointerLabel.text = "RIGHT CLICK · TEST" }, LinearLayout.LayoutParams(0, dp(46), 1f))
            body.addView(clicks)
            body.addView(space(14))
            body.addView(label("POINTER SENSITIVITY  ·  $pointerSpeed%", 11f, grey, true))
            val speed = SeekBar(this).apply {
                max = 100
                progress = pointerSpeed
                contentDescription = "Pointer sensitivity"
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                        pointerSpeed = value.coerceAtLeast(10)
                        prefs.edit().putInt("pointer_speed", pointerSpeed).apply()
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
        body.addView(btn("⚙  SETTINGS", false) { show("settings") })
        body.addView(space(8))
        body.addView(btn("←  HOME", false) { show("home") })
    }

    private fun display(body: LinearLayout) {
        body.addView(label("Display", 27f, white, true))
        body.addView(space(5))
        body.addView(label("These controls affect the Android app shell. Linux display scaling will be connected with the desktop backend.", 13f, grey))
        body.addView(space(18))
        body.addView(menu("⤢", "Landscape mode", "Rotate the app to a desktop layout") {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            toast("Landscape mode enabled. Rotate back using Android quick settings or reopen Display.")
        })
        body.addView(menu("▯", "Portrait mode", "Return to a phone layout") {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            toast("Portrait mode enabled.")
        })
        body.addView(menu("↻", "Follow device rotation", "Allow automatic orientation") {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            toast("Automatic orientation enabled.")
        })
        body.addView(space(18))
        body.addView(btn("←  SETTINGS", false) { show("settings") })
    }

    private fun session(body: LinearLayout) {
        body.addView(label("Linux setup", 27f, white, true))
        body.addView(space(6))
        body.addView(label("Project Nox is being built in milestones. This build does not yet download or start Ubuntu.", 13f, grey))
        body.addView(space(18))
        val status = column(card, 15)
        status.addView(label("MILESTONE 01", 10f, grey, true))
        status.addView(space(6))
        status.addView(label("Android shell and touch-first settings", 15f, white, true))
        status.addView(space(5))
        status.addView(label("Complete: navigation, touchpad demo, persistent sensitivity and orientation settings.", 12f, grey))
        body.addView(status)
        body.addView(space(10))
        val next = column(card, 15)
        next.addView(label("MILESTONE 02", 10f, red, true))
        next.addView(space(6))
        next.addView(label("Ubuntu ARM64 runtime", 15f, white, true))
        next.addView(space(5))
        next.addView(label("To do: PRoot bootstrap, safe downloads, extraction progress, start/stop process management and storage checks.", 12f, grey))
        body.addView(next)
        body.addView(space(18))
        body.addView(btn("←  SETTINGS", false) { show("settings") })
    }

    private fun about(body: LinearLayout) {
        body.addView(label("Project Nox", 29f, white, true))
        body.addView(space(6))
        body.addView(label("Version 0.2.0 · Touch UI milestone", 13f, grey))
        body.addView(space(18))
        body.addView(label("THE GOAL", 10f, grey, true))
        body.addView(space(5))
        body.addView(label("A Linux desktop on Android without root, with Ubuntu ARM64 and a monochrome interface inspired by Nothing OS.", 14f, white))
        body.addView(space(18))
        body.addView(label("WHAT WORKS IN THIS BUILD", 10f, grey, true))
        body.addView(space(5))
        body.addView(label("• Settings navigation\n• Touchpad gesture demo\n• Saved pointer sensitivity\n• Saved touchpad visibility\n• Portrait, landscape and automatic orientation\n• Android soft keyboard launch", 13f, grey))
        body.addView(space(18))
        body.addView(label("WHAT IS NOT READY YET", 10f, grey, true))
        body.addView(space(5))
        body.addView(label("Ubuntu installation, a real Linux desktop display, Linux mouse/keyboard forwarding and audio. Touch input here is still an app demo, not a remote Linux cursor.", 13f, grey))
        body.addView(space(18))
        body.addView(btn("←  SETTINGS", false) { show("settings") })
    }

    private fun menu(icon: String, title: String, subtitle: String, action: () -> Unit): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(card, 14)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        row.addView(label(icon, 21f, white, true).apply { gravity = Gravity.CENTER }, LinearLayout.LayoutParams(dp(40), dp(46)))
        val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        texts.addView(label(title, 14f, white, true))
        texts.addView(space(3))
        texts.addView(label(subtitle, 11f, grey))
        row.addView(texts, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(label("›", 23f, grey))
        row.layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) }
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
        textSize = 11f
        isAllCaps = false
        setTextColor(if (primary) black else white)
        background = rounded(if (primary) white else raised, 12)
        minHeight = dp(44)
        minimumHeight = dp(44)
        setPadding(dp(9), dp(7), dp(9), dp(7))
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
            isSingleLine = true
        }
        root.addView(input, 1, LinearLayout.LayoutParams(-1, dp(48)))
        input.requestFocus()
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
        toast("Android keyboard opened. Linux keyboard forwarding comes in a later milestone.")
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
