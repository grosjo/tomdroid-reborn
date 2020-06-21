package org.tomdroid.reborn;

import android.content.*;
import android.net.Uri;

import androidx.preference.*;

public class TPrefs
{
	private static final String TAG = "Preferences";

	// Global definition for Tomdroid
	public static final String      AUTHORITY                       = "org.tomdroid.reborn.notes";
	public static final Uri CONTENT_URI                     = Uri.parse("content://" + AUTHORITY + "/notes");
	//public static final String      CONTENT_TYPE            = "vnd.android.cursor.dir/vnd.tomdroid.note";
	//public static final String      CONTENT_ITEM_TYPE       = "vnd.android.cursor.item/vnd.tomdroid.note";
	//public static final String      PROJECT_HOMEPAGE        = "https://github.com/grosjo/tomdroid-reborn/";
	//public static final String CALLED_FROM_SHORTCUT_EXTRA = "org.tomdroid.reborn.CALLED_FROM_SHORTCUT";
	//public static final String IS_NEW_NOTE_EXTRA = "org.tomdroid.reborn.IS_NEW_NOTE";
	//public static final String SHORTCUT_NAME                = "org.tomdroid.reborn.SHORTCUT_NAME";

	public enum Key
	{
		SYNC_AUTO_ACTIVE ("sync_auto", 0),
		SYNC_AUTO ("sync_period", 10),
		SYNC_CONFLICT ("sync_conflict", 0),
		SYNC_SDCARD_ACTIVE ("sync_file_switch", 1),
		SYNC_SDCARD ("sync_file", "tomdroid"),
		SYNC_NC_ACTIVE ("sync_nc_switch", false),
		SYNC_NC_URL ("sync_nc", "https://YOURSERVER/index.php/apps/grauphel"),

		DISPLAY_SCALE ("display_scale", 100),
		DISPLAY_COLOR_TITLE ("color_title", "#000055"),
		DISPLAY_COLOR_TEXT ("color_text", "#000000"),
		DISPLAY_COLOR_BACKGROUND ("color_background", "#FFFF55"),
		DISPLAY_COLOR_HIGHLIGHT ("color_highlight", "#555500"),
		DISPLAY_SORT_ORDER ("sorttype", true),

		NOTEBOOKS_MULTIPLE ("allowmultiple",true),

		NC_KEY ("nc_key", ""),
		NC_TOKEN ("access_token", ""),
		NC_TOKEN_SECRET ("access_token_secret", "");

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

	public static void init(Context context, boolean clean)
	{
		client = PreferenceManager.getDefaultSharedPreferences(context);
		editor = client.edit();
		
		if (clean)
			editor.clear().apply();

		if(getString(Key.NC_KEY).length()<5)
		{
			putString(Key.NC_KEY,MD5(String.format("%d",(long)(Math.random() * 99999999999L + 1123234345))));
		}
	}
	
	public static String getString(Key key)
	{
		return client.getString(key.getName(), (String) key.getDefault());
	}
	
	public static void putString(Key key, String value) {
		
		if (value == null)
			editor.putString(key.getName(), (String)key.getDefault());
		else
			editor.putString(key.getName(), value);
		editor.commit();
	}
	
	public static long getLong(Key key) {
		
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
}
