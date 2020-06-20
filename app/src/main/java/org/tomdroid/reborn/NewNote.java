
package org.tomdroid.reborn;

import java.util.UUID;
import android.content.Context;


/**
 * Creates a new note object 
 *
 */
public class NewNote {

	// Logging info
	private static final String	TAG = "NewNote";
	// indicates, if note was never saved before (for dismiss dialogue)
	
	public static Note createNewNote(Context context, String title, String xmlContent) {
		TLog.v(TAG, "Creating new note");
		
		Note note = new Note();
		
		note.setTitle(title);

		UUID newid = UUID.randomUUID();
		note.setGuid(newid.toString());
		note.setLastChangeDate();
		note.setXmlContent(xmlContent);
		
		return note;
	}

}
