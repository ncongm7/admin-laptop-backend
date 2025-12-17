package com.example.backendlaptop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("🔄 [DatabaseInitializer] Checking database schema for 'chat_sessions'...");

        try {
            // 1. Add current_state
            addColumnIfNotExists("chat_sessions", "current_state", "NVARCHAR(50)");

            // 2. Add goal
            addColumnIfNotExists("chat_sessions", "goal", "NVARCHAR(50)");

            // 3. Add progress_data
            addColumnIfNotExists("chat_sessions", "progress_data", "NVARCHAR(MAX)");

            // 4. Add context_data
            addColumnIfNotExists("chat_sessions", "context_data", "NVARCHAR(MAX)");

            // 5. Add step_count
            addColumnIfNotExists("chat_sessions", "step_count", "INT");

            // 6. Add is_stuck
            addColumnIfNotExists("chat_sessions", "is_stuck", "BIT");

            // 7. Add current_intent
            addColumnIfNotExists("chat_sessions", "current_intent", "NVARCHAR(50)");

            log.info("✅ [DatabaseInitializer] Schema check completed.");
        } catch (Exception e) {
            log.error("❌ [DatabaseInitializer] Error updating schema: {}", e.getMessage());
        }
    }

    private void addColumnIfNotExists(String tableName, String columnName, String columnType) {
        String checkSql = "SELECT COL_LENGTH('" + tableName + "', '" + columnName + "')";
        Integer length = jdbcTemplate.queryForObject(checkSql, Integer.class);

        if (length == null) {
            log.info("🛠️ [DatabaseInitializer] Adding missing column '{}.{}'...", tableName, columnName);
            String alterSql = "ALTER TABLE " + tableName + " ADD " + columnName + " " + columnType;
            jdbcTemplate.execute(alterSql);
        }
    }
}
