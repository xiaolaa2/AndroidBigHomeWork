package com.example.androidbighomework.todoPage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.androidbighomework.Theme.MyTheme

@Composable
public fun MyButton(todoTypeIndex: Int, selfIndex: Int, text: String, onClick: () -> Unit) {
    // 自定义按钮
    Column(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(MyTheme.size.buttonRoundedCorner))
            .background(if (todoTypeIndex == selfIndex) Color(0xffd9ecff) else Color(0xffE6E8EB))
            .padding(
                horizontal = 7.dp,
                vertical = 8.dp
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onClick()
                    }
                )
            }
    ) {
        Text(
            text = text,
            style =
            if (todoTypeIndex == selfIndex)
                MyTheme.typography.regularText.copy(color = Color(0xff409EFF))
            else
                MyTheme.typography.regularText
        )
    }
}

@Composable
fun MyDialog(
    dialogVisible: Boolean,
    modifier: Modifier,
    onClose: () -> Unit,
    onConfirm: () -> Unit = {},
    title: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    // 添加待办弹窗
    AnimatedVisibility(
        modifier = Modifier
            .zIndex(1f),
        visible = dialogVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 450)),
        exit = fadeOut(animationSpec = tween(durationMillis = 450))
    ) {
        Box(modifier = Modifier) {
            // 遮罩层最大
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 60.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {

                        })
                    }
            ) {

            }

            // 内容部分
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f)
            ) {
                val dialogBody = createRef()
                // 渐入渐出动画
                AnimatedVisibility(
                    modifier = Modifier.constrainAs(dialogBody) {
                        centerTo(parent)
                    },
                    visible = dialogVisible,
                    enter = fadeIn(animationSpec = tween(durationMillis = 550)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 550))
                ) {
                    // 弹出框本身
                    Column(
                        modifier = modifier
                    ) {
                        // 标题栏和按钮
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .height(60.dp)
                                .fillMaxWidth()
                                .background(
                                    Color(0xffE6E8EB)
                                )
                                .padding(horizontal = MyTheme.elevation.sidePadding)
                        ) {
                            title()
                            Row() {
                                // 提交按钮
                                IconButton(onClick = {
                                    onConfirm()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = null
                                    )
                                }
                                IconButton(onClick = {
                                    onClose()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null
                                    )
                                }
                            }
                        }

                        // 底部内容
                        content()
                    }
                }
            }
        }
    }
}