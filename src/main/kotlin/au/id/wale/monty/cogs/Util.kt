package au.id.wale.monty.cogs

import au.id.wale.monty.Constants
import com.jagrosh.jdautilities.menu.ButtonEmbedPaginator
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.Describe
import me.devoxin.flight.api.annotations.Greedy
import me.devoxin.flight.api.context.Context
import me.devoxin.flight.api.context.ContextType
import me.devoxin.flight.api.entities.Cog
import net.dv8tion.jda.api.EmbedBuilder
import org.jsoup.Jsoup
import java.awt.Color
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

class Util : Cog {
    override fun name(): String = "Util"

    @Command(description = "Searches DuckDuckGo with a given query", aliases = ["google", "ddg"])
    fun search(ctx: Context, @Greedy @Describe("The search query.") query: String) {
        try {
            val document = Jsoup.connect("https://duckduckgo.com/html/?q=$query").get()
            val results = document.getElementById("links")?.getElementsByClass("results_links")

            if (results.isNullOrEmpty()) {
                ctx.send("**Error**: No results.")
                return
            }

            val ddgIconUrl = "https://duckduckgo.com/assets/icons/meta/DDG-icon_256x256.png"

            val embedPaginator = ButtonEmbedPaginator.Builder()
                .setEventWaiter(Constants.eventWaiter)
                .setTimeout(2, TimeUnit.MINUTES)
                .setUsers(ctx.author)

            for (result in results) {
                val title = result.getElementsByClass("links_main").first()!!.getElementsByTag("a").first()
                val embed = EmbedBuilder()
                embed.setColor(0xde5833)
                embed.setTitle(title!!.text())
                embed.setFooter("Retrieved from DuckDuckGo", ddgIconUrl)
                embed.setTimestamp(OffsetDateTime.now())
                embed.addField("URL", title.attr("href"), false)
                embed.addField("Description", result.getElementsByClass("result__snippet").first()!!.text(), false)
                embedPaginator.addItems(embed.build())
            }

            if (ctx.contextType == ContextType.MESSAGE) {
                val context = ctx.asMessageContext!!
                embedPaginator.build().paginate(context.guildChannel ?: context.messageChannel, 0)
            } else {
                val context = ctx.asSlashContext!!
                embedPaginator.build().paginate(context.guildChannel ?: context.messageChannel, 0)
            }
        } catch (e: Exception) {
            throw Exception(e)
        }
    }
}