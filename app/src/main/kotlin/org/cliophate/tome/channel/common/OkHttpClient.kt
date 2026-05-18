package org.cliophate.tome.channel.common

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.cliophate.tome.common.withSslBypass
import org.cliophate.tome.common.withTrustedCertificates
import org.cliophate.tome.lib.domain.connection.ServerRequestHeader
import org.cliophate.tome.persistence.preferences.TomeSharedPreferences
import java.util.concurrent.TimeUnit

fun createOkHttpClient(
  requestHeaders: List<ServerRequestHeader>?,
  preferences: TomeSharedPreferences,
  context: Context,
): OkHttpClient {
  val clientCertAlias = preferences.getClientCertAlias()

  var builder = OkHttpClient.Builder()

  builder =
    when (preferences.getSslBypass()) {
      true -> builder.withSslBypass(context, clientCertAlias)
      false -> builder.withTrustedCertificates(context, clientCertAlias)
    }

  return builder
    .addInterceptor(loggingInterceptor())
    .addInterceptor { chain -> authInterceptor(chain, preferences, requestHeaders) }
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(120, TimeUnit.SECONDS)
    .build()
}

private fun loggingInterceptor() =
  HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.NONE
  }

private fun authInterceptor(
  chain: Interceptor.Chain,
  preferences: TomeSharedPreferences,
  requestHeaders: List<ServerRequestHeader>?,
): Response {
  val original: Request = chain.request()
  val requestBuilder: Request.Builder = original.newBuilder()

  val bearer = preferences.getAccessToken() ?: preferences.getToken()
  bearer?.let { requestBuilder.header("Authorization", "Bearer $it") }

  requestHeaders
    ?.filter { it.name.isNotEmpty() }
    ?.filter { it.value.isNotEmpty() }
    ?.forEach { requestBuilder.header(it.name, it.value) }

  return chain.proceed(requestBuilder.build())
}
