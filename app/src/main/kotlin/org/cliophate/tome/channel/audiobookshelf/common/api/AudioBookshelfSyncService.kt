package org.cliophate.tome.channel.audiobookshelf.common.api

import org.cliophate.tome.channel.common.OperationResult
import org.cliophate.tome.lib.domain.PlaybackProgress

interface AudioBookshelfSyncService {
  suspend fun syncProgress(
    itemId: String,
    progress: PlaybackProgress,
  ): OperationResult<Unit>
}
