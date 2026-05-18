package org.cliophate.tome.ui.screens.library.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.cliophate.tome.R
import org.cliophate.tome.lib.domain.LibraryType
import org.cliophate.tome.viewmodel.CachingModelView
import org.cliophate.tome.viewmodel.LibraryViewModel
import org.cliophate.tome.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrarySettingsComposable(
  cachingModelView: CachingModelView = hiltViewModel(),
  onDismissRequest: () -> Unit,
  onForceLocalToggled: () -> Unit,
  onHideCompletedToggled: () -> Unit,
  settingsModelView: SettingsViewModel = hiltViewModel(),
  libraryViewModel: LibraryViewModel = hiltViewModel(),
) {
  val forceCache by cachingModelView.forceCache.collectAsState(false)
  val hideCompleted by settingsModelView.hideCompleted.collectAsState(false)

  val context = LocalContext.current

  ModalBottomSheet(
    containerColor = colorScheme.background,
    onDismissRequest = onDismissRequest,
    content = {
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .padding(horizontal = 16.dp),
      ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
          item {
            LibrarySettingsComposableItem(
              title = context.getString(R.string.show_downloaded_content_only),
              state = forceCache,
              onStateChange = { onForceLocalToggled() },
            )

            if (libraryViewModel.fetchPreferredLibraryType() == LibraryType.LIBRARY) {
              LibrarySettingsComposableItem(
                title = stringResource(R.string.hide_completed_items),
                state = hideCompleted,
                onStateChange = { onHideCompletedToggled() },
              )
            }

            HorizontalDivider()
          }
        }
      }
    },
  )
}

@Composable
fun LibrarySettingsComposableItem(
  title: String,
  state: Boolean,
  onStateChange: (Boolean) -> Unit,
) {
  ListItem(
    modifier = Modifier,
    headlineContent = { Text(text = title) },
    trailingContent = {
      Switch(
        checked = state,
        onCheckedChange = onStateChange,
        enabled = true,
        colors =
          SwitchDefaults.colors(
            uncheckedTrackColor = colorScheme.background,
            checkedBorderColor = colorScheme.onSurface,
            checkedThumbColor = colorScheme.onSurface,
            checkedTrackColor = colorScheme.background,
          ),
      )
    },
  )
}
