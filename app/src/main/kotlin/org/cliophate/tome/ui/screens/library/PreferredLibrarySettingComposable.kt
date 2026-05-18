package org.cliophate.tome.ui.screens.library

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotInterested
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.runtime.Composable
import org.cliophate.tome.lib.domain.Library
import org.cliophate.tome.lib.domain.LibraryType
import org.cliophate.tome.ui.icons.BookHeadphones
import org.cliophate.tome.ui.screens.settings.composable.CommonSettingsItem
import org.cliophate.tome.ui.screens.settings.composable.CommonSettingsItemComposable

@Composable
fun PreferredLibrarySettingComposable(
  libraries: List<Library>,
  preferredLibrary: Library?,
  onDismissRequest: () -> Unit,
  onItemSelected: (Library) -> Unit,
) {
  CommonSettingsItemComposable(
    items = libraries.map { CommonSettingsItem(it.id, it.title, it.type.provideIcon()) },
    selectedItem = preferredLibrary?.let { CommonSettingsItem(it.id, it.title, it.type.provideIcon()) },
    onDismissRequest = { onDismissRequest() },
    onItemSelected = { item ->
      val selectedItem =
        libraries.find { it.id == item.id }
          ?: return@CommonSettingsItemComposable

      if (selectedItem != preferredLibrary) {
        onItemSelected(selectedItem)
      }
    },
  )
}

fun LibraryType.provideIcon() =
  when (this) {
    LibraryType.LIBRARY -> BookHeadphones
    LibraryType.PODCAST -> Icons.Outlined.Podcasts
    LibraryType.UNKNOWN -> Icons.Outlined.NotInterested
  }
