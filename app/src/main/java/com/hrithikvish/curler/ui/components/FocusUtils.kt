package com.hrithikvish.curler.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

// BasicTextField only reacts to taps on its actual text content, not its full layout
// bounds, so a mostly-empty field ignores taps outside the placeholder/cursor area.
// This forwards any tap in the wrapping box to the field's focus request. Focus alone
// doesn't trigger the IME here since the tap is consumed by this wrapper rather than
// BasicTextField's own gesture detector, so the keyboard is shown explicitly too.
fun Modifier.tapToFocus(focusRequester: FocusRequester): Modifier = composed {
    val keyboardController = LocalSoftwareKeyboardController.current
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
    ) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}
