package org.cliophate.tome.channel.audiobookshelf.common.converter

import org.cliophate.tome.channel.audiobookshelf.common.model.metadata.LibraryItemResponse
import org.cliophate.tome.lib.domain.Library
import org.cliophate.tome.lib.domain.LibraryType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryResponseConverter
  @Inject
  constructor() {
    fun apply(response: List<LibraryItemResponse>): List<Library> =
      response
        .map {
          it
            .mediaType
            .toLibraryType()
            .let { type -> Library(it.id, it.name, type) }
        }

    private fun String.toLibraryType() =
      when (this) {
        "podcast" -> LibraryType.PODCAST
        "book" -> LibraryType.LIBRARY
        else -> LibraryType.UNKNOWN
      }
  }
