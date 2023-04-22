package au.id.wale.monty.cogs

import au.id.wale.monty.entities.github.Releases
import au.id.wale.monty.util.gson.GsonZuluDateAdapter
import au.id.wale.monty.util.uploadToHastebin
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.Describe
import me.devoxin.flight.api.annotations.Greedy
import me.devoxin.flight.api.context.MessageContext
import me.devoxin.flight.api.entities.Cog
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.BufferedSink
import okio.Okio
import okio.buffer
import okio.sink
import org.slf4j.LoggerFactory
import java.awt.Color
import java.io.File
import java.io.IOException
import java.util.*
import javax.script.ScriptEngineManager
import kotlin.system.exitProcess


class Developer : Cog {
    override fun name(): String = "Developer"

    private val client = OkHttpClient()
    private val gson = GsonBuilder().registerTypeAdapter(Date::class.java, GsonZuluDateAdapter()).create()

    private val LOG = LoggerFactory.getLogger(this::class.java)

    @Command(description = "Evaluates arbitrary code.", developerOnly = true)
    fun eval(ctx: MessageContext, @Greedy @Describe("Arbitrary Kotlin code") code: String) {
        val cleanCode = code.replace("```", "```")
            .replace("```kt", "")
            .trim()

        val engine = ScriptEngineManager().getEngineByExtension("kts")
        engine.put("ctx", ctx)
        engine.put("guild", ctx.guild)
        engine.put("jda", ctx.jda)

        val start = System.currentTimeMillis()
        try {
            val result = engine.eval(cleanCode)
            if (result == null) {
                ctx.send {
                    val end = System.currentTimeMillis()
                    setTitle("Evaluated!")
                    setColor(Color.GREEN)
                    addField("Result", "<no output>", false)
                    setFooter("Evaluated in ${end - start}ms.")
                }
                return
            }

            if (result.toString().length > 1024) {
                val key = result.toString().uploadToHastebin()
                ctx.send {
                    val end = System.currentTimeMillis()
                    setTitle("Evaluated!")
                    setColor(Color.GREEN)
                    addField("Result", "https://haste.erisa.uk/$key", false)
                    setFooter("Evaluated in ${end - start}ms.")
                }
                return
            } else {
                ctx.send {
                    val end = System.currentTimeMillis()
                    setTitle("Evaluated!")
                    setColor(Color.GREEN)
                    addField("Result", "```kt\n${result}\n```", false)
                    setFooter("Evaluated in ${end - start}ms.")
                }
                return
            }
        } catch (e: Exception) {
            if (e.stackTraceToString().length > 1024) {
                val key = e.stackTraceToString().uploadToHastebin()
                ctx.send {
                    val end = System.currentTimeMillis()
                    setTitle("Errored!")
                    setColor(Color.RED)
                    addField("Message", "https://haste.erisa.uk/$key", false)
                    setFooter("Evaluated in ${end - start}ms.")
                }
                return
            } else {
                ctx.send {
                    val end = System.currentTimeMillis()
                    setTitle("Errored!")
                    setColor(Color.RED)
                    addField("Message", "```\n${e.stackTraceToString()}\n```", false)
                    setFooter("Evaluated in ${end - start}ms.")
                }
                return
            }
        }
    }

    @Command(description = "Hot-updates the Monty JAR to the latest build.", developerOnly = true)
    fun hotswitch(ctx: MessageContext) {
        // This dynamically grabs the location of the JAR, but there are some caveats:
        // This might not work in IntelliJ's runner.
        // Some operating systems may implement a file lock on the JAR file, e.g. Windows.
        // todo: actually check for a FS lock
        val jarFile = File(this::class.java.protectionDomain.codeSource.location.toURI())

        // Get the set of releases on the GitHub repository and filter.
        val request = Request.Builder()
            .url("https://api.github.com/repos/wale/monty/releases")
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string()
        if(body.isNullOrEmpty()) {
            ctx.send("**Error**: Malformed request to GitHub when getting the list of releases. (Empty body)")
        } else {
            val releasesType = object : TypeToken<Releases>() {}.type
            val releaseList = gson.fromJson<Releases>(body, releasesType)
            val releaseDates = arrayListOf<Date>()
            // Get the latest release, we will currently ignore nightlies
            for (release in releaseList) {
                if(!release.prerelease) {
                    releaseDates.add(release.publishedAt)
                } else {
                    continue
                }
            }
            val latestRelease = releaseList.first { it.publishedAt == Collections.max(releaseDates) }

            // Now download the latest release
            ctx.send("Downloading latest release...").run {
                val downloadRequest = Request.Builder()
                    .url(latestRelease.assets.first().browserDownloadURL)
                    .build()
                val downloadResponse = client.newCall(downloadRequest).execute()

                if (!downloadResponse.isSuccessful) {
                    throw IOException("Unexpected code: $response")
                }

                downloadResponse.body!!.source().use { bufferedSource ->
                    val bufferedSink: BufferedSink = jarFile.sink().buffer()
                    bufferedSink.writeAll(bufferedSource)
                    bufferedSink.close()
                }
                ctx.send("Restarting client...").run {
                    LOG.info("Exit induced by the `hotswitch` command.")
                    exitProcess(0)
                }
            }
        }
    }
}