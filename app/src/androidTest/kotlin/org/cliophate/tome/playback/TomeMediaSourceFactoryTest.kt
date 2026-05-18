package org.cliophate.tome.playback

import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import org.cliophate.tome.content.ExternalCoverProvider
import org.cliophate.tome.playback.service.FileClip
import org.cliophate.tome.playback.service.PlaybackService.Companion.CHAPTER_START_MS
import org.cliophate.tome.playback.service.PlaybackService.Companion.FILE_SEGMENTS
import org.cliophate.tome.playback.service.TomeMediaSourceFactory
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TomeMediaSourceFactoryTest {
  private lateinit var mediaSourceFactory: DefaultMediaSourceFactory
  private lateinit var tomeMediaSourceFactory: TomeMediaSourceFactory

  @Before
  fun setUp() {
    mediaSourceFactory = mockk(relaxed = true)
    tomeMediaSourceFactory = TomeMediaSourceFactory(mediaSourceFactory)
  }

  @Test
  fun no_exception_thrown_if_no_files() {
    val mediaSource =
      tomeMediaSourceFactory.createMediaSource(
        MediaItem
          .Builder()
          .setMediaId(TomeMediaSourceFactory.MediaId("book-id", 5).toString())
          .setRequestMetadata(
            MediaItem.RequestMetadata
              .Builder()
              .setExtras(bundleOf(FILE_SEGMENTS to arrayListOf<FileClip>()))
              .build(),
          ).setMediaMetadata(
            MediaMetadata
              .Builder()
              .setAlbumTitle("title")
              .setTitle("chapter")
              .setArtist("book")
              .setIsBrowsable(false)
              .setIsPlayable(true)
              .setArtworkUri(ExternalCoverProvider.coverUri("book-id"))
              .setMediaType(MediaMetadata.MEDIA_TYPE_AUDIO_BOOK_CHAPTER)
              .setExtras(bundleOf(CHAPTER_START_MS to (500 * 1000).toLong()))
              .build(),
          ).build(),
      )
    assertNotNull(mediaSource)
  }
}
