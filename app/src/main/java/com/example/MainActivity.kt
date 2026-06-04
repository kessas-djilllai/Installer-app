package com.example

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

@Composable
fun AppInstallerScreen(modifier: Modifier = Modifier) {
  val installStatus by PackageInstallReceiver.installStatus.collectAsState()
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  // Dynamic colors for status indicators
  val statusColor = when {
    installStatus.contains("successful", ignoreCase = true) -> Color(0xFF10B981)
    installStatus.contains("error", ignoreCase = true) || installStatus.contains("failed", ignoreCase = true) -> Color(0xFFEF4444)
    installStatus == "Idle" -> MaterialTheme.colorScheme.primary
    else -> Color(0xFFF59E0B) // Amber for processing
  }

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
        imageVector = Icons.Default.PlayArrow,
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
          installStatus.contains("successful", ignoreCase = true) -> Icons.Default.Check
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
        PackageInstallReceiver.updateStatus("Initializing session installer...")
        coroutineScope.launch {
          AppSessionInstaller.installApp(context, "secondary_app.apk")
        }
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(58.dp)
        .shadow(4.dp, shape = RoundedCornerShape(18.dp)),
      shape = RoundedCornerShape(18.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
      )
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = "Execute",
          modifier = Modifier.size(24.dp)
        )
        Text(
          text = "Execute Local Install",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      }
    }

    // Informative Safe-Install Disclaimers
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
      )
    ) {
      Row(
        modifier = Modifier.padding(14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Info,
          contentDescription = "Attention Icon",
          tint = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.size(20.dp)
        )
        Text(
          text = "System prompts require direct authentication and explicit consent. All tasks run ethically with prior knowledge and complete user control overrides.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSecondaryContainer
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
