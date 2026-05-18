package org.cliophate.tome.channel.audiobookshelf

import org.cliophate.tome.channel.audiobookshelf.common.api.AudiobookshelfAuthService
import org.cliophate.tome.channel.audiobookshelf.library.LibraryAudiobookshelfChannel
import org.cliophate.tome.channel.audiobookshelf.podcast.PodcastAudiobookshelfChannel
import org.cliophate.tome.channel.common.ChannelAuthService
import org.cliophate.tome.channel.common.ChannelProvider
import org.cliophate.tome.channel.common.MediaChannel
import org.cliophate.tome.lib.domain.LibraryType
import org.cliophate.tome.persistence.preferences.TomeSharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudiobookshelfChannelProvider
  @Inject
  constructor(
    private val podcastAudiobookshelfChannel: PodcastAudiobookshelfChannel,
    private val libraryAudiobookshelfChannel: LibraryAudiobookshelfChannel,
    private val audiobookshelfAuthService: AudiobookshelfAuthService,
    private val sharedPreferences: TomeSharedPreferences,
  ) : ChannelProvider {
    override fun provideMediaChannel(): MediaChannel {
      val libraryType =
        sharedPreferences
          .getPreferredLibrary()
          ?.type
          ?: LibraryType.UNKNOWN

      return when (libraryType) {
        LibraryType.LIBRARY -> libraryAudiobookshelfChannel
        LibraryType.PODCAST -> podcastAudiobookshelfChannel
        LibraryType.UNKNOWN -> libraryAudiobookshelfChannel
      }
    }

    override fun provideChannelAuth(): ChannelAuthService = audiobookshelfAuthService
  }
