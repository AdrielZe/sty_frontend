package com.example.training_tracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.training_tracker.R
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.CyanGradient

enum class StyLogoLayout { HORIZONTAL, VERTICAL }

/**
 * @param titleSize font size for the "STY" text
 * @param layout HORIZONTAL puts the tagline to the right; VERTICAL puts it below
 */
@Composable
fun StyLogo(
    titleSize: TextUnit = 22.sp,
    layout: StyLogoLayout = StyLogoLayout.VERTICAL,
    centerTitle: Boolean = false,
    modifier: Modifier = Modifier
) {
    val taglineSize = (titleSize.value * 0.28f).coerceAtLeast(8f).sp
    val taglineLetterSpacing = (titleSize.value * 0.04f).sp

    when (layout) {
        StyLogoLayout.VERTICAL -> {
            Column(
                horizontalAlignment = if (centerTitle) Alignment.CenterHorizontally else Alignment.Start,
                modifier = modifier
            ) {
                Text(
                    text = stringResource(R.string.home_app_title),
                    style = TextStyle(brush = CyanGradient),
                    fontSize = titleSize,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (titleSize.value * 0.13f).sp
                )
                Text(
                    text = stringResource(R.string.stronger_than_yesterday),
                    fontSize = taglineSize,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = taglineLetterSpacing,
                    color = CyanAccent.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center
                )
            }
        }

        StyLogoLayout.HORIZONTAL -> {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = modifier
            ) {
                Text(
                    text = stringResource(R.string.home_app_title),
                    style = TextStyle(brush = CyanGradient),
                    fontSize = titleSize,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (titleSize.value * 0.13f).sp
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.stronger_than_yesterday),
                    fontSize = taglineSize,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = taglineLetterSpacing,
                    color = CyanAccent.copy(alpha = 0.65f)
                )
            }
        }
    }
}
