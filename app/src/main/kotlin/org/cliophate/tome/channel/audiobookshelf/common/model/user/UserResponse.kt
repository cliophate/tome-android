package org.cliophate.tome.channel.audiobookshelf.common.model.user

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import org.cliophate.tome.channel.audiobookshelf.common.model.MediaProgressResponse

@Keep
@JsonClass(generateAdapter = true)
data class UserResponse(
  val mediaProgress: List<MediaProgressResponse>?,
)
