package org.cliophate.tome.content.cache.persistent.converter

import org.cliophate.tome.content.cache.persistent.entity.MediaProgressEntity
import org.cliophate.tome.lib.domain.MediaProgress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaProgressEntityConverter
  @Inject
  constructor() {
    fun apply(entity: MediaProgressEntity): MediaProgress =
      MediaProgress(
        currentTime = entity.currentTime,
        isFinished = entity.isFinished,
        lastUpdate = entity.lastUpdate,
      )
  }
