package org.tomdroid.reborn;

import android.util.Log;

import static java.text.MessageFormat.format;

/**
 * @author Piotr Adamski <mcveat@gmail.com>
 */
public class TLog {
    // Logging should be disabled for release builds
    private static final boolean LOGGING_ENABLED = true;

    public static void v(String tag, Throwable t, String msg, Object... args) {
        if (LOGGING_ENABLED) Log.v(tag, format(msg, args), t);
    }

    public static void v(String tag, String msg, Object... args) {
    	if (LOGGING_ENABLED) Log.v(tag, format(msg, args));
    }

    public static void d(String tag, Throwable t, String msg, Object... args) {
        if (LOGGING_ENABLED) Log.d(tag, format(msg, args), t);
    }

    public static void d(String tag, String msg, Object... args) {
    	if (LOGGING_ENABLED) Log.d(tag, format(msg, args));
    }

    public static void i(String tag, Throwable t, String msg, Object... args) {
        Log.i(tag, format(msg, args), t);
    }

    public static void i(String tag, String msg, Object... args) {
        Log.i(tag, format(msg, args));
    }

    public static void w(String tag, Throwable t, String msg, Object... args) {
        Log.w(tag, format(msg, args), t);
    }

    public static void w(String tag, String msg, Object... args) {
        Log.w(tag, format(msg, args));
    }

    public static void e(String tag, Throwable t, String msg, Object... args) {
        Log.e(tag, format(msg, args), t);
    }

    public static void e(String tag, String msg, Object... args) {
        Log.e(tag, format(msg, args));
    }
/**
 * FIXME disabled since they were introduced in API level 8 and we target lower
    public static void wtf(String tag, Throwable t, String msg, Object... args) {
        Log.wtf(tag, format(msg, args), t);
    }

    public static void wtf(String tag, String msg, Object... args) {
        Log.wtf(tag, format(msg, args));
    }
 */
}
