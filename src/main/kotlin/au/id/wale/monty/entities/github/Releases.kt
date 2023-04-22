package au.id.wale.monty.entities.github

import java.util.*
import kotlin.collections.ArrayList

typealias Releases = ArrayList<Release>

data class Release (
    val url: String,
    val assetsURL: String,
    val uploadURL: String,
    val htmlURL: String,
    val id: Long,
    val author: Author,
    val nodeID: String,
    val tagName: String,
    val targetCommitish: String,
    val name: String,
    val draft: Boolean,
    val prerelease: Boolean,
    val createdAt: Date,
    val publishedAt: Date,
    val assets: List<Asset>,
    val tarballURL: String,
    val zipballURL: String,
    val body: String
)

data class Asset (
    val url: String,
    val id: Long,
    val nodeID: String,
    val name: String,
    val label: String,
    val uploader: Author,
    val contentType: String,
    val state: String,
    val size: Long,
    val downloadCount: Long,
    val createdAt: Date,
    val updatedAt: Date,
    val browserDownloadURL: String
)

data class Author (
    val login: String,
    val id: Long,
    val nodeID: String,
    val avatarURL: String,
    val gravatarID: String,
    val url: String,
    val htmlURL: String,
    val followersURL: String,
    val followingURL: String,
    val gistsURL: String,
    val starredURL: String,
    val subscriptionsURL: String,
    val organizationsURL: String,
    val reposURL: String,
    val eventsURL: String,
    val receivedEventsURL: String,
    val type: String,
    val siteAdmin: Boolean
)
