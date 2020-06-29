package org.tomdroid.reborn;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.*;
import androidx.preference.Preference.OnPreferenceChangeListener;

import android.text.InputType;
import android.view.MenuItem;
import android.widget.EditText;
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
	private ListPreference sync_conflict = null;
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
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings);

		customFragment = new PrefFragment();

		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.settings_container, customFragment)
				.commit();

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		if (id == android.R.id.home)
		{
			TPrefs.commit();
			onBackPressed();  return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
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
		sync_conflict = (ListPreference) customFragment.findPreference((TPrefs.Key.SYNC_CONFLICT.getName()));
		sync_file_switch = (SwitchPreference) customFragment.findPreference(TPrefs.Key.SYNC_SDCARD_ACTIVE.getName());
		sync_file = (EditTextPreference) customFragment.findPreference(TPrefs.Key.SYNC_SDCARD.getName());
		sync_nc_switch = (SwitchPreference) customFragment.findPreference(TPrefs.Key.SYNC_NC_ACTIVE.getName());
		sync_nc = (EditTextPreference) customFragment.findPreference(TPrefs.Key.SYNC_NC_URL.getName());

		display_scale.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
			@Override
			public void onBindEditText(@NonNull EditText editText) {
				editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
			}
		});
		sync_period.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
			@Override
			public void onBindEditText(@NonNull EditText editText) {
				editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
			}
		});
		sync_nc.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
			@Override
			public void onBindEditText(@NonNull EditText editText) {
				editText.setInputType(InputType.TYPE_TEXT_VARIATION_URI | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
			}
		});
		sync_file.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
			@Override
			public void onBindEditText(@NonNull EditText editText) {
				editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
			}
		});
		color_highlight.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
			@Override
			public void onBindEditText(@NonNull EditText editText) {
				editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
			}
		});
		color_background.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
			@Override
			public void onBindEditText(@NonNull EditText editText) {
				editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
			}
		});
		color_text.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
			@Override
			public void onBindEditText(@NonNull EditText editText) {
				editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
			}
		});
		color_title.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
			@Override
			public void onBindEditText(@NonNull EditText editText) {
				editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
			}
		});


		final Activity act = this;

		// Chance NC URK
		sync_nc.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference, Object locationUri) {

				if (((String) locationUri).compareTo(TPrefs.getString(TPrefs.Key.SYNC_NC_URL)) != 0) {
					TPrefs.putString(TPrefs.Key.SYNC_NC_TOKEN, "");
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
						path = new File(getExternalFilesDir(null)
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
		display_scale.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object locationUri)
			{
				TLog.e(TAG,"Display scale change "+locationUri.toString());

				long i =0;
				try
				{
					i = Long.parseLong((String) locationUri);
				}
				catch (Exception e) {}

				if ((i < 30) || (i > 500))
				{
					String s = (String)TPrefs.Key.DISPLAY_SCALE.getDefault();
					display_scale.setText(s);
					TPrefs.putString(TPrefs.Key.DISPLAY_SCALE, s);
					return false;
				}
				return true;
			}
		});

		// Chance SYNC PERIOD
		sync_period.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object locationUri)
			{
				long i =0;
				try
				{
					i = Long.parseLong((String) locationUri);
				}
				catch (Exception e) {}
				if ((i < 1) || (i > 20000))
				{
					String s = (String)TPrefs.Key.SYNC_PERIOD.getDefault();
					sync_period.setText(s);
					TPrefs.putString(TPrefs.Key.SYNC_PERIOD, s);
					return false;
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

}
