package au.id.wale.monty.cogs

import au.id.wale.monty.util.uploadToHastebin
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.Describe
import me.devoxin.flight.api.annotations.Greedy
import me.devoxin.flight.api.context.MessageContext
import me.devoxin.flight.api.entities.Cog
import java.awt.Color
import java.io.File
import javax.script.ScriptEngineManager
import kotlin.script.experimental.jsr223.KotlinJsr223DefaultScriptEngineFactory

class Developer : Cog {
    override fun name(): String = "Developer"

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

    /**
     * todo: implement the command below
     * @Command(description = "Hot-updates the Monty JAR to the latest build", developerOnly = true)
    fun hotswitch(ctx: MessageContext) {
        // This dynamically grabs the location of the JAR, but there are some caveats
        // This might not work in IntelliJ's runner.
        val jarFile = File(this::class.java.protectionDomain.codeSource.location.toURI())

        // Get the latest release from GitHub
        val url = "https://github.com/wale/monty/releases/latest/download/monty-"
    } **/
}