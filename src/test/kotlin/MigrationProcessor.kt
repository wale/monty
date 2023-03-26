import io.ebean.annotation.Platform
import io.ebean.dbmigration.DbMigration

fun main() {
    DbMigration.create().apply {
        setPlatform(Platform.POSTGRES)
    }.generateMigration()
}