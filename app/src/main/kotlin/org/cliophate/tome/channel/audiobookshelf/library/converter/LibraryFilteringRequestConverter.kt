package org.cliophate.tome.channel.audiobookshelf.library.converter

import org.cliophate.tome.persistence.preferences.TomeSharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryFilteringRequestConverter
  @Inject
  constructor() {
    fun apply(preferences: TomeSharedPreferences): String? {
      val hideCompleted = preferences.getHideCompleted()

      if (hideCompleted) {
        return "progress.bm90LWZpbmlzaGVk" // not-finished
      }

      return null
    }
  }
