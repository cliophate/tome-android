package org.cliophate.tome.channel.audiobookshelf.common.converter

import org.cliophate.tome.channel.audiobookshelf.common.model.playback.PlaybackSessionResponse
import org.cliophate.tome.lib.domain.PlaybackSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackSessionResponseConverter
  @Inject
  constructor() {
    fun apply(response: PlaybackSessionResponse): PlaybackSession =
      PlaybackSession.remote(
        sessionId = response.id,
        itemId = response.libraryItemId,
      )
  }
