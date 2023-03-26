package au.id.wale.monty.cogs

import au.id.wale.monty.Constants
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.jagrosh.jdautilities.menu.ButtonEmbedPaginator
import me.devoxin.flight.api.CommandFunction
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.Describe
import me.devoxin.flight.api.annotations.Greedy
import me.devoxin.flight.api.context.Context
import me.devoxin.flight.api.context.ContextType
import me.devoxin.flight.api.entities.Cog
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.awt.Color
import java.util.concurrent.TimeUnit

class Info : Cog {
    override fun name(): String = "Info"

    @Command(description = "Provides help and description of the command.")
    fun help(ctx: Context, @Greedy @Describe("The command or cog to search for.") command: String?) {
        command?.let {
            val commands = ctx.commandClient.commands
            val cmd = commands.findCommandByName(it)
                ?: commands.findCommandByAlias(it)

            when {
                cmd != null -> buildCommandHelp(ctx, cmd)
                else -> commands.findCogByName(command)?.let { cog -> buildCogHelp(ctx, cog) }
            } ?: ctx.send("No command or cog found by that name.")
        } ?: buildPaginatedHelp(ctx)
    }

    private fun buildCogHelp(ctx: Context, cog: Cog) {
        val commands = ctx.commandClient.commands.findCommandsByCog(cog).filter { !it.properties.hidden }
        val embed = EmbedBuilder()
            .setTitle("Cog ${cog.name()} Help")
            .setColor(ctx.member?.color ?: Color.getColor("#6495ed"))

        for (cmd in commands) {
            embed.addField(cmd.name, cmd.properties.description, false)
        }

        if (ctx.contextType == ContextType.MESSAGE) {
            val context = ctx.asMessageContext!!
            context.send(MessageCreateData.fromEmbeds(embed.build()))
        } else {
            val context = ctx.asSlashContext!!
            context.send(MessageCreateData.fromEmbeds(embed.build()))
        }
    }

    private fun buildCommandHelp(ctx: Context, cmd: CommandFunction) {
        var args: String = ""
        for (arg in cmd.arguments) {
            var name: String = ""
            name = if (arg.greedy) {
                "...${cmd.name}"
            } else {
                cmd.name
            }

            args += if (arg.isNullable) {
                "[${name}]"
            } else {
                "<${name}>"
            }
        }
        if (ctx.contextType == ContextType.MESSAGE) {
            val context = ctx.asMessageContext!!
            context.send {
                setColor(ctx.member?.color ?: Color.getColor("#6495ed"))
                setTitle("`${cmd.name}` Help")
                addField("Arguments", args, false)
                addField("Aliases", cmd.properties.aliases.joinToString(", "), false)
                addField("Description", cmd.properties.description, false)
            }
        } else {
            val context = ctx.asSlashContext!!
            val embed = EmbedBuilder()
                .setColor(ctx.member?.color ?: Color.getColor("#6495ed"))
                .setTitle("`${cmd.name}` Help")
                .addField("Arguments", args, false)
                .addField("Aliases", cmd.properties.aliases.joinToString(", "), false)
                .addField("Description", cmd.properties.description, false)

            context.send(MessageCreateData.fromEmbeds(embed.build()))
        }
    }

    private fun buildPaginatedHelp(ctx: Context) {
        val commands = ctx.commandClient.commands.values.filter { !it.properties.hidden }
        val cogs = commands.groupBy { it.cog.name() }.mapValues { it.value.toSet() }

        val paginator = ButtonEmbedPaginator.Builder()
            .setUsers(ctx.author)
            .setEventWaiter(Constants.eventWaiter)
            .setTimeout(2, TimeUnit.MINUTES)

        for (entry in cogs.entries.sortedBy { it.key }) {
            val embed = EmbedBuilder()
                .setColor(ctx.member?.color ?: Color.getColor("#6495ed"))
                .setTitle("Group: ${entry.key}")

            for (cmd in entry.value.sortedBy { it.name }) {
                val description = cmd.properties.description
                embed.addField(cmd.name, description, false)
            }
            paginator.addItems(embed.build())
        }

        paginator.build().paginate(ctx.guildChannel ?: ctx.messageChannel, 0)
    }

    @Command(description = "Gets the bot's response time.")
    suspend fun ping(ctx: Context) {
        if (ctx.contextType == ContextType.MESSAGE) {
            val context = ctx.asMessageContext!!
            val start = System.currentTimeMillis()
            context.typingAsync {
                val end = System.currentTimeMillis()
                context.send("Websocket: `${end - start}ms`, " +
                        "REST: `${context.jda.restPing.complete()}ms`, " +
                        "Gateway: `${context.jda.gatewayPing}ms`")
            }
        }
        if (ctx.contextType == ContextType.SLASH) {
            val context = ctx.asSlashContext!!
            val start = System.currentTimeMillis()
            context.deferAsync(false).run {
                val end = System.currentTimeMillis()
                context.reply("Websocket: `${end - start}ms`, " +
                        "REST: `${context.jda.restPing.complete()}ms`, " +
                        "Gateway: `${context.jda.gatewayPing}ms`")
            }
        }
    }
}