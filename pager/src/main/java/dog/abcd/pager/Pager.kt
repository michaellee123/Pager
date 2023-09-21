@file:OptIn(ExperimentalMaterialApi::class)

package dog.abcd.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.SwipeableState
import androidx.compose.material.ThresholdConfig
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


fun <T> List<T>.makeLoop(loopLimit: Int = 1): List<T> {
    val result = ArrayList<T>()
    if (loopLimit == 1) {
        result.addAll(this)
        result.add(0, last())
        result.add(first())
        return result
    }
    if (loopLimit == 3) {
        when (size) {
            0 -> {
                // 返回空的
            }

            1 -> {
                //返回7个满的
                for (i in 0 until 8) {
                    result.add(first())
                }
            }

            2 -> {
                result.add(first())
                result.add(this[1])
                for (i in 0 until 3) {
                    result.add(0, result.last())
                    result.add(result[1])
                }
            }

            3 -> {
                result.addAll(this)
                result.addAll(this)
                result.addAll(this)
            }

            else -> {
                result.addAll(this.subList(this.size - 3, this.size))
                result.addAll(this)
                result.addAll(this.subList(0, 3))
            }
        }
        return result
    } else {
        throw IllegalArgumentException("loopLimit must be 1 or 3")
    }
}

class PagerSwipeState(
    val total: Int = 0,
    val current: Int = 0,
    val from: Int = 0,
    val to: Int = 0,
    val fraction: Float = 0f
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> BasicPager(
    modifier: Modifier = Modifier,
    thresholds: (from: Int, to: Int) -> ThresholdConfig = { _, _ ->
        FractionalThreshold(0.3f)
    },
    velocityThreshold: Dp = SwipeableDefaults.VelocityThreshold,
    loop: Boolean = false,
    loopLimit: Int = 0,
    userEnable: Boolean = true,
    autoSwipe: Boolean = false,
    duration: Long = 3000,
    pagerSwipeState: MutableState<PagerSwipeState> = mutableStateOf(PagerSwipeState()),
    data: List<T>,
    content: @Composable (pageIndex: Int, item: T, swipeAbleState: SwipeableState<Int>, size: Size) -> Unit
): SwipeableState<Int> {
    val coroutineScope = rememberCoroutineScope()

    val count = data.size
    var enabled by remember {
        mutableStateOf(true)
    }
    val swipeAbleState =
        rememberSwipeableState(initialValue = minOf(data.size - 1, maxOf(0, loopLimit)))
    var size by remember {
        mutableStateOf(Size(1f, 1f))
    }

    val pairs = mutableListOf<Pair<Float, Int>>()
    for (i in 0 until count) {
        pairs.add(-i * size.width to i)
    }

    val anchors = mapOf(*pairs.toTypedArray())

    Box(
        modifier = modifier
            .onGloballyPositioned {
                size = it.size.toSize()
            }
            .background(Color.Transparent)
            .swipeable(
                state = swipeAbleState,
                anchors = anchors,
                thresholds = thresholds,
                orientation = Orientation.Horizontal,
                enabled = if (userEnable.not()) false else if (count <= 1) false else enabled,
                velocityThreshold = velocityThreshold
            ),
    ) {
        for (i in 0 until count) {
            content(i, data[i], swipeAbleState, size)
        }

        if (loop) {
            LaunchedEffect(key1 = swipeAbleState.progress, block = {
                if (swipeAbleState.progress.to == count - loopLimit && swipeAbleState.progress.fraction >= 0.9f) {
                    enabled = false
                    swipeAbleState.snapTo(loopLimit)
                } else if (swipeAbleState.progress.to == loopLimit - 1 && swipeAbleState.progress.fraction >= 0.9f) {
                    enabled = false
                    swipeAbleState.snapTo(count - 1 - loopLimit)
                } else {
                    enabled = true
                }

                val total = if (data.size > 2 * loopLimit) {
                    data.size - 2 * loopLimit
                } else {
                    data.size
                }

                var current = swipeAbleState.targetValue - loopLimit
                if (current < 0) {
                    current = total - 1
                } else if (current > total - 1) {
                    current = 0
                }

                pagerSwipeState.value = PagerSwipeState(
                    total,
                    current,
                    swipeAbleState.progress.from - loopLimit,
                    swipeAbleState.progress.to - loopLimit,
                    swipeAbleState.progress.fraction
                )
            })
        }
    }
    if (autoSwipe) {
        LaunchedEffect(key1 = swipeAbleState.currentValue, block = {
            while (true) {
                delay(duration)
                coroutineScope.launch {
                    try {
                        swipeAbleState.animateTo(swipeAbleState.targetValue + 1)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }
    if (swipeAbleState.currentValue >= (count - loopLimit)) {
        SideEffect {
            enabled = false
            coroutineScope.launch {
                swipeAbleState.snapTo(loopLimit)
            }
        }
    }
    return swipeAbleState
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> LinearPager(
    modifier: Modifier = Modifier,
    thresholds: (from: Int, to: Int) -> ThresholdConfig = { _, _ ->
        FractionalThreshold(0.3f)
    },
    velocityThreshold: Dp = SwipeableDefaults.VelocityThreshold,
    loop: Boolean = true,
    userEnable: Boolean = true,
    autoSwipe: Boolean = true,
    duration: Long = 3000,
    pagerSwipeState: MutableState<PagerSwipeState> = mutableStateOf(PagerSwipeState()),
    data: List<T>,
    content: @Composable (data: T) -> Unit
): SwipeableState<Int> {
    val realData = if (loop) {
        data.makeLoop(1)
    } else {
        data
    }
    return BasicPager(
        modifier,
        thresholds,
        velocityThreshold,
        loop,
        if (loop) 1 else 0,
        userEnable,
        autoSwipe,
        duration,
        pagerSwipeState,
        realData
    ) { pageIndex, data, swipeAbleState, size ->
        val originOffset = pageIndex * size.width.roundToInt()
        Box(modifier = Modifier
            .offset {
                IntOffset(originOffset + swipeAbleState.offset.value.roundToInt(), 0)
            }) {
            content(data)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> StackPager(
    modifier: Modifier = Modifier,
    thresholds: (from: Int, to: Int) -> ThresholdConfig = { _, _ ->
        FractionalThreshold(0.3f)
    },
    velocityThreshold: Dp = SwipeableDefaults.VelocityThreshold,
    userEnable: Boolean = true,
    autoSwipe: Boolean = true,
    duration: Long = 3000,
    pagerSwipeState: MutableState<PagerSwipeState> = mutableStateOf(PagerSwipeState()),
    stackOffsetStep: Dp = 8.dp,
    alphaStep: Float = 0.35f,
    scaleStep: Float = 0.05f,
    data: List<T>,
    content: @Composable (data: T) -> Unit
): SwipeableState<Int> {
    val loopLimit = 3
    val realData = data.makeLoop(3)
    val count = realData.size
    return BasicPager(
        modifier,
        thresholds,
        velocityThreshold,
        true,
        loopLimit,
        userEnable,
        autoSwipe,
        duration,
        pagerSwipeState,
        realData
    ) { pageIndex, data, swipeAbleState, size ->
        // 当前的偏移量
        val nowOffset = swipeAbleState.offset.value.absoluteValue

        // 自己在总列表中的进度位置就是index

        //当前滑动过的进度
        val progress = nowOffset / size.width

        val offsetMulti = progress - pageIndex

//        Log.e("offsetMulti", "index:$pageIndex : $offsetMulti")

        val alpha = if (offsetMulti > 0) {
            (1 - offsetMulti * 2).coerceIn(0f, 1f)
        } else {
            (1 - (offsetMulti.absoluteValue * alphaStep)).coerceIn(0f, 1f)
        }

//        Log.e("alpha", "index:$pageIndex : $alpha")

        val scale = if (offsetMulti > 0) {
            (1 + offsetMulti * scaleStep * 3)
        } else {
            (1 - (offsetMulti.absoluteValue * scaleStep)).coerceIn(0f, 1f)
        }

//        Log.e("scale", "index:$pageIndex : $scale")

        Box(
            modifier = Modifier
                .zIndex(if (alpha <= 0) 0f else (count - pageIndex).toFloat())
                .offset {
                    val offset = if (offsetMulti > 0) {
                        (offsetMulti * 2 * stackOffsetStep.toPx()).toInt()
                    } else {
                        (offsetMulti * stackOffsetStep.toPx()).toInt()
                    }
                    IntOffset(0, offset)
                }
                .alpha(alpha)
                .scale(scale)
        ) {
            content(data)
        }
    }
}