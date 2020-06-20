package org.tomdroid.reborn;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.util.Linkify;
import android.text.util.Linkify.MatchFilter;
import android.text.util.Linkify.TransformFilter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO this class is starting to smell
public class ViewNote extends ActionBarActivity {
	public static final String CALLED_FROM_SHORTCUT_EXTRA = "org.tomdroid.CALLED_FROM_SHORTCUT";
    public static final String SHORTCUT_NAME = "org.tomdroid.SHORTCUT_NAME";

    // UI elements
	private TextView content;
	private TextView title;

	// Model objects
	private Note note;

	private SpannableStringBuilder noteContent;

	// Logging info
	private static final String TAG = "ViewNote";
    // UI feedback handler
	
	private Uri uri;

	// TODO extract methods in here
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Preferences.init(this, Tomdroid.CLEAR_PREFERENCES);
		setContentView(R.layout.note_view);
		
		content = (TextView) findViewById(R.id.content);
		title = (TextView) findViewById(R.id.title);

		// this we will call on resume as well.
		updateTextAttributes();
        uri = getIntent().getData();
    }

	private void handleNoteUri(final Uri uri) {// We were triggered by an Intent URI
        TLog.d(TAG, "ViewNote started: Intent-filter triggered.");

        // TODO validate the good action?
        // intent.getAction()

        // TODO verify that getNote is doing the proper validation
        note = NoteManager.getNote(this, uri);

        if(note != null) {
			title.setText((CharSequence) note.getTitle());
            noteContent = note.getNoteContent(noteContentHandler);
        } else {
            TLog.d(TAG, "The note {0} doesn't exist", uri);
            showNoteNotFoundDialog(uri);
        }
    }

    private void showNoteNotFoundDialog(final Uri uri) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        addCommonNoteNotFoundDialogElements(builder);
        addShortcutNoteNotFoundElements(uri, builder);
        builder.show();
    }

    private void addShortcutNoteNotFoundElements(final Uri uri, final AlertDialog.Builder builder) {
        final boolean proposeShortcutRemoval;
        final boolean calledFromShortcut = getIntent().getBooleanExtra(CALLED_FROM_SHORTCUT_EXTRA, false);
        final String shortcutName = getIntent().getStringExtra(SHORTCUT_NAME);
        proposeShortcutRemoval = calledFromShortcut && uri != null && shortcutName != null;

        if (proposeShortcutRemoval) {
            final Intent removeIntent = new NoteViewShortcutsHelper(this).getRemoveShortcutIntent(shortcutName, uri);
            builder.setPositiveButton(getString(R.string.btnRemoveShortcut), new OnClickListener() {
                public void onClick(final DialogInterface dialogInterface, final int i) {
                    sendBroadcast(removeIntent);
                    finish();
                }
            });
        }
    }

    private void addCommonNoteNotFoundDialogElements(final AlertDialog.Builder builder) {
        builder.setMessage(getString(R.string.messageNoteNotFound))
                .setTitle(getString(R.string.titleNoteNotFound))
                .setNeutralButton(getString(R.string.btnOk), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
    }

	@Override
	public void onResume(){
		TLog.v(TAG, "resume view note");
		super.onResume();

        if (uri == null) {
			TLog.d(TAG, "The Intent's data was null.");
            showNoteNotFoundDialog(uri);
        } else handleNoteUri(uri);
		updateTextAttributes();
	}
	
	private void updateTextAttributes() {
		float baseSize = Float.parseFloat(Preferences.getString(Preferences.Key.BASE_TEXT_SIZE));
		content.setTextSize(baseSize);
		title.setTextSize(baseSize*1.3f);

		title.setTextColor(Color.BLUE);
		title.setPaintFlags(title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		title.setBackgroundColor(0xffffffff);

		content.setBackgroundColor(0xffffffff);
		content.setTextColor(Color.DKGRAY);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Create the menu based on what is defined in res/menu/noteview.xml
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_note, menu);
		
        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	        case android.R.id.home:
	        	// app icon in action bar clicked; go home
                Intent intent = new Intent(this, Tomdroid.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            	return true;
			case R.id.menuPrefs:
				startActivity(new Intent(this, PreferencesActivity.class));
				return true;
            case R.id.view_note_send:
            	showDialog(Tomdroid.DIALOG_SEND_CHOOSE);
				return true;
			case R.id.view_note_edit:
				startEditNote();
				return true;
			case R.id.view_note_delete:
				deleteNote();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		TLog.i(TAG, "id: {0}, Tomdroid: {1}", id, Tomdroid.DIALOG_SEND_CHOOSE);
	    super.onCreateDialog (id);
		switch(id) {
		    case Tomdroid.DIALOG_SEND_CHOOSE:
                return new AlertDialog.Builder(this)
				.setMessage(getString(R.string.sendChoice))
				.setTitle(getString(R.string.sendChoiceTitle))
		        .setPositiveButton(getString(R.string.btnSendAsFile), null)
				.setNegativeButton(getString(R.string.btnSendAsText), null)
				.create();
		    default:
		    	return null;
	    }
	}
	@Override
	protected void onPrepareDialog(int id, final Dialog dialog) {
	    super.onPrepareDialog (id, dialog);
	    final Activity activity = this;
	    switch(id) {
		    case Tomdroid.DIALOG_SEND_CHOOSE:
                final Uri intentUri = uri;
		    	((AlertDialog) dialog).setButton(Dialog.BUTTON_POSITIVE, getString(R.string.btnSendAsFile), new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						(new UtilSend(activity, intentUri, true)).send();

					}
				});
		    	((AlertDialog) dialog).setButton(Dialog.BUTTON_NEGATIVE, getString(R.string.btnSendAsText), new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) { 
		                (new UtilSend(activity, intentUri, false)).send();
					}
				});
			    break;
	    }
	}
	
	private void deleteNote() {
		final Activity activity = this;
		new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.delete_note)
        .setMessage(R.string.delete_message)
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
        		NoteManager.deleteNote(activity, note);
        		Toast.makeText(activity, getString(R.string.messageNoteDeleted), Toast.LENGTH_SHORT).show();
        		activity.finish();
            }

        })
        .setNegativeButton(R.string.no, null)
        .show();
	}

	private void showNote(boolean xml) {
		if(xml) {
			content.setText(note.getXmlContent());
			title.setText((CharSequence) note.getTitle());
			this.setTitle(this.getTitle() + " - XML");
			return;
		}
		LinkInternalSpan[] links = noteContent.getSpans(0, noteContent.length(), LinkInternalSpan.class);
		MatchFilter noteLinkMatchFilter = LinkInternalSpan.getNoteLinkMatchFilter(noteContent, links);

		// show the note (spannable makes the TextView able to output styled text)
		content.setText(noteContent, TextView.BufferType.SPANNABLE);

		// add links to stuff that is understood by Android except phone numbers because it's too aggressive
		// TODO this is SLOWWWW!!!!
		int linkFlags = 0;
		

		Linkify.addLinks(content, linkFlags);


			Pattern pattern = NoteManager.buildNoteLinkifyPattern(this, note.getTitle());
	
			if(pattern != null) {
				Linkify.addLinks(
					content,
					pattern,
					Tomdroid.CONTENT_URI+"/",
					noteLinkMatchFilter,
					noteTitleTransformFilter
				);
	
				// content.setMovementMethod(LinkMovementMethod.getInstance());
			}
		title.setText((CharSequence) note.getTitle());
	}

	private Handler noteContentHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			//parsed ok - show
			if(msg.what == NoteContentBuilder.PARSE_OK) {
				showNote(false);

			//parsed not ok - error
			} else if(msg.what == NoteContentBuilder.PARSE_ERROR) {

				new AlertDialog.Builder(ViewNote.this)
					.setMessage(getString(R.string.messageErrorNoteParsing))
					.setTitle(getString(R.string.error))
					.setNeutralButton(getString(R.string.btnOk), new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							showNote(false);
						}})
					.show();
        	}
		}
	};

	// custom transform filter that takes the note's title part of the URI and translate it into the note id
	// this was done to avoid problems with invalid characters in URI (ex: ? is the query separator but could be in a note title)
	private TransformFilter noteTitleTransformFilter = new TransformFilter() {

		public String transformUrl(Matcher m, String str) {

			int id = NoteManager.getNoteId(ViewNote.this, str);

			// return something like content://org.tomdroid.notes/notes/3
			return Tomdroid.CONTENT_URI.toString()+"/"+id;
		}
	};

    protected void startEditNote() {
		final Intent i = new Intent(Intent.ACTION_VIEW, uri, this, EditNote.class);
		startActivity(i);
	}
	
}
