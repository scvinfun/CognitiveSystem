package Common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CS_DateFormatter {
    public static final SimpleDateFormat FULL_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
    public static final SimpleDateFormat DISPLAY_FORMAT = new SimpleDateFormat("dd/MMM/yyyy - HH:mm", Locale.US);

    public static String toDiplayDateFormat(String date_str) throws ParseException {
        return DISPLAY_FORMAT.format(FULL_FORMAT.parse(date_str));
    }
}
