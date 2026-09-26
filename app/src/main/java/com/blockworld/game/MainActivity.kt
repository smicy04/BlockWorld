package com.blockworld.game

import android.app.Activity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        window.setNavigationBarColor(android.graphics.Color.BLACK)
        window.setStatusBarColor(android.graphics.Color.BLACK)

        setContentView(GameView(this))
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        // Pause menu akan kita tambahkan nanti.
    }
}
