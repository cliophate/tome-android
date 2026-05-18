package org.cliophate.tome.ui.screens.library.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.request.ImageRequest
import org.cliophate.tome.R
import org.cliophate.tome.ui.components.AsyncShimmeringImage
import org.cliophate.tome.viewmodel.SeriesBook
import org.cliophate.tome.viewmodel.SeriesShelf

@Composable
fun SeriesOverviewSummaryComposable(
  seriesCount: Int,
  totalBooks: Int,
) {
  Text(
    text = "$seriesCount series • $totalBooks books",
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
  )
}

@Composable
fun SeriesShelfComposable(
  shelf: SeriesShelf,
  imageLoader: ImageLoader,
  onSelected: () -> Unit,
) {
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .clickable(onClick = onSelected)
        .padding(vertical = 10.dp),
  ) {
    shelf.author?.let {
      Text(
        text = it,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
      )

      Spacer(modifier = Modifier.height(2.dp))
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(
        text = shelf.title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f),
      )

      Icon(
        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        modifier = Modifier.size(18.dp),
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = shelf.summary,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
    )

    Spacer(modifier = Modifier.height(14.dp))

    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(end = 8.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      items(
        items = shelf.books.take(10),
        key = { it.id },
      ) { book ->
        SeriesCoverComposable(
          book = book,
          imageLoader = imageLoader,
          width = 92.dp,
          onClick = onSelected,
        )
      }
    }
  }
}

@Composable
fun SeriesDetailHeaderComposable(
  shelf: SeriesShelf,
  onStartListening: () -> Unit,
) {
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(top = 8.dp, bottom = 20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = shelf.title,
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
      color = MaterialTheme.colorScheme.onBackground,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = shelf.summary,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
    )

    Spacer(modifier = Modifier.height(20.dp))

    Button(
      onClick = onStartListening,
      shape = RoundedCornerShape(28.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(
        text = stringResource(R.string.series_start_listening),
        modifier = Modifier.padding(vertical = 4.dp),
      )
    }

    shelf.description?.let {
      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = it,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.92f),
        maxLines = 5,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
fun SeriesDetailGridComposable(
  shelf: SeriesShelf,
  imageLoader: ImageLoader,
  onBookSelected: (SeriesBook) -> Unit,
) {
  val rows = remember(shelf.books) { shelf.books.chunked(3) }

  Column(
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    rows.forEach { rowBooks ->
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        rowBooks.forEach { book ->
          Box(modifier = Modifier.weight(1f)) {
            SeriesCoverComposable(
              book = book,
              imageLoader = imageLoader,
              width = null,
              onClick = { onBookSelected(book) },
            )
          }
        }

        repeat(3 - rowBooks.size) {
          Spacer(modifier = Modifier.weight(1f))
        }
      }
    }
  }
}

@Composable
private fun SeriesCoverComposable(
  book: SeriesBook,
  imageLoader: ImageLoader,
  width: androidx.compose.ui.unit.Dp?,
  onClick: () -> Unit,
) {
  val context = LocalContext.current
  val imageRequest =
    remember(book.id) {
      ImageRequest
        .Builder(context)
        .data(book.id)
        .build()
    }

  Box(
    modifier =
      Modifier
        .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
        .aspectRatio(1f)
        .clip(RoundedCornerShape(12.dp))
        .clickable(onClick = onClick),
  ) {
    AsyncShimmeringImage(
      imageRequest = imageRequest,
      imageLoader = imageLoader,
      contentDescription = "${book.title} cover",
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize(),
      error = painterResource(R.drawable.cover_fallback),
    )

    book.sequenceLabel?.let {
      Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
        shape = RoundedCornerShape(topStart = 12.dp),
        modifier = Modifier.align(Alignment.BottomEnd),
      ) {
        Text(
          text = it,
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
      }
    }

    if (book.isFinished) {
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.14f)),
      )
    }
  }
}
