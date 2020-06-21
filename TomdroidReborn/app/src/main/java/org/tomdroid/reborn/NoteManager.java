package org.tomdroid.reborn;

import android.app.Activity;
import androidx.loader.content.*;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.Html;
import android.widget.ListAdapter;

import java.util.*;
import java.util.regex.*;


public class NoteManager
{
	private static final String TAG = "NoteManager";
 	
	private static String sortOrder;
	private static Boolean sortOrderBy;

	public static void setSortOrder(Boolean orderBy)
	{
		sortOrderBy = orderBy;
		if(orderBy) {
			sortOrder = Note.TITLE + " ASC";
		} else {
			sortOrder = Note.CHANGE_DATE + " DESC";
		}
	}

	public static Boolean getSortOrder() {
		return sortOrderBy;
	}

	// check in a note exists in the content provider
	public static boolean noteExists(Activity activity, String guid)
	{
		Uri notes = TPrefs.CONTENT_URI;

		String[] projection = { Note.ID };

		String[] whereArgs = new String[1];
		whereArgs[0] = guid;
		
		// The note identifier is the guid
		ContentResolver cr = activity.getContentResolver();
		Cursor cursor = cr.query(notes,
                projection,
                Note.GUID + "= ?",
                whereArgs,
                null);

		boolean returnvalue = false;
		if (cursor != null && cursor.getCount() != 0) {
			returnvalue = true;
		}
		cursor.close();
		return returnvalue;
	}

	public static void undeleteNote(Activity activity, Note note)
	{
		note.removeTag("system:deleted");
		note.Save(activity);
	}
	
	// this function just adds a "deleted" tag, to allow remote delete when syncing
	public static void deleteNote(Activity activity, Note note)
	{
		note.removeTag("system:deleted");
		note.Save(activity);
	}

	public static void deleteNote(Activity activity, String guid)
	{
		Note note = new Note();
		note.Load(activity,guid);
		deleteNote(activity, note);
	}

	public static void purgeDeletedNotes(Activity activity)
	{
		Uri notes = TPrefs.CONTENT_URI;
		String where = Note.TAGS + " LIKE '%system:deleted%'";
		ContentResolver cr = activity.getContentResolver();
		int rows = cr.delete(notes, where, null);
		TLog.v(TAG, "Deleted {0} local notes based on system:deleted tag",rows);
	}

	public static void deleteAllNotes(Activity activity)
	{
		Uri notes = TPrefs.CONTENT_URI;
		ContentResolver cr = activity.getContentResolver();
		int rows = cr.delete(notes, null, null);
		TLog.v(TAG, "Deleted {0} local notes",rows);
	}

	public static Cursor getAllNotes(Activity activity)
	{
		Uri notes = TPrefs.CONTENT_URI;

		String[] projection = { Note.ID, Note.TITLE, Note.CHANGE_DATE, Note.GUID };
		String where = "(" + Note.TAGS + " NOT LIKE '%" + "system:deleted" + "%')";
		String[] selectionArgs = null;

		CursorLoader cursorLoader = new CursorLoader(
				activity,
				notes,
				projection,
				where,
				selectionArgs,
				sortOrder);

		return cursorLoader.loadInBackground();
	}

	public static Note[] getAllNotesAsNotes(Activity activity, boolean includeNotebookTemplates)
	{
		Uri uri = TPrefs.CONTENT_URI;

		String[] projection = {Note.ID, Note.TITLE, Note.CHANGE_DATE, Note.GUID};

		String where = "(" + Note.TAGS + " NOT LIKE '%" + "system:deleted" + "%')";
		String orderBy = Note.CHANGE_DATE + " DESC";

		String[] selectionArgs = null;

		CursorLoader cursorLoader = new CursorLoader(
				activity,
				uri,
				projection,
				where,
				selectionArgs,
				orderBy);

		Cursor cursor = cursorLoader.loadInBackground();

		if (cursor == null || cursor.getCount() == 0)
		{
			TLog.d(TAG, "no notes in cursor");
			return null;
		}
		TLog.d(TAG, "{0} notes in cursor", cursor.getCount());
		Note[] notes = new Note[cursor.getCount()];
		cursor.moveToFirst();
		int key = 0;

		while (!cursor.isAfterLast())
		{
			Note note = new Note();
			note.Guid = cursor.getString(cursor.getColumnIndexOrThrow(Note.GUID));
			notes[key++] = note;
			cursor.moveToNext();
		}
		cursor.close();

		while (key > 0)
		{
			key--;
			notes[key].Load(activity, notes[key].Guid);
		}
		return notes;
	}
	

	// gets the titles of the notes present in the db, used in ViewNote.buildLinkifyPattern()
	public static Cursor getTitles(Activity activity)
	{
		String[] projection = { Note.TITLE, Note.GUID };

		String selection = Note.TAGS + " NOT LIKE '%system:deleted%'";
		String[] selectionArgs = null;
		String sortOrder = null;

		CursorLoader cursorLoader = new CursorLoader(
				activity,
				TPrefs.CONTENT_URI,
				projection,
				selection,
				selectionArgs,
				sortOrder);

		return cursorLoader.loadInBackground();
	}
	
	// gets the ids of the notes present in the db, used in SyncService.deleteNotes()
	public static Cursor getGuids(Activity activity)
	{
		String[] projection = { Note.ID, Note.GUID };

		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = null;

		CursorLoader cursorLoader = new CursorLoader(
				activity,
				TPrefs.CONTENT_URI,
				projection,
				selection,
				selectionArgs,
				sortOrder);

		return cursorLoader.loadInBackground();
	}

	public static Cursor getNewNotes(Activity activity)
	{
		String[] projection = { Note.ID, Note.GUID };

		String selection = Note.CHANGE_DATE + " > "+TPrefs.getString(TPrefs.Key.LATEST_SYNC_DATE)+")";
		String[] selectionArgs = null;
		String sortOrder = null;

		CursorLoader cursorLoader = new CursorLoader(
				activity,
				TPrefs.CONTENT_URI,
				projection,
				selection,
				selectionArgs,
				sortOrder);

		return cursorLoader.loadInBackground();
	}


	public static Pattern buildNoteLinkifyPattern(Activity activity, String noteTitle)  {
	
		StringBuilder sb = new StringBuilder();
		Cursor cursor = getTitles(activity);
	
		// cursor must not be null and must return more than 0 entry
		if (!(cursor == null || cursor.getCount() == 0)) {
	
			String title;
	
			cursor.moveToFirst();
	
			do {
				title = cursor.getString(cursor.getColumnIndexOrThrow(Note.TITLE));
				if(title.length() == 0 || title.equals(noteTitle))
					continue;
				// Pattern.quote() here make sure that special characters in the note's title are properly escaped
				sb.append("("+Pattern.quote(title)+")|");
	
			} while (cursor.moveToNext());
			
			// if only empty titles, return
			if (sb.length() == 0)
				return null;
			
			// get rid of the last | that is not needed (I know, its ugly.. better idea?)
			String pt = sb.substring(0, sb.length()-1);
	
			// return a compiled match pattern
			return Pattern.compile(pt, Pattern.CASE_INSENSITIVE);
	
		} else {
	
			// TODO send an error to the user
			TLog.d(TAG, "Cursor returned null or 0 notes");
		}
		cursor.close();
		
		return null;
	}
	
	public static Boolean toggleSortOrder()
	{
		Boolean orderBy = !getSortOrder();
		setSortOrder(orderBy);
		return orderBy;
	}
}
