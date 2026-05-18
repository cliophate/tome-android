package org.cliophate.tome.channel.audiobookshelf.common.converter

import org.cliophate.tome.channel.audiobookshelf.common.model.connection.ConnectionInfoResponse
import org.cliophate.tome.channel.common.ConnectionInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionInfoResponseConverter
  @Inject
  constructor() {
    fun apply(response: ConnectionInfoResponse): ConnectionInfo =
      ConnectionInfo(
        username = response.user.username,
        serverVersion = response.serverSettings?.version,
        buildNumber = response.serverSettings?.buildNumber,
      )
  }
