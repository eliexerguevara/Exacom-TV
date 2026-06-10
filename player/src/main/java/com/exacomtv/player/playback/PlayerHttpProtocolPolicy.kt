package com.exacomtv.player.playback

import com.exacomtv.domain.model.VodHttpProtocolMode

object PlayerHttpProtocolPolicy {
    fun forceHttp1(
        resolvedStreamType: ResolvedStreamType,
        vodHttpProtocolMode: VodHttpProtocolMode
    ): Boolean {
        return resolvedStreamType == ResolvedStreamType.PROGRESSIVE &&
            vodHttpProtocolMode == VodHttpProtocolMode.COMPATIBILITY_HTTP1
    }
}
