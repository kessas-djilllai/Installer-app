package com.example

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

object AppSessionInstaller {
    suspend fun installApp(context: Context, assetFileName: String) = withContext(Dispatchers.IO) {
        val packageInstaller = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        var sessionId = -1
        var session: PackageInstaller.Session? = null
        try {
            PackageInstallReceiver.updateStatus("Preparing installation...")
            sessionId = packageInstaller.createSession(params)
            session = packageInstaller.openSession(sessionId)
            
            val inputStream: InputStream = try {
                context.assets.open(assetFileName)
            } catch (e: Exception) {
                Log.e("AppSessionInstaller", "Asset not found: $assetFileName", e)
                session.abandon()
                PackageInstallReceiver.updateStatus("Error: $assetFileName not found in assets")
                return@withContext
            }
            
            PackageInstallReceiver.updateStatus("Streaming APK data...")
            
            val outputStream: OutputStream = session.openWrite("package", 0, -1)
            
            val buffer = ByteArray(65536)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            session.fsync(outputStream)
            
            inputStream.close()
            outputStream.close()
            
            val intent = Intent(context, PackageInstallReceiver::class.java).apply {
                action = PackageInstallReceiver.ACTION_INSTALL_COMPLETE
                setPackage(context.packageName) // Ensure it routes back to our app
            }
            
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                intent,
                flags
            )
            
            PackageInstallReceiver.updateStatus("Committing installation session...")
            session.commit(pendingIntent.intentSender)
        } catch (e: Exception) {
            e.printStackTrace()
            session?.abandon()
            PackageInstallReceiver.updateStatus("Installation error: ${e.message}")
        }
    }
}
