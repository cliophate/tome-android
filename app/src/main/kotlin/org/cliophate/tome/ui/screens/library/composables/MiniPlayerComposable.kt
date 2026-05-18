package org.cliophate.tome.ui.screens.library.composables

import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.request.ImageRequest
import org.cliophate.tome.R
import org.cliophate.tome.common.withHaptic
import org.cliophate.tome.lib.domain.DetailedItem
import org.cliophate.tome.ui.components.AsyncShimmeringImage
import org.cliophate.tome.ui.navigation.AppNavigationService
import org.cliophate.tome.viewmodel.PlayerViewModel

@Composable
fun MiniPlayerComposable(
  navController: AppNavigationService,
  book: DetailedItem,
  imageLoader: ImageLoader,
  playerViewModel: PlayerViewModel,
) {
  val view: View = LocalView.current

  val isPlaying: Boolean by playerViewModel.isPlaying.observeAsState(false)
  var backgroundVisible by remember { mutableStateOf(true) }

  val dismissState =
    rememberSwipeToDismissBoxState(
      initialValue = SwipeToDismissBoxValue.Settled,
      positionalThreshold = { it * 0.2f },
      confirmValueChange = { newValue: SwipeToDismissBoxValue ->
        val dismissing =
          when (newValue) {
            SwipeToDismissBoxValue.EndToStart,
            SwipeToDismissBoxValue.StartToEnd,
            -> true

            else -> false
          }

        if (dismissing) {
          withHaptic(view) {
            backgroundVisible = false
            playerViewModel.clearPlayingBook()
          }
        }

        dismissing
      },
    )

  SwipeToDismissBox(
    state = dismissState,
    backgroundContent = {
      Row(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        AnimatedVisibility(
          visible = backgroundVisible,
          exit = fadeOut(animationSpec = tween(300)),
        ) {
          CloseActionBackground()
        }

        AnimatedVisibility(
          visible = backgroundVisible,
          exit = fadeOut(animationSpec = tween(300)),
        ) {
          CloseActionBackground()
        }
      }
    },
  ) {
    AnimatedVisibility(
      visible = backgroundVisible,
      exit = fadeOut(animationSpec = tween(300)),
    ) {
      val context = LocalContext.current
      val imageRequest =
        remember(book.id) {
          ImageRequest
            .Builder(context)
            .data(book.id)
            .build()
        }

      Surface(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable { navController.showPlayer(book.id, book.title, book.subtitle) },
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
      ) {
        Column {
          Row(
            modifier =
              Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            AsyncShimmeringImage(
              imageRequest = imageRequest,
              imageLoader = imageLoader,
              contentDescription = "${book.title} cover",
              contentScale = ContentScale.FillBounds,
              modifier =
                Modifier
                  .size(48.dp)
                  .aspectRatio(1f)
                  .clip(RoundedCornerShape(6.dp)),
              error = painterResource(R.drawable.cover_fallback),
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
              text = book.title,
              style =
                typography.bodyMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = colorScheme.onSurface,
                ),
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
              modifier =
                Modifier
                  .clip(CircleShape)
                  .background(colorScheme.primary)
                  .clickable { withHaptic(view) { playerViewModel.togglePlayPause() } }
                  .padding(10.dp),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = colorScheme.onPrimary,
                modifier = Modifier.size(24.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun CloseActionBackground() {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier =
      Modifier
        .width(80.dp)
        .padding(vertical = 8.dp),
  ) {
    Icon(
      imageVector = Icons.Outlined.Close,
      contentDescription = stringResource(R.string.mini_player_action_close),
      tint = colorScheme.onSurface,
      modifier = Modifier.size(24.dp),
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = stringResource(R.string.mini_player_action_close),
      style = typography.labelSmall,
      color = colorScheme.onSurface,
    )
  }
}
