package com.aistarter.agent.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseSchemaServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private DatabaseMetaData metaData;
    @InjectMocks
    private DatabaseSchemaService schemaService;

    @Test
    void loadSchemaSummaryShouldReturnTableInfo() throws Exception {
        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(
                Map.of("table_name", "orders", "column_name", "id", "data_type", "bigint")));

        String summary = schemaService.loadSchemaSummary();
        assertTrue(summary.contains("orders.id"));
    }

    @Test
    void loadIndexSummaryShouldReturnIndexInfo() throws Exception {
        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(
                Map.of("tablename", "orders", "indexname", "orders_pkey", "indexdef", "CREATE UNIQUE INDEX")));

        String summary = schemaService.loadIndexSummary();
        assertTrue(summary.contains("orders_pkey"));
    }
}
