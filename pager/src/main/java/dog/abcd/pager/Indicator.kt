package dog.abcd.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

@Composable
fun ScrollBallIndicator(
    modifier: Modifier = Modifier,
    swipeState: PagerSwipeState,
    ballSize: Dp = 10.dp,
    spaceSize: Dp = 10.dp,
    indicatorSize: Dp = 10.dp,
    unSelectColor: Color = Color(0x66FFFFFF),
    selectColor: Color = Color.White,
    underIndicator: Boolean = false,
) {

    Box(
        modifier = modifier
    ) {
        var totalWidth by remember { mutableStateOf(0) }
        Row(
            modifier = Modifier
                .onGloballyPositioned {
                    totalWidth = it.size.width
                }
                .zIndex(2f)
                .align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(spaceSize)
        ) {
            for (i in 0 until swipeState.total) {
                Box(
                    modifier = Modifier
                        .size(ballSize)
                        .background(unSelectColor, CircleShape)
                )
            }
        }
        Spacer(
            modifier = Modifier
                .offset {
                    val max = (swipeState.total - 1).coerceIn(1, Int.MAX_VALUE)
                    val progress =
                        if (swipeState.from < swipeState.to) {
                            swipeState.fraction
                        } else if (swipeState.from > swipeState.to) {
                            -swipeState.fraction
                        } else {
                            0f
                        }
                    val offsetMulti = (swipeState.from + progress).coerceIn(0f, max.toFloat())
                    val gap = (totalWidth.toFloat() - ballSize.toPx()) / (max * 2) * 2
                    val offset = offsetMulti * gap - (indicatorSize.toPx() - ballSize.toPx()) / 2
                    IntOffset(
                        offset.roundToInt(),
                        0
                    )
                }
                .size(indicatorSize)
                .background(selectColor, CircleShape)
                .zIndex(if (underIndicator) 1f else 3f)
                .align(Alignment.CenterStart)
        )

    }
}

@Composable
fun BasicIndicator(
    modifier: Modifier = Modifier,
    swipeState: PagerSwipeState,
    height: Dp = 6.dp,
    unSelectWidth: Dp = 6.dp,
    selectWidth: Dp = 18.dp,
    spaceSize: Dp = 6.dp,
    unSelectColor: Color = Color(0x66FFFFFF),
    selectColor: Color = Color.White
) {
    Row(
        modifier = modifier
    ) {
        for (i in 0 until swipeState.total) {
            val select = swipeState.current == i
            Spacer(
                modifier = Modifier
                    .size(if (select) selectWidth else unSelectWidth, height)
                    .background(
                        if (select) selectColor else unSelectColor,
                        CircleShape
                    )
            )
            if (i < swipeState.total - 1) {
                Spacer(modifier = Modifier.width(spaceSize))
            }
        }
    }
}