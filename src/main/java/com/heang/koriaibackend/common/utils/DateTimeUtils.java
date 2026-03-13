package com.heang.koriaibackend.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

    private static final DateTimeFormatter DEFAULT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtils () {
        // Private constructor to prevent instantiation
    }

    public static String now() {
        return LocalDateTime.now().format(DEFAULT_FORMAT);
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime.format(DEFAULT_FORMAT);
    }}
