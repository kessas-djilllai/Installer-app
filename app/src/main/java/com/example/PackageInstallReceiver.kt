package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PackageInstallReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_INSTALL_COMPLETE = "com.example.INSTALL_COMPLETE"
        
        private val _installStatus = MutableStateFlow<String>("Idle")
        val installStatus: StateFlow<String> = _installStatus
        
        fun updateStatus(message: String) {
            _installStatus.value = message
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_INSTALL_COMPLETE) return
        
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                _installStatus.value = "Waiting for user confirmation..."
                val confirmationIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                }
                
                if (confirmationIntent != null) {
                    confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(confirmationIntent)
                }
            }
            PackageInstaller.STATUS_SUCCESS -> {
                _installStatus.value = "تم التثبيت بنجاح!\nيمكنك الآن إلغاء تثبيت تطبيق المثبت."
            }
            PackageInstaller.STATUS_FAILURE, PackageInstaller.STATUS_FAILURE_ABORTED,
            PackageInstaller.STATUS_FAILURE_BLOCKED, PackageInstaller.STATUS_FAILURE_CONFLICT,
            PackageInstaller.STATUS_FAILURE_INCOMPATIBLE, PackageInstaller.STATUS_FAILURE_INVALID,
            PackageInstaller.STATUS_FAILURE_STORAGE -> {
                _installStatus.value = "Installation failed: $message"
                Log.e("InstallReceiver", "Fail status: $status, message: $message")
            }
            else -> {
                _installStatus.value = "Unknown status: $status, message: $message"
            }
        }
    }
}
