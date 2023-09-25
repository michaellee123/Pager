@file:OptIn(ExperimentalMaterialApi::class)

package dog.abcd.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.SwipeableState
import androidx.compose.material.ThresholdConfig
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

fun <T> List<T>.makeLoop(loopLimit: Int = 1): List<T> {
    val result = ArrayList<T>()
    if (isEmpty()) {
        return result
    }
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
    var total: Int = 0,
    internal var swipeAbleState: SwipeableState<Int> = SwipeableState(0),
    internal var loopLimit: Int = 0,
) {

    val current: Int
        get() {
            var current = swipeAbleState.targetValue - loopLimit
            if (current < 0) {
                current = total - 1
            } else if (current > total - 1) {
                current = 0
            }
            return current
        }

    val from: Int get() = swipeAbleState.progress.from - loopLimit

    val to: Int get() = swipeAbleState.progress.to - loopLimit

    val fraction: Float get() = swipeAbleState.progress.fraction

    @OptIn(ExperimentalMaterialApi::class)
    suspend fun snapTo(to: Int) {
        try {
            if (to in 0 until total) {
                swipeAbleState.snapTo(to + loopLimit)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    suspend fun animateTo(to: Int) {
        try {
            if (to in 0 until total) {
                swipeAbleState.animateTo(to + loopLimit)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun rememberPagerSwipeState(): PagerSwipeState {
    return remember {
        PagerSwipeState()
    }
}

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
    widthPx: Float,
    data: List<T>,
    pagerSwipeState: PagerSwipeState = remember { PagerSwipeState() },
    content: @Composable (pageIndex: Int, item: T, swipeAbleState: SwipeableState<Int>, widthPx: Float) -> Unit
) {
    if (data.isEmpty()) {
        Box(modifier = modifier)
        return
    }
    val coroutineScope = rememberCoroutineScope()

    val count = data.size
    var enabled by remember {
        mutableStateOf(true)
    }

    val swipeAbleState: SwipeableState<Int> =
        rememberSwipeableState(initialValue = maxOf(0, minOf(data.size - 1, loopLimit)))

    val total = if (data.size > 2 * loopLimit) {
        data.size - 2 * loopLimit
    } else {
        data.size
    }

    pagerSwipeState.total = total
    pagerSwipeState.swipeAbleState = swipeAbleState
    pagerSwipeState.loopLimit = loopLimit

    val pairs = mutableListOf<Pair<Float, Int>>()
    for (i in 0 until count) {
        pairs.add(-i * widthPx to i)
    }

    val anchors = mapOf(*pairs.toTypedArray())

    Box(
        modifier = modifier
            .background(Color.Transparent)
            .then(
                if (data.isNotEmpty()) {
                    Modifier
                        .swipeable(
                            state = swipeAbleState,
                            anchors = anchors,
                            thresholds = thresholds,
                            orientation = Orientation.Horizontal,
                            enabled = if (userEnable.not()) false else if (count <= 1) false else enabled,
                            velocityThreshold = velocityThreshold
                        )
                } else {
                    Modifier
                }
            ),
    ) {
        for (i in 0 until count) {
            content(i, data[i], swipeAbleState, widthPx)
        }
    }
    if (loop) {
        LaunchedEffect(key1 = swipeAbleState.progress, block = {
            if (swipeAbleState.progress.to == count - loopLimit && swipeAbleState.progress.fraction >= 0.9f) {
                enabled = false
                if (loopLimit in 0 until anchors.size) {
                    swipeAbleState.snapTo(loopLimit)
                }
            } else if (swipeAbleState.progress.to == loopLimit - 1 && swipeAbleState.progress.fraction >= 0.9f) {
                enabled = false
                if (count - 1 - loopLimit in 0 until anchors.size) {
                    swipeAbleState.snapTo(count - 1 - loopLimit)
                }
            } else {
                enabled = true
            }
        })
    }
    if (autoSwipe) {
        LaunchedEffect(key1 = swipeAbleState.progress.fraction, block = {
            while (true) {
                delay(duration)
                coroutineScope.launch {
                    try {
                        if (swipeAbleState.targetValue + 1 in 0 until anchors.size) {
                            swipeAbleState.animateTo(swipeAbleState.targetValue + 1)
                        }
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
                try {
                    if (loopLimit in 0 until anchors.size) {
                        swipeAbleState.snapTo(loopLimit)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
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
    pagerSwipeState: PagerSwipeState = remember { PagerSwipeState() },
    widthPx: Float,
    data: List<T>,
    content: @Composable (data: T, index: Int) -> Unit
) {
    val realData = if (loop) {
        data.makeLoop(1)
    } else {
        data
    }
    BasicPager(
        modifier,
        thresholds,
        velocityThreshold,
        loop,
        if (loop) 1 else 0,
        userEnable,
        autoSwipe,
        duration,
        widthPx,
        realData,
        pagerSwipeState,
    ) { pageIndex, data, swipeAbleState, widthPx ->
        val originOffset = pageIndex * widthPx.roundToInt()
        Box(modifier = Modifier
            .graphicsLayer {
                translationX = originOffset + swipeAbleState.offset.value
            }
        ) {
            content(data, pageIndex - (if (loop) 1 else 0))
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
    pagerSwipeState: PagerSwipeState = remember { PagerSwipeState() },
    stackOffsetStep: Dp = 8.dp,
    alphaStep: Float = 0.35f,
    scaleStep: Float = 0.05f,
    widthPx: Float,
    data: List<T>,
    content: @Composable (data: T, index: Int) -> Unit
) {
    val loopLimit = 3
    val realData = data.makeLoop(3)
    val count = realData.size
    BasicPager(
        modifier,
        thresholds,
        velocityThreshold,
        true,
        loopLimit,
        userEnable,
        autoSwipe,
        duration,
        widthPx,
        realData,
        pagerSwipeState,
    ) { pageIndex, data, swipeAbleState, widthPx ->
        var alpha by remember {
            mutableStateOf(0f)
        }
        Box(
            modifier = Modifier
                .zIndex(if (alpha <= 0) 0f else (count - pageIndex).toFloat())
                .graphicsLayer {
                    // 当前的偏移量
                    val nowOffset = swipeAbleState.offset.value.absoluteValue

                    // 自己在总列表中的进度位置就是index

                    //当前滑动过的进度
                    val progress = nowOffset / widthPx

                    val offsetMulti = progress - pageIndex

                    // Log.e("offsetMulti", "index:$pageIndex : $offsetMulti")

                    alpha = if (offsetMulti > 0) {
                        (1 - offsetMulti * 2).coerceIn(0f, 1f)
                    } else {
                        (1 - (offsetMulti.absoluteValue * alphaStep)).coerceIn(0f, 1f)
                    }

                    // Log.e("alpha", "index:$pageIndex : $alpha")

                    val scale = if (offsetMulti > 0) {
                        (1 + offsetMulti * scaleStep * 3)
                    } else {
                        (1 - (offsetMulti.absoluteValue * scaleStep)).coerceIn(0f, 1f)
                    }

                    // Log.e("scale", "index:$pageIndex : $scale")
                    this.alpha = alpha
                    this.scaleX = scale
                    this.scaleY = scale
                    this.translationY = if (offsetMulti > 0) {
                        (offsetMulti * 2 * stackOffsetStep.toPx())
                    } else {
                        (offsetMulti * stackOffsetStep.toPx())
                    }
                }
        ) {
            content(data, pageIndex - loopLimit)
        }
    }
}
