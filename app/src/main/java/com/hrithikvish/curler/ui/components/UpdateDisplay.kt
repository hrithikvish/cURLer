package com.hrithikvish.curler.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hrithikvish.curler.R
import com.hrithikvish.curler.data.update.UpdateState

// Which glyph a state renders — Progress carries its own percent since the
// ring needs to redraw its sweep, not just pick an icon.
sealed interface UpdateIcon {
    data object Download : UpdateIcon
    data class Progress(val percent: Int?) : UpdateIcon // null = indeterminate
    data object Check : UpdateIcon
}

data class UpdateDisplay(
    val icon: UpdateIcon,
    val title: String,
    val subtitle: String,
    val actionLabel: String?, // null while Downloading — no button, matches both mockups
)

// The one place "what does this state look like" is defined — shared by
// Home's UpdateBar and About's UpdateRow so they never drift out of sync.
// Returns null for states with nothing to report: the row/bar isn't just
// hidden, it's absent from the layout entirely.
@Composable
fun updateDisplayFor(state: UpdateState): UpdateDisplay? = when (state) {
    UpdateState.Idle,
    UpdateState.Checking,
    is UpdateState.Failed -> null
    is UpdateState.Available -> UpdateDisplay(
        icon = UpdateIcon.Download,
        title = stringResource(R.string.update_title_available),
        subtitle = stringResource(R.string.update_subtitle_available),
        actionLabel = stringResource(R.string.update_action_update),
    )
    is UpdateState.Downloading -> UpdateDisplay(
        icon = UpdateIcon.Progress(state.percent),
        title = stringResource(R.string.update_title_downloading),
        subtitle = state.percent?.let { stringResource(R.string.update_subtitle_downloading_percent, it) }
            ?: stringResource(R.string.update_subtitle_downloading_indeterminate),
        actionLabel = null,
    )
    UpdateState.Downloaded -> UpdateDisplay(
        icon = UpdateIcon.Check,
        title = stringResource(R.string.update_title_downloaded),
        subtitle = stringResource(R.string.update_subtitle_downloaded),
        actionLabel = stringResource(R.string.update_action_restart),
    )
}

/** Renders an [UpdateIcon] — a static glyph for Download/Check, a live progress ring for Progress. */
@Composable
fun UpdateStateIcon(
    icon: UpdateIcon,
    tint: Color,
    trackTint: Color,
    modifier: Modifier = Modifier
) {
    when (icon) {
        UpdateIcon.Download -> Icon(
            painter = painterResource(R.drawable.ic_update_download),
            contentDescription = null,
            tint = tint,
            modifier = modifier,
        )

        UpdateIcon.Check -> Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = tint,
            modifier = modifier,
        )

        is UpdateIcon.Progress -> if (icon.percent != null) {
            // Determinate: real byte counts from the InstallStateUpdatedListener.
            CircularProgressIndicator(
                progress = { icon.percent / 100f },
                modifier = modifier,
                color = tint,
                trackColor = trackTint,
                strokeWidth = 2.dp,
            )
        } else {
            // Indeterminate: placeholder until the first real progress tick arrives.
            CircularProgressIndicator(
                modifier = modifier,
                color = tint,
                trackColor = trackTint,
                strokeWidth = 2.dp,
            )
        }
    }
}
