
package org.tomdroid.reborn;

import android.os.*;
import android.text.*;

import org.xml.sax.*;

import javax.xml.parsers.*;
import java.io.StringReader;

public class NoteContentBuilder {
	
	public static final int PARSE_OK = 0;
	public static final int PARSE_ERROR = 1;
	
	private InputSource noteContentIs;
	
	// this is what we are building here
	private SpannableStringBuilder noteContent = new SpannableStringBuilder();
	
	private final String TAG = "NoteContentBuilder";
	
	// thread related
	private Runnable runner;
	private Handler parentHandler;
	private String subjectName;
	private String noteContentString;
	
	public NoteContentBuilder() {}
	
	public NoteContentBuilder setCaller(Handler parent) {
		
		parentHandler = parent;
		return this;
	}
	
	/**
	 * Allows you to give a string that will be appended to errors in order to make them more useful.
	 * You'll probably want to set it to the Note's title.
	 * @param title
	 * @return this (builder pattern) 
	 */
	public NoteContentBuilder setTitle(String title) {
		
		subjectName = title;
		return this;
	}
	
	public NoteContentBuilder setInputSource(String nc) {
		
		noteContentString = "<note-content>"+nc+"</note-content>";
		noteContentIs = new InputSource(new StringReader(noteContentString));
		return this;
	}
	
	public SpannableStringBuilder build() {
		
		runner = new Runnable() {
			
			public void run() {
				
				
				boolean successful = true;
				
				try {
					// Parsing
			    	// XML 
			    	// Get a SAXParser from the SAXPArserFactory
			        SAXParserFactory spf = SAXParserFactory.newInstance();

			        // trashing the namespaces but keep prefixes (since we don't have the xml header)
			        spf.setFeature("http://xml.org/sax/features/namespaces", false);
			        spf.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
			        SAXParser sp = spf.newSAXParser();

					TLog.v(TAG, "parsing note {0}", subjectName);
					
			        sp.parse(noteContentIs, new NoteContentHandler(noteContent));
			        
					// error finding
					Object[] allSpans = noteContent.getSpans(0, noteContent.length(), Object.class);
					for (Object span : allSpans) {
						TLog.v(TAG, "({0}/{1}) {2}", noteContent.getSpanStart(span), noteContent.getSpanEnd(span), span.getClass().toString());
					}
				} catch (Exception e) {
					e.printStackTrace();
					// TODO handle error in a more granular way
					TLog.e(TAG, "There was an error parsing the note {0}", noteContentString);
					successful = false;
				}
				
				warnHandler(successful);
			}
		};
		Thread thread = new Thread(runner);
		thread.start();
				
		return noteContent;
	}

	
    private void warnHandler(boolean successful) {
		
		// notify the main UI that we are done here (sending an ok along with the note's title)
		Message msg = Message.obtain();
		if (successful) {
			msg.what = PARSE_OK;
		} else {
			
			msg.what = PARSE_ERROR;
		}
		
		parentHandler.sendMessage(msg);
    }
}
