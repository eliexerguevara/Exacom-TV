package com.exacomtv.app.ui.model

import com.exacomtv.domain.model.Channel

fun Channel.guideLookupKey(): String? {
    return epgChannelId?.trim()?.takeIf { it.isNotEmpty() }
        ?: streamId.takeIf { it > 0L }?.toString()
}
