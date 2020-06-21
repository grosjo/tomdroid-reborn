package org.tomdroid.reborn;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.util.Linkify.MatchFilter;
import android.view.View;

/*
 * This class is responsible for parsing the xml note content
 * and formatting the contents in a SpannableStringBuilder
 */
public class LinkInternalSpan extends ClickableSpan {

	// Logging info
	private static final String TAG = "LinkInternalSpan";
	
	private String title;
	public LinkInternalSpan(String title) {
		super();
		this.title = title;
	}

	@Override
	public void onClick(View v) {
		Activity act = (Activity)v.getContext();
		int id = NoteManager.getNoteId(act, title);
		Uri intentUri;
		if(id != 0) {
			intentUri = Uri.parse(Tomdroid.CONTENT_URI.toString()+"/"+id);
		} else {
			/* TODO: open new note */
			TLog.d(TAG, "link: {0} was clicked", title);
			return;
		}
		Intent i = new Intent(Intent.ACTION_VIEW, intentUri);
		act.startActivity(i);
	}
	
	public static MatchFilter getNoteLinkMatchFilter(final SpannableStringBuilder noteContent, final LinkInternalSpan[] links) {
		
		return new MatchFilter() {
			
			public boolean acceptMatch(CharSequence s, int start, int end) {
				int spanstart, spanend;
				for(LinkInternalSpan link: links) {
					spanstart = noteContent.getSpanStart(link);
					spanend = noteContent.getSpanEnd(link);
					if(!(end <= spanstart || spanend <= start)) {
						return false;
					}
				}
				return true;
			}
		};
	}
}
