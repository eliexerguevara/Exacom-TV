package com.exacomtv.player.playback

internal fun shouldPreservePlaybackStateForRetry(
    category: PlaybackErrorCategory,
    playbackStarted: Boolean,
    liveBufferingRecoveryArmed: Boolean
): Boolean =
    category != PlaybackErrorCategory.LIVE_WINDOW ||
        hasEffectivePlaybackStarted(
            playbackStarted = playbackStarted,
            liveBufferingRecoveryArmed = liveBufferingRecoveryArmed
        )

internal fun hasEffectivePlaybackStarted(
    playbackStarted: Boolean,
    liveBufferingRecoveryArmed: Boolean
): Boolean = playbackStarted || liveBufferingRecoveryArmed

internal fun shouldArmPlaybackStartedRecovery(
    preserveRetryState: Boolean,
    mediaChanged: Boolean,
    playbackStarted: Boolean,
    playbackStartedRecoveryArmed: Boolean
): Boolean =
    preserveRetryState &&
        !mediaChanged &&
        hasEffectivePlaybackStarted(
            playbackStarted = playbackStarted,
            liveBufferingRecoveryArmed = playbackStartedRecoveryArmed
        )

/**
 * Initial grace period before the stall detector starts evaluating buffering/ready stalls.
 *
 * Live MPEG-TS sources (ProgressiveMediaSource + MODE_MULTI_PMT) need to scan further into
 * the stream to locate the PAT/PMT and a usable keyframe than manifest-based live formats
 * (HLS/DASH/SmoothStreaming segments are already keyframe-aligned). Using the default
 * (HLS-tuned) grace period here causes the engine to treat normal initial buffering as a
 * stall, tear down and recreate the player, and never give the TS extractor enough
 * uninterrupted time to start producing frames - resulting in a permanent
 * BUFFERING/"Retrying TS x/10" loop with position stuck at -1.
 */
internal fun stallInitialGraceMsFor(resolvedStreamType: ResolvedStreamType): Long = when (resolvedStreamType) {
    ResolvedStreamType.MPEG_TS_LIVE -> 25_000L
    else -> 8_000L
}

/** See [stallInitialGraceMsFor]. */
internal fun bufferingStallThresholdMsFor(resolvedStreamType: ResolvedStreamType): Long = when (resolvedStreamType) {
    ResolvedStreamType.MPEG_TS_LIVE -> 25_000L
    else -> 8_000L
}
