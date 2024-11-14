package com.emmajson.weatherapp.ui.screencomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Brush

@Composable
fun WeatherItemShimmer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFF0D47A1), shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Column for date and temperatures
        Column(
            modifier = Modifier.weight(1f)
        ) {
            ShimmerPlaceholder(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(20.dp)
                    .padding(bottom = 8.dp)
            )
            ShimmerPlaceholder(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(15.dp)
                    .padding(bottom = 4.dp)
            )
            ShimmerPlaceholder(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(15.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Placeholder for weather icon
        ShimmerPlaceholder(
            modifier = Modifier
                .size(50.dp)
                .background(Color.Gray, shape = RoundedCornerShape(8.dp))
        )
    }
}

@Composable
fun ShimmerPlaceholder(modifier: Modifier) {
    // Animation for shimmer effect
    val shimmerColors = listOf(
        Color.Gray.copy(alpha = 0.7f),
        Color.Gray.copy(alpha = 0.3f),
        Color.Gray.copy(alpha = 0.7f)
    )
    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
    )

    Spacer(
        modifier = modifier
            .background(brush, shape = RoundedCornerShape(8.dp))
    )
}
