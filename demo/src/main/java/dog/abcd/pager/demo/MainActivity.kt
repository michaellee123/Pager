package dog.abcd.pager.demo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dog.abcd.pager.BasicIndicator
import dog.abcd.pager.LinearPager
import dog.abcd.pager.PagerSwipeState
import dog.abcd.pager.ScrollBallIndicator
import dog.abcd.pager.StackPager
import dog.abcd.pager.demo.ui.theme.PagerTheme
import dog.abcd.pager.rememberPagerSwipeState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PagerTheme {
                PagerView()
            }
        }
    }

    val list = listOf(
        "https://img1.baidu.com/it/u=3883861376,1990928193&fm=253&fmt=auto&app=138&f=JPEG?w=889&h=500",
        "https://img2.baidu.com/it/u=1129258209,1595745832&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=500",
        "https://img1.baidu.com/it/u=4181165498,3675904594&fm=253&fmt=auto&app=138&f=JPEG?w=820&h=461",
        "https://img0.baidu.com/it/u=3946476543,2380972261&fm=253&fmt=auto&app=138&f=JPEG?w=889&h=500",
        "https://img0.baidu.com/it/u=1901343054,2381723066&fm=253&fmt=auto&app=120&f=JPEG?w=800&h=500"
    )

    val colors = listOf(
        Color(0xFFFF9800),
        Color(0xFFCDDC39),
        Color(0xFF4CAF50),
        Color(0xFF3F51B5),
        Color(0xFF673AB7),
    )

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    @Preview(showBackground = true)
    fun PagerView() {
        val colorPagerSwipeState = rememberPagerSwipeState()
        val linearPagerSwipeState = rememberPagerSwipeState()
        val stackPagerSwipeState = rememberPagerSwipeState()
        Column(
            Modifier
                .fillMaxSize()
                .drawBehind {
                    val nowColor = colors[colorPagerSwipeState.from % colors.size]
                    val to =
                        if (colorPagerSwipeState.to < 0) colorPagerSwipeState.total - 1 else colorPagerSwipeState.to
                    Log.e("PagerView", "from:${colorPagerSwipeState.from},to:$to")
                    val targetColor = colors[to % colors.size]
                    val showColor = Color(
                        red = (targetColor.red - nowColor.red) * colorPagerSwipeState.fraction + nowColor.red,
                        green = (targetColor.green - nowColor.green) * colorPagerSwipeState.fraction + nowColor.green,
                        blue = (targetColor.blue - nowColor.blue) * colorPagerSwipeState.fraction + nowColor.blue,
                        alpha = 0.3f
                    )
                    //draw a rect with showColor
                    drawRect(showColor)
                }
        ) {

            Spacer(modifier = Modifier.statusBarsPadding())

            LinearPager(
                data = colors,
                pagerSwipeState = colorPagerSwipeState,
                duration = 5000,
                widthPx = resources.displayMetrics.widthPixels.toFloat()
            ) { it, index ->
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(10.dp)
                        .shadow(5.dp, RoundedCornerShape(10.dp))
                        .background(it, RoundedCornerShape(10.dp))
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box {

                LinearPager(
                    data = list,
                    pagerSwipeState = linearPagerSwipeState,
                    duration = 5000,
                    widthPx = resources.displayMetrics.widthPixels.toFloat()
                ) { it, index ->
                    AsyncImage(
                        model = it,
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }

                Text(
                    text = "${linearPagerSwipeState.current + 1}/${linearPagerSwipeState.total}",
                    modifier = Modifier
                        .padding(20.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color(0x66FFFFFF), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                ScrollBallIndicator(
                    modifier = Modifier
                        .padding(20.dp)
                        .padding(bottom = 60.dp)
                        .align(Alignment.BottomCenter),
                    swipeState = linearPagerSwipeState,
                    ballSize = 12.dp,
                    spaceSize = 10.dp,
                    indicatorSize = 8.dp,
                    underIndicator = false,
                    selectColor = Color(0xFFD0B5FF),
                    unSelectColor = Color(0xFF5D5866),
                )

                ScrollBallIndicator(
                    modifier = Modifier
                        .padding(20.dp)
                        .padding(bottom = 30.dp)
                        .align(Alignment.BottomCenter),
                    swipeState = linearPagerSwipeState,
                    ballSize = 10.dp,
                    spaceSize = 10.dp,
                    indicatorSize = 20.dp,
                    underIndicator = true,
                    selectColor = Color(0xFF673AB7),
                    unSelectColor = Color(0xFFFF9800),
                )

                ScrollBallIndicator(
                    modifier = Modifier
                        .padding(20.dp)
                        .align(Alignment.BottomCenter)
                        .background(Color(0x66FFFFFF), CircleShape)
                        .padding(vertical = 5.dp, horizontal = 8.dp),
                    swipeState = linearPagerSwipeState,
                    ballSize = 10.dp,
                    spaceSize = 10.dp,
                )

            }

            Spacer(modifier = Modifier.height(20.dp))

            Box {
                StackPager(
                    modifier = Modifier,
                    data = list,
                    pagerSwipeState = stackPagerSwipeState,
                    stackOffsetStep = 10.dp,
                    widthPx = resources.displayMetrics.widthPixels.toFloat()
                ) { it, index ->

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(10.dp)
                            .shadow(5.dp, RoundedCornerShape(10.dp))
                    ) {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    Toast
                                        .makeText(
                                            this@MainActivity,
                                            "click $index",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                        )
                        Text(
                            text = it,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .padding(10.dp)
                                .padding(bottom = 15.dp)
                                .align(Alignment.BottomCenter)
                                .background(Color(0x33000000), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                BasicIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(20.dp), swipeState = stackPagerSwipeState
                )

                ScrollBallIndicator(
                    modifier = Modifier
                        .padding(20.dp)
                        .background(Color(0xFFFFFFFF), CircleShape)
                        .padding(2.dp)
                        .align(Alignment.TopStart),
                    swipeState = stackPagerSwipeState,
                    ballSize = 10.dp,
                    spaceSize = 0.dp,
                    indicatorSize = 10.dp,
                    underIndicator = false,
                    selectColor = Color(0xFF3F51B5),
                    unSelectColor = Color(0x00FFFFFF),
                )
            }
            Text(
                text = "Hello world!",
                modifier = Modifier.padding(horizontal = 10.dp),
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}