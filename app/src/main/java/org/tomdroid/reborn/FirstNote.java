package org.tomdroid.reborn;

import android.app.Activity;
import android.text.TextUtils;

/**
 * Creates an introductory note object 
 * @author Olivier Bilodeau <olivier@bottomlesspit.org>
 *
 */
public class FirstNote {

	// Logging info
	private static final String	TAG = "FirstNote";
	
	public static Note createFirstNote(Activity activity) {
		TLog.v(TAG, "Creating first note");
		
		Note note = new Note();
		
		note.setTitle(activity.getString(R.string.firstNoteTitle));
		// FIXME as soon as we can create notes, make sure GUID is unique! - we are referencing this UUID elsewhere, don't forget to check! 
		note.setGuid("8f837a99-c920-4501-b303-6a39af57a714");
		note.setLastChangeDate("2010-10-09T16:50:12.219-04:00");
		
		
		// reconstitute HTML in note content 

		String[] contentarray = activity.getResources().getStringArray(R.array.firstNoteContent);
		String content = TextUtils.join("\n", contentarray);
		
		content = content.replaceAll("(?m)^=(.+)=$", "<size:large>$1</size:large>")
				.replaceAll("(?m)^-(.+)$", "<list-item dir=\"ltr\">$1</list-item>")
				.replaceAll("/list-item>\n<list-item", "/list-item><list-item")
				.replaceAll("(<list-item.+</list-item>)", "<list>$1</list>")
				.replaceAll("/list-item><list-item", "/list-item>\n<list-item");
		
		note.setXmlContent(content);
		
		return note;
	}
}
