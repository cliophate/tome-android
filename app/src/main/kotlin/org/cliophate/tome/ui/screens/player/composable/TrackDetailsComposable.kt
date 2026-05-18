package org.cliophate.tome.ui.screens.player.composable

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.request.ImageRequest
import org.cliophate.tome.R
import org.cliophate.tome.ui.components.AsyncShimmeringImage
import org.cliophate.tome.viewmodel.LibraryViewModel
import org.cliophate.tome.viewmodel.PlayerViewModel

@Composable
fun TrackDetailsComposable(
  libraryViewModel: LibraryViewModel,
  viewModel: PlayerViewModel,
  modifier: Modifier = Modifier,
  imageLoader: ImageLoader,
) {
  val book by viewModel.book.observeAsState()

  val context = LocalContext.current

  val imageRequest =
    remember(book?.id) {
      ImageRequest
        .Builder(context)
        .data(book?.id)
        .size(coil3.size.Size.ORIGINAL)
        .build()
    }

  val configuration = LocalConfiguration.current
  val screenHeight = configuration.screenHeightDp.dp
  val maxImageHeight = screenHeight * 0.33f

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.widthIn(max = 420.dp),
  ) {
    AsyncShimmeringImage(
      imageRequest = imageRequest,
      imageLoader = imageLoader,
      contentDescription = "${book?.title} cover",
      contentScale = ContentScale.FillBounds,
      modifier =
        Modifier
          .heightIn(max = maxImageHeight)
          .aspectRatio(1f)
          .clip(RoundedCornerShape(8.dp)),
      error = painterResource(R.drawable.cover_fallback),
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
      text = book?.title.orEmpty(),
      style = typography.headlineSmall,
      fontWeight = FontWeight.SemiBold,
      color = colorScheme.onBackground,
      textAlign = TextAlign.Center,
      overflow = TextOverflow.Ellipsis,
      maxLines = 2,
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
    )

    Spacer(modifier = Modifier.height(2.dp))

    val supportingLine =
      book
        ?.author
        ?.takeIf { it.isNotBlank() }
        ?: book
          ?.subtitle
          ?.takeIf { it.isNotBlank() }

    supportingLine?.let {
      Text(
        text = it,
        style = typography.bodyLarge,
        color = colorScheme.onBackground.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
      )

      Spacer(modifier = Modifier.height(6.dp))
    }

    book
      ?.subtitle
      ?.takeIf { subtitle -> subtitle.isNotBlank() && subtitle != supportingLine }
      ?.let {
        Text(
          text = it,
          style = typography.bodyMedium,
          color = colorScheme.onBackground.copy(alpha = 0.6f),
          textAlign = TextAlign.Center,
          overflow = TextOverflow.Ellipsis,
          maxLines = 1,
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(2.dp))
      }
  }
}
