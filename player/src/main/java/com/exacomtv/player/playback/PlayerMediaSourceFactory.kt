@file:androidx.media3.common.util.UnstableApi

package com.exacomtv.player.playback

import android.net.Uri
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.ts.TsExtractor
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.exacomtv.domain.model.DrmInfo
import com.exacomtv.domain.model.DrmScheme
import com.exacomtv.domain.model.VodHttpProtocolMode
import com.exacomtv.domain.model.StreamInfo
import com.exacomtv.domain.model.StreamType
import java.util.UUID

/** Explicit CEA-608 and CEA-708 closed caption formats for MPEG-TS streams. */
private val TS_SUBTITLE_FORMATS: List<Format> = listOf(
    Format.Builder().setSampleMimeType(MimeTypes.APPLICATION_CEA608).build(),
    Format.Builder()
        .setSampleMimeType(MimeTypes.APPLICATION_CEA708)
        .setCodecs("cea-708")
        .setAccessibilityChannel(1)
        .build()
)

internal fun liveMpegTsExtractorsFactory(): DefaultExtractorsFactory =
    DefaultExtractorsFactory()
        // MODE_HLS makes TsExtractor.seek() unconditionally throw
        // IllegalStateException (Preconditions.checkState(mode != MODE_HLS)),
        // regardless of the requested position. ProgressiveMediaPeriod's
        // ExtractingLoadable calls extractor.seek(loadable.position, ...)
        // whenever it (re)starts reading a loadable, so with MODE_HLS every
        // load attempt for a live .ts stream fails immediately with an
        // UnexpectedLoaderException, forcing a full reconnect loop
        // ("Retrying TS x/10" / repeated "new client started streaming" on the
        // Xtream server). MODE_MULTI_PMT (the default) allows seek() to
        // succeed for position 0 / matching positions, so playback can
        // actually proceed and resume normally.
        .setTsExtractorMode(TsExtractor.MODE_MULTI_PMT)
        .setTsExtractorFlags(
            DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS
                or DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES
                or DefaultTsPayloadReaderFactory.FLAG_IGNORE_SPLICE_INFO_STREAM
        )
        .setTsSubtitleFormats(TS_SUBTITLE_FORMATS)

@UnstableApi
class PlayerMediaSourceFactory(
    private val dataSourceFactoryProvider: PlayerDataSourceFactoryProvider
) {
    fun create(
        streamInfo: StreamInfo,
        resolvedStreamType: ResolvedStreamType,
        retryPolicy: PlayerRetryPolicy,
        vodHttpProtocolMode: VodHttpProtocolMode = VodHttpProtocolMode.COMPATIBILITY_HTTP1,
        preload: Boolean = false
    ): Pair<PlayerTimeoutProfile, MediaSource> {
        val (timeoutProfile, dataSourceFactory) = dataSourceFactoryProvider.createFactory(
            streamInfo = streamInfo,
            resolvedStreamType = resolvedStreamType,
            vodHttpProtocolMode = vodHttpProtocolMode,
            preload = preload
        )
        val mediaItem = buildMediaItem(streamInfo)
        val mediaSource = when {
            streamInfo.streamType == StreamType.RTSP || resolvedStreamType == ResolvedStreamType.RTSP ->
                RtspMediaSource.Factory().createMediaSource(mediaItem)
            resolvedStreamType == ResolvedStreamType.HLS -> HlsMediaSource.Factory(dataSourceFactory)
                .setAllowChunklessPreparation(true)
                .setLoadErrorHandlingPolicy(retryPolicy)
                .createMediaSource(mediaItem)

            resolvedStreamType == ResolvedStreamType.DASH -> DashMediaSource.Factory(dataSourceFactory)
                .setLoadErrorHandlingPolicy(retryPolicy)
                .createMediaSource(mediaItem)

            resolvedStreamType == ResolvedStreamType.SMOOTH_STREAMING -> SsMediaSource.Factory(dataSourceFactory)
                .apply {
                    if (streamInfo.drmInfo?.scheme == DrmScheme.CLEARKEY) {
                        setManifestParser(ClearKeySmoothStreamingManifestParser())
                    }
                }
                .setLoadErrorHandlingPolicy(retryPolicy)
                .createMediaSource(mediaItem)

            resolvedStreamType == ResolvedStreamType.MPEG_TS_LIVE -> ProgressiveMediaSource.Factory(
                dataSourceFactory,
                liveMpegTsExtractorsFactory()
            )
                .setLoadErrorHandlingPolicy(retryPolicy)
                .createMediaSource(mediaItem)

            resolvedStreamType == ResolvedStreamType.PROGRESSIVE -> ProgressiveMediaSource.Factory(dataSourceFactory)
                .setLoadErrorHandlingPolicy(retryPolicy)
                .createMediaSource(mediaItem)

            else -> DefaultMediaSourceFactory(
                dataSourceFactory,
                DefaultExtractorsFactory()
                    .setTsExtractorFlags(
                        DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS
                            or DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES
                            or DefaultTsPayloadReaderFactory.FLAG_IGNORE_SPLICE_INFO_STREAM
                    )
                    .setTsSubtitleFormats(TS_SUBTITLE_FORMATS)
            )
                .setLoadErrorHandlingPolicy(retryPolicy)
                .createMediaSource(mediaItem)
        }

        Log.i(
            TAG,
            "media-source streamType=$resolvedStreamType timeout=$timeoutProfile source=${mediaSource::class.java.simpleName} target=${PlaybackLogSanitizer.sanitizeUrl(streamInfo.url)}"
        )
        return timeoutProfile to mediaSource
    }

    private fun buildMediaItem(streamInfo: StreamInfo): MediaItem {
        return MediaItem.Builder()
            .setUri(Uri.parse(streamInfo.url))
            .setMediaId(mediaIdFor(streamInfo))
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(streamInfo.title)
                    .build()
            )
            .apply {
                streamInfo.drmInfo?.let { drmInfo ->
                    setDrmConfiguration(
                        MediaItem.DrmConfiguration.Builder(drmInfo.scheme.toUuid())
                            .setLicenseUri(drmInfo.licenseUrl)
                            .setLicenseRequestHeaders(drmInfo.headers)
                            .setMultiSession(drmInfo.multiSession)
                            .setForceDefaultLicenseUri(drmInfo.forceDefaultLicenseUrl)
                            .setPlayClearContentWithoutKey(drmInfo.playClearContentWithoutKey)
                            .build()
                    )
                }
            }
            .build()
    }

    fun mediaIdFor(streamInfo: StreamInfo): String {
        val drmKey = streamInfo.drmInfo?.let { "${it.scheme}:${it.licenseUrl}" }.orEmpty()
        return stableHash("${streamInfo.url}|${streamInfo.title.orEmpty()}|$drmKey")
    }

    private fun DrmScheme.toUuid(): UUID = when (this) {
        DrmScheme.WIDEVINE -> C.WIDEVINE_UUID
        DrmScheme.PLAYREADY -> C.PLAYREADY_UUID
        DrmScheme.CLEARKEY -> C.CLEARKEY_UUID
    }

    companion object {
        private const val TAG = "PlayerMediaSourceFactory"
    }
}
