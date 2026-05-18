package org.cliophate.tome.channel.audiobookshelf.common.model.bookmark

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class BookmarkRequest(
  val time: Int,
  val title: String,
)
