package com.pulsewave.app.core.designsystem.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * The Networks tab: two overlapping hills on a baseline, the same picture as the channel chart,
 * so the icon says "networks side by side" rather than a generic radar.
 */
object NetworksIcons {
    private fun PathBuilder.tall() {
        moveTo(2.5f, 19.5f); curveTo(4.5f, 19.5f, 5.5f, 5f, 8.5f, 5f); curveTo(11.5f, 5f, 12.5f, 19.5f, 14.5f, 19.5f)
    }
    private fun PathBuilder.short() {
        moveTo(9.5f, 19.5f); curveTo(11.5f, 19.5f, 12.8f, 10f, 15.5f, 10f); curveTo(18.2f, 10f, 19.5f, 19.5f, 21.5f, 19.5f)
    }

    val Outlined: ImageVector by lazy {
        ImageVector.Builder("Networks.Outlined", 24.dp, 24.dp, 24f, 24f).apply {
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) { tall() }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) { short() }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round) { moveTo(2f, 20.5f); lineTo(22f, 20.5f) }
        }.build()
    }

    val Filled: ImageVector by lazy {
        ImageVector.Builder("Networks.Filled", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.Black), fillAlpha = 0.55f) { short(); close() }
            path(fill = SolidColor(Color.Black)) { tall(); close() }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round) { moveTo(2f, 20.5f); lineTo(22f, 20.5f) }
        }.build()
    }
}

/**
 * "Test a website": a browser window whose page is a heartbeat line, i.e. "is this site alive and
 * how fast does it answer", rather than a generic globe or gauge.
 */
object SiteCheckIcon {
    val Filled: ImageVector by lazy {
        ImageVector.Builder("SiteCheck", 24.dp, 24.dp, 24f, 24f).apply {
            // Window frame with a title bar.
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineJoin = StrokeJoin.Round) {
                moveTo(5f, 3.5f); lineTo(19f, 3.5f)
                curveTo(20.4f, 3.5f, 21.5f, 4.6f, 21.5f, 6f); lineTo(21.5f, 18f)
                curveTo(21.5f, 19.4f, 20.4f, 20.5f, 19f, 20.5f); lineTo(5f, 20.5f)
                curveTo(3.6f, 20.5f, 2.5f, 19.4f, 2.5f, 18f); lineTo(2.5f, 6f)
                curveTo(2.5f, 4.6f, 3.6f, 3.5f, 5f, 3.5f); close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(2.5f, 6f); curveTo(2.5f, 4.6f, 3.6f, 3.5f, 5f, 3.5f); lineTo(19f, 3.5f)
                curveTo(20.4f, 3.5f, 21.5f, 4.6f, 21.5f, 6f); lineTo(21.5f, 8f); lineTo(2.5f, 8f); close()
            }
            // The page: a pulse, the site answering.
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(5.5f, 14.5f); lineTo(8.5f, 14.5f); lineTo(10.2f, 11f); lineTo(12.6f, 17.5f); lineTo(14.6f, 12.5f); lineTo(15.8f, 14.5f); lineTo(18.5f, 14.5f)
            }
        }.build()
    }
}
