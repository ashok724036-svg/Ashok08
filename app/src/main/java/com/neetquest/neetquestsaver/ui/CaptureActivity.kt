package com.neetquest.neetquestsaver.ui

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.neetquest.neetquestsaver.service.ScreenCaptureService

/**
 * Transparent activity launched by FloatingBubbleService.
 * Requests MediaProjection permission then starts capture.
 */
class CaptureActivity : ComponentActivity() {

    private val projectionManager by lazy {
        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private val capturePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            ScreenCaptureService.startCapture(this, result.resultCode, result.data!!)
            // Navigate to crop editor in MainActivity
            val intent = Intent(this, MainActivity::class.java).apply {
                action = ACTION_OPEN_CROP
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        capturePermissionLauncher.launch(projectionManager.createScreenCaptureIntent())
    }

    companion object {
        const val ACTION_OPEN_CROP = "com.neetquest.neetquestsaver.OPEN_CROP"
    }
}
