package org.cliophate.tome.channel.audiobookshelf.common.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Buffer
import org.cliophate.tome.channel.audiobookshelf.common.model.MediaProgressResponse
import org.cliophate.tome.channel.audiobookshelf.common.model.bookmark.BookmarkRequest
import org.cliophate.tome.channel.audiobookshelf.common.model.bookmark.BookmarksItemResponse
import org.cliophate.tome.channel.audiobookshelf.common.model.bookmark.BookmarksResponse
import org.cliophate.tome.channel.audiobookshelf.common.model.connection.ConnectionInfoResponse
import org.cliophate.tome.channel.audiobookshelf.common.model.metadata.AuthorItemsResponse
import org.cliophate.tome.channel.audiobookshelf.common.model.metadata.LibraryResponse
import org.cliophate.tome.channel.audiobookshelf.common.model.playback.PlaybackSessionResponse
import org.cliophate.tome.channel.audiobookshelf.common.model.playback.PlaybackStartRequest
import org.cliophate.tome.channel.audiobookshelf.common.model.playback.ProgressSyncRequest
import org.cliophate.tome.channel.audiobookshelf.common.model.user.ListeningStatsResponse
import org.cliophate.tome.channel.audiobookshelf.common.model.user.PersonalizedFeedResponse
import org.cliophate.tome.channel.audiobookshelf.common.model.user.UserResponse
import org.cliophate.tome.channel.audiobookshelf.library.model.BookResponse
import org.cliophate.tome.channel.audiobookshelf.library.model.LibraryItemsResponse
import org.cliophate.tome.channel.audiobookshelf.library.model.LibrarySearchResponse
import org.cliophate.tome.channel.audiobookshelf.podcast.model.PodcastItemsResponse
import org.cliophate.tome.channel.audiobookshelf.podcast.model.PodcastResponse
import org.cliophate.tome.channel.audiobookshelf.podcast.model.PodcastSearchResponse
import org.cliophate.tome.channel.common.OperationResult
import org.cliophate.tome.lib.domain.Bookmark
import org.cliophate.tome.lib.domain.CreateBookmarkRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioBookshelfRepository
  @Inject
  constructor(
    private val audioBookShelfApiService: AudioBookShelfApiService,
  ) {
    suspend fun fetchLibraries(): OperationResult<LibraryResponse> =
      audioBookShelfApiService
        .makeRequest { it.fetchLibraries() }

    suspend fun fetchAuthorItems(authorId: String): OperationResult<AuthorItemsResponse> =
      audioBookShelfApiService
        .makeRequest {
          it.fetchAuthorLibraryItems(
            authorId = authorId,
          )
        }

    suspend fun searchPodcasts(
      libraryId: String,
      query: String,
      limit: Int,
    ): OperationResult<PodcastSearchResponse> =
      audioBookShelfApiService
        .makeRequest {
          it.searchPodcasts(
            libraryId = libraryId,
            request = query,
            limit = limit,
          )
        }

    suspend fun searchBooks(
      libraryId: String,
      query: String,
      limit: Int,
    ): OperationResult<LibrarySearchResponse> =
      audioBookShelfApiService
        .makeRequest {
          it.searchLibraryItems(
            libraryId = libraryId,
            request = query,
            limit = limit,
          )
        }

    suspend fun fetchLibraryItems(
      libraryId: String,
      pageSize: Int,
      pageNumber: Int,
      sort: String,
      direction: String,
      filter: String?,
    ): OperationResult<LibraryItemsResponse> =
      audioBookShelfApiService.makeRequest {
        it.fetchLibraryItems(
          libraryId = libraryId,
          pageSize = pageSize,
          pageNumber = pageNumber,
          sort = sort,
          desc = direction,
          filter = filter,
        )
      }

    suspend fun fetchPodcastItems(
      libraryId: String,
      pageSize: Int,
      pageNumber: Int,
      sort: String,
      direction: String,
    ): OperationResult<PodcastItemsResponse> =
      audioBookShelfApiService
        .makeRequest {
          it.fetchPodcastItems(
            libraryId = libraryId,
            pageSize = pageSize,
            pageNumber = pageNumber,
            sort = sort,
            desc = direction,
          )
        }

    suspend fun fetchBook(itemId: String): OperationResult<BookResponse> =
      audioBookShelfApiService.makeRequest {
        it.fetchLibraryItem(
          itemId = itemId,
        )
      }

    suspend fun fetchPodcastItem(itemId: String): OperationResult<PodcastResponse> =
      audioBookShelfApiService.makeRequest {
        it.fetchPodcastEpisode(
          itemId = itemId,
        )
      }

    suspend fun fetchBookmarks(): OperationResult<BookmarksResponse> =
      audioBookShelfApiService
        .makeRequest {
          it.fetchBookmarks()
        }

    suspend fun createBookmarks(request: CreateBookmarkRequest): OperationResult<BookmarksItemResponse> =
      audioBookShelfApiService
        .makeRequest {
          it.createBookmarks(
            libraryItemId = request.libraryItemId,
            request =
              BookmarkRequest(
                title = request.title,
                time = request.time,
              ),
          )
        }

    suspend fun dropBookmark(bookmark: Bookmark): OperationResult<Unit> =
      audioBookShelfApiService
        .makeRequest {
          it.dropBookmarks(
            libraryItemId = bookmark.libraryItemId,
            totalTime = bookmark.totalPosition.toInt(),
          )
        }

    suspend fun fetchConnectionInfo(): OperationResult<ConnectionInfoResponse> =
      audioBookShelfApiService.makeRequest {
        it.fetchConnectionInfo()
      }

    suspend fun fetchPersonalizedFeed(
      libraryId: String,
      limit: Int? = null,
    ): OperationResult<List<PersonalizedFeedResponse>> =
      audioBookShelfApiService.makeRequest {
        it.fetchPersonalizedFeed(
          libraryId = libraryId,
          limit = limit,
        )
      }

    suspend fun fetchListeningStats(): OperationResult<ListeningStatsResponse> =
      audioBookShelfApiService.makeRequest {
        it.fetchListeningStats()
      }

    suspend fun fetchLibraryItemProgress(itemId: String): OperationResult<MediaProgressResponse> =
      audioBookShelfApiService.makeRequest {
        it.fetchLibraryItemProgress(
          itemId = itemId,
        )
      }

    suspend fun fetchUserInfoResponse(): OperationResult<UserResponse> =
      audioBookShelfApiService.makeRequest {
        it.fetchUserInfo()
      }

    suspend fun startPlayback(
      itemId: String,
      request: PlaybackStartRequest,
    ): OperationResult<PlaybackSessionResponse> =
      audioBookShelfApiService.makeRequest {
        it.startLibraryPlayback(
          itemId = itemId,
          syncProgressRequest = request,
        )
      }

    suspend fun startPodcastPlayback(
      itemId: String,
      episodeId: String,
      request: PlaybackStartRequest,
    ): OperationResult<PlaybackSessionResponse> =
      audioBookShelfApiService.makeRequest {
        it.startPodcastPlayback(
          itemId = itemId,
          episodeId = episodeId,
          syncProgressRequest = request,
        )
      }

    suspend fun publishLibraryItemProgress(
      itemId: String,
      progress: ProgressSyncRequest,
    ): OperationResult<Unit> =
      audioBookShelfApiService.makeRequest {
        it.publishLibraryItemProgress(
          itemId = itemId,
          syncProgressRequest = progress,
        )
      }

    suspend fun fetchBookCover(
      itemId: String,
      width: Int?,
    ): OperationResult<Buffer> =
      audioBookShelfApiService
        .makeRequest {
          when (width == null) {
            true -> it.getItemCover(itemId = itemId)
            false -> it.getItemCover(itemId = itemId, width)
          }
        }.map { response ->
          withContext(Dispatchers.IO) {
            response.use {
              Buffer().apply { writeAll(it.source()) }
            }
          }
        }
  }
