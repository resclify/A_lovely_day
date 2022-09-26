package com.tastyrhino.android

import android.os.Bundle

import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.tastyrhino.ALovelyDay

/** Launches the Android application. */
class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize(ALovelyDay(), AndroidApplicationConfiguration().apply {
            // Configure your application here.
        })
    }
}
