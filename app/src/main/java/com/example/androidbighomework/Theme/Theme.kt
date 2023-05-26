package com.example.androidbighomework.Theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class CustomColors(
    val content: Color,
    val component: Color,
    val background: List<Color>,
    val navBorder: Color,
    val actionBarIcon: Color
)

@Immutable
data class CustomTypography(
    val body: TextStyle,
    val topTitle: TextStyle,
    val title: TextStyle,
    val regularText: TextStyle,
    val secondaryContent: TextStyle,
    val todoListTitle: TextStyle,
    val todoListText: TextStyle,
    val textButton: TextStyle
)

@Immutable
data class CustomElevation(
    val topTitlePadding: Dp,
    val sidePadding: Dp,
    val contentPadding: Dp,
)

@Immutable
data class CustomSize(
    val roundedCorner: Dp,
    val actionBarIcon: Dp,
    val buttonRoundedCorner: Dp
)

@Immutable
data class CustomShadow(
    val toDoListText: Shadow
)


val LocalCustomColors = staticCompositionLocalOf {
    CustomColors(
        content = Color.Unspecified,
        component = Color.Unspecified,
        background = emptyList(),
        navBorder = Color.Unspecified,
        actionBarIcon = Color.Unspecified
    )
}
val LocalCustomTypography = staticCompositionLocalOf {
    CustomTypography(
        body = TextStyle.Default,
        topTitle = TextStyle.Default,
        title = TextStyle.Default,
        regularText = TextStyle.Default,
        secondaryContent = TextStyle.Default,
        todoListTitle = TextStyle.Default,
        todoListText= TextStyle.Default,
        textButton = TextStyle.Default
    )
}

val LocalCustomElevation = staticCompositionLocalOf {
    CustomElevation(
        topTitlePadding = Dp.Unspecified,
        sidePadding = Dp.Unspecified,
        contentPadding = Dp.Unspecified,
    )
}

val LocalCustomSize = staticCompositionLocalOf {
    CustomSize(
        roundedCorner = Dp.Unspecified,
        actionBarIcon = Dp.Unspecified,
        buttonRoundedCorner = Dp.Unspecified
    )
}

val LocalCustomShadow = staticCompositionLocalOf {
    CustomShadow(
        toDoListText = Shadow()
    )
}

@Composable
fun MyTheme(content: @Composable () -> Unit) {
    val customTypography = CustomTypography(
        body = TextStyle(fontSize = 16.sp),
        topTitle = TextStyle(fontSize = 24.sp, color = Color.White),
        title = TextStyle(fontSize = 24.sp, color = Color(0xff303133)),
        regularText = TextStyle(fontSize = 14.sp, color = Color(0xff606266)),
        secondaryContent = TextStyle(fontSize = 14.sp, color = Color(0xff909399)),
        todoListTitle = TextStyle(fontSize = 20.sp, color = Color.White),
        todoListText = TextStyle(fontSize = 14.sp, color = Color.White),
        textButton = TextStyle(fontSize = 22.sp, color = Color.White),
    )
    val customColors = CustomColors(
        content = Color(0xFFDD0D3C),
        component = Color(0xFFC20029),
        background = listOf(Color.White, Color(0xff4e57ff)),
        navBorder = Color(0xffEBEEF5),
        actionBarIcon = Color.White
    )
    val customElevation = CustomElevation(
        topTitlePadding = 16.dp,
        sidePadding = 20.dp,
        contentPadding = 10.dp,
    )
    val customSize = CustomSize(
        roundedCorner = 12.dp,
        buttonRoundedCorner = 6.dp,
        actionBarIcon = 32.dp
    )
    val customShadow = CustomShadow(
        toDoListText = Shadow(
                color = Color.Black,
                offset = Offset(1f, 1f),
                blurRadius = 4f
            )
    )

    CompositionLocalProvider(
        LocalCustomColors provides customColors,
        LocalCustomTypography provides customTypography,
        LocalCustomElevation provides customElevation,
        LocalCustomSize provides customSize,
        LocalCustomShadow provides customShadow,
        content = content
    )
}

// Use with eg. CustomTheme.elevation.small
object MyTheme {
    val colors: CustomColors
        @Composable
        get() = LocalCustomColors.current
    val typography: CustomTypography
        @Composable
        get() = LocalCustomTypography.current
    val elevation: CustomElevation
        @Composable
        get() = LocalCustomElevation.current
    val size: CustomSize
        @Composable
        get() = LocalCustomSize.current
    val shadow: CustomShadow
        @Composable
        get() = LocalCustomShadow.current
}