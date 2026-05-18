package org.cliophate.tome.channel.common

import android.net.Uri
import okio.Buffer
import org.cliophate.tome.channel.audiobookshelf.Host
import org.cliophate.tome.lib.domain.Book
import org.cliophate.tome.lib.domain.Bookmark
import org.cliophate.tome.lib.domain.CreateBookmarkRequest
import org.cliophate.tome.lib.domain.DetailedItem
import org.cliophate.tome.lib.domain.Library
import org.cliophate.tome.lib.domain.LibraryType
import org.cliophate.tome.lib.domain.PagedItems
import org.cliophate.tome.lib.domain.PlaybackProgress
import org.cliophate.tome.lib.domain.PlaybackSession
import org.cliophate.tome.lib.domain.RecentBook

interface MediaChannel {
  fun getLibraryType(): LibraryType

  fun provideFileUri(
    libraryItemId: String,
    fileId: String,
  ): Uri

  suspend fun syncProgress(
    sessionId: String,
    progress: PlaybackProgress,
  ): OperationResult<Unit>

  suspend fun fetchBookCover(
    bookId: String,
    width: Int? = null,
  ): OperationResult<Buffer>

  suspend fun fetchBooks(
    libraryId: String,
    pageSize: Int,
    pageNumber: Int,
  ): OperationResult<PagedItems<Book>>

  suspend fun searchBooks(
    libraryId: String,
    query: String,
    limit: Int,
  ): OperationResult<List<Book>>

  suspend fun fetchLibraries(): OperationResult<List<Library>>

  suspend fun startPlayback(
    bookId: String,
    episodeId: String,
    supportedMimeTypes: List<String>,
    deviceId: String,
  ): OperationResult<PlaybackSession>

  fun fetchConnectionHost(): OperationResult<Host>

  suspend fun fetchConnectionInfo(): OperationResult<ConnectionInfo>

  suspend fun fetchRecentListenedBooks(libraryId: String): OperationResult<List<RecentBook>>

  suspend fun fetchBook(bookId: String): OperationResult<DetailedItem>

  suspend fun fetchBookmarks(libraryItemId: String): OperationResult<List<Bookmark>>

  suspend fun dropBookmark(bookmark: Bookmark): OperationResult<Unit>

  suspend fun createBookmark(request: CreateBookmarkRequest): OperationResult<Bookmark>
}
