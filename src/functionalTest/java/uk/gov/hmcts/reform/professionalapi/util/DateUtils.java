package uk.gov.hmcts.reform.professionalapi.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public final class DateUtils {
    public static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private DateUtils() {
    }

    public static String formatDateString(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage());
        }
        return sdf.format(date);
    }

    public static LocalDateTime convertStringToLocalDate(String dateString) {
        String formattedStringDate = formatDateString(dateString);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        LocalDateTime localDateTime = LocalDateTime.parse(formattedStringDate, formatter);
        return localDateTime;
    }

    public static String generateRandomDate(String daysToMinus, String minToMinus) {
        LocalDateTime localDateTimeNow = LocalDateTime.now();
        LocalDateTime localDateTime = convertStringToLocalDate(localDateTimeNow.toString());
        if (daysToMinus != null) {
            localDateTime = localDateTime.minusDays(Integer.parseInt(daysToMinus));
        } else if (minToMinus != null) {
            localDateTime = localDateTime.minusMinutes(Integer.parseInt(minToMinus));
        }
        return localDateTime.toString().trim();
    }
}
