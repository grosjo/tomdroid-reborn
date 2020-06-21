
package org.tomdroid.reborn;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.Html;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

public class Note
{

	private static final String TAG = "Note";

	public static final int NOTE_BULLET_INTENT_FACTOR = 30;			// intent factor of bullet lists
	public static final String NOTE_MONOSPACE_TYPEFACE = "monospace";
	public static final float NOTE_SIZE_SMALL_FACTOR = 0.8f;
	public static final float NOTE_SIZE_LARGE_FACTOR = 1.5f;
	public static final float NOTE_SIZE_HUGE_FACTOR = 1.8f;

	public static final String NOTE_CONTENT_PLAIN = "content_plain";

	// Members
	public static final String ID = "_id";
	public long id;

	public static final String GUID = "guid";
	public String Guid;

	public static final String CREATION_DATE = "creationdate";
	public TDate CreationDate;

	public static final String CHANGE_DATE = "changedate";
	public TDate ChangeDate;

	public static final String META_CHANGE_DATE = "metachangedate";
	public TDate MetaChangeDate;

	public static final String VERSION = "version";
	public String Version;

	public static final String REVISION = "rev";
	public int Rev;

	public static final String SYNC_DATE = "syncdate";
	public TDate SyncDate;

	public static final String TITLE = "title";
	private String Title;

	public static final String NOTE_CONTENT = "content";
	private String Content;

	public static final String NOTE_PARAMS = "params";
	public boolean OpenOnStartup;
	public boolean Pinned;
	private int CursorPosition, SelectBoundPosition;
	private int Width;
	private int Height;
	private int X,Y;

	public static final String TAGS = "tags";
	private ArrayList<String> Tags;
	public String Error;

	public Note()
	{
		id =0;
		Guid = CreateGUID();
		CreationDate = new TDate();
		ChangeDate = null;
		MetaChangeDate = null;
		Version = "0.3";
		Rev = 0;
		SyncDate = null;
		Title = "New note "+CreationDate.formatTomboy();
		Content = "Empty note";
		OpenOnStartup = false;
		Pinned = false;
		CursorPosition = 0;
		SelectBoundPosition = 0;
		Width = 300; Height=300;
		X =10; Y=10;
		Tags = new ArrayList<String>();
		Error = "";
	}

	public Note(JSONObject json)
	{
		/*
		// These methods return an empty string if the key is not found
		setTitle(XmlUtils.unescape(json.optString("title")));
		setGuid(json.optString("guid"));
		setLastChangeDate(json.optString("last-change-date"));
		String newXMLContent = json.optString("note-content");
		setXmlContent(newXMLContent);
		JSONArray jtags = json.optJSONArray("tags");
		String tag;
		tags = new String();
		if (jtags != null) {
			for (int i = 0; i < jtags.length(); i++ ) {
				tag = jtags.optString(i);
				tags += tag + ",";
			}
		}
		*/
	}

	public static String CreateGUID()
	{
		return UUID.randomUUID().toString();
	}

	public static String ReplaceAngles(String s)
	{
		s = s.replaceAll("&lt;","<");
		s = s.replaceAll("&gt;",">");
		s = s.replaceAll("&x9;","\t");
		s = s.replaceAll("&apos;","'");
		s = s.replaceAll("&quot;","\"");
		return s.replaceAll("&amp;","&");
	}

	public static String EncodeAngles(String s)
	{
		s = s.replaceAll("<","&lt;");
		s = s.replaceAll(">","&gt;");
		s = s.replaceAll("\t","&x9;");
		s = s.replaceAll("'","&apos;");
		s = s.replaceAll("\"","&quot;");
		return s.replaceAll("&","&amp;");
	}

	public void setTitle(String s)
	{
		Title = s;
		ChangeDate = new TDate();
	}

	public void setContent(String s)
	{
		Content = s;
		ChangeDate = new TDate();
	}

	public void setPosition(int x,int y)
	{
		X = x; Y = y;
		MetaChangeDate = new TDate();
	}

	public void setSize(int w,int h)
	{
		Width = w; Height = h;
		MetaChangeDate = new TDate();
	}

	public void setCursor(int pos,int s)
	{
		CursorPosition = pos;
		SelectBoundPosition = s;
		MetaChangeDate = new TDate();
	}

	public void addTag(String tag)
	{
		int i=0;
		while (i<Tags.size())
		{
			if(Tags.get(i).compareTo(tag)==0) return;
			i = i+1;
		}
		Tags.add(tag);
		MetaChangeDate = new TDate();
	}
	
	public void removeTag(String tag)
	{
		int i=0;
		while(i<Tags.size())
		{
			if(Tags.get(i).compareTo(tag)==0)
			{
				Tags.remove(i);
				return;
			}
			i=i+1;
		}
		MetaChangeDate = new TDate();
	}

	@Override
	public String toString()
	{
		return new String("Note: "+Title + " (" + ChangeDate + ")");
	}

	private String tagsToXml()
	{
		String tags = "";

		if(Tags.size()>0)
		{
			int i=0;
			while(i<Tags.size())
			{
				tags = tags + "<tag>"+Tags.get(i)+"</tag>";
				i++;
			}
			tags = "<tags>"+tags+"</tags>";
		}
		return tags;
	}

	private String tagsToString()
	{
		String tags = "";

		int i=0;
		while(i<Tags.size())
		{
			if(i>0) { tags = tags + ","; }
			tags = tags + Tags.get(i);
			i++;
		}
		return tags;
	}

	// gets full xml to be exported as .note file
	public String toXML()
	{
		String note = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+
				"<note version=\""+Version+"\" >\n"+
				"<title>"+EncodeAngles(Title)+"</title>\n"+
				"<text xml:space=\"preserve\"><note-content version=\""+Version+"\">"
				+EncodeAngles(Title)+"\n\n" // added for compatibility
				+EncodeAngles(Content)+"</note-content></text>\n"
				+"<last-change-date>"+ChangeDate.formatTomboy()+"</last-change-date>\n"
				+"<last-metadata-change-date>"+MetaChangeDate.formatTomboy()+"</last-metadata-change-date>\n"
				+"<create-date>"+CreationDate.formatTomboy()+"</create-date>\n"
				+"<cursor-position>" +CursorPosition+"</cursor-position>\n"
				+"<selection-bound-position>"+SelectBoundPosition+"</selection-bound-position>\n"
				+"<open-on-startup>" +OpenOnStartup+"</open-on-startup>\n"
				+"<width>" +Width+"</width><height>"+Height+"</height>\n"
				+"<x>"+X+"</x><y>"+Y+"</y>\n"
				+tagsToXml()+"\n\t<open-on-startup>False</open-on-startup>\n</note>\n";

		return note;
	}

	public boolean Load(Activity activity, String guid)
	{
		Uri notes = TPrefs.CONTENT_URI;

		String[] projection = { Note.ID, Note.CREATION_DATE, Note.CHANGE_DATE, Note.META_CHANGE_DATE,
				Note.VERSION, Note.REVISION, Note.SYNC_DATE, Note.TITLE, Note.NOTE_CONTENT,
				Note.NOTE_PARAMS, Note.TAGS };

		String[] whereArgs = new String[1];
		whereArgs[0] = guid;

		ContentResolver cr = activity.getContentResolver();
		Cursor cursor = cr.query(notes,
				projection,
				Note.GUID + "= ?",
				whereArgs,
				null);

		if (cursor == null || cursor.getCount() == 0) return false;

		id = cursor.getInt(cursor.getColumnIndexOrThrow(Note.ID));
		Guid = guid;
		CreationDate = new TDate(cursor.getLong(cursor.getColumnIndexOrThrow(Note.CREATION_DATE)));
		ChangeDate = new TDate(cursor.getLong(cursor.getColumnIndexOrThrow(Note.CHANGE_DATE)));
		MetaChangeDate = new TDate(cursor.getLong(cursor.getColumnIndexOrThrow(Note.META_CHANGE_DATE)));
		Version = cursor.getString(cursor.getColumnIndexOrThrow(Note.VERSION));
		Rev = cursor.getInt(cursor.getColumnIndexOrThrow(Note.REVISION));
		SyncDate = new TDate(cursor.getLong(cursor.getColumnIndexOrThrow(Note.SYNC_DATE)));;
		Title = cursor.getString(cursor.getColumnIndexOrThrow(Note.TITLE));
		Content = cursor.getString(cursor.getColumnIndexOrThrow(Note.NOTE_CONTENT));
		String params = cursor.getString(cursor.getColumnIndexOrThrow(Note.NOTE_PARAMS));
		String[] par = params.split(",");
		OpenOnStartup = (par[0].compareTo("1")==0);
		Pinned = (par[1].compareTo("1")==0);
		CursorPosition = Integer.parseInt(par[2]);
		SelectBoundPosition = Integer.parseInt(par[3]);;
		Width = Integer.parseInt(par[4]);
		Height = Integer.parseInt(par[5]);
		X = Integer.parseInt(par[6]);
		Y = Integer.parseInt(par[7]);;

		Tags = new ArrayList<String>();
		params = cursor.getString(cursor.getColumnIndexOrThrow(Note.TAGS));
		par = params.split(",");
		int i = par.length;
		while(i>0) { i--; Tags.add(par[i]); }
		Error = "";

		return true;
	}

	public Uri Save(Activity activity)
	{
		Uri notes = TPrefs.CONTENT_URI;

		String[] select = new String[1];
		select[0] = ID;

		String[] whereArgs = new String[1];
		whereArgs[0] = Guid;

		ContentResolver cr = activity.getContentResolver();
		Cursor cursor = cr.query(notes,
				select,
				Note.GUID + "= ?",
				whereArgs,
				null);

		ContentValues values = new ContentValues();
		values.put(Note.GUID, Guid);
		values.put(Note.CREATION_DATE, CreationDate.toLong());
		values.put(Note.CHANGE_DATE, ChangeDate.toLong());
		values.put(Note.META_CHANGE_DATE, MetaChangeDate.toLong());
		values.put(Note.VERSION, Version);
		values.put(Note.REVISION, Rev);
		values.put(Note.SYNC_DATE, SyncDate.toLong());
		values.put(Note.TITLE, Title);
		values.put(Note.NOTE_CONTENT, Content);
		String params = "";
		if(OpenOnStartup) { params = "1"; } else { params = "0"; }
		if(Pinned) { params = params + ",1"; } else { params = params + ",0"; }
		params = params + "," + CursorPosition;
		params = params + "," + SelectBoundPosition;
		params = params + "," + Width;
		params = params + "," + Height;
		params = params + "," + X;
		params = params + "," + Y;
		values.put(Note.NOTE_PARAMS, params);
		values.put(Note.TAGS,tagsToString());

		Uri uri = null;

		if (cursor == null || cursor.getCount() == 0)
		{
			TLog.v(TAG, "A new note has been detected (not yet in db)");
			uri = cr.insert(TPrefs.CONTENT_URI, values);
			String l = uri.getLastPathSegment();
			id = Long.parseLong(l);
			TLog.v(TAG, "Note inserted in content provider. URL: {0} ID: {1} TITLE:{2} GUID:{3}", id, uri, Title,Guid);
		}
		else
		{
			id = cursor.getInt(cursor.getColumnIndexOrThrow(Note.ID));
			cr.update(TPrefs.CONTENT_URI, values, Note.GUID+" = ?", whereArgs);
			uri = Uri.parse(TPrefs.CONTENT_URI+"/"+id);
			TLog.v(TAG, "Note updated in content provider. URL: {0} ID: {1} TITLE:{2} GUID:{3}", id, uri, Title,Guid);
		}
		cursor.close();

		return uri;
	}
}
