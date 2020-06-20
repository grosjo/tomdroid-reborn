package org.tomdroid.reborn;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

/**
 * An abstract class that handles some common action bar-related functionality in the app. This
 * class provides functionality useful for both phones and tablets, and does not require any Android
 * 3.0-specific features, although it uses them if available.
 *
 */
public abstract class ActionBarHelper {
    protected Activity mActivity;
    
    protected boolean showHomeButtonEnabled = true;

    public static ActionBarHelper createInstance(Activity activity) {

        return new ActionBarHelperBase(activity);
    }

    protected ActionBarHelper(Activity activity) {
        mActivity = activity;
    }

    public void onCreate(Bundle savedInstanceState) {
    }

    public void onPostCreate(Bundle savedInstanceState) {
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    protected void onTitleChanged(CharSequence title, int color)
    { }

    public abstract void setRefreshActionItemState(boolean refreshing);

    public MenuInflater getMenuInflater(MenuInflater superMenuInflater) {
        return superMenuInflater;
    }
    
    public void setHomeButtonEnabled(boolean state) {
    	showHomeButtonEnabled = state;
    }
}
