package dev.hellevang.openrz67.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.hellevang.openrz67.R
import dev.hellevang.openrz67.bluetooth.ConnectionState

@Composable
fun Header(connectionState: ConnectionState, batteryPercent: Int?, onReconnect: () -> Unit) {
    val isConnected = connectionState == ConnectionState.Connected
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.header_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                val dot = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                Spacer(Modifier.size(8.dp).clip(CircleShape).background(dot))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = when (connectionState) {
                        ConnectionState.Idle -> stringResource(R.string.state_idle)
                        ConnectionState.Scanning -> stringResource(R.string.state_scanning)
                        ConnectionState.Connecting -> stringResource(R.string.state_connecting)
                        ConnectionState.Connected ->
                            if (batteryPercent == null) stringResource(R.string.state_connected)
                            else stringResource(R.string.state_connected_battery, batteryPercent)
                        ConnectionState.Disconnected -> stringResource(R.string.state_disconnected)
                        ConnectionState.Failed -> stringResource(R.string.state_failed)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!isConnected) {
            TextButton(onClick = onReconnect) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.reconnect))
            }
        }
    }
}
