package org.tomdroid.reborn;

import java.util.*;
import java.util.regex.*;

public class TTime extends android.text.format.Time {

    // 2000-02-01T01:00:00.0000000		Zone will be added below +01:00
    private static final String Y_M_D_T_H_M_S_0000000 = "%Y-%m-%dT%H:%M:%S.0000000";
        
	/**
     * Return a string in the RFC 3339 format with adoptions to Tomboy format (extra Milliseconds). 
     * <p>
     * Time is expressed the time as Y-M-D-T-H-M-S +- GMT</p>
     * <p>
     * Examle: 2000-02-01T01:00:00.0000000+01:00</p>
     * @return string in the RFC 3339 format plus extra Tomboy Milliseconds.
     */

    public String formatTomboy() {
        	Locale locale = Locale.getDefault();
        	Locale.setDefault(Locale.US);
            String base = format(Y_M_D_T_H_M_S_0000000);
            String sign = (gmtoff < 0) ? "-" : "+";
            int offset = (int)Math.abs(gmtoff);
            int minutes = (offset % 3600) / 60;
            int hours = offset / 3600;
            Locale.setDefault(locale);
            return String.format(Locale.US, "%s%s%02d:%02d", base, sign, hours, minutes);
    }
    
    @Override
    public String toString() {
    	return formatTomboy();
    }
    
	// Date converter pattern (remove extra sub milliseconds from datetime string)
	// ex: will strip 3020 in 2010-01-23T12:07:38.7743020-05:00
	private static final Pattern dateCleaner = Pattern.compile(
			"(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3})" +	// matches: 2010-01-23T12:07:38.774
			".+" + 														// matches what we are getting rid of
			"([-\\+]\\d{2}:\\d{2})");									// matches timezone (-xx:xx or +xx:xx)
    
    /**
     * Parse a time in RFC 3339 and Tomboy format.  This method also parses 
     * simple dates (that is, strings that contain no time or time offset).  
     * For example, all of the following strings are valid:
     * 
     * <ul>
     *   <li>"2008-10-13T16:00:00.000Z"</li>
     *   <li>"2008-10-13T16:00:00.000+07:00"</li>
     *   <li>"2008-10-13T16:00:00.000000-07:00"</li>
     *   <li>"2008-10-13"</li>
     * </ul>
     * 
     * <p>
     * If the string contains a time and time offset, then the time offset will
     * be used to convert the time value to UTC.
     * </p>
     * 
     * <p>
     * If the given string contains just a date (with no time field), then
     * the {@link #allDay} field is set to true and the {@link #hour},
     * {@link #minute}, and  {@link #second} fields are set to zero.
     * </p>
     * 
     * <p>
     * Returns true if the resulting time value is in UTC time.
     * </p>
     *
     * @param s the string to parse
     * @return true if the resulting time value is in UTC time
     * @throws android.util.TimeFormatException if s cannot be parsed.
     */
     public boolean parseTomboy(String s) {
         
    	// regexp out the sub-milliseconds from tomboy's datetime format
 		// Normal RFC 3339 format: 			2008-10-13T16:00:00.000-07:00
 		// Tomboy's (C# library) format: 	2010-01-23T12:07:38.7743020-05:00
 		Matcher m = dateCleaner.matcher(s);
 		if (m.find()) {
 			//TLog.d(TAG, "I had to clean out extra sub-milliseconds from the date");
 			s = m.group(1)+m.group(2);
 			//TLog.v(TAG, "new date: {0}", lastChangeDateStr);
 		}
         return parse3339(s);
     }
}
