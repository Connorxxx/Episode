package com.connor.episode.features.common.ui.animate

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith


fun heightTransition(
    durationMillis: Int = 300
): AnimatedContentTransitionScope<Boolean>.() -> ContentTransform = {
    // 使用短暂的淡入淡出效果增强动画感
    fadeIn(animationSpec = tween(durationMillis = 100)) togetherWith
            fadeOut(animationSpec = tween(durationMillis = 100)) using
            // 自定义SizeTransform实现展开/收缩效果
            SizeTransform(
                // 定义尺寸变化的动画规格
                sizeAnimationSpec = { initialSize, targetSize ->
                    tween(
                        durationMillis = durationMillis,
                        easing = LinearOutSlowInEasing
                    )
                },
                // 不裁剪内容，允许内容在动画过程中可见
                clip = true,
            )
}