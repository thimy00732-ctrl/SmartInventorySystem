
package com.sis.backend.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

object DatabaseConfig {

    fun createDataSource(): DataSource {
        val config = HikariConfig().apply {
            jdbcUrl = System.getenv("DB_URL")
                ?: "jdbc:mysql://localhost:3306/smart_inventory_db"

            username = System.getenv("DB_USER")
                ?: "root"

            password = System.getenv("DB_PASSWORD")
                ?: "220305"

            driverClassName = "com.mysql.cj.jdbc.Driver"

            maximumPoolSize = 10
            minimumIdle = 2
            connectionTimeout = 10_000
            poolName = "SmartInventoryPool"
        }

        return HikariDataSource(config)
    }
}
