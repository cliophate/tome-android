package org.cliophate.tome.ui.screens.library.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import org.cliophate.tome.R

enum class LibraryTab {
  HOME,
  BOOKS,
  SERIES,
  SEARCH,
  SETTINGS,
}

@Composable
fun LibraryBottomBarComposable(
  selectedTab: LibraryTab,
  onTabSelected: (LibraryTab) -> Unit,
  modifier: Modifier = Modifier,
) {
  val labelStyle = typography.labelSmall.copy(fontSize = 10.sp)

  NavigationBar(
    modifier = modifier.fillMaxWidth(),
    containerColor = colorScheme.background,
    contentColor = colorScheme.onBackground,
  ) {
    LibraryTab.entries.forEach { tab ->
      val (icon, label) =
        when (tab) {
          LibraryTab.HOME -> Icons.Outlined.Home to stringResource(R.string.library_tab_home)
          LibraryTab.BOOKS -> Icons.AutoMirrored.Filled.LibraryBooks to stringResource(R.string.library_tab_books)
          LibraryTab.SERIES -> Icons.Outlined.Dashboard to stringResource(R.string.library_tab_series)
          LibraryTab.SEARCH -> Icons.Outlined.Search to stringResource(R.string.library_tab_search)
          LibraryTab.SETTINGS -> Icons.Outlined.Settings to stringResource(R.string.library_tab_settings)
        }

      NavigationBarItem(
        selected = selectedTab == tab,
        onClick = { onTabSelected(tab) },
        icon = {
          Icon(
            imageVector = icon,
            contentDescription = label,
          )
        },
        label = {
          Text(
            text = label,
            style = labelStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        colors =
          NavigationBarItemDefaults.colors(
            selectedIconColor = colorScheme.primary,
            selectedTextColor = colorScheme.primary,
            indicatorColor = colorScheme.surfaceContainer,
          ),
      )
    }
  }
}
