package org.cliophate.tome.channel.audiobookshelf.library.converter

import org.cliophate.tome.common.LibraryOrderingConfiguration
import org.cliophate.tome.common.LibraryOrderingDirection
import org.cliophate.tome.common.LibraryOrderingOption
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryOrderingRequestConverter
  @Inject
  constructor() {
    fun apply(configuration: LibraryOrderingConfiguration): Pair<String, String> {
      val option =
        when (configuration.option) {
          LibraryOrderingOption.TITLE -> "media.metadata.title"
          LibraryOrderingOption.AUTHOR -> "media.metadata.authorName"
          LibraryOrderingOption.CREATED_AT -> "addedAt"
          LibraryOrderingOption.UPDATED_AT -> "mtimeMs"
        }

      val direction =
        when (configuration.direction) {
          LibraryOrderingDirection.ASCENDING -> "0"
          LibraryOrderingDirection.DESCENDING -> "1"
        }

      return option to direction
    }
  }
