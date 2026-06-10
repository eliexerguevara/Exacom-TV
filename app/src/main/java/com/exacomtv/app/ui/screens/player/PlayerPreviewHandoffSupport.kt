package com.exacomtv.app.ui.screens.player

import com.exacomtv.player.PlaybackState
import com.exacomtv.player.PlayerStats

internal fun shouldRenewAdoptedPreviewOnFullscreen(
    playbackState: PlaybackState,
    playerStats: PlayerStats
): Boolean = playbackState != PlaybackState.READY || playerStats.ttffMs <= 0L
