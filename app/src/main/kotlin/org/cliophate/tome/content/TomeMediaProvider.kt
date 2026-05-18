package org.cliophate.tome.content

import android.net.Uri
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.cliophate.tome.channel.audiobookshelf.AudiobookshelfChannelProvider
import org.cliophate.tome.channel.audiobookshelf.common.api.AudioBookshelfRepository
import org.cliophate.tome.channel.audiobookshelf.common.model.user.ListeningStatsResponse
import org.cliophate.tome.channel.audiobookshelf.common.model.user.PersonalizedFeedItemResponse
import org.cliophate.tome.channel.audiobookshelf.common.model.user.PersonalizedFeedResponse
import org.cliophate.tome.channel.audiobookshelf.library.model.LibraryItem
import org.cliophate.tome.channel.common.ChannelAuthService
import org.cliophate.tome.channel.common.MediaChannel
import org.cliophate.tome.channel.common.OperationError
import org.cliophate.tome.channel.common.OperationResult
import org.cliophate.tome.content.cache.persistent.LocalCacheRepository
import org.cliophate.tome.content.cache.temporary.CachedBookmarkProvider
import org.cliophate.tome.content.cache.temporary.CachedCoverProvider
import org.cliophate.tome.lib.domain.Book
import org.cliophate.tome.lib.domain.Bookmark
import org.cliophate.tome.lib.domain.DetailedItem
import org.cliophate.tome.lib.domain.Library
import org.cliophate.tome.lib.domain.LibraryType
import org.cliophate.tome.lib.domain.PagedItems
import org.cliophate.tome.lib.domain.PlaybackProgress
import org.cliophate.tome.lib.domain.PlaybackSession
import org.cliophate.tome.lib.domain.RecentBook
import org.cliophate.tome.lib.domain.UserAccount
import org.cliophate.tome.lib.domain.isSame
import org.cliophate.tome.persistence.preferences.TomeSharedPreferences
import org.cliophate.tome.playback.service.calculateChapterIndex
import org.cliophate.tome.ui.screens.library.model.HomeSummary
import org.cliophate.tome.ui.screens.library.model.ListeningStats
import timber.log.Timber
import java.io.File
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TomeMediaProvider
  @Inject
  constructor(
    private val preferences: TomeSharedPreferences,
    private val channelProvider: AudiobookshelfChannelProvider,
    private val audioBookshelfRepository: AudioBookshelfRepository,
    private val localCacheRepository: LocalCacheRepository,
    private val cachedCoverProvider: CachedCoverProvider,
    private val cachedBookmarkProvider: CachedBookmarkProvider,
  ) {
    suspend fun dropBookmark(bookmark: Bookmark) = cachedBookmarkProvider.dropBookmark(bookmark = bookmark)

    suspend fun createBookmark(
      libraryItemId: String,
      chapterPosition: Double,
      totalPosition: Double,
    ): Bookmark? {
      val playingItem = preferences.getPlayingItem() ?: return null

      return cachedBookmarkProvider
        .createBookmark(
          chapterTime = chapterPosition,
          libraryItemId = libraryItemId,
          totalTime = totalPosition,
          currentChapter = playingItem.chapters[calculateChapterIndex(playingItem, totalPosition)].title,
        )
    }

    suspend fun provideBookmarks(playingItemId: String): List<Bookmark> =
      cachedBookmarkProvider
        .provideBookmarks(playingItemId)
        .sortedByDescending { it.createdAt }
        .fold(emptyList()) { acc, item -> if (acc.any { it.isSame(item) }) acc else acc + item }

    suspend fun updateAndProvideBookmarks(playingItemId: String): List<Bookmark> =
      cachedBookmarkProvider
        .fetchBookmarks(playingItemId)
        .sortedByDescending { it.createdAt }
        .fold(emptyList()) { acc, b -> if (acc.any { it.isSame(b) }) acc else acc + b }

    fun provideFileUri(
      libraryItemId: String,
      chapterId: String,
    ): OperationResult<Uri> {
      Timber.d("Fetching File $libraryItemId and $chapterId URI")

      return when (preferences.isForceCache()) {
        true -> {
          localCacheRepository
            .provideFileUri(libraryItemId, chapterId)
            ?.let { OperationResult.Success(it) }
            ?: OperationResult.Error(OperationError.InternalError)
        }

        false -> {
          localCacheRepository
            .provideFileUri(libraryItemId, chapterId)
            ?.let { OperationResult.Success(it) }
            ?: providePreferredChannel()
              .provideFileUri(libraryItemId, chapterId)
              .let { OperationResult.Success(it) }
        }
      }
    }

    suspend fun syncProgress(
      sessionId: String,
      detailedItem: DetailedItem,
      progress: PlaybackProgress,
    ): OperationResult<Unit> {
      Timber.d("Syncing Progress for ${detailedItem.id}. $progress")

      localCacheRepository.syncProgress(detailedItem, progress)

      val channelSyncResult =
        providePreferredChannel()
          .syncProgress(sessionId, progress)

      return when (preferences.isForceCache()) {
        true -> OperationResult.Success(Unit)
        false -> channelSyncResult
      }
    }

    suspend fun fetchBookCover(bookId: String): OperationResult<File> {
      Timber.d("Fetching Cover stream for $bookId")
      return when (preferences.isForceCache()) {
        true -> {
          localCacheRepository.fetchBookCover(bookId)
        }

        false -> {
          cachedCoverProvider.provideCover(
            channel = providePreferredChannel(),
            itemId = bookId,
          )
        }
      }
    }

    suspend fun searchBooks(
      libraryId: String,
      query: String,
      limit: Int,
    ): OperationResult<List<Book>> {
      Timber.d("Searching books with query $query of library: $libraryId")

      return when (preferences.isForceCache()) {
        true -> {
          localCacheRepository.searchBooks(libraryId = libraryId, query = query)
        }

        false -> {
          providePreferredChannel()
            .searchBooks(
              libraryId = libraryId,
              query = query,
              limit = limit,
            )
        }
      }
    }

    suspend fun fetchBooks(
      libraryId: String,
      pageSize: Int,
      pageNumber: Int,
    ): OperationResult<PagedItems<Book>> {
      Timber.d("Fetching page $pageNumber of library: $libraryId")

      return when (preferences.isForceCache()) {
        true -> localCacheRepository.fetchBooks(libraryId = libraryId, pageSize = pageSize, pageNumber = pageNumber)
        false -> providePreferredChannel().fetchBooks(libraryId = libraryId, pageSize = pageSize, pageNumber = pageNumber)
      }
    }

    suspend fun fetchHomeSummary(libraryId: String): OperationResult<HomeSummary> {
      Timber.d("Fetching home summary of library: $libraryId")

      if (preferences.isForceCache()) {
        return OperationResult.Success(provideCachedHomeSummary(libraryId, stats = null))
      }

      return coroutineScope {
        val personalizedFeed = async { audioBookshelfRepository.fetchPersonalizedFeed(libraryId, limit = HOME_SHELF_LIMIT * 2) }
        val listeningStats = async { audioBookshelfRepository.fetchListeningStats() }
        val userInfo = async { audioBookshelfRepository.fetchUserInfoResponse() }

        val progressIds =
          userInfo
            .await()
            .fold(
              onSuccess = { response ->
                response
                  .mediaProgress
                  ?.map { it.libraryItemId }
                  ?.toSet()
                  ?: emptySet()
              },
              onFailure = { emptySet() },
            )

        val finishedIds =
          userInfo
            .await()
            .fold(
              onSuccess = { response ->
                response
                  .mediaProgress
                  ?.filter { it.isFinished }
                  ?.map { it.libraryItemId }
                  ?.toSet()
                  ?: emptySet()
              },
              onFailure = { emptySet() },
            )

        val stats =
          listeningStats
            .await()
            .fold(
              onSuccess = { response -> response.toListeningStats() },
              onFailure = { null },
            )

        when (val feedResult = personalizedFeed.await()) {
          is OperationResult.Success -> {
            val shelves = feedResult.data
            val recentlyAdded = provideShelfBooks(shelves, SHELF_RECENTLY_ADDED, finishedIds).take(HOME_SHELF_LIMIT)
            val recommended = provideShelfBooks(shelves, SHELF_RECOMMENDED, finishedIds)

            OperationResult.Success(
              HomeSummary(
                stats = stats,
                recentlyAdded = recentlyAdded,
                unplayed =
                  (recommended + recentlyAdded)
                    .filterNot { it.id in progressIds }
                    .distinctBy { it.id }
                    .take(HOME_SHELF_LIMIT),
              ),
            )
          }

          is OperationResult.Error -> {
            provideRemoteHomeSummaryFallback(
              libraryId = libraryId,
              progressIds = progressIds,
              finishedIds = finishedIds,
              stats = stats,
            )
          }
        }
      }
    }

    suspend fun fetchSeriesBooks(libraryId: String): OperationResult<List<DetailedItem>> {
      Timber.d("Fetching series books of library: $libraryId")

      return when (preferences.isForceCache()) {
        true -> {
          localCacheRepository
            .fetchDetailedItems()
            .map { result ->
              result.items.filter { item ->
                item.libraryId == libraryId &&
                  item.series.isNotEmpty() &&
                  (!preferences.getHideCompleted() || item.progress?.isFinished != true)
              }
            }
        }

        false -> {
          coroutineScope {
            val seriesCandidates = mutableListOf<Book>()
            var pageNumber = 0
            var loadedCount = 0

            while (true) {
              when (
                val pageResult =
                  providePreferredChannel().fetchBooks(
                    libraryId = libraryId,
                    pageSize = SERIES_PAGE_SIZE,
                    pageNumber = pageNumber,
                  )
              ) {
                is OperationResult.Error -> {
                  return@coroutineScope OperationResult.Error(pageResult.code, pageResult.message)
                }

                is OperationResult.Success -> {
                  val page = pageResult.data
                  seriesCandidates += page.items.filter { it.series.isNullOrBlank().not() }
                  loadedCount += page.items.size

                  if (page.items.isEmpty() || loadedCount >= page.totalItems) {
                    break
                  }

                  pageNumber += 1
                }
              }
            }

            val detailedBooks = mutableListOf<DetailedItem>()

            seriesCandidates
              .distinctBy { it.id }
              .chunked(SERIES_DETAIL_CHUNK_SIZE)
              .forEach { chunk ->
                chunk
                  .map { async { fetchBook(it.id) } }
                  .awaitAll()
                  .forEach { result ->
                    result.fold(
                      onSuccess = { item ->
                        if (item.series.isNotEmpty()) {
                          detailedBooks += item
                        }
                      },
                      onFailure = {},
                    )
                  }
              }

            OperationResult.Success(detailedBooks)
          }
        }
      }
    }

    suspend fun fetchLibraries(): OperationResult<List<Library>> {
      Timber.d("Fetching List of libraries")

      return when (preferences.isForceCache()) {
        true -> {
          localCacheRepository.fetchLibraries()
        }

        false -> {
          providePreferredChannel()
            .fetchLibraries()
            .also {
              it.foldAsync(
                onSuccess = { libraries -> localCacheRepository.updateLibraries(libraries) },
                onFailure = {},
              )
            }
        }
      }
    }

    suspend fun startPlayback(
      itemId: String,
      chapterId: String,
      supportedMimeTypes: List<String>,
      deviceId: String,
    ): OperationResult<PlaybackSession> {
      Timber.d("Starting Playback for $itemId. $supportedMimeTypes are supported")

      return providePreferredChannel()
        .startPlayback(
          bookId = itemId,
          episodeId = chapterId,
          supportedMimeTypes = supportedMimeTypes,
          deviceId = deviceId,
        ).foldAsync(
          onSuccess = {
            OperationResult.Success(it)
          },
          onFailure = {
            OperationResult.Success(PlaybackSession.local(itemId))
          },
        )
    }

    suspend fun fetchRecentListenedBooks(libraryId: String): OperationResult<List<RecentBook>> {
      Timber.d("Fetching Recent books of library $libraryId")

      return when (preferences.isForceCache()) {
        true -> {
          localCacheRepository.fetchRecentListenedBooks(libraryId)
        }

        false -> {
          providePreferredChannel()
            .fetchRecentListenedBooks(libraryId)
            .map { items -> syncFromLocalProgress(libraryId = libraryId, detailedItems = items) }
        }
      }
    }

    suspend fun fetchBook(bookId: String): OperationResult<DetailedItem> {
      Timber.d("Fetching Detailed book info for $bookId")

      return when (preferences.isForceCache()) {
        true -> {
          localCacheRepository
            .fetchBook(bookId)
            ?.let { OperationResult.Success(it) }
            ?: OperationResult.Error(OperationError.InternalError)
        }

        false -> {
          providePreferredChannel()
            .fetchBook(bookId)
            .map { syncFromLocalProgress(it) }
            .map { trimProgress(it) }
        }
      }
    }

    suspend fun authorize(
      host: String,
      username: String,
      password: String,
    ): OperationResult<UserAccount> {
      Timber.d("Authorizing for $username@$host")
      return provideAuthService().authorize(host, username, password) { onPostLogin(host, it) }
    }

    suspend fun startOAuth(
      host: String,
      onSuccess: () -> Unit,
      onFailure: (OperationError) -> Unit,
    ) {
      Timber.d("Starting OAuth for $host")

      return provideAuthService()
        .startOAuth(
          host = host,
          onSuccess = onSuccess,
          onFailure = { onFailure(it) },
        )
    }

    suspend fun onPostLogin(
      host: String,
      account: UserAccount,
    ) {
      provideAuthService()
        .persistCredentials(
          host = host,
          username = account.username,
          token = account.token,
          accessToken = account.accessToken,
          refreshToken = account.refreshToken,
        )

      fetchLibraries()
        .fold(
          onSuccess = {
            val preferredLibrary =
              it
                .find { item -> item.id == account.preferredLibraryId }
                ?: it.firstOrNull()

            preferredLibrary
              ?.let { library ->
                preferences.savePreferredLibrary(
                  Library(
                    id = library.id,
                    title = library.title,
                    type = library.type,
                  ),
                )
              }
          },
          onFailure = {
            account
              .preferredLibraryId
              ?.let { library ->
                Library(
                  id = library,
                  title = "Default Library",
                  type = LibraryType.LIBRARY,
                )
              }?.let { preferences.savePreferredLibrary(it) }
          },
        )
    }

    private suspend fun syncFromLocalProgress(
      libraryId: String,
      detailedItems: List<RecentBook>,
    ): List<RecentBook> {
      val localRecentlyBooks =
        localCacheRepository
          .fetchRecentListenedBooks(libraryId)
          .fold(
            onSuccess = { it },
            onFailure = { return@fold detailedItems },
          )

      val syncedRecentlyBooks =
        detailedItems
          .mapNotNull { item -> localRecentlyBooks.find { it.id == item.id }?.let { item to it } }
          .map { (remote, local) ->
            val remoteTimestamp = remote.listenedLastUpdate ?: 0L
            if (remoteTimestamp > 0L) {
              return@map remote
            }

            val localTimestamp = local.listenedLastUpdate ?: 0L

            when (localTimestamp > 0L) {
              true -> local
              false -> remote
            }
          }

      return detailedItems
        .map { item ->
          syncedRecentlyBooks
            .find { item.id == it.id }
            ?.let { synced ->
              item.copy(
                listenedPercentage = synced.listenedPercentage,
                listenedLastUpdate = synced.listenedLastUpdate,
              )
            }
            ?: item
        }
    }

    private fun trimProgress(detailedItem: DetailedItem): DetailedItem {
      val totalDuration = detailedItem.chapters.sumOf { it.duration }
      val progress = detailedItem.progress?.currentTime ?: return detailedItem

      return when {
        progress <= 0 -> detailedItem.copy(progress = null)
        progress >= totalDuration -> detailedItem.copy(progress = null)
        else -> detailedItem
      }
    }

    private suspend fun syncFromLocalProgress(detailedItem: DetailedItem): DetailedItem {
      val cachedProgress = localCacheRepository.fetchPlayingItemProgress(detailedItem.id)
      val channelProgress = detailedItem.progress

      if (channelProgress != null) {
        localCacheRepository.cacheMediaProgress(detailedItem, channelProgress)

        Timber.d(
          "Using Audiobookshelf progress for ${detailedItem.id}: $channelProgress",
        )

        return detailedItem.copy(progress = channelProgress)
      }

      if (cachedProgress == null) {
        return detailedItem
      }

      Timber.d(
        """
        Falling back to cached playback progress for ${detailedItem.id}:
            Channel Progress: $channelProgress
            Cached Progress: $cachedProgress
        """.trimIndent(),
      )

      return detailedItem.copy(progress = cachedProgress)
    }

    private suspend fun provideCachedHomeSummary(
      libraryId: String,
      stats: ListeningStats?,
    ): HomeSummary {
      val cachedItems =
        localCacheRepository
          .fetchDetailedItems()
          .fold(
            onSuccess = { it.items },
            onFailure = { emptyList() },
          ).filter { it.libraryId == libraryId }

      val recentlyAdded =
        cachedItems
          .sortedByDescending { it.createdAt }
          .map(::toBook)
          .take(HOME_SHELF_LIMIT)

      val unplayed =
        cachedItems
          .filter { item ->
            val progress = item.progress
            progress == null || progress.currentTime <= 0.0
          }.sortedByDescending { it.createdAt }
          .map(::toBook)
          .take(HOME_SHELF_LIMIT)

      return HomeSummary(
        stats = stats,
        recentlyAdded = recentlyAdded,
        unplayed = unplayed,
      )
    }

    private suspend fun provideRemoteHomeSummaryFallback(
      libraryId: String,
      progressIds: Set<String>,
      finishedIds: Set<String>,
      stats: ListeningStats?,
    ): OperationResult<HomeSummary> {
      val liveItemsResult =
        audioBookshelfRepository.fetchLibraryItems(
          libraryId = libraryId,
          pageSize = HOME_SHELF_LIMIT * 3,
          pageNumber = 0,
          sort = "addedAt",
          direction = "1",
          filter = if (preferences.getHideCompleted()) HOME_FILTER_NOT_FINISHED else null,
        )

      return when (liveItemsResult) {
        is OperationResult.Success -> {
          val books = liveItemsResult.data.results.mapNotNull { toBook(it, isFinished = it.id in finishedIds) }

          OperationResult.Success(
            HomeSummary(
              stats = stats,
              recentlyAdded = books.take(HOME_SHELF_LIMIT),
              unplayed =
                books
                  .filterNot { it.id in progressIds }
                  .distinctBy { it.id }
                  .take(HOME_SHELF_LIMIT),
            ),
          )
        }

        is OperationResult.Error -> {
          OperationResult.Success(
            provideCachedHomeSummary(libraryId, stats = stats),
          )
        }
      }
    }

    private fun provideShelfBooks(
      shelves: List<PersonalizedFeedResponse>,
      shelfId: String,
      finishedIds: Set<String>,
    ) = shelves
      .firstOrNull { it.id == shelfId }
      ?.entities
      ?.mapNotNull { toBook(it, isFinished = it.id in finishedIds) }
      ?: emptyList()

    private fun toBook(
      item: PersonalizedFeedItemResponse,
      isFinished: Boolean = false,
    ): Book? {
      val media = item.media ?: return null

      return Book(
        id = item.id,
        title = media.metadata.title,
        subtitle = media.metadata.subtitle,
        series = null,
        author = media.metadata.authorName,
        isFinished = isFinished,
      )
    }

    private fun toBook(
      item: LibraryItem,
      isFinished: Boolean = false,
    ): Book? {
      val title = item.media.metadata.title ?: return null

      return Book(
        id = item.id,
        title = title,
        subtitle = item.media.metadata.subtitle,
        series = item.media.metadata.seriesName,
        author = item.media.metadata.authorName,
        isFinished = isFinished,
      )
    }

    private fun toBook(item: DetailedItem) =
      Book(
        id = item.id,
        title = item.title,
        subtitle = item.subtitle,
        series = item.series.firstOrNull()?.name,
        author = item.author,
        isFinished = item.progress?.isFinished == true,
      )

    private fun ListeningStatsResponse.toListeningStats() =
      ListeningStats(
        todaySeconds = today,
        streakDays = calculateListeningStreak(days),
      )

    private fun calculateListeningStreak(days: Map<String, Int>): Int {
      val listeningDays =
        days
          .filterValues { it > 0 }
          .keys
          .mapNotNull { date -> runCatching { LocalDate.parse(date) }.getOrNull() }
          .toSet()

      val start = listeningDays.maxOrNull() ?: return 0
      var streak = 0
      var cursor = start

      while (cursor in listeningDays) {
        streak += 1
        cursor = cursor.minusDays(1)
      }

      return streak
    }

    fun fetchConnectionHost() = providePreferredChannel().fetchConnectionHost()

    suspend fun fetchConnectionInfo() = providePreferredChannel().fetchConnectionInfo()

    fun provideAuthService(): ChannelAuthService = channelProvider.provideChannelAuth()

    fun providePreferredChannel(): MediaChannel = channelProvider.provideMediaChannel()

    companion object {
      private const val HOME_SHELF_LIMIT = 10
      private const val HOME_FILTER_NOT_FINISHED = "progress.bm90LWZpbmlzaGVk"
      private const val SHELF_RECENTLY_ADDED = "recently-added"
      private const val SHELF_RECOMMENDED = "recommended"
      private const val SERIES_PAGE_SIZE = 100
      private const val SERIES_DETAIL_CHUNK_SIZE = 8
    }
  }
