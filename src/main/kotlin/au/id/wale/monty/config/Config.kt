package au.id.wale.monty.config

data class DiscordConfig(
    val token: String,
    val prefixes: List<String>,
    val owners: List<Long>
)

data class DatabaseConfig(
    val username: String,
    val password: String,
    val url: String
)

data class Config(
    val discord: DiscordConfig,
    val db: DatabaseConfig
)