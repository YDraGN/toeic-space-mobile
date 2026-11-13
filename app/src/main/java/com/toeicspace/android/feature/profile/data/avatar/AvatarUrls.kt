package com.toeicspace.android.feature.profile.data.avatar

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

private const val AVATAR_KEY_PREFIX = "avatars/"

/**
 * The public address of an uploaded avatar, built the same way as the web client: the object key
 * is taken from the presigned [uploadUrl] (everything from `avatars/`, which drops the bucket
 * prefix), served from [cdnBaseUrl]. [version] busts image caches, since every upload overwrites
 * the same key. Without a CDN base the upload URL's own origin is used, as the web does.
 */
internal fun publicAvatarUrl(
    uploadUrl: String,
    cdnBaseUrl: String,
    version: Long,
): String {
    val parsed = uploadUrl.toHttpUrlOrNull()
    val path = parsed?.encodedPath ?: uploadUrl.substringBefore('?')
    val keyStart = path.indexOf(AVATAR_KEY_PREFIX)
    val key = if (keyStart >= 0) path.substring(keyStart) else path.trimStart('/')
    val base =
        cdnBaseUrl.trimEnd('/').ifEmpty {
            parsed
                ?.newBuilder()
                ?.encodedPath("/")
                ?.query(null)
                ?.build()
                ?.toString()
                ?.trimEnd('/')
                .orEmpty()
        }
    return "$base/$key?v=$version"
}

/** Where uploaded avatars are served from; see `AVATAR_CDN_URL` in local.properties. */
data class AvatarStorageConfig(
    val cdnBaseUrl: String,
)
