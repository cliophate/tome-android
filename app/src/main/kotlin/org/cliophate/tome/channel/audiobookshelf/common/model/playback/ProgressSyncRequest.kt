package org.cliophate.tome.channel.audiobookshelf.common.model.playback

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class ProgressSyncRequest(
  val timeListened: Int,
  val currentTime: Double,
)
