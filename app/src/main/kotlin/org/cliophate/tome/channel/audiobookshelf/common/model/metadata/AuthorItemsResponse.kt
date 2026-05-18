package org.cliophate.tome.channel.audiobookshelf.common.model.metadata

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import org.cliophate.tome.channel.audiobookshelf.library.model.LibraryItem

@Keep
@JsonClass(generateAdapter = true)
data class AuthorItemsResponse(
  val libraryItems: List<LibraryItem>,
)
