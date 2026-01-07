package com.hesham.robotcontrollerapplication.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hesham.robotcontrollerapplication.R
import com.hesham.robotcontrollerapplication.RobotActions
import com.hesham.robotcontrollerapplication.ui.theme.BlueColor
import com.hesham.robotcontrollerapplication.ui.theme.buttonColor

@Composable
fun DirectionButton(
    modifier: Modifier = Modifier,
    selectedDirection: Boolean,
    action: RobotActions,
    rotation: Float,
    isPowerOn: Boolean,
    onClick: (RobotActions) -> Unit
) {
    IconButton(
        enabled = isPowerOn,
        colors = IconButtonDefaults.iconButtonColors(containerColor = buttonColor, disabledContainerColor = buttonColor),
        onClick = {
            onClick(action)
        },
        modifier = modifier
            .shadow(elevation = 4.dp, shape = CircleShape, spotColor = Color.White)
//            .padding(2.dp)
            .clip(shape = CircleShape)
            .border(width = 1.dp, color = Color.Gray, shape = CircleShape)
            .height(60.dp)
    ) {
//        Icon(
//            tint = BlueColor,
//            painter = painterResource(R.drawable.arrow_icon), contentDescription = "$action",
//            modifier = Modifier.rotate(rotation)
//        )
        Icon(
            painter = painterResource(R.drawable.arrow_up),
            contentDescription = null,
//            tint = if (isPowerOn)Color.White else Color.White.copy(.2f),
            tint = if (selectedDirection) Color.Green else {
                if (isPowerOn) Color.White else Color.Unspecified.copy(.2f)
            },
            modifier = Modifier
                .rotate(rotation)
                .size(33.dp)
        )
    }

}
