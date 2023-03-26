package au.id.wale.monty

import au.id.wale.monty.commandclient.ClientListener
import au.id.wale.monty.config.Config
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import io.ebean.Database
import io.ebean.DatabaseFactory
import io.ebean.datasource.DataSourceConfig
import io.ebean.migration.MigrationConfig
import io.ebean.migration.MigrationRunner
import me.devoxin.flight.api.CommandClientBuilder
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent

import org.slf4j.LoggerFactory
import java.util.*

private var LOG = LoggerFactory.getLogger("Monty-Main")

lateinit var config: Config
lateinit var db: Database

fun main(args: Array<String>) {
    LOG.info("Starting bot...")

    LOG.info("Loading config...").run {
        config = ConfigLoaderBuilder.default()
            .addFileSource("monty.bot.toml")
            .build()
            .loadConfigOrThrow()
    }
    LOG.info("Connecting to database...").run {
        val datasource = DataSourceConfig()
            .setUsername(config.db.username)
            .setPassword(config.db.password)
            .setUrl(config.db.url)

        val dbConfig = io.ebean.config.DatabaseConfig()
        dbConfig.dataSourceConfig = datasource

        val migrationConfig = MigrationConfig()
        migrationConfig.dbUsername = config.db.username
        migrationConfig.dbPassword = config.db.password
        migrationConfig.dbUrl = config.db.url

        val migrationRunner = MigrationRunner(migrationConfig)

        LOG.info("Running migrations...").run {
            migrationRunner.run()
        }

        db = DatabaseFactory.create(dbConfig)
    }
    LOG.info("Creating client...").run {
        val commandClient = CommandClientBuilder()
            .setPrefixes(config.discord.prefixes)
            .configureDefaultHelpCommand {
                this.enabled = false
            }
            .setOwnerIds(*config.discord.owners.toLongArray())
            .setAllowMentionPrefix(true)
            .addEventListeners(ClientListener())
            .registerDefaultParsers()
            .build()

        commandClient.commands.register("au.id.wale.monty.cogs")

        val jda = JDABuilder.create(config.discord.token, EnumSet.allOf(GatewayIntent::class.java))
            .addEventListeners(commandClient)
            .addEventListeners(Constants.eventWaiter)
            .build()

        jda.updateCommands().addCommands(commandClient.commands.toDiscordCommands()).queue()

        LOG.info("Connected as ${jda.selfUser.name}#${jda.selfUser.discriminator}!")
    }
}