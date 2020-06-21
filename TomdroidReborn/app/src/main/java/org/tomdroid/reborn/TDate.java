package org.tomdroid.reborn;

import android.text.format.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class TDate
{
    private final String TAG = "TDate";
    private Date date;

    // 2000-02-01T01:00:00.0000000		Zone will be added below +01:00
    private static final String TOMBOY_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX";

    public TDate()
    {
        date = new Date();
    }

    public TDate(String s)
    {
        parseTomboy(s);
    }

    public TDate(long l)
    {
        date = new Date();
        date.setTime(l);
    }

    public String formatTomboy()
    {
        Locale locale = Locale.getDefault();
        Locale.setDefault(Locale.US);
        DateFormat d = new DateFormat();
        return d.format(TOMBOY_FORMAT,date).toString();
    }

    @Override
    public String toString() {
    	return formatTomboy();
    }
    
	public boolean parseTomboy(String s)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(TOMBOY_FORMAT);
        try
        {
            date = sdf.parse(s);
        }
        catch(Exception e)
        {
            date = new Date();
            e.printStackTrace();
            TLog.e(TAG,"Date error "+s);
            return false;
        }
        return true;
     }

     public long toLong()
     {
        return date.getTime();
     }
}
