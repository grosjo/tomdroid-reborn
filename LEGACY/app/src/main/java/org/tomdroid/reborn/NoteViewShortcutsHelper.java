package org.tomdroid.reborn;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import static android.content.Intent.ShortcutIconResource.fromContext;

/**
 * @author Piotr Adamski <mcveat@gmail.com>
 */
public class NoteViewShortcutsHelper {
    private final Context context;

    public NoteViewShortcutsHelper(final Context context) {
        this.context = context;
    }

    public Intent getCreateShortcutIntent(final Cursor item) {
        final String name = getNoteTitle(item);
        final Uri uri = Tomdroid.getNoteIntentUri(getNoteId(item));
        return getCreateShortcutIntent(name, uri);
    }

    private Intent getCreateShortcutIntent(final String name, final Uri uri) {
        Intent i = new Intent();
        i.putExtra(Intent.EXTRA_SHORTCUT_INTENT, getNoteViewShortcutIntent(name, uri));
        i.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        final Intent.ShortcutIconResource icon = fromContext(context, R.drawable.ic_shortcut);
        i.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        return i;
    }

    public Intent getBroadcastableCreateShortcutIntent(final Uri uri, final String name) {
        final Intent i = getCreateShortcutIntent(name, uri);
        i.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        return i;
    }

    public Intent getRemoveShortcutIntent(final String name, final Uri uri) {
        final Intent i = new Intent();
        i.putExtra(Intent.EXTRA_SHORTCUT_INTENT, getNoteViewShortcutIntent(name, uri));
        i.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        i.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
        return i;
    }

    private Intent getNoteViewShortcutIntent(final String name, final Uri intentUri) {
        final Intent i = new Intent(Intent.ACTION_VIEW, intentUri, context, ViewNote.class);
        i.putExtra(ViewNote.CALLED_FROM_SHORTCUT_EXTRA, true);
        i.putExtra(ViewNote.SHORTCUT_NAME, name);
        return i;
    }

    private String getNoteTitle(final Cursor item) {
        return item.getString(item.getColumnIndexOrThrow(Note.TITLE));
    }

    private int getNoteId(final Cursor item) {
        return item.getInt(item.getColumnIndexOrThrow(Note.ID));
    }
}
