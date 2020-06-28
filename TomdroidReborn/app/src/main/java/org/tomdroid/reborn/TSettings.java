package org.tomdroid.reborn;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.*;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.widget.Toast;

import java.io.File;

public class TSettings extends AppCompatActivity
{
	private static final String TAG = "PreferencesActivity";
	private PrefFragment customFragment;

	private EditTextPreference display_scale = null;
	private CheckBoxPreference allowmultiple = null;
	private SwitchPreference sorttype = null;
	private EditTextPreference color_title = null;
	private EditTextPreference color_text = null;
	private EditTextPreference color_background = null;
	private EditTextPreference color_highlight = null;
	private SwitchPreference sync_auto = null;
	private EditTextPreference sync_period = null;
	private DropDownPreference sync_conflict = null;
	private SwitchPreference sync_file_switch = null;
	private EditTextPreference sync_file = null;
	private SwitchPreference sync_nc_switch = null;
	private EditTextPreference sync_nc = null;

	//private Handler	 preferencesMessageHandler	= new PreferencesMessageHandler(this);

	public static class PrefFragment extends PreferenceFragmentCompat
	{
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
		{
			TLog.e(TAG,"onCreatePreferences "+rootKey);
			setPreferencesFromResource(R.xml.settings, rootKey);
		}
	}

	//private static ProgressDialog syncProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings);

		customFragment = new PrefFragment();

		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.settings_container, customFragment)
				.commit();

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Fill the Preferences fields
		display_scale = (EditTextPreference) customFragment.findPreference(TPrefs.Key.DISPLAY_SCALE.getName());
		allowmultiple = (CheckBoxPreference) customFragment.findPreference(TPrefs.Key.NOTEBOOKS_MULTIPLE.getName());
		sorttype = (SwitchPreference) customFragment.findPreference(TPrefs.Key.DISPLAY_SORT_ORDER.getName());
		color_title = (EditTextPreference) customFragment.findPreference(TPrefs.Key.DISPLAY_COLOR_TITLE.getName());
		color_text = (EditTextPreference) customFragment.findPreference(TPrefs.Key.DISPLAY_COLOR_TEXT.getName());
		color_background = (EditTextPreference) customFragment.findPreference(TPrefs.Key.DISPLAY_COLOR_BACKGROUND.getName());
		color_highlight = (EditTextPreference) customFragment.findPreference(TPrefs.Key.DISPLAY_COLOR_HIGHLIGHT.getName());
		sync_auto = (SwitchPreference) customFragment.findPreference(TPrefs.Key.SYNC_AUTO.getName());
		sync_period = (EditTextPreference) customFragment.findPreference(TPrefs.Key.SYNC_PERIOD.getName());
		sync_conflict = (DropDownPreference) customFragment.findPreference((TPrefs.Key.SYNC_CONFLICT.getName()));
		sync_file_switch = (SwitchPreference) customFragment.findPreference(TPrefs.Key.SYNC_SDCARD_ACTIVE.getName());
		sync_file = (EditTextPreference) customFragment.findPreference(TPrefs.Key.SYNC_SDCARD.getName());
		sync_nc_switch = (SwitchPreference) customFragment.findPreference(TPrefs.Key.SYNC_NC_ACTIVE.getName());
		sync_nc = (EditTextPreference) customFragment.findPreference(TPrefs.Key.SYNC_NC_URL.getName());

		// Set the default values if nothing exists
		setDefaults();

		final Activity act = this;


		// Chance NC URK
		sync_nc.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference, Object locationUri) {

				if (((String) locationUri).compareTo(TPrefs.getString(TPrefs.Key.SYNC_NC_URL)) != 0) {
					TPrefs.putString(TPrefs.Key.NC_TOKEN, "");
				}
				return true;
			}
		});

		// Change the Folder Location
		sync_file.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object locationUri) {

				boolean retval = true;
				// if it is the same, return false. this is important as we would reset the sync-values later
				if (locationUri.equals(TPrefs.getString(TPrefs.Key.SYNC_SDCARD))) {
					retval = false;
				} else if ((locationUri.toString().contains("\t")) || (locationUri.toString().contains("\n"))) {
					Toast.makeText(act, "Folder invalid " + locationUri.toString(), Toast.LENGTH_SHORT).show();
					retval = false;
				} else {
					File path = new File("/");
					if (locationUri.toString().startsWith("/")) {
						path = new File(locationUri + "/");
					} else {
						path = new File(Environment.getExternalStorageDirectory()
								+ "/" + locationUri + "/");
					}

					if (!path.exists()) {
						TLog.w(TAG, "Folder {0} does not exist.", path);
						Toast.makeText(act, "Folder invalid " + path, Toast.LENGTH_SHORT).show();
						retval = false;
					} else {
						TLog.d(TAG, "Changed Folder to: " + path.toString());
						//Tomdroid.NOTES_PATH = path.toString();
						sync_file.setSummary(path.getPath());
					}
				}
				return retval;
			}
		});

		// Chance DISPLAY SCALE
		display_scale.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object locationUri) {
				int i = 0;
				try {
					i = Integer.parseInt((String) locationUri);
				} catch (Exception e) {
					i = 0;
				}
				if ((i < 30) || (i > 500)) {
					i = Integer.parseInt((String) TPrefs.Key.DISPLAY_SCALE.getDefault());
					display_scale.setText(new Integer(i).toString());
					TPrefs.putLong(TPrefs.Key.DISPLAY_SCALE, i);
				}
				return true;
			}
		});

		// Chance SYNC PERIOD
		sync_period.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object locationUri) {
				int i = 0;
				try {
					i = Integer.parseInt((String) locationUri);
				} catch (Exception e) {
					i = 0;
				}
				if ((i < 1) || (i > 20000)) {
					i = Integer.parseInt((String) TPrefs.Key.SYNC_PERIOD.getDefault());
					sync_period.setText(new Integer(i).toString());
					TPrefs.putLong(TPrefs.Key.SYNC_PERIOD, i);
				}
				return true;
			}
		});
	}

	/*
	private void reauthenticate() {

		// don't do anything, we'll authenticate on sync instead
		// save empty config, wiping old config
		
		WebOAuthConnection auth = new WebOAuthConnection();
		auth.saveConfiguration();
	}
	*/

	private void setDefaults()
	{
		String s = (String) TPrefs.Key.SYNC_NC_URL.getDefault();
		sync_nc.setDefaultValue(s);
		if (sync_nc.getText() == null) sync_nc.setText(s);
		sync_nc.setSummary(TPrefs.getString(TPrefs.Key.SYNC_NC_URL));

		s = (String) TPrefs.Key.SYNC_SDCARD.getDefault();
		sync_file.setDefaultValue(s);
		if (sync_file.getText() == null) sync_file.setText(s);

		int i = (Integer) TPrefs.Key.DISPLAY_SCALE.getDefault();
		display_scale.setDefaultValue(i);
		if (Integer.parseInt(display_scale.getText()) < 50) display_scale.setText(new Integer(i).toString());

		boolean b = (Boolean) TPrefs.Key.NOTEBOOKS_MULTIPLE.getDefault();
		allowmultiple.setDefaultValue(b);

		b = (Boolean) TPrefs.Key.DISPLAY_SORT_ORDER.getDefault();
		sorttype.setDefaultValue(b);

		s = (String) TPrefs.Key.DISPLAY_COLOR_TITLE.getDefault();
		color_title.setDefaultValue(s);
		if (color_title.getText() == null) color_title.setText(s);

		s = (String) TPrefs.Key.DISPLAY_COLOR_TEXT.getDefault();
		color_text.setDefaultValue(s);
		if (color_text.getText() == null) color_text.setText(s);

		s = (String) TPrefs.Key.DISPLAY_COLOR_BACKGROUND.getDefault();
		color_background.setDefaultValue(s);
		if (color_background.getText() == null) color_background.setText(s);

		s = (String) TPrefs.Key.DISPLAY_COLOR_HIGHLIGHT.getDefault();
		color_highlight.setDefaultValue(s);
		if (color_highlight.getText() == null) color_highlight.setText(s);

		b = (Boolean) TPrefs.Key.SYNC_AUTO.getDefault();
		sync_auto.setDefaultValue(b);

		i = (Integer) TPrefs.Key.SYNC_PERIOD.getDefault();
		sync_period.setDefaultValue(new Integer(i));

		i = (Integer) TPrefs.Key.SYNC_CONFLICT.getDefault();
		sync_conflict.setDefaultValue(new Integer(i));
		if ((Integer.parseInt(sync_conflict.getValue()) < 1) || (Integer.parseInt(sync_conflict.getValue()) > 4)) sync_conflict.setValue(new Integer(i).toString());

		b = (Boolean) TPrefs.Key.SYNC_SDCARD_ACTIVE.getDefault();
		sync_file_switch.setDefaultValue(b);

		s = (String) TPrefs.Key.SYNC_SDCARD.getDefault();
		sync_file.setDefaultValue(s);
		if (sync_file.getText() == null) sync_file.setText(s);

		b = (Boolean) TPrefs.Key.SYNC_NC_ACTIVE.getDefault();
		sync_nc_switch.setDefaultValue(b);

		s = (String) TPrefs.Key.SYNC_NC_URL.getDefault();
		sync_nc.setDefaultValue(s);
		if (sync_nc.getText() == null) sync_nc.setText(s);
	}
}
