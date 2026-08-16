@file:OptIn(ExperimentalMaterial3Api::class)

package com.hrithikvish.curler.ui.screens.about

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hrithikvish.curler.R
import com.hrithikvish.curler.data.model.ChangelogEntry
import com.hrithikvish.curler.ui.theme.CurlerTheme
import com.hrithikvish.curler.ui.theme.PillShape
import com.hrithikvish.curler.ui.theme.codeMono

private val CardShape = RoundedCornerShape(28.dp)
private val MarkShape = RoundedCornerShape(20.dp)

private val CardRowHorizontalPadding = 18.dp
private val CardRowVerticalPadding = 14.dp

// Explicit FLAG_ACTIVITY_NEW_TASK so the launched browser/app gets its own
// task, separate from cURLer's — without it, starting these from an Activity
// context can dock the new activity into cURLer's own task, so swiping it
// away from Recents takes cURLer down with it.
private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

private fun openEmail(context: Context, email: String) {
    val intent = Intent(Intent.ACTION_SENDTO, "mailto:$email".toUri())
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AboutViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AboutContent(uiState = uiState, onBack = onBack, modifier = modifier)
}

@Composable
private fun AboutContent(
    uiState: AboutUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val githubProfileUrl = stringResource(R.string.about_url_github_profile)
    val githubBugUrl = stringResource(R.string.about_url_github_bug)
    val githubFeatureUrl = stringResource(R.string.about_url_github_feature)
    val linkedInUrl = stringResource(R.string.about_url_linkedin)
    val devEmail = stringResource(R.string.about_dev_email)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(id = R.string.about_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
        ) {
            AppIdentity(versionName = uiState.versionName, versionCode = uiState.versionCode)

            if (uiState.changelogEntries.isNotEmpty()) {
                SectionLabel(stringResource(R.string.about_changelog_label))
                ChangelogCard(entries = uiState.changelogEntries)
            }

            SectionLabel(stringResource(R.string.about_dev_section_label))
            DeveloperCard(
                devImageBitmap = uiState.devImageBitmap,
                onGitHubClick = { openUrl(context, githubProfileUrl) },
                onLinkedInClick = { openUrl(context, linkedInUrl) },
                onEmailClick = { openEmail(context, devEmail) },
            )

            SectionLabel(stringResource(R.string.about_feedback_label))
            Column(modifier = Modifier.fillMaxWidth()) {
                FeedbackRow(
                    icon = painterResource(R.drawable.ic_bug),
                    label = stringResource(R.string.about_feedback_bug),
                    onClick = { openUrl(context, githubBugUrl) },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                FeedbackRow(
                    icon = painterResource(R.drawable.ic_idea),
                    label = stringResource(R.string.about_feedback_feature),
                    onClick = { openUrl(context, githubFeatureUrl) },
                )
            }

            Text(
                text = stringResource(R.string.about_footer),
                style = codeMono.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun AppIdentity(versionName: String, versionCode: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(MarkShape)
                .background(MaterialTheme.colorScheme.onSurface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_terminal),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(30.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(stringResource(R.string.home_title), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.about_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.about_version_format, versionName, versionCode),
            style = codeMono.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clip(PillShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(top = 26.dp, bottom = 10.dp),
    )
}

@Composable
private fun ChangelogCard(entries: List<ChangelogEntry>, modifier: Modifier = Modifier) {
    val latest = entries.first { it.isLatest }
    var showAllSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        ChangelogRow(entry = latest)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ViewAllChangelogRow(onClick = { showAllSheet = true })
    }

    if (showAllSheet) {
        ChangelogSheet(entries = entries, onDismiss = { showAllSheet = false })
    }
}

@Composable
private fun ViewAllChangelogRow(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = CardRowHorizontalPadding, vertical = CardRowVerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.about_changelog_view_all),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangelogSheet(
    entries: List<ChangelogEntry>,
    onDismiss: () -> Unit
) {
    val maxSheetHeight = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height.toDp()
    } * 0.8f

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = maxSheetHeight)
                .padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.about_changelog_label),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
            ) {
                entries.forEachIndexed { index, entry ->
                    ChangelogRow(entry = entry)
                    if (index < entries.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ChangelogRow(
    entry: ChangelogEntry,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CardRowHorizontalPadding, vertical = CardRowVerticalPadding),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (entry.isLatest) {
                Text(
                    text = stringResource(R.string.about_changelog_latest_badge),
                    style = codeMono.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .clip(PillShape)
                        .background(MaterialTheme.colorScheme.onSurface)
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                )
            }
            Text(
                text = entry.version,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = entry.dateLabel,
                style = codeMono.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(
            modifier = Modifier.padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            entry.notes.forEach { note ->
                Row {
                    Text(
                        text = "–  ",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp, lineHeight = 17.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun DeveloperCard(
    devImageBitmap: Bitmap?,
    onGitHubClick: () -> Unit,
    onLinkedInClick: () -> Unit,
    onEmailClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(18.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer {
                        shape = CircleShape
                        clip = true
                    }
                    .background(MaterialTheme.colorScheme.onSurface),
                contentAlignment = Alignment.Center,
            ) {
                if (devImageBitmap != null) {
                    Image(
                        bitmap = devImageBitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(48.dp),
                    )
                } else {
                    Text(
                        text = stringResource(R.string.about_dev_name).take(1),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.surface,
                    )
                }
            }
            Column {
                Text(
                    text = stringResource(R.string.about_dev_name),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.about_dev_role),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        /*Text(
            text = stringResource(R.string.about_dev_bio),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(16.dp))*/
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LinkChip(
                iconPainter = painterResource(R.drawable.ic_github),
                label = stringResource(R.string.about_link_github),
                onClick = onGitHubClick,
            )
            LinkChip(
                iconPainter = painterResource(R.drawable.ic_linkedin),
                label = stringResource(R.string.about_link_linkedin),
                onClick = onLinkedInClick,
            )
            LinkChip(
                icon = Icons.Filled.Email,
                label = stringResource(R.string.about_link_email),
                onClick = onEmailClick,
            )
        }
    }
}

@Composable
private fun LinkChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconPainter: Painter? = null,
) {
    Row(
        modifier = modifier
            .clip(PillShape)
            .border(1.3.dp, MaterialTheme.colorScheme.outline, PillShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        when {
            icon != null -> Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(15.dp),
            )
            iconPainter != null -> Icon(
                painter = iconPainter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(15.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun FeedbackRow(
    icon: Painter,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutScreenPreview() {
    CurlerTheme {
        AboutContent(
            uiState = AboutUiState(versionName = "1.0", versionCode = 1),
            onBack = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun AboutScreenTallPreview() {
    CurlerTheme {
        AboutContent(
            uiState = AboutUiState(versionName = "1.0", versionCode = 1),
            onBack = {},
        )
    }
}
