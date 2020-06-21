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

public class PreferencesActivity extends AppCompatActivity
{
	private static final String TAG = "PreferencesActivity";

	private EditTextPreference fontScale = null;
	private CheckBoxPreference allowmultiple = null;
	private SwitchPreference sorttype = null;
	private EditTextPreference color_title = null;
	private EditTextPreference color_text = null;
	private EditTextPreference color_background = null;
	private EditTextPreference color_highlight = null;
	private SwitchPreference sync = null;
	private EditTextPreference syncperiod = null;
	private DropDownPreference sync_conflict = null;
	private SwitchPreference sync_file_switch = null;
	private EditTextPreference sync_file = null;
	private SwitchPreference sync_nc_switch = null;
	private EditTextPreference sync_nc = null;

	//private Handler	 preferencesMessageHandler	= new PreferencesMessageHandler(this);

	public static class PrefFragment extends PreferenceFragmentCompat
	{
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.preferences, rootKey);
		}
	}

	//private static ProgressDialog syncProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);
		PrefFragment customFragment = new PrefFragment();
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.preferences, customFragment)
				.commit();

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		// Fill the Preferences fields
		fontScale = (EditTextPreference)customFragment.findPreference(Preferences.Key.DISPLAY_SCALE.getName());
		allowmultiple = (CheckBoxPreference)customFragment.findPreference(Preferences.Key.NOTEBOOKS_MULTIPLE.getName());
		sorttype = (SwitchPreference)customFragment.findPreference(Preferences.Key.DISPLAY_SORT_ORDER.getName());
		color_title = (EditTextPreference) customFragment.findPreference(Preferences.Key.DISPLAY_COLOR_TITLE.getName());
		color_text = (EditTextPreference) customFragment.findPreference(Preferences.Key.DISPLAY_COLOR_TEXT.getName());
		color_background = (EditTextPreference) customFragment.findPreference(Preferences.Key.DISPLAY_COLOR_BACKGROUND.getName());
		color_highlight = (EditTextPreference) customFragment.findPreference(Preferences.Key.DISPLAY_COLOR_HIGHLIGHT.getName());
		sync = (SwitchPreference)customFragment.findPreference(Preferences.Key.SYNC_AUTO_ACTIVE.getName());
		syncperiod = (EditTextPreference)customFragment.findPreference(Preferences.Key.SYNC_AUTO.getName());
		sync_conflict = (DropDownPreference)customFragment.findPreference((Preferences.Key.SYNC_CONFLICT.getName()));
		sync_file_switch = (SwitchPreference)customFragment.findPreference(Preferences.Key.SYNC_SDCARD_ACTIVE.getName());
		sync_file = (EditTextPreference)customFragment.findPreference(Preferences.Key.SYNC_SDCARD.getName());
		sync_nc_switch = (SwitchPreference)customFragment.findPreference(Preferences.Key.SYNC_NC_ACTIVE.getName());
		sync_nc = (EditTextPreference)customFragment.findPreference(Preferences.Key.SYNC_NC_URL.getName());

		// Set the default values if nothing exists
		setDefaults();

		final Activity act = this;

		// Chance NC URK
		sync_nc.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference, Object locationUri) {

				if (((String)locationUri).compareTo(Preferences.getString(Preferences.Key.SYNC_NC_URL)) !=0)
				{
					Preferences.putString(Preferences.Key.NC_TOKEN,"");
				}
				return true;
			}
		});

		// Change the Folder Location
		sync_file.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference, Object locationUri) {

				boolean retval = true;
				// if it is the same, return false. this is important as we would reset the sync-values later
				if (locationUri.equals(Preferences.getString(Preferences.Key.SYNC_SDCARD))) {
					retval =  false;
				}
				else if ((locationUri.toString().contains("\t")) || (locationUri.toString().contains("\n"))) {
					Toast.makeText(act, "Folder invalid "+locationUri.toString(), Toast.LENGTH_SHORT).show();
					retval =  false;
				}
				else {
					File path = new File("/");
					if (locationUri.toString().startsWith("/")) {
						path = new File(locationUri + "/");
					} else {
						path = new File(Environment.getExternalStorageDirectory()
								+ "/" + locationUri + "/");
					}
	
					if(!path.exists()) {
						TLog.w(TAG, "Folder {0} does not exist.", path);
						Toast.makeText(act, "Folder invalid "+path, Toast.LENGTH_SHORT).show();
						retval =  false;
					}
					else
					{
						TLog.d(TAG, "Changed Folder to: " + path.toString());
						Tomdroid.NOTES_PATH = path.toString();
						sync_file.setSummary(Tomdroid.NOTES_PATH);
					}
				}
				return retval;
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
		String defaultServer = (String)Preferences.Key.SYNC_NC_URL.getDefault();
		sync_nc.setDefaultValue(defaultServer);
		if(sync_nc.getText() == null)
			sync_nc.setText(defaultServer);
		sync_nc.setSummary(Preferences.getString(Preferences.Key.SYNC_NC_URL));

		String defaultLocation = (String)Preferences.Key.SYNC_SDCARD.getDefault();
		sync_file.setDefaultValue(defaultLocation);
		if(sync_file.getText() == null)
			sync_file.setText(defaultLocation);
/*
		String defaultOrder = (String)Preferences.Key.SORT_ORDER.getDefault();
		String sortOrder = Preferences.getString(Preferences.Key.SORT_ORDER);
		defaultSort.setDefaultValue(defaultOrder);
		if(defaultSort.getValue() == null)
			defaultSort.setValue(defaultOrder);
		if(sortOrder.equals("sort_title"))
			defaultSort.setSummary(getString(R.string.sortByTitle));
		else
			defaultSort.setSummary(getString(R.string.sortByDate));

 */
	}


}
