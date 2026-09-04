package com.ir0.iptv.domain.customization

import com.ir0.iptv.domain.classification.ContentType

data class ContentCustomization(
    val hidden: Boolean = false,
    val favorite: Boolean = false,
    val manualType: ContentType? = null
)
