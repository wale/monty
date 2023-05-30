package au.id.wale.monty.cogs

import au.id.wale.monty.Constants
import au.id.wale.monty.config
import au.id.wale.monty.entities.ridespace.RideSpaceDeparture
import au.id.wale.monty.entities.ridespace.RideSpaceTrip
import au.id.wale.monty.util.await
import au.id.wale.monty.util.gson.GsonZuluDateAdapter
import au.id.wale.monty.util.plusAssign
import au.id.wale.monty.util.toPTVUrl
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.jagrosh.jdautilities.menu.ButtonEmbedPaginator
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.Cooldown
import me.devoxin.flight.api.annotations.Describe
import me.devoxin.flight.api.annotations.Greedy
import me.devoxin.flight.api.context.Context
import me.devoxin.flight.api.entities.BucketType
import me.devoxin.flight.api.entities.Cog
import net.dv8tion.jda.api.EmbedBuilder
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class PTV : Cog {
    override fun name(): String = "PTV"

    private val client = OkHttpClient.Builder().readTimeout(1, TimeUnit.MINUTES).build()
    private val LOG = LoggerFactory.getLogger(this::class.java)

    @Command(description = "Gets up-to-date train schedules on the Metro Trains Melbourne network",
        aliases = ["mtm"])
    @Cooldown(duration = 1, timeUnit = TimeUnit.MINUTES, bucket = BucketType.USER)
    suspend fun metro(ctx: Context, @Greedy @Describe("The train station") station: String) {
        // Search for the station first, to retrieve its ID
        val path = "v3/search/$station" // route_types = 0 == [Metro] Train stops
        val urlBuilder = HttpUrl.Builder()
            .scheme("https")
            .host("timetableapi.ptv.vic.gov.au")
            .addPathSegments(path)
            .addQueryParameter("route_types", "0")
            .addQueryParameter("match_stop_by_suburb", "false")

        val url = toPTVUrl(config.api.ptv.developerId, config.api.ptv.developerKey, urlBuilder)
        val request = Request.Builder()
            .url(url)
            .build()
        val response = client.newCall(request).await()
        val status = response.code
        val gson = GsonBuilder().registerTypeAdapter(Date::class.java, GsonZuluDateAdapter()).create()

        if (status != 200) {
            ctx.send("**Error**: Failed to make request to PTV, as it returned a status code of ${status}.")
            LOG.error("**Error**: Failed to make request to PTV. ${response.request.url}")
            return
        } else {
            val body = response.body?.string()
            if(body.isNullOrEmpty()) {
                ctx.send("**Error**: Malformed request to PTV, as the request body is null.")
                return
            } else {
                val json = JSONObject(body)
                val stops = json.getJSONArray("stops")
                if (stops.isEmpty) {
                    ctx.send("**Error**: No stops with the name of $station.")
                    return
                } else {
                    val id = stops.getJSONObject(0).getInt("stop_id")
                    // With all checks passing, we need to get the departures.
                    val depRequest = Request.Builder()
                        .url("https://ridespace.coronavirus.vic.gov.au/api/stop/Train/PTV$id/trips")
                        .build()
                    val depResponse = client.newCall(depRequest).await()
                    val depStatus = depResponse.code
                    if (depStatus != 200) {
                        ctx.send("**Error**: Failed to make request to RideSpace, as it returned a status code of ${status}.")
                        LOG.error("Failed to make request to RideSpace. \n $body")
                    } else {
                        val depBody = depResponse.body?.string()
                        if (depBody.isNullOrEmpty()) {
                            ctx.send("**Error**: Malformed request to PTV, as the request body is null.")
                            return
                        } else {
                            val departureType = object : TypeToken<ArrayList<RideSpaceDeparture>>() {}.type
                            val departureList = gson.fromJson<ArrayList<RideSpaceDeparture>>(depBody, departureType)

                            val paginator = ButtonEmbedPaginator.Builder()
                                .setEventWaiter(Constants.eventWaiter)
                                .setTimeout(2, TimeUnit.MINUTES)

                            for ((i, destination) in departureList.withIndex()) {
                                val embed = EmbedBuilder()
                                embed.setColor(0x0072CE)
                                embed.setThumbnail("https://upload.wikimedia.org/wikipedia/commons/7/73/Metro_Trains_Melbourne_Logo.png")
                                embed.setTitle("Trains from ${station.replaceFirstChar { it.uppercase() }}")
                                embed.setDescription("Trains to **${destination.name}**.")
                                for (trip in destination.trips) {
                                    embed.addField(trip.label, formatTrip(trip), false)
                                }
                                embed.setFooter("Retrieved from RideSpace", "https://ridespace.coronavirus.vic.gov.au/assets/icons/apple-touch-icon.png")
                                embed.setTimestamp(OffsetDateTime.now())
                                paginator.addItems(embed.build())
                            }
                            paginator.build().paginate(ctx.guildChannel ?: ctx.messageChannel, 0)
                        }
                    }
                }
            }
        }
    }

    private fun formatTrip(trip: RideSpaceTrip): String {
        val format = StringBuilder()
        format += if (trip.arrivalLabel == "SCHEDULED") {
            val minutesBefore = ChronoUnit.MINUTES.between(
                Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.of("Australia/Melbourne")),
                Instant.ofEpochMilli(trip.departureTime.time).atZone(ZoneId.of("Australia/Melbourne")),
                )
            "**Scheduled in**: _${minutesBefore} minutes_"
        } else {
            "**Departing in**: <t:${trip.departureTime.time / 1000}:R>"
        }

        format += if (trip.platform != 0) {
            "**Platform**: ${trip.platform}"
        } else {
            "**Platform**: Unknown"
        }

        return format.toString()
    }
}