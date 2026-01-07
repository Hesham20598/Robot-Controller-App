package com.hesham.robotcontrollerapplication.ui.screens

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.robotcontroller.RobotBluetoothController
import com.hesham.robotcontrollerapplication.R
import com.hesham.robotcontrollerapplication.RobotActions
import com.hesham.robotcontrollerapplication.RobotSpeed
import com.hesham.robotcontrollerapplication.UiEvents
import com.hesham.robotcontrollerapplication.enableImmersiveMode
import com.hesham.robotcontrollerapplication.ui.components.DirectionButton
import com.hesham.robotcontrollerapplication.ui.components.LoadingDialog
import com.hesham.robotcontrollerapplication.ui.components.RobotSpeedSlider
import com.hesham.robotcontrollerapplication.ui.theme.backgroundColor
import com.hesham.robotcontrollerapplication.ui.theme.buttonColor
import com.hesham.robotcontrollerapplication.ui.theme.disconnectedColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent() {
    val activity = LocalActivity.current as Activity

        activity.enableImmersiveMode()

    val context = LocalContext.current
    val controller = remember {
        RobotBluetoothController(context)
    }
    val isPowerOn = controller.isConnected.collectAsStateWithLifecycle().value
    val isLoading = controller.isLoading.collectAsStateWithLifecycle().value
    val scope = rememberCoroutineScope()

    val snackBarHostState = remember { SnackbarHostState() }
    var snackbarColor by remember { mutableStateOf(Color.White) }
    var selectedDirection by remember{
        mutableStateOf(RobotActions.None)
    }
    var isBoxOpened by remember{
        mutableStateOf(false)
    }

    LoadingDialog(isLoading)


    LaunchedEffect(Unit) {
        controller.events.collect { event ->
            when (event) {
                is UiEvents.ShowSnackbar -> {
                    snackbarColor = event.color ?: Color.White
                    snackBarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            delay(300)
            snackBarHostState.currentSnackbarData?.dismiss()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackBarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 0.dp),
                    containerColor = if (snackbarColor == Color.Red || snackbarColor == Color.Green) snackbarColor.copy(
                        .79f
                    ) else snackbarColor,
                    contentColor = if (snackbarColor == Color.Red || snackbarColor == Color.Green) Color.White else Color.Black
                )
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize()
                .background(buttonColor)
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                        .size(10.dp)
                        .background(
                            if (isPowerOn) Color.Green else Color.Red.copy(.6f),
                            shape = CircleShape
                        )
                )
                LazyVerticalGrid(
                    userScrollEnabled = false,
                    modifier = Modifier
                        .shadow(
                            elevation = 5.dp,
                            shape = CircleShape,
                            ambientColor = Color.White,
                            spotColor = if (isPowerOn) Color.Green else Color.White,
                            clip = false
                        )
                        .width(500.dp)
                        .height(300.dp)
                        .background(buttonColor, shape = CircleShape)
                        .clip(CircleShape)
                        .border(width = 1.dp , color = if (isPowerOn) Color.Green.copy(.3f) else Color.Gray, shape = CircleShape),
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(horizontal = 47.dp, vertical = 45.dp),
                    horizontalArrangement = Arrangement.spacedBy(25.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)

                ) {
                    item() {
                        DirectionButton(
                            action = RobotActions.ForwardLeft,
                            rotation = -45f,
                            selectedDirection = selectedDirection == RobotActions.ForwardLeft,
                            isPowerOn = isPowerOn,
                            onClick = { directionClicked ->
                                scope.launch {
                                    selectedDirection = directionClicked
                                    controller.sendActions(directionClicked)
                                }
                            }
                        )
                    }
                    item() {
                        DirectionButton(
                            modifier = Modifier.padding(bottom = 4.dp),
                            action = RobotActions.Forward,
                            selectedDirection = selectedDirection == RobotActions.Forward,
                            rotation = 0f,
                            isPowerOn = isPowerOn,
                            onClick = { directionClicked ->
                                scope.launch {
                                    selectedDirection = directionClicked
                                    controller.sendActions(directionClicked)
                                }
                            }
                        )
                    }
                    item() {
                        DirectionButton(
                            action = RobotActions.ForwardRight,
                            rotation = 45f,
                            selectedDirection = selectedDirection == RobotActions.ForwardRight,
                            isPowerOn = isPowerOn,
                            onClick = { directionClicked ->
                                scope.launch {
                                    selectedDirection = directionClicked
                                    controller.sendActions(directionClicked)
                                }
                            }
                        )
                    }
                    item() {
                        DirectionButton(
                            action = RobotActions.Left,
                            rotation = -90f,
                            selectedDirection = selectedDirection == RobotActions.Left,
                            isPowerOn = isPowerOn,
                            onClick = { directionClicked ->
                                scope.launch {
                                    selectedDirection = directionClicked
                                    controller.sendActions(directionClicked)
                                }
                            }
                        )
                    }
                    item() {


                        IconButton(
                            enabled = isPowerOn,
                            colors = IconButtonDefaults.iconButtonColors(containerColor = buttonColor, disabledContainerColor = buttonColor),
                            onClick = {
                                scope.launch {
                                     selectedDirection = RobotActions.None
                                    controller.sendActions(RobotActions.Stop)
                                }
                            },
                            modifier = Modifier
                                .shadow(
                                    elevation = 4.dp,
                                    shape = CircleShape,
                                    spotColor = Color.White
                                )

                                .clip(shape = CircleShape)
                                .border(width = 1.dp, color = Color.Gray,shape = CircleShape)
                                .height(60.dp)
                        ) {
                            Text("Stop",color =    if (isPowerOn) Color.White else disconnectedColor)
                        }

//                            IconButton(
//                                modifier = Modifier
//                                    .height(50.dp)
//                                    .shadow(
//                                        elevation = 2.dp,
//                                        spotColor = Color.White,
//                                        shape = ButtonDefaults.squareShape,
//                                        ambientColor = Color.White,
//                                        clip = true
//                                    ),
//                                onClick = {
//                                    scope.launch {
//                                        controller.sendActions(RobotActions.Stop)
//                                    }
//                                }
//                            ) {
//                                Text(
//                                    "Stop Robot",
//                                    fontWeight = FontWeight.W600,
//                                    color = if (isPowerOn) Color.White else disconnectedColor
//                                )
//                            }

                    }
                    item() {
                        DirectionButton(
                            action = RobotActions.Right,
                            rotation = 90f,
                            selectedDirection = selectedDirection ==RobotActions.Right ,
                            isPowerOn = isPowerOn,
                            onClick = { directionClicked ->
                                scope.launch {
                                    selectedDirection = directionClicked
                                    controller.sendActions(directionClicked)
                                }
                            }
                        )
                    }
                    item() {
                        DirectionButton(
                            action = RobotActions.BackwardLeft,
                            rotation = 225f,
                            selectedDirection = selectedDirection == RobotActions.BackwardLeft,
                            isPowerOn = isPowerOn,
                            onClick = { directionClicked ->
                                scope.launch {
                                    selectedDirection = directionClicked
                                    controller.sendActions(directionClicked)
                                }
                            }
                        )
                    }
                    item() {
                        DirectionButton(
                            modifier = Modifier.padding(top = 4.dp),
                            action = RobotActions.Backward,
                           selectedDirection = selectedDirection== RobotActions.Backward,
                            rotation = 180f,
                            isPowerOn = isPowerOn,
                            onClick = { directionClicked ->
                                scope.launch {
                                    selectedDirection = RobotActions.Backward
                                    controller.sendActions(directionClicked)
                                }
                            }
                        )
                    }
                    item() {
                        DirectionButton(
                            action = RobotActions.BackwardRight,
                            rotation = 135f,
                            selectedDirection = selectedDirection == RobotActions.BackwardRight,
                            isPowerOn = isPowerOn,
                            onClick = { directionClicked ->
                                scope.launch {
                                    selectedDirection = directionClicked
                                    controller.sendActions(directionClicked)
                                }
                            }
                        )
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(20.dp))
                    RobotSpeedSlider(
                        enabled = isPowerOn,
                        initialLevel = 5,
                        onSpeedCommit = { speed ->
                            controller.setSpeed(speed)
                        },

                        )
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 40.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Button(
                            enabled = isPowerOn && !isBoxOpened,
                            elevation = ButtonDefaults.buttonElevation(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor, disabledContainerColor = buttonColor),
                            shapes = ButtonDefaults.shapes(shape = ButtonDefaults.squareShape),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .shadow(
                                    elevation = 2.dp,
                                    spotColor = Color.White,
                                    shape = ButtonDefaults.squareShape,
                                    ambientColor = Color.White,
                                    clip = true
                                ),
                            onClick = {
                                scope.launch {
                                    controller.sendActions(RobotActions.OpenBox)
                                    isBoxOpened = true
                                }
                            }
                        ) {
                            Text(
                                "Open Box",
                                fontWeight = FontWeight.W600,
                                color = if (isBoxOpened) Color.Green else {if (isPowerOn) Color.White else disconnectedColor}
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Button(enabled = isPowerOn && isBoxOpened,
                            elevation = ButtonDefaults.buttonElevation(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor, disabledContainerColor = buttonColor),
                            shapes = ButtonDefaults.shapes(shape = ButtonDefaults.squareShape),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .shadow(
                                    elevation = 2.dp,
                                    spotColor = Color.White,
                                    shape = ButtonDefaults.squareShape,
                                    ambientColor = Color.White,
                                    clip = true
                                ),
                            onClick = {
                                scope.launch {
                                    controller.sendActions(RobotActions.CloseBox)
                                    isBoxOpened = false
                                }
                            }
                        ) {
                            Text(
                                "Close Box",
                                fontWeight = FontWeight.W600,
                                color = if (isPowerOn) Color.White else disconnectedColor
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            modifier = Modifier.size(60.dp),
                            onClick = {
                                if (isPowerOn) {
                                    scope.launch {
                                        controller.sendActions(RobotActions.Stop)
                                        selectedDirection = RobotActions.None
                                        controller.disconnect()
                                    }

                                } else {
                                    scope.launch {
                                        controller.ensureConnected()
                                    }
                                }
                            }) {
                            Image(
                                modifier = Modifier,
                                painter = painterResource(if (isPowerOn) R.drawable.power_off_icon else R.drawable.power_on_icon),
                                contentDescription = "Power Button",
                            )
                        }
//                        Button(
//                            elevation = ButtonDefaults.buttonElevation(4.dp),
//                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
//                            shapes = ButtonDefaults.shapes(shape = ButtonDefaults.squareShape),
//                            modifier = Modifier
//                                .height(50.dp)
//                                .shadow(
//                                    elevation = 2.dp,
//                                    spotColor = Color.White,
//                                    shape = ButtonDefaults.squareShape,
//                                    ambientColor = Color.White,
//                                    clip = true
//                                ),
//                            onClick = {
//                                scope.launch {
//                                    controller.sendActions(RobotActions.Stop)
//                                }
//                            }
//                        ) {
//                            Text(
//                                "Stop Robot",
//                                fontWeight = FontWeight.W600,
//                                color = if (isPowerOn) Color.White else disconnectedColor
//                            )
//                        }
                    }
                }

            }
//            Column(
//                verticalArrangement = Arrangement.Center,
//                modifier = Modifier
//                    .shadow(
//                        elevation = 12.dp,
//                        shape = CircleShape,
//                        ambientColor = Color.White,
//                        spotColor = Color.White.copy(.96f),
//                        clip = true
//                    )
//                    .clip(CircleShape)
//                    .background(roundedCircleColor, shape = CircleShape)
//                    .padding(horizontal = 30.dp, vertical = 20.dp)
//                    .align(Alignment.Center)
//
//
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceEvenly
//                ) {
//                    DirectionButton(
//                        action = RobotActions.ForwardLeft,
//                        rotation = -45f,
//                        onClick = { directionClicked ->
//                            scope.launch {
//                                controller.sendActions(directionClicked)
//                            }
//                        }
//                    )
//
//                    DirectionButton(
//                        action = RobotActions.Forward,
//                        rotation = 0f,
//                        onClick = { directionClicked ->
//                            scope.launch {
//                                controller.sendActions(directionClicked)
//                            }
//                        }
//                    )
//                    DirectionButton(
//                        action = RobotActions.ForwardRight,
//                        rotation = 45f,
//                        onClick = { directionClicked ->
//                            scope.launch {
//                                controller.sendActions(directionClicked)
//                            }
//                        }
//                    )
//                }
//                Spacer(Modifier.height(20.dp))
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceEvenly
//                ) {
//                    DirectionButton(
//                        action = RobotActions.Left,
//                        rotation = -90f,
//                        onClick = { directionClicked ->
//                            scope.launch {
//                                controller.sendActions(directionClicked)
//                            }
//                        }
//                    )
//                    //! power button .......................
//                    Image(
//                        modifier = Modifier
//                            .size(60.dp)
//                            .clickable(
//                                interactionSource = null,
//                                indication = null,
//                                onClick = {
//                                    if (isPowerOn) {
//                                        scope.launch {
//                                            controller.disconnect()
//                                        }
//
//                                    } else {
//                                        scope.launch {
//                                            controller.ensureConnected()
//                                        }
//                                    }
//                                }),
//                        painter = painterResource(if (isPowerOn) R.drawable.power_off_icon else R.drawable.power_on_icon),
//                        contentDescription = "Power Button",
//                    )
//
//                    DirectionButton(
//                        action = RobotActions.Right,
//                        rotation = 90f,
//                        onClick = { directionClicked ->
//                            scope.launch {
//                                controller.sendActions(directionClicked)
//                            }
//                        }
//                    )
//
//                }
//                Spacer(Modifier.height(20.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceEvenly
//                ) {
//                    DirectionButton(
//                        action = RobotActions.BackwardLeft,
//                        rotation = 225f,
//                        onClick = { directionClicked ->
//                            scope.launch {
//                                controller.sendActions(directionClicked)
//                            }
//                        }
//                    )
//
//                    DirectionButton(
//                        action = RobotActions.Backward,
//                        rotation = 180f,
//                        onClick = { directionClicked ->
//                            scope.launch {
//                                controller.sendActions(directionClicked)
//                            }
//                        }
//                    )
//                    DirectionButton(
//                        action = RobotActions.BackwardRight,
//                        rotation = 135f,
//                        onClick = { directionClicked ->
//                            scope.launch {
//                                controller.sendActions(directionClicked)
//                            }
//                        }
//                    )
//                }
//            }
            Card(
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .shadow(
                        elevation = 2.dp,
                        spotColor = Color.White,
                        clip = true,
                        shape = CircleShape
                    )
                    .align(Alignment.BottomCenter),
                colors = CardDefaults.cardColors(containerColor = buttonColor.copy(.95f)),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Text(
                    text = if (isPowerOn) "Connected" else "Not Connected",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W600,
                        color = if (isPowerOn) Color.White else disconnectedColor
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)
                )
            }

        }

    }
}

val SpeedSteps: List<SpeedStep> = listOf(
    SpeedStep(level = 0, speed = RobotSpeed.SPEED_10, label = "10%"),
    SpeedStep(level = 1, speed = RobotSpeed.SPEED_20, label = "20%"),
    SpeedStep(level = 2, speed = RobotSpeed.SPEED_30, label = "30%"),
    SpeedStep(level = 3, speed = RobotSpeed.SPEED_40, label = "40%"),
    SpeedStep(level = 4, speed = RobotSpeed.SPEED_50, label = "50%"),
    SpeedStep(level = 5, speed = RobotSpeed.SPEED_60, label = "60%"),
    SpeedStep(level = 6, speed = RobotSpeed.SPEED_70, label = "70%"),
    SpeedStep(level = 7, speed = RobotSpeed.SPEED_80, label = "80%"),
    SpeedStep(level = 8, speed = RobotSpeed.SPEED_90, label = "90%"),
    SpeedStep(level = 9, speed = RobotSpeed.SPEED_95, label = "95%"),
    SpeedStep(level = 10, speed = RobotSpeed.SPEED_100, label = "100%")
)

data class SpeedStep(
    val level: Int,            // 0..10
    val speed: RobotSpeed,     // enum
    val label: String
)

fun speedFromLevel(level: Int): RobotSpeed {
    val safe = level.coerceIn(0, 10)
    return SpeedSteps.first { it.level == safe }.speed
}

fun labelFromLevel(level: Int): String {
    val safe = level.coerceIn(0, 10)
    return SpeedSteps.first { it.level == safe }.label
}