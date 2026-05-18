package org.cliophate.tome.channel.audiobookshelf.common.converter

import org.cliophate.tome.channel.audiobookshelf.common.model.bookmark.BookmarksResponse
import org.cliophate.tome.lib.domain.Bookmark
import org.cliophate.tome.lib.domain.BookmarkSyncState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarksResponseConverter
  @Inject
  constructor(
    private val itemConverter: BookmarkItemResponseConverter,
  ) {
    fun apply(
      response: BookmarksResponse,
      syncState: BookmarkSyncState,
    ): List<Bookmark> =
      response
        .bookmarks
        .map { itemConverter.apply(item = it, syncState = syncState) }
  }
