package com.eseict.gasmodule.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
@Slf4j
public class Util {

    private static final DateTimeFormatter COMPACT_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public LocalDateTime toLocalDateTime(String yyyymmddhhmmss) {
        if (yyyymmddhhmmss == null || yyyymmddhhmmss.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(yyyymmddhhmmss, COMPACT_FORMAT);
        } catch (DateTimeParseException e) {
            log.error("날짜 파싱 실패: {}", yyyymmddhhmmss, e);
            return null;
        }
    }

    public boolean isGasEvent(String state) {
        return state != null && (state.contains("1차") || state.contains("2차"));
    }
}
