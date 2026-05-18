package org.cliophate.tome.playback.service

import android.net.Uri

fun apply(
  mediaItemId: String,
  fileId: String,
): Uri =
  Uri
    .Builder()
    .scheme("tome")
    .appendPath(mediaItemId)
    .appendPath(fileId)
    .build()

fun unapply(uri: Uri): Pair<String, String>? {
  if (uri.scheme != "tome") return null

  val segments = uri.pathSegments
  if (segments.size != 2) return null

  val mediaItemId = segments[0].takeIf { it.isNotEmpty() } ?: return null
  val fileId = segments[1].takeIf { it.isNotEmpty() } ?: return null

  return mediaItemId to fileId
}
