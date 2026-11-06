package com.example.arcticplex.data

data class PlexResponse(
    val MediaContainer: MediaContainer?
)

data class MediaContainer(
    val size: Int?,
    val Directory: List<PlexDirectory>?,
    val Metadata: List<PlexMediaItem>?
)

data class PlexDirectory(
    val key: String,
    val title: String,
    val type: String
)

data class PlexMediaItem(
    val ratingKey: String,
    val title: String,
    val year: Int?,
    val summary: String?,
    val rating: Double?,
    val contentRating: String?,
    val duration: Long?,
    val viewOffset: Long?,
    val thumb: String?,
    val art: String?,
    val type: String?,
    val Media: List<MediaPartContainer>?
) {
    fun getFormattedDuration(): String {
        val totalMinutes = (duration ?: 0L) / 60000
        if (totalMinutes <= 0) return ""
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    }
}

data class MediaPartContainer(
    val videoResolution: String?,
    val audioCodec: String?,
    val Part: List<MediaPart>?
)

data class MediaPart(
    val id: Int?,
    val key: String,
    val duration: Long?,
    val container: String?
)
