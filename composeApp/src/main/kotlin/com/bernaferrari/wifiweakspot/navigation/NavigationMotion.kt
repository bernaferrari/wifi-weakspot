@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay

private val NoMotion = ContentTransform(EnterTransition.None, ExitTransition.None)

/**
 * Pushes slide in from the trailing edge while the screen underneath drifts a fifth of the way
 * (parallax, so it reads as depth). Back, including the predictive back gesture, plays the same
 * motion in reverse, so swiping back previews exactly what releasing will do.
 */
@Composable
fun rememberPushTransitionMetadata(reduceMotion: Boolean): Map<String, Any> {
    val motionScheme = MaterialTheme.motionScheme
    val direction = LocalLayoutDirection.current
    return remember(motionScheme, direction, reduceMotion) {
        if (reduceMotion) {
            return@remember NavDisplay.transitionSpec { NoMotion } + NavDisplay.popTransitionSpec { NoMotion } +
                NavDisplay.predictivePopTransitionSpec { _ -> NoMotion }
        }
        val sign = if (direction == LayoutDirection.Rtl) -1 else 1
        val pop = ContentTransform(
            targetContentEnter = slideInHorizontally(motionScheme.defaultSpatialSpec()) { -sign * it / 5 },
            initialContentExit = slideOutHorizontally(motionScheme.defaultSpatialSpec()) { sign * it }
        )
        NavDisplay.transitionSpec {
            ContentTransform(
                targetContentEnter = slideInHorizontally(motionScheme.defaultSpatialSpec()) { sign * it },
                initialContentExit = slideOutHorizontally(motionScheme.defaultSpatialSpec()) { -sign * it / 5 }
            )
        } + NavDisplay.popTransitionSpec { pop } + NavDisplay.predictivePopTransitionSpec { _ -> pop }
    }
}

private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

/**
 * Material fade through for tabs, which have no spatial relationship: the old screen clears
 * quickly, then the new one settles in from a slight scale, so two screens never blend.
 */
fun <T : Any> fadeThrough(reduceMotion: Boolean): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    if (reduceMotion) NoMotion
    else ContentTransform(
        targetContentEnter = fadeIn(tween(210, delayMillis = 90, easing = EmphasizedDecelerate)) +
            scaleIn(tween(210, delayMillis = 90, easing = EmphasizedDecelerate), initialScale = 0.92f),
        initialContentExit = fadeOut(tween(90, easing = EmphasizedAccelerate))
    )
}
