package com.ninthbalcony.pushuprpg.ui.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Однострочный Text для узких контейнеров (кнопки, табы, заголовки колонок).
 * Кегль уменьшается автоматически и только когда перевод не помещается,
 * поэтому длинные немецкие/французские слова не переносятся посреди слова,
 * а короткие языки рендерятся в исходном размере.
 */
@Composable
fun AutoResizeText(
    text: String,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    minFontSize: TextUnit = 8.sp,
) {
    val resolvedColor = color.takeOrElse { LocalContentColor.current }
    BasicText(
        text = text,
        modifier = modifier,
        style = LocalTextStyle.current.merge(
            color = resolvedColor,
            fontWeight = fontWeight,
            textAlign = textAlign ?: TextAlign.Unspecified,
            letterSpacing = letterSpacing,
        ),
        maxLines = 1,
        autoSize = TextAutoSize.StepBased(
            minFontSize = minFontSize,
            maxFontSize = fontSize,
            stepSize = 0.5.sp,
        ),
    )
}
