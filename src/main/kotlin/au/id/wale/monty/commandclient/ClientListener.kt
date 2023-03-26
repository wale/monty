package au.id.wale.monty.commandclient

import au.id.wale.monty.util.uploadToHastebin
import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.context.Context
import me.devoxin.flight.api.context.MessageContext
import me.devoxin.flight.api.entities.CheckType
import me.devoxin.flight.api.exceptions.BadArgument
import me.devoxin.flight.api.hooks.CommandEventAdapter
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

class ClientListener : CommandEventAdapter {
    private val LOG = LoggerFactory.getLogger(this::class.java)
    private val client = OkHttpClient()

    override fun onBadArgument(ctx: Context, command: CommandFunction, error: BadArgument) {
        ctx.send("You have provided a bad argument on the command ${command.name}. \n" +
                "```\n${error.message}\n```")
        LOG.error("Bad argument provided on command ${command.name}.", error)
    }

    override fun onBotMissingPermissions(ctx: MessageContext, command: CommandFunction, permissions: List<Permission>) {
        ctx.send("Monty is missing the following permissions `${permissions.joinToString(", ")}`.")
        LOG.error("Monty is missing permissions on the command ${command.name}: ${permissions.joinToString(",")}")
    }

    override fun onCheckFailed(ctx: Context, command: CommandFunction, checkType: CheckType) {
        when (checkType) {
            CheckType.DEVELOPER_CHECK -> {
                LOG.error("Developer command (${command.name}) was attempted to be run by ${ctx.author}.")
            }
            CheckType.GUILD_CHECK -> {
                ctx.send("This command is only supported within guilds.")
                LOG.error("Guild-only command (${command.name}) was attempted to be run by ${ctx.author}.")
            }
            CheckType.NSFW_CHECK -> {
                LOG.error("NSFW-only command (${command.name}) was attempted to be run by ${ctx.author}.")
            }
            CheckType.EXECUTION_CONTEXT -> {
                ctx.send("This command is only supported with the context of `${command.contextType}`.")
                LOG.error("Wrong context type used for ${command.name}, which only supports ${command.contextType}.")
            }
        }
    }

    override fun onCommandCooldown(ctx: Context, command: CommandFunction, cooldown: Long) {
        val dateformat = SimpleDateFormat("mm 'minutes' ss 'seconds'")
        val date = Date(cooldown)
        ctx.send("The command ${command.name} is on cooldown. You have ${dateformat.format(date)} left.")
    }

    override fun onCommandError(ctx: Context, command: CommandFunction, error: Throwable) {
        if(error.stackTraceToString().length > 2000) {
            val key = error.stackTraceToString().uploadToHastebin()
            ctx.send("The command ${command.name} encountered an error: **https://haste.erisa.uk/${key}**")
        } else {
            ctx.send("The command ${command.name} encountered an error. \n " +
                    "```\n${error.stackTraceToString()}\n```")
            LOG.error("The command ${command.name} failed to execute.", error)
        }
    }

    override fun onCommandPostInvoke(ctx: Context, command: CommandFunction, failed: Boolean) {
        if (failed) {
            LOG.error("The command ${command.name} failed to execute. See the following log.")
        }
    }

    override fun onCommandPreInvoke(ctx: Context, command: CommandFunction): Boolean {
        LOG.info("User ${ctx.author} ran command ${command.name}.")
        return true
    }

    override fun onInternalError(error: Throwable) {
        LOG.error("Monty has encountered a command client-internal error.", error)
    }

    override fun onParseError(ctx: Context, command: CommandFunction, error: Throwable) {
        if(error.stackTraceToString().length > 2000) {
            val key = error.stackTraceToString().uploadToHastebin()
            ctx.send("The command ${command.name} failed to parse: **https://haste.erisa.uk/${key}**")
        } else {
            ctx.send("The command ${command.name} failed to parse. \n " +
                    "```\n${error.stackTraceToString()}\n```")
            LOG.error("The command ${command.name} failed to parse.", error)
        }
    }

    override fun onUnknownCommand(event: MessageReceivedEvent, command: String, args: List<String>) {
        return
    }

    override fun onUserMissingPermissions(
        ctx: MessageContext,
        command: CommandFunction,
        permissions: List<Permission>
    ) {
        ctx.send("You are missing the following permissions to run ${command.name}: `${permissions.joinToString(", ")}`")
        LOG.error("User ${ctx.author.id} is missing permissions on the command ${command.name}: ${permissions.joinToString(",")}")
    }
}