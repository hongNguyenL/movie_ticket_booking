package com.nguyen.movieticket.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SequenceFixer {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void fixSequences() {
        try {
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(
                "SELECT table_name, column_name FROM information_schema.columns " +
                "WHERE table_schema = 'public' AND column_default LIKE 'nextval(%'"
            );

            for (Map<String, Object> row : tables) {
                String table = (String) row.get("table_name");
                String column = (String) row.get("column_name");
                String seqName = table + "_" + column + "_seq";
                jdbcTemplate.update("SELECT setval('" + seqName + "', (SELECT COALESCE(MAX(" + column + "), 1) FROM " + table + "))");
                log.info("Fixed sequence {} for {}.{}", seqName, table, column);
            }
        } catch (Exception e) {
            log.warn("Could not fix sequences: {}", e.getMessage());
        }
    }
}
