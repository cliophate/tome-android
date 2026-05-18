package org.cliophate.tome.ui.screens.player.composable

import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Forward30
import androidx.compose.material.icons.outlined.Replay5
import androidx.compose.material.icons.rounded.PauseCircleFilled
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.cliophate.tome.common.withHaptic
import org.cliophate.tome.ui.extensions.formatTime
import org.cliophate.tome.viewmodel.PlayerViewModel

@Composable
fun TrackControlComposable(
  viewModel: PlayerViewModel,
  modifier: Modifier = Modifier,
) {
  val isPlaying by viewModel.isPlaying.observeAsState(false)
  val currentTrackIndex by viewModel.currentChapterIndex.observeAsState(0)
  val currentTrackPosition by viewModel.currentChapterPosition.observeAsState(0.0)
  val currentTrackDuration by viewModel.currentChapterDuration.observeAsState(0.0)

  val book by viewModel.book.observeAsState()
  val chapters = book?.chapters ?: emptyList()
  val currentTrackTitle = chapters.getOrNull(currentTrackIndex)?.title.orEmpty()

  val view: View = LocalView.current

  var sliderPosition by remember { mutableDoubleStateOf(0.0) }
  var isDragging by remember { mutableStateOf(false) }
  val remainingChapterTime = maxOf(0.0, currentTrackDuration - sliderPosition).toInt()

  LaunchedEffect(currentTrackPosition, currentTrackIndex, currentTrackDuration) {
    if (!isDragging) {
      sliderPosition = currentTrackPosition
    }
  }

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .widthIn(max = 420.dp)
        .padding(horizontal = 12.dp),
  ) {
    Text(
      text = currentTrackTitle,
      style = typography.titleMedium,
      color = colorScheme.onBackground,
      fontWeight = FontWeight.Medium,
      textAlign = TextAlign.Center,
      minLines = 1,
      modifier = Modifier.fillMaxWidth(),
    )

    Column(
      modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
    ) {
      Slider(
        value = sliderPosition.toFloat(),
        onValueChange = { newPosition ->
          isDragging = true
          sliderPosition = newPosition.toDouble()
        },
        onValueChangeFinished = {
          isDragging = false
          viewModel.seekTo(sliderPosition)
        },
        valueRange = 0f..currentTrackDuration.toFloat(),
        colors =
          SliderDefaults.colors(
            thumbColor = colorScheme.primary,
            activeTrackColor = colorScheme.primary,
          ),
        modifier = Modifier.fillMaxWidth(),
      )
    }

    Box(
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column {
        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .offset(y = (-4).dp)
              .padding(horizontal = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = sliderPosition.toInt().formatTime(true),
            style = typography.bodySmall,
            color = colorScheme.onBackground.copy(alpha = 0.6f),
          )

          Text(
            text = "${remainingChapterTime.formatTime(false)} remaining",
            style = typography.bodySmall,
            color = colorScheme.onBackground.copy(alpha = 0.6f),
          )

          Text(
            text = "-${remainingChapterTime.formatTime(true)}",
            style = typography.bodySmall,
            color = colorScheme.onBackground.copy(alpha = 0.6f),
          )
        }
      }

      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(top = 22.dp, start = 8.dp, end = 8.dp)
            .align(Alignment.BottomCenter),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(
          onClick = {
            withHaptic(view) { viewModel.previousTrack() }
          },
          enabled = true,
        ) {
          Icon(
            imageVector = Icons.Rounded.SkipPrevious,
            contentDescription = "Previous Track",
            tint = colorScheme.onBackground,
            modifier = Modifier.size(32.dp),
          )
        }

        SeekButton(
          icon = Icons.Outlined.Replay5,
          contentDescription = "Replay 5 seconds",
          onClick = { withHaptic(view) { viewModel.skipBy(-5.0) } },
        )

        IconButton(
          onClick = { withHaptic(view) { viewModel.togglePlayPause() } },
          modifier = Modifier.size(72.dp),
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Rounded.PauseCircleFilled else Icons.Rounded.PlayCircleFilled,
            contentDescription = "Play / Pause",
            tint = colorScheme.primary,
            modifier = Modifier.fillMaxSize(),
          )
        }

        SeekButton(
          icon = Icons.Outlined.Forward30,
          contentDescription = "Forward 30 seconds",
          onClick = { withHaptic(view) { viewModel.skipBy(30.0) } },
        )

        IconButton(
          onClick = {
            if (currentTrackIndex < chapters.size - 1) {
              withHaptic(view) { viewModel.nextTrack() }
            }
          },
          enabled = currentTrackIndex < chapters.size - 1,
        ) {
          Icon(
            imageVector = Icons.Rounded.SkipNext,
            contentDescription = "Next Track",
            tint =
              if (currentTrackIndex < chapters.size - 1) {
                colorScheme.onBackground
              } else {
                colorScheme.onBackground.copy(
                  alpha = 0.3f,
                )
              },
            modifier = Modifier.size(36.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun SeekButton(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
) {
  IconButton(
    onClick = onClick,
    modifier = Modifier.size(60.dp),
  ) {
    Icon(
      icon,
      contentDescription = contentDescription,
      tint = colorScheme.onBackground,
      modifier = Modifier.size(30.dp),
    )
  }
}
