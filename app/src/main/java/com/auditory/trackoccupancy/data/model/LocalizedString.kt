package com.auditory.trackoccupancy.data.model

import android.content.Context
import com.auditory.trackoccupancy.R
import com.google.gson.annotations.SerializedName

data class LocalizedString(
    @SerializedName("ru") val ru: String,
    @SerializedName("en") val en: String
) {
    fun getLocalizedValue(context: Context): String {
        val currentLanguage = context.resources.configuration.locales[0].language
        return when (currentLanguage) {
            "ru" -> ru
            else -> en
        }
    }
}
