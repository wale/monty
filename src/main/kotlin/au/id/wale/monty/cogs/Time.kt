package au.id.wale.monty.cogs

import au.id.wale.monty.db.UserEntity
import au.id.wale.monty.db.query.QUserEntity
import me.devoxin.flight.api.annotations.Command
import me.devoxin.flight.api.annotations.Describe
import me.devoxin.flight.api.annotations.Greedy
import me.devoxin.flight.api.context.Context
import me.devoxin.flight.api.entities.Cog
import net.dv8tion.jda.api.entities.User
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

class Time : Cog {
    override fun name(): String = "Time"

    @Command(description = "Sets the timezone for a user", aliases = ["tz"])
    fun timezone(ctx: Context, @Greedy @Describe("A tzdb-formatted timezone") timezone: String) {
        val isValid = timezone in TimeZone.getAvailableIDs()
        if (isValid) {
            val user = QUserEntity().id.eq(ctx.author.idLong).findOneOrEmpty()
            if (user.isEmpty) {
                val newUser = UserEntity()
                newUser.id = ctx.author.idLong
                newUser.timezone = timezone
                newUser.save()
                val currentTime = Calendar.getInstance(TimeZone.getTimeZone(timezone)).time
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                ctx.send("Timezone saved! Your current time is `${dateFormat.format(currentTime)}`.")
            } else {
                val existingUser = user.get()
                existingUser.timezone = timezone
                existingUser.save()

                val currentTime = Calendar.getInstance(TimeZone.getTimeZone(timezone)).time
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                ctx.send("Timezone saved! Your current time is `${dateFormat.format(currentTime)}`.")
            }
        } else {
            ctx.send("**Error**: You need to provide a timezone in the `tzdb` format (https://en.wikipedia.org/wiki/List_of_tz_database_time_zones). \n" +
                    "Examples include: `Asia/Tokyo`, `Europe/Berlin`, `Australia/Melbourne`.")
        }
    }

    @Command(description = "Gets the current time for a user.", aliases = ["tf"])
    fun timefor(ctx: Context, @Greedy @Describe("A user to optionally get the timezone for.") user: User?) {
        if (user != null) {
            val dbUser = QUserEntity().id.eq(user.idLong).findOneOrEmpty()

            if (dbUser.isEmpty) {
                ctx.send("The user **${user.name}#${user.discriminator}** does not have a timezone set. " +
                        "To add a timezone, they must use `timezone <user/db>`.")
            } else {
                val existingDbUser = dbUser.get()
                val timezone = existingDbUser.timezone
                val currentTime = Calendar.getInstance(TimeZone.getTimeZone(timezone)).time
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                ctx.send("**${user.name}**'s current time is `${dateFormat.format(currentTime)}`.")
            }
        } else {
            val dbUser = QUserEntity().id.eq(ctx.author.idLong).findOneOrEmpty()

            if (dbUser.isEmpty) {
                ctx.send("You **${ctx.author.name}#${ctx.author.discriminator}** do not have a timezone set. " +
                        "To add a timezone, you must use `timezone <user/db>`.")
            } else {
                val existingDbUser = dbUser.get()
                val timezone = existingDbUser.timezone
                val currentTime = Calendar.getInstance(TimeZone.getTimeZone(timezone)).time
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                ctx.send("**${ctx.author.name}**'s current time is `${dateFormat.format(currentTime)}`.")
            }
        }
    }
}