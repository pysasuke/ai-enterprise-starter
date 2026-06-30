package com.aistarter.agent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DatabaseSchemaService {

    private final JdbcTemplate jdbcTemplate;

    public String loadSchemaSummary() {
        String dbProduct = detectDatabaseProduct();
        if ("MySQL".equalsIgnoreCase(dbProduct)) {
            return loadMySqlSchema();
        }
        return loadPostgresSchema();
    }

    public String loadIndexSummary() {
        String dbProduct = detectDatabaseProduct();
        if ("MySQL".equalsIgnoreCase(dbProduct)) {
            return loadMySqlIndexes();
        }
        return loadPostgresIndexes();
    }

    private String detectDatabaseProduct() {
        try {
            return jdbcTemplate.getDataSource().getConnection().getMetaData().getDatabaseProductName();
        } catch (Exception ex) {
            return "PostgreSQL";
        }
    }

    private String loadPostgresSchema() {
        String sql = """
                SELECT table_name, column_name, data_type
                FROM information_schema.columns
                WHERE table_schema = 'public'
                ORDER BY table_name, ordinal_position
                LIMIT 200
                """;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        StringBuilder sb = new StringBuilder("Tables and columns:\n");
        for (Map<String, Object> row : rows) {
            sb.append("- ")
                    .append(row.get("table_name"))
                    .append(".")
                    .append(row.get("column_name"))
                    .append(" (")
                    .append(row.get("data_type"))
                    .append(")\n");
        }
        return sb.toString();
    }

    private String loadPostgresIndexes() {
        String sql = """
                SELECT tablename, indexname, indexdef
                FROM pg_indexes
                WHERE schemaname = 'public'
                ORDER BY tablename, indexname
                LIMIT 100
                """;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        StringBuilder sb = new StringBuilder("Indexes:\n");
        for (Map<String, Object> row : rows) {
            sb.append("- ")
                    .append(row.get("tablename"))
                    .append(": ")
                    .append(row.get("indexname"))
                    .append(" -> ")
                    .append(row.get("indexdef"))
                    .append("\n");
        }
        return sb.toString();
    }

    private String loadMySqlSchema() {
        String sql = """
                SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                ORDER BY TABLE_NAME, ORDINAL_POSITION
                LIMIT 200
                """;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        StringBuilder sb = new StringBuilder("Tables and columns:\n");
        for (Map<String, Object> row : rows) {
            sb.append("- ")
                    .append(row.get("TABLE_NAME"))
                    .append(".")
                    .append(row.get("COLUMN_NAME"))
                    .append(" (")
                    .append(row.get("DATA_TYPE"))
                    .append(")\n");
        }
        return sb.toString();
    }

    private String loadMySqlIndexes() {
        String sql = """
                SELECT TABLE_NAME, INDEX_NAME, COLUMN_NAME, NON_UNIQUE
                FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX
                LIMIT 200
                """;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        StringBuilder sb = new StringBuilder("Indexes:\n");
        for (Map<String, Object> row : rows) {
            sb.append("- ")
                    .append(row.get("TABLE_NAME"))
                    .append(": ")
                    .append(row.get("INDEX_NAME"))
                    .append(" on ")
                    .append(row.get("COLUMN_NAME"))
                    .append(" (unique=")
                    .append("0".equals(String.valueOf(row.get("NON_UNIQUE"))) ? "yes" : "no")
                    .append(")\n");
        }
        return sb.toString();
    }
}
