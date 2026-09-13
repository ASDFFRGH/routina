package com.routina.app.feature.update

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class AvailableUpdate(
    val version: String,
    val apkUrl: String,
)

/** Queries the public GitHub release endpoint. All errors deliberately become no update. */
class AppUpdateChecker(
    private val currentVersion: String,
    private val responseLoader: suspend () -> String = ::loadLatestRelease,
) {
    suspend fun findAvailableUpdate(): AvailableUpdate? = runCatching {
        withContext(Dispatchers.IO) {
            parseAvailableUpdate(responseLoader(), currentVersion)
        }
    }.getOrNull()

    companion object {
        private const val LATEST_RELEASE_URL =
            "https://api.github.com/repos/ASDFFRGH/routina/releases/latest"

        fun parseAvailableUpdate(response: String, currentVersion: String): AvailableUpdate? {
            val release = Json { ignoreUnknownKeys = true }.decodeFromString<GitHubRelease>(response)
            val releaseVersion = StableVersion.parse(release.tagName) ?: return null
            val installedVersion = StableVersion.parse(currentVersion) ?: return null
            if (releaseVersion <= installedVersion) return null

            val apkUrl = release.assets.firstOrNull { asset ->
                asset.name.endsWith(".apk", ignoreCase = true) &&
                    asset.downloadUrl.startsWith("https://github.com/ASDFFRGH/routina/releases/download/")
            }?.downloadUrl ?: return null

            return AvailableUpdate(version = releaseVersion.toString(), apkUrl = apkUrl)
        }

        private fun loadLatestRelease(): String {
            val connection = (URL(LATEST_RELEASE_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = 5_000
                readTimeout = 5_000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "Routina-Android")
            }
            return try {
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw IllegalStateException("GitHub release request failed: ${connection.responseCode}")
                }
                connection.inputStream.bufferedReader().use { reader -> reader.readText() }
            } finally {
                connection.disconnect()
            }
        }
    }
}

@Serializable
private data class GitHubRelease(
    @kotlinx.serialization.SerialName("tag_name") val tagName: String,
    val assets: List<GitHubAsset>,
)

@Serializable
private data class GitHubAsset(
    val name: String,
    @kotlinx.serialization.SerialName("browser_download_url") val downloadUrl: String,
)

data class StableVersion(val major: Int, val minor: Int, val patch: Int) : Comparable<StableVersion> {
    override fun compareTo(other: StableVersion): Int = compareValuesBy(this, other, StableVersion::major, StableVersion::minor, StableVersion::patch)

    override fun toString(): String = "$major.$minor.$patch"

    companion object {
        private val pattern = Regex("^v?(\\d+)\\.(\\d+)\\.(\\d+)$")

        fun parse(value: String): StableVersion? {
            val match = pattern.matchEntire(value) ?: return null
            return runCatching {
                StableVersion(
                    major = match.groupValues[1].toInt(),
                    minor = match.groupValues[2].toInt(),
                    patch = match.groupValues[3].toInt(),
                )
            }.getOrNull()
        }
    }
}
