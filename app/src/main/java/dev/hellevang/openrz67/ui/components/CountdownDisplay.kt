package dev.hellevang.openrz67.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.hellevang.openrz67.ui.theme.Dimens

@Composable
fun CountdownDisplay(
    startDelayedTrigger: Boolean,
    countdownTimeLeft: Int,
    countdownDuration: Int,
    onDurationChange: (Int) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    
    val durationOptions = listOf(
        2 to "2s",
        5 to "5s", 
        10 to "10s",
        15 to "15s",
        30 to "30s",
        60 to "1m",
        120 to "2m",
        180 to "3m",
        240 to "4m"
    )
    if (startDelayedTrigger) {
        if (countdownTimeLeft > 0) {
            Text(
                fontSize = Dimens.CountdownTextSize,
                color = MaterialTheme.colors.secondary,
                text = "$countdownTimeLeft"
            )
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                fontSize = Dimens.BodyTextSize,
                color = MaterialTheme.colors.onBackground,
                text = "Start"
            )
            
            Box {
                OutlinedButton(
                    onClick = { dropdownExpanded = true },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(durationOptions.find { it.first == countdownDuration }?.second ?: "${countdownDuration}s")
                }
                
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    durationOptions.forEach { (seconds, label) ->
                        DropdownMenuItem(
                            onClick = {
                                onDurationChange(seconds)
                                dropdownExpanded = false
                            }
                        ) {
                            Text(label)
                        }
                    }
                }
            }
            
            Text(
                fontSize = Dimens.BodyTextSize,
                color = MaterialTheme.colors.onBackground,
                text = "countdown"
            )
        }
    }
}