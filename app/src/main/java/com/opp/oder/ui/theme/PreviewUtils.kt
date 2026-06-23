package com.opp.oder.ui.theme

import androidx.compose.runtime.Composable

@Composable
fun OderPreview(content: @Composable () -> Unit) {
    OderTheme(darkTheme = true) {
        content()
    }
}

@Composable
fun OderPreviewLight(content: @Composable () -> Unit) {
    OderTheme(darkTheme = false) {
        content()
    }
}
