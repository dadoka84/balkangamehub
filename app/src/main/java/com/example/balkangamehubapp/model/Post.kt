package com.example.balkangamehubapp.model

import com.google.gson.annotations.SerializedName

data class Post(
    val id: Int,

    val date: String?,
    @SerializedName("date_gmt")
    val dateGmt: String?,

    val title: Rendered,
    val excerpt: Rendered?,
    val content: Rendered?,

    val categories: List<Int>? = emptyList(),

    @SerializedName("yoast_head_json")
    val yoast: YoastHead?,

    @SerializedName("_embedded")
    val embedded: Embedded?
)

data class Rendered(
    val rendered: String
)

data class Embedded(

    // featured image
    @SerializedName("wp:featuredmedia")
    val media: List<Media>?,

    // categories with full data (name, slug, id)
    @SerializedName("wp:term")
    val terms: List<List<Term>>?
)


// ---------------------- MEDIA (IMAGES) ----------------------

data class Media(
    @SerializedName("source_url")
    val imageUrl: String?, // fallback

    @SerializedName("media_details")
    val mediaDetails: MediaDetails?
) {
    val bestImageUrl: String?
        get() = mediaDetails?.sizes?.mediumLarge?.sourceUrl
            ?: mediaDetails?.sizes?.medium?.sourceUrl
            ?: mediaDetails?.sizes?.large?.sourceUrl
            ?: imageUrl
}

data class MediaDetails(
    val sizes: Sizes?
)

data class Sizes(
    @SerializedName("medium_large")
    val mediumLarge: SizeInfo?,
    val medium: SizeInfo?,
    val large: SizeInfo?,
    val thumbnail: SizeInfo?
)

data class SizeInfo(
    @SerializedName("source_url")
    val sourceUrl: String
)


// ---------------------- CATEGORIES ----------------------

data class Term(
    val id: Int,
    val name: String,
    val slug: String,
    val taxonomy: String
)


// ---------------------- YOAST SEO ----------------------

data class YoastHead(
    @SerializedName("twitter_misc")
    val twitterMisc: TwitterMisc?
)

data class TwitterMisc(
    @SerializedName("Written by")
    val writtenBy: String?
)


// ---------------------- AUTHOR EXTENSION ----------------------

val Post.authorName: String
    get() = yoast?.twitterMisc?.writtenBy ?: "Balkan Game Hub Team"
