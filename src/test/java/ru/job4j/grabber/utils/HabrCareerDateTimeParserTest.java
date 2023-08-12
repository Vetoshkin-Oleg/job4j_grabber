package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;

class HabrCareerDateTimeParserTest {

    @Test
    void parse() {
        HabrCareerDateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        String date = "2023-08-12T12:27:16+03:00";
        LocalDateTime expextDate = LocalDateTime.of(2023, Month.AUGUST, 13,
                12, 27, 16);
        Assertions.assertEquals(expextDate, dateTimeParser.parse(date)
                .plus(1, ChronoUnit.DAYS));
    }
}