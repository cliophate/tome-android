package org.cliophate.tome.channel.common

interface ChannelProvider {
  fun provideMediaChannel(): MediaChannel

  fun provideChannelAuth(): ChannelAuthService
}
