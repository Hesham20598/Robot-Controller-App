package com.hesham.robotcontrollerapplication.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.hesham.robotcontrollerapplication.RobotSpeed
import com.hesham.robotcontrollerapplication.ui.screens.labelFromLevel
import com.hesham.robotcontrollerapplication.ui.screens.speedFromLevel
import com.hesham.robotcontrollerapplication.ui.theme.backgroundColor
import com.hesham.robotcontrollerapplication.ui.theme.buttonColor
import com.hesham.robotcontrollerapplication.ui.theme.roundedCircleColor
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun RobotSpeedSlider(
    modifier: Modifier = Modifier,
    initialLevel: Int = 5, // default 60%
    enabled: Boolean ,
    onSpeedCommit: suspend (RobotSpeed) -> Unit
) {
    val scope = rememberCoroutineScope()

    // level: 0..10
    var level by rememberSaveable { mutableIntStateOf(initialLevel.coerceIn(0, 10)) }

    val label = remember(level) { labelFromLevel(level) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Speed", color = if (enabled) Color.White  else Color.Unspecified)
            Text(label, color = if (enabled) Color.White else Color.Unspecified)
        }

        Slider(
            colors = SliderDefaults.colors(
                thumbColor = if (enabled) Color.White else Color.Unspecified,
                activeTrackColor = backgroundColor.copy(.3f),
                activeTickColor = buttonColor,
                inactiveTrackColor = backgroundColor.copy(.3f),
                inactiveTickColor = Color.White,
                disabledThumbColor = Color.Unspecified.copy(.25f),


            ),
            enabled = enabled,
            value = level.toFloat(),
            onValueChange = { v ->
                level = v.roundToInt().coerceIn(0, 10)
            },
            onValueChangeFinished = {
                val speed = speedFromLevel(level)
                scope.launch {
                    onSpeedCommit(speed)
                }
            },
            valueRange = 0f..10f,
            steps = 9 // rounding هو اللي بيخليها discrete
        )
    }
}
