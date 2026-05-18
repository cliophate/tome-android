package org.cliophate.tome.channel.audiobookshelf.common.model.user

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class PersonalizedFeedResponse(
  val id: String,
  val label: String,
  val labelStringKey: String,
  val type: String,
  val entities: List<PersonalizedFeedItemResponse>,
  val category: String,
)

@Keep
@JsonClass(generateAdapter = true)
data class PersonalizedFeedItemResponse(
  val id: String,
  val libraryId: String,
  val media: PersonalizedFeedItemMediaResponse?,
)

@Keep
@JsonClass(generateAdapter = true)
data class PersonalizedFeedItemMediaResponse(
  val id: String,
  val metadata: PersonalizedFeedItemMetadataResponse,
)

@Keep
@JsonClass(generateAdapter = true)
data class PersonalizedFeedItemMetadataResponse(
  val title: String,
  val subtitle: String?,
  val authorName: String?,
)
