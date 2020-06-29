package org.tomdroid.reborn;

import android.content.*;
import android.net.Uri;

import androidx.preference.*;

public class TPrefs
{
	private static final String TAG = "Preferences";

	// Global definition for Tomdroid
	public static final String      AUTHORITY                       = "org.tomdroid.reborn.notes";
	public static final Uri 		CONTENT_URI                     = Uri.parse("content://" + AUTHORITY + "/notes");
	//public static final String      CONTENT_TYPE            = "vnd.android.cursor.dir/vnd.tomdroid.note";
	//public static final String      CONTENT_ITEM_TYPE       = "vnd.android.cursor.item/vnd.tomdroid.note";
	//public static final String      PROJECT_HOMEPAGE        = "https://github.com/grosjo/tomdroid-reborn/";
	//public static final String CALLED_FROM_SHORTCUT_EXTRA = "org.tomdroid.reborn.CALLED_FROM_SHORTCUT";
	//public static final String IS_NEW_NOTE_EXTRA = "org.tomdroid.reborn.IS_NEW_NOTE";
	//public static final String SHORTCUT_NAME                = "org.tomdroid.reborn.SHORTCUT_NAME";

	public enum Key
	{
		FIRST_RUN ("first_run", "OK"),
		SYNC_AUTO ("sync_auto", false),
		SYNC_PERIOD ("sync_period", "10"),
		SYNC_CONFLICT ("sync_conflict", "1"),
		SYNC_SDCARD_ACTIVE ("sync_file_switch", true),
		SYNC_SDCARD ("sync_file", "tomdroid"),

		DISPLAY_SCALE ("display_scale", "100"),
		DISPLAY_COLOR_TITLE ("color_title", "#000055"),
		DISPLAY_COLOR_TEXT ("color_text", "#000000"),
		DISPLAY_COLOR_BACKGROUND ("color_background", "#FFFF55"),
		DISPLAY_COLOR_HIGHLIGHT ("color_highlight", "#555500"),
		DISPLAY_SORT_ORDER ("sorttype", true),

		NOTEBOOKS_MULTIPLE ("allowmultiple",true),

		SYNC_NC_ACTIVE ("sync_nc_switch", false),
		SYNC_NC_URL ("sync_nc", "https://YOURSERVER/index.php/apps/grauphel"),
		SYNC_NC_KEY ("nc_key", ""),
		SYNC_NC_TOKEN ("access_token", ""),
		SYNC_NC_TOKEN_SECRET ("access_token_secret", ""),

		LATEST_SYNC_DATE ("latest_sync_date",0L);

		private String name = "";
		private Object defaultValue = "";
		
		Key(String name, Object defaultValue)
		{
			this.name = name;
			this.defaultValue = defaultValue;
		}
		
		public String getName() {
			return name;
		}
		
		public Object getDefault() {
			return defaultValue;
		}
	}
	
	private static SharedPreferences client = null;
	private static SharedPreferences.Editor editor = null;

	public static String MD5(String md5)
	{
		try
		{
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i)
			{
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
			}
			return sb.toString();
		}
		catch (Exception e)
		{
			TLog.e(TAG,e,"MD5 error");
		}
		return null;
	}

	public static void init(Context context)
	{
		client = PreferenceManager.getDefaultSharedPreferences(context);
		editor = client.edit();

		//if (clean)
		//editor.clear().apply();

		if(client.contains(Key.FIRST_RUN.getName())) //Already saved
		{
			TLog.e(TAG,"Contains already data");
		}
		else
		{
			TLog.e(TAG,"set default");
			editor.putString(Key.FIRST_RUN.getName(),"OK");
			editor.putBoolean(Key.SYNC_AUTO.getName(),(Boolean)Key.SYNC_AUTO.getDefault());
			editor.putString(Key.SYNC_PERIOD.getName(),Key.SYNC_PERIOD.getDefault().toString());
			editor.putString(Key.SYNC_CONFLICT.getName(),Key.SYNC_CONFLICT.getDefault().toString());
			editor.putBoolean(Key.SYNC_SDCARD_ACTIVE.getName(),(Boolean)Key.SYNC_SDCARD_ACTIVE.getDefault());
			editor.putString(Key.SYNC_SDCARD.getName(),Key.SYNC_SDCARD.getDefault().toString());
			editor.putString(Key.DISPLAY_SCALE.getName(),Key.DISPLAY_SCALE.getDefault().toString());
			editor.putString(Key.DISPLAY_COLOR_TITLE.getName(),Key.DISPLAY_COLOR_TITLE.getDefault().toString());
			editor.putString(Key.DISPLAY_COLOR_TEXT.getName(),Key.DISPLAY_COLOR_TEXT.getDefault().toString());
			editor.putString(Key.DISPLAY_COLOR_BACKGROUND.getName(),Key.DISPLAY_COLOR_BACKGROUND.getDefault().toString());
			editor.putString(Key.DISPLAY_COLOR_HIGHLIGHT.getName(),Key.DISPLAY_COLOR_HIGHLIGHT.getDefault().toString());
			editor.putBoolean(Key.DISPLAY_SORT_ORDER.getName(),(Boolean)Key.DISPLAY_SORT_ORDER.getDefault());
			editor.putBoolean(Key.NOTEBOOKS_MULTIPLE.getName(),(Boolean)Key.NOTEBOOKS_MULTIPLE.getDefault());
			editor.putBoolean(Key.SYNC_NC_ACTIVE.getName(),(Boolean)Key.SYNC_NC_ACTIVE.getDefault());
			editor.putString(Key.SYNC_NC_URL.getName(),Key.SYNC_NC_URL.getDefault().toString());
			editor.putString(Key.SYNC_NC_KEY.getName(),MD5(String.format("%d",(long)(Math.random() * 99999999999L + 1123234345))));
			editor.putString(Key.SYNC_NC_TOKEN.getName(),"");
			editor.putString(Key.SYNC_NC_TOKEN_SECRET.getName(),"");
			editor.putLong(Key.LATEST_SYNC_DATE.getName(),(Long)Key.LATEST_SYNC_DATE.getDefault());
		}
		editor.commit();
	}

	public static String getString(Key key)
	{
		return client.getString(key.getName(), key.getDefault().toString());
	}
	
	public static void putString(Key key, String value) {
		
		if (value == null)
			editor.putString(key.getName(), key.getDefault().toString());
		else
			editor.putString(key.getName(), value);
		editor.commit();
	}
	
	public static long getLong(Key key)
	{
		return client.getLong(key.getName(), (Long)key.getDefault());
	}
	
	public static void putLong(Key key, long value) {
		
		editor.putLong(key.getName(), value);
		editor.commit();
	}
	
	public static boolean getBoolean(Key key) {
		
		return client.getBoolean(key.getName(), (Boolean)key.getDefault());
	}
	
	public static void putBoolean(Key key, boolean value) {
		
		editor.putBoolean(key.getName(), value);
		editor.commit();
	}

	public static void commit()
	{
		editor.commit();
	}
}
