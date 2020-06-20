package org.tomdroid.reborn;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * @author Piotr Adamski <mcveat@gmail.com>
 */
public class ShortcutActivity extends ActionBarListActivity {
    private final String TAG = ShortcutActivity.class.getName();
    private ListAdapter adapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Preferences.init(this, Tomdroid.CLEAR_PREFERENCES);
        TLog.d(TAG, "creating shortcut...");
        setContentView(R.layout.shortcuts_list);
		// Disable the tomdroid icon home button
		setHomeButtonEnabled(false);
        setTitle(R.string.shortcuts_view_caption);
        adapter = NoteManager.getListAdapter(this);
        setListAdapter(adapter);
        getListView().setEmptyView(findViewById(R.id.list_empty));

    }

    @Override
    protected void onListItemClick(final ListView l, final View v, final int position, final long id) {
        final Cursor item = (Cursor) adapter.getItem(position);
        final NoteViewShortcutsHelper helper = new NoteViewShortcutsHelper(this);
        setResult(RESULT_OK, helper.getCreateShortcutIntent(item));
        finish();
    }
}
