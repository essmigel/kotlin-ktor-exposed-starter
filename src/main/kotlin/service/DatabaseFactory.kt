package service

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import model.Widgets
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        // Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        val config = HikariConfig().apply {
            jdbcUrl         = "jdbc:mysql://localhost/test?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC"
            driverClassName = "com.mysql.cj.jdbc.Driver"
            username        = "test1"
            password        = "test1"
            maximumPoolSize = 10
        }
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)
        //Database.connect(hikari())
        transaction {
            addLogger(StdOutSqlLogger)
            create(Widgets)
            Widgets.insert {
                it[name] = "widget one"
                it[quantity] = 27
                it[dateUpdated] = System.currentTimeMillis()
            }
            Widgets.insert {
                it[name] = "widget two"
                it[quantity] = 14
                it[dateUpdated] = System.currentTimeMillis()
            }
        }
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.h2.Driver"
        config.jdbcUrl = "jdbc:h2:mem:test"
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(
            block: suspend () -> T): T =
            newSuspendedTransaction { block() }

}