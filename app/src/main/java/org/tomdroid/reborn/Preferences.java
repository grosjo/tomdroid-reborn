package org.tomdroid.reborn;

import android.content.*;
import androidx.preference.*;

public class Preferences {
	
	public enum Key {
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

		NC_ACCESS_TOKEN ("access_token", ""),
		NC_ACCESS_TOKEN_SECRET ("access_token_secret", ""),
		NC_REQUEST_TOKEN ("request_token", ""),
		NC_REQUEST_TOKEN_SECRET ("request_token_secret", ""),
		NC_OAUTH_10A ("oauth_10a", false),
		NC_AUTHORIZE_URL ("authorize_url", ""),
		NC_ACCESS_TOKEN_URL ("access_token_url", ""),
		NC_REQUEST_TOKEN_URL ("request_token_url", ""),

		LATEST_SYNC_REVISION ("latest_sync_revision", -1L),
		LATEST_SYNC_DATE ("latest_sync_date", (new TTime()).formatTomboy()), // will be used to tell whether we have newer notes

		LAST_FILE_PATH ("last_file_path", "/"),

		SYNC_SERVER_ROOT_API ("sync_server_root_api", ""),
		SYNC_SERVER_USER_API ("sync_server_user_api", ""),

		FIRST_RUN ("first_run", true),

		BASE_TEXT_SIZE("base_text_size","18");

		private String name = "";
		private Object defaultValue = "";
		
		Key(String name, Object defaultValue) {
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
	
	public static void init(Context context, boolean clean) {
		
		client = PreferenceManager.getDefaultSharedPreferences(context);
		editor = client.edit();
		
		if (clean)
			editor.clear().commit();
	}
	
	public static String getString(Key key) {
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
