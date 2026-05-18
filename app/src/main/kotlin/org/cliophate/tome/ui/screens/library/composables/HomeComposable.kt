package org.cliophate.tome.ui.screens.library.composables

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.cliophate.tome.R
import org.cliophate.tome.lib.domain.Book
import org.cliophate.tome.ui.components.AsyncShimmeringImage
import org.cliophate.tome.ui.navigation.AppNavigationService
import org.cliophate.tome.ui.screens.library.model.ListeningStats

@Composable
fun HomeStatsComposable(stats: ListeningStats) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    HomeStatCardComposable(
      title = "Listening Time",
      value = stats.todaySeconds.formatListeningTime(),
      subtitle = "Today",
      modifier = Modifier.weight(1f),
    )

    HomeStatCardComposable(
      title = "Listening Streak",
      value = if (stats.streakDays == 1) "1 day" else "${stats.streakDays} days",
      subtitle = "Current",
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun HomeStatCardComposable(
  title: String,
  value: String,
  subtitle: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(20.dp),
    color = MaterialTheme.colorScheme.surfaceContainer,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface,
      )

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = value,
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface,
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
      )
    }
  }
}

@Composable
fun HomeSectionHeaderComposable(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    color = MaterialTheme.colorScheme.onBackground,
    modifier = Modifier.padding(bottom = 12.dp),
  )
}

@Composable
fun HomeBookShelfComposable(
  title: String,
  books: List<Book>,
  imageLoader: ImageLoader,
  navController: AppNavigationService,
) {
  if (books.isEmpty()) {
    return
  }

  val configuration = LocalConfiguration.current
  val screenWidth = remember { configuration.screenWidthDp.dp }
  val itemsVisible = 2.35f
  val spacing = 16.dp
  val totalSpacing = spacing * (itemsVisible + 1)
  val itemWidth = (screenWidth - totalSpacing) / itemsVisible

  Column(modifier = Modifier.fillMaxWidth()) {
    HomeSectionHeaderComposable(title)

    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      contentPadding = PaddingValues(horizontal = 4.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      items(
        items = books,
        key = { it.id },
      ) { book ->
        HomeBookItemComposable(
          book = book,
          imageLoader = imageLoader,
          navController = navController,
          width = itemWidth,
        )
      }
    }
  }
}

@Composable
private fun HomeBookItemComposable(
  book: Book,
  imageLoader: ImageLoader,
  navController: AppNavigationService,
  width: androidx.compose.ui.unit.Dp,
) {
  val context = LocalContext.current
  val imageRequest =
    remember(book.id) {
      ImageRequest
        .Builder(context)
        .data(book.id)
        .crossfade(300)
        .build()
    }

  Column(
    modifier =
      Modifier
        .width(width)
        .clickable { navController.showPlayer(book.id, book.title, book.subtitle) },
  ) {
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .aspectRatio(1f)
          .clip(RoundedCornerShape(12.dp)),
    ) {
      AsyncShimmeringImage(
        imageRequest = imageRequest,
        imageLoader = imageLoader,
        contentDescription = "${book.title} cover",
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize(),
        error = painterResource(R.drawable.cover_fallback),
      )

      if (book.isFinished) {
        FinishedBadgeComposable(modifier = Modifier.align(Alignment.TopEnd))
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text(
      text = book.title,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
      color = MaterialTheme.colorScheme.onBackground,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
    )

    book.author?.takeIf { it.isNotBlank() }?.let {
      Spacer(modifier = Modifier.height(2.dp))

      Text(
        text = it,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun FinishedBadgeComposable(modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.padding(8.dp),
    shape = RoundedCornerShape(999.dp),
    color = MaterialTheme.colorScheme.primary,
  ) {
    Text(
      text = stringResource(R.string.book_finished_marker),
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
      color = MaterialTheme.colorScheme.onPrimary,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
    )
  }
}

private fun Int.formatListeningTime(): String {
  val hours = this / 3600
  val minutes = (this % 3600) / 60

  return when {
    hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
    hours > 0 -> "${hours}h"
    minutes > 0 -> "${minutes}m"
    else -> "0m"
  }
}
