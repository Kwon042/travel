package com.example.travelProj.global.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class DateUtil {
    public static Date convertToDate(LocalDateTime dateTime) {
        return Timestamp.valueOf(dateTime);
    }
}
