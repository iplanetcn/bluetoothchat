package com.phenix.bluetoothchat

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * LauncherActivity
 *
 * @author john
 * @since 2023-09-01
 */
class LauncherActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // keep the splash screen visible for this activity
        splashScreen.setKeepOnScreenCondition{ true }

        lifecycleScope.launch(Dispatchers.Main) {
            delay(1000)
            MainActivity.start(this@LauncherActivity)
            this@LauncherActivity.finish()
        }
    }
}