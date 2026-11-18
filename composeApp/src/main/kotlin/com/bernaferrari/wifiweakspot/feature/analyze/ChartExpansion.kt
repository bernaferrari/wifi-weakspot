@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package com.bernaferrari.wifiweakspot.feature.analyze

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.bernaferrari.wifiweakspot.core.designsystem.LocalReduceMotion

internal class ChartExpansion(val isExpanded: () -> Boolean, val content: @Composable () -> Unit)

internal class ChartExpansionHost(val sharedScope: SharedTransitionScope) {
    var explorer by mutableStateOf<ChartExpansion?>(null)
}

internal val LocalChartExpansionHost = staticCompositionLocalOf<ChartExpansionHost?> { null }

@Composable
internal fun ChartExpansionLayout(content: @Composable () -> Unit) {
    SharedTransitionLayout(Modifier.fillMaxSize()) {
        val host = remember(this) { ChartExpansionHost(this) }
        CompositionLocalProvider(LocalChartExpansionHost provides host) {
            val explorer = host.explorer
            val semantics = if (explorer?.isExpanded() == true) Modifier.clearAndSetSemantics {} else Modifier
            Box(Modifier.fillMaxSize().then(semantics)) { content() }
            explorer?.content?.invoke()
        }
    }
}

@Composable
internal fun AnimatedVisibilityScope.chartSharedBounds(key: Any, expanded: Boolean): Modifier {
    val host = LocalChartExpansionHost.current ?: return Modifier
    if (LocalReduceMotion.current) return Modifier
    return with(host.sharedScope) {
        Modifier.sharedBounds(
            rememberSharedContentState(key),
            animatedVisibilityScope = this@chartSharedBounds,
            enter = fadeIn(tween(160)),
            exit = fadeOut(tween(120)),
            boundsTransform = BoundsTransform { _, _ -> tween(280) },
            renderInOverlayDuringTransition = expanded,
        )
    }
}
