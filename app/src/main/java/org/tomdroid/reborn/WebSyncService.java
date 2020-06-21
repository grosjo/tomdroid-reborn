package org.tomdroid.reborn;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import org.json.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class WebSyncService extends SyncService implements SyncServiceAuth {

	private static final String TAG = "SnowySyncService";
	private String lastGUID;
	private long latestRemoteRevision = -1;
	private long latestLocalRevision = -1;

	public WebSyncService(Activity activity, Handler handler) {
		super(activity, handler);
	}

	@Override
	public int getDescriptionAsId() {
		return R.string.prefTomboyWeb;
	}

	@Override
	public String getName() {
		return "tomboy-web";
	}

	public boolean isConfigured() {
		//WebOAuthConnection auth = getAuthConnection();
		return auth.isAuthenticated();
	}

	@Override
	public boolean needsServer() {
		return true;
	}

	@Override
	public boolean needsLocation() {
		return false;
	}

	@Override
	public boolean needsAuth() {
		//WebOAuthConnection auth = getAuthConnection();
		return !auth.isAuthenticated();
	}

	public void getAuthUri(final String server, final Handler handler) {

		execInThread(new Runnable() {

			public void run() {

				// Reset the authentication credentials
				//WebOAuthConnection auth = new WebOAuthConnection();
				Uri authUri = null;

				try {
					authUri = auth.getAuthorizationUrl(server);

				} catch (UnknownHostException e) {
					TLog.e(TAG, "Internet connection not available");
					sendMessage(NO_INTERNET);
				}

				Message message = new Message();
				message.obj = authUri;
				handler.sendMessage(message);
			}

		});
	}

	public void remoteAuthComplete(final Uri uri, final Handler handler) {

		execInThread(new Runnable() {

			public void run() {

				try {
					// TODO: might be intelligent to show something like a
					// progress dialog
					// else the user might try to sync before the authorization
					// process
					// is complete
					//WebOAuthConnection auth = getAuthConnection();
					boolean result = auth.getAccess(uri
							.getQueryParameter("oauth_verifier"));

					if (result) {
						TLog.i(TAG, "The authorization process is complete.");
						handler.sendEmptyMessage(AUTH_COMPLETE);
						return;
						//sync(true);
					} else {
						TLog.e(TAG,
								"Something went wrong during the authorization process.");
						sendMessage(AUTH_FAILED);
					}
				} catch (UnknownHostException e) {
					TLog.e(TAG, "Internet connection not available");
					sendMessage(NO_INTERNET);
				}

				// We don't care what we send, just remove the dialog
				handler.sendEmptyMessage(0);
			}
		});
	}

	@Override
	public boolean isSyncable() {
		return super.isSyncable() && isConfigured();
	}

	@Override
	protected void getNotesForSync(final boolean push) {
		this.push = push;
		
		// start loading snowy notes
		setSyncProgress(0);
		this.lastGUID = null;

		TLog.v(TAG, "Loading Snowy notes");

		final String userRef = Preferences
				.getString(Preferences.Key.SYNC_SERVER_USER_API);

		syncInThread(new Runnable() {


			public void run() {

				//WebOAuthConnection auth = getAuthConnection();
				latestRemoteRevision = (int)Preferences.getLong(Preferences.Key.LATEST_SYNC_REVISION);

				try {
					TLog.v(TAG, "contacting " + userRef);
					String rawResponse = auth.get(userRef);
					if(cancelled) {
						doCancel();
						return; 
					}
					if (rawResponse == null) {
						sendMessage(CONNECTING_FAILED);
						setSyncProgress(100);
						return;
					}

					setSyncProgress(30);

					try {
						JSONObject response = new JSONObject(rawResponse);

						// get notes list without content, to check for revision
						
						String notesUrl = response.getJSONObject("notes-ref").getString("api-ref");
						rawResponse = auth.get(notesUrl);
						response = new JSONObject(rawResponse);
						
						latestLocalRevision = (Long)Preferences.getLong(Preferences.Key.LATEST_SYNC_REVISION);
						
						setSyncProgress(35);

						latestRemoteRevision = response.getLong("latest-sync-revision");
						sendMessage(LATEST_REVISION,(int)latestRemoteRevision,0);
						TLog.d(TAG, "old latest sync revision: {0}, remote latest sync revision: {1}", latestLocalRevision, latestRemoteRevision);

						Cursor newLocalNotes = NoteManager.getNewNotes(activity); 
						
						// same sync revision + no new local notes = no need to sync
						
						if (latestRemoteRevision <= latestLocalRevision && newLocalNotes.getCount() == 0) {
							TLog.v(TAG, "old sync revision on server, cancelling");
							finishSync(true);
							return;
						}

						// don't get notes if older revision - only pushing notes
						
						if (push && latestRemoteRevision <= latestLocalRevision) {
							TLog.v(TAG, "old sync revision on server, pushing new notes");
							
							JSONArray notes = response.getJSONArray("notes");
							List<String> notesList = new ArrayList<String>();
							for (int i = 0; i < notes.length(); i++)
								notesList.add(notes.getJSONObject(i).optString("guid"));
							prepareSyncableNotes(newLocalNotes);
							setSyncProgress(50);
							return;
						}
						
						// get notes list with content to find changes
						
						TLog.v(TAG, "contacting " + notesUrl);
						sendMessage(SYNC_CONNECTED);
						rawResponse = auth.get(notesUrl + "?include_notes=true");
						if(cancelled) {
							doCancel();
							return; 
						}
						response = new JSONObject(rawResponse);
						latestRemoteRevision = response.getLong("latest-sync-revision");
						sendMessage(LATEST_REVISION,(int)latestRemoteRevision,0);

						JSONArray notes = response.getJSONArray("notes");
						setSyncProgress(50);

						TLog.v(TAG, "number of notes: {0}", notes.length());

						ArrayList<Note> notesList = new ArrayList<Note>();

						for (int i = 0; i < notes.length(); i++)
							notesList.add(new Note(notes.getJSONObject(i)));

						if(cancelled) {
							doCancel();
							return; 
						}						
						
						// close cursor
						newLocalNotes.close();
						prepareSyncableNotes(notesList);
						
					} catch (JSONException e) {
						TLog.e(TAG, e, "Problem parsing the server response");
						sendMessage(PARSING_FAILED,
								ErrorList.createErrorWithContents(
										"JSON parsing", "json", e, rawResponse));
						setSyncProgress(100);
						return;
					}
				} catch (java.net.UnknownHostException e) {
					TLog.e(TAG, "Internet connection not available");
					sendMessage(NO_INTERNET);
					setSyncProgress(100);
					return;
				}
				if(cancelled) {
					doCancel();
					return; 
				}
				if (isSyncable())
					finishSync(true);
			}
		});
	}

	public void finishSync(boolean refresh) {

		// delete leftover local notes
		NoteManager.purgeDeletedNotes(activity);
		
		TTime now = new TTime();
		now.setToNow();
		String nowString = now.formatTomboy();
		Preferences.putString(Preferences.Key.LATEST_SYNC_DATE, nowString);
		Preferences.putLong(Preferences.Key.LATEST_SYNC_REVISION, latestRemoteRevision);

		setSyncProgress(100);
		if (refresh)
			sendMessage(PARSING_COMPLETE);
	}

	private /*WebOAuthConnection*/TOTO getAuthConnection() {

		// TODO: there needs to be a way to reset these values, otherwise cannot
		// change server!

		//WebOAuthConnection auth = new WebOAuthConnection();

		auth.accessToken = Preferences.getString(Preferences.Key.NC_ACCESS_TOKEN);
		auth.accessTokenSecret = Preferences
				.getString(Preferences.Key.NC_ACCESS_TOKEN_SECRET);
		auth.requestToken = Preferences
				.getString(Preferences.Key.NC_REQUEST_TOKEN);
		auth.requestTokenSecret = Preferences
				.getString(Preferences.Key.NC_REQUEST_TOKEN_SECRET);
		auth.oauth10a = Preferences.getBoolean(Preferences.Key.NC_OAUTH_10A);
		auth.authorizeUrl = Preferences
				.getString(Preferences.Key.NC_AUTHORIZE_URL);
		auth.accessTokenUrl = Preferences
				.getString(Preferences.Key.NC_ACCESS_TOKEN_URL);
		auth.requestTokenUrl = Preferences
				.getString(Preferences.Key.NC_REQUEST_TOKEN_URL);

		return auth;
	}

	// push syncable notes
	@Override
	public void pushNotes(final ArrayList<Note> notes) {
		if(notes.size() == 0)
			return;
		if(cancelled) {
			doCancel();
			return; 
		}		
		final String userRef = Preferences
				.getString(Preferences.Key.SYNC_SERVER_USER_API);
		
		final long newRevision = Preferences.getLong(Preferences.Key.LATEST_SYNC_REVISION)+1;
				
		syncInThread(new Runnable() {
			public void run() {
				//WebOAuthConnection auth = getAuthConnection();
				try {
					TLog.v(TAG, "pushing {0} notes to remote service, sending rev #{1}",notes.size(), newRevision);
					String rawResponse = auth.get(userRef);
					if(cancelled) {
						doCancel();
						return; 
					}		
					try {
						TLog.v(TAG, "creating JSON");

						JSONObject data = new JSONObject();
						data.put("latest-sync-revision", newRevision);
						JSONArray Jnotes = new JSONArray();
						for(Note note : notes) {
							JSONObject Jnote = new JSONObject();
							Jnote.put("guid", note.getGuid());
							
							if(note.getTags().contains("system:deleted")) // deleted note
								Jnote.put("command","delete");
							else { // changed note
								Jnote.put("title", XmlUtils.escape(note.getTitle()));
								Jnote.put("note-content", note.getXmlContent());
								Jnote.put("note-content-version", "0.1");
								Jnote.put("last-change-date", note.getLastChangeDate());
								Jnote.put("create-date", note.getCreateDate());
								Jnote.put("last-metadata-change-date", note.getLastChangeDate());  // TODO: is this different?
							}
							Jnotes.put(Jnote);
						}
						data.put("note-changes", Jnotes);
						
						JSONObject response = new JSONObject(rawResponse);
						if(cancelled) {
							doCancel();
							return; 
						}		
						String notesUrl = response.getJSONObject("notes-ref")
								.getString("api-ref");

						TLog.v(TAG, "put url: {0}", notesUrl);
						
						if(cancelled) {
							doCancel();
							return; 
						}	
						
						TLog.v(TAG, "pushing data to remote service: {0}",data.toString());
						response = new JSONObject(auth.put(notesUrl,
								data.toString()));

						TLog.v(TAG, "put response: {0}", response.toString());
						latestRemoteRevision = response.getLong("latest-sync-revision");
						sendMessage(LATEST_REVISION,(int)latestRemoteRevision,0);

					} catch (JSONException e) {
						TLog.e(TAG, e, "Problem parsing the server response");
						sendMessage(NOTE_PUSH_ERROR,
								ErrorList.createErrorWithContents(
										"JSON parsing", "json", e, rawResponse));
						return;
					}
				} catch (java.net.UnknownHostException e) {
					TLog.e(TAG, "Internet connection not available");
					sendMessage(NO_INTERNET);
					return;
				}
				// success, finish sync
				finishSync(true);
			}

		});
	}

	@Override
	protected void pullNote(final String guid) {

		// start loading snowy notes

		TLog.v(TAG, "pulling remote note");

		final String userRef = Preferences
				.getString(Preferences.Key.SYNC_SERVER_USER_API);

		syncInThread(new Runnable() {

			public void run() {

				//WebOAuthConnection auth = getAuthConnection();

				try {
					TLog.v(TAG, "contacting " + userRef);
					String rawResponse = auth.get(userRef);

					try {
						JSONObject response = new JSONObject(rawResponse);
						String notesUrl = response.getJSONObject("notes-ref")
								.getString("api-ref");

						TLog.v(TAG, "contacting " + notesUrl + guid);

						rawResponse = auth.get(notesUrl + guid
								+ "?include_notes=true");

						response = new JSONObject(rawResponse);
						JSONArray notes = new JSONArray();
						// Specifications say to look in the notes array if we receive many notes
						// However, if we request one single note, it is saved in the "note" array instead.
						try {
							notes = response.getJSONArray("notes");
						} catch (JSONException e) {
							notes = response.getJSONArray("note");
						}
						JSONObject jsonNote = notes.getJSONObject(0);

						TLog.v(TAG, "parsing remote note");

						insertNote(new Note(jsonNote));

					} catch (JSONException e) {
						TLog.e(TAG, e, "Problem parsing the server response");
						sendMessage(NOTE_PULL_ERROR,
								ErrorList.createErrorWithContents(
										"JSON parsing", "json", e, rawResponse));
						return;
					}

				} catch (java.net.UnknownHostException e) {
					TLog.e(TAG, "Internet connection not available");
					sendMessage(NO_INTERNET);
					return;
				}

				sendMessage(NOTE_PULLED);
			}
		});
	}
	public void deleteAllNotes() {

		TLog.v(TAG, "Deleting Snowy notes");

		final String userRef = Preferences.getString(Preferences.Key.SYNC_SERVER_USER_API);
		
		final long newRevision;
		
		if(latestLocalRevision > latestRemoteRevision)
			newRevision = latestLocalRevision+1;
		else
			newRevision = latestRemoteRevision+1;
		
		syncInThread(new Runnable() {

			public void run() {

				//WebOAuthConnection auth = getAuthConnection();

				try {
					TLog.v(TAG, "contacting " + userRef);
					String rawResponse = auth.get(userRef);
					if (rawResponse == null) {
						return;
					}
					try {
						JSONObject response = new JSONObject(rawResponse);
						String notesUrl = response.getJSONObject("notes-ref").getString("api-ref");

						TLog.v(TAG, "contacting " + notesUrl);
						response = new JSONObject(auth.get(notesUrl));

						JSONArray notes = response.getJSONArray("notes");
						setSyncProgress(50);

						TLog.v(TAG, "number of notes: {0}", notes.length());
						
						ArrayList<String> guidList = new ArrayList<String>();

						for (int i = 0; i < notes.length(); i++) {
							JSONObject ajnote = notes.getJSONObject(i);
							guidList.add(ajnote.getString("guid"));
						}

						TLog.v(TAG, "creating JSON");

						JSONObject data = new JSONObject();
						data.put("latest-sync-revision",newRevision);
						JSONArray Jnotes = new JSONArray();
						for(String guid : guidList) {
							JSONObject Jnote = new JSONObject();
							Jnote.put("guid", guid);
							Jnote.put("command","delete");
							Jnotes.put(Jnote);
						}
						data.put("note-changes", Jnotes);

						response = new JSONObject(auth.put(notesUrl,data.toString()));

						TLog.v(TAG, "delete response: {0}", response.toString());

						
						// reset latest sync date so we can push our notes again
						
						latestRemoteRevision = (int)response.getLong("latest-sync-revision");
						Preferences.putLong(Preferences.Key.LATEST_SYNC_REVISION, latestRemoteRevision);
						Preferences.putString(Preferences.Key.LATEST_SYNC_DATE,new TTime().formatTomboy());
						
					} catch (JSONException e) {
						TLog.e(TAG, e, "Problem parsing the server response");
						sendMessage(PARSING_FAILED,
								ErrorList.createErrorWithContents(
										"JSON parsing", "json", e, rawResponse));
						setSyncProgress(100);
						return;
					}
				} catch (java.net.UnknownHostException e) {
					TLog.e(TAG, "Internet connection not available");
					sendMessage(NO_INTERNET);
					setSyncProgress(100);
					return;
				}
				sendMessage(REMOTE_NOTES_DELETED);
			}
		});
	}

	@Override
	public void backupNotes() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void localSyncComplete() {
		Preferences.putLong(Preferences.Key.LATEST_SYNC_REVISION, latestRemoteRevision);
	}
}
