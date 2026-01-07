package com.hesham.robotcontrollerapplication

import androidx.compose.ui.graphics.Color

sealed class UiEvents {
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = "",
        val color: Color? = null
    ) : UiEvents()
}