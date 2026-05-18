package org.cliophate.tome.ui.screens.library.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.cliophate.tome.R

@Composable
fun DefaultActionComposable(
  isConnectedToServer: Boolean,
  onConnectionRequested: () -> Unit,
  onFiltersRequested: () -> Unit,
) {
  val statusDescription =
    stringResource(
      when (isConnectedToServer) {
        true -> R.string.connection_status_connected
        false -> R.string.connection_status_disconnected
      },
    )

  Row {
    IconButton(onClick = { onConnectionRequested() }) {
      androidx.compose.foundation.layout.Box(
        modifier =
          Modifier
            .semantics { contentDescription = statusDescription }
            .size(12.dp)
            .background(
              color = if (isConnectedToServer) ConnectedDotColor else MaterialTheme.colorScheme.error,
              shape = CircleShape,
            ),
      )
    }

    IconButton(onClick = { onFiltersRequested() }) {
      Icon(
        imageVector = Icons.Outlined.FilterAlt,
        contentDescription = stringResource(R.string.library_filters),
      )
    }
  }
}

private val ConnectedDotColor = Color(0xFF34C759)
