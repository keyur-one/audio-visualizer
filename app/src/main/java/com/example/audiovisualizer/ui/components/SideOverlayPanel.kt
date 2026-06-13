package com.example.audiovisualizer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class PanelSide { Left, Right }

@Composable
fun SideOverlayPanel(
    visible: Boolean,
    side: PanelSide,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val slideFromLeft = side == PanelSide.Left

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInHorizontally { if (slideFromLeft) -it else it },
        exit = fadeOut() + slideOutHorizontally { if (slideFromLeft) -it else it },
        modifier = modifier.fillMaxSize(),
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            )
            Box(
                modifier = Modifier
                    .align(if (slideFromLeft) Alignment.CenterStart else Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(320.dp)
                    .background(Color(0xD9120A22)),
            ) {
                content()
            }
        }
    }
}
