package com.example

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
          AppInstallerScreen(
            modifier = Modifier
              .padding(innerPadding)
              .fillMaxSize()
          )
        }
      }
    }
  }
}

fun isNetworkAvailable(context: Context): Boolean {
  val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  val network = connectivityManager.activeNetwork ?: return false
  val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
  return when {
    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
    else -> false
  }
}

@Composable
fun AppInstallerScreen(modifier: Modifier = Modifier) {
  val installStatus by PackageInstallReceiver.installStatus.collectAsState()
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  var showDisconnectDialog by remember { mutableStateOf(false) }

  // Dynamic colors for status indicators
  val statusColor = when {
    installStatus.contains("بنجاح", ignoreCase = true) || installStatus.contains("successful", ignoreCase = true) -> Color(0xFF10B981)
    installStatus.contains("error", ignoreCase = true) || installStatus.contains("failed", ignoreCase = true) -> Color(0xFFEF4444)
    installStatus == "Idle" -> MaterialTheme.colorScheme.primary
    else -> Color(0xFFF59E0B) // Amber for processing
  }

  // Check if installation is currently active
  val isInstalling = installStatus != "Idle" &&
      !installStatus.contains("بنجاح", ignoreCase = true) && !installStatus.contains("successful", ignoreCase = true) &&
      !installStatus.contains("failed", ignoreCase = true) &&
      !installStatus.contains("error", ignoreCase = true)

  Column(
    modifier = modifier
      .verticalScroll(rememberScrollState())
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(24.dp)
  ) {
    Spacer(modifier = Modifier.height(16.dp))

    // Modern Header Badge Design
    Box(
      modifier = Modifier
        .size(96.dp)
        .shadow(12.dp, shape = RoundedCornerShape(26.dp), clip = false)
        .background(
          brush = Brush.linearGradient(
            colors = listOf(
              MaterialTheme.colorScheme.primary,
              Color(0xFF60A5FA)
            )
          ),
          shape = RoundedCornerShape(26.dp)
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.ArrowDownward,
        contentDescription = "Install Icon",
        modifier = Modifier.size(44.dp),
        tint = Color.White
      )
    }

    // Title & Explanation
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = "Package Launcher",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = (-0.5).sp
        ),
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Deploy, stream, and register offline packages directly from secure device directories with full system parity.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
    }

    // Modern Separator Line
    Spacer(
      modifier = Modifier
        .width(80.dp)
        .height(4.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    )

    // Playback / Installation Status Text overlay
    if (installStatus != "Idle") {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 12.dp)
      ) {
        val statusIcon = when {
          installStatus.contains("بنجاح", ignoreCase = true) || installStatus.contains("successful", ignoreCase = true) -> Icons.Default.Check
          installStatus.contains("error", ignoreCase = true) || installStatus.contains("failed", ignoreCase = true) -> Icons.Default.Warning
          else -> Icons.Default.Refresh
        }
        Icon(
          imageVector = statusIcon,
          contentDescription = "Status",
          tint = statusColor,
          modifier = Modifier.size(18.dp)
        )
        Text(
          text = installStatus,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = statusColor
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Modern Primary Trigger Button
    Button(
      onClick = {
        if (!isInstalling) {
          if (isNetworkAvailable(context)) {
            showDisconnectDialog = true
          } else {
            PackageInstallReceiver.updateStatus("Initializing session installer...")
            coroutineScope.launch {
              AppSessionInstaller.installApp(context, "secondary_app.apk")
            }
          }
        }
      },
      enabled = !isInstalling,
      modifier = Modifier
        .fillMaxWidth()
        .height(58.dp)
        .shadow(if (isInstalling) 0.dp else 4.dp, shape = RoundedCornerShape(18.dp)),
      shape = RoundedCornerShape(18.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = if (isInstalling) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
        disabledContentColor = Color.White
      )
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        if (isInstalling) {
          CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 2.5.dp,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = "Installing Package...",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        } else {
          Icon(
            imageVector = Icons.Default.ArrowDownward,
            contentDescription = "Execute",
            modifier = Modifier.size(24.dp)
          )
          Text(
            text = "Execute Local Install",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }



    if (showDisconnectDialog) {
      AlertDialog(
        onDismissRequest = { showDisconnectDialog = false },
        title = { Text(text = "تنبيه", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
        text = { Text(text = "اقطع الاتصال بالانترنت لتفادي حدوث اي خلل", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
        confirmButton = {
          TextButton(onClick = { showDisconnectDialog = false }) {
            Text("حسناً")
          }
        }
      )
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
