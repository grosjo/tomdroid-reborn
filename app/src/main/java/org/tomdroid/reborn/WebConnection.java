package org.tomdroid.reborn;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
//import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

public abstract class WebConnection {
	
	private static final String TAG = "WebConnection";
	
	public abstract String get(String uri) throws UnknownHostException;
	public abstract String put(String uri, String data) throws UnknownHostException;
	
	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}
	
	protected String parseResponse(HttpResponse response) {
		
		if (response == null)
			return "";
		
		String result = null;
		
		// Examine the response status
		TLog.i(TAG, "Response status : {0}", response.getStatusLine().toString());

		// Get hold of the response entity
		HttpEntity entity = response.getEntity();
		// If the response does not enclose an entity, there is no need
		// to worry about connection release

		if (entity != null) {
			
			try {
				InputStream instream;
				
				instream = entity.getContent();
				
				result = convertStreamToString(instream);
				
				TLog.i(TAG, "Received : {0}", result);
				
				// Closing the input stream will trigger connection release
				instream.close();
				
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	protected HttpResponse execute(HttpUriRequest request) throws UnknownHostException {
		
		HttpClient httpclient = WebSSLSocketFactory.getNewHttpClient();
		
		try {
			// Execute the request
			TLog.i(TAG, "Sending http-header: {0}: {1}", "X-Tomboy-Client", Tomdroid.HTTP_HEADER);
			request.addHeader("X-Tomboy-Client", Tomdroid.HTTP_HEADER);
			HttpResponse response = httpclient.execute(request);
			return response;
			
		}catch (UnknownHostException e){
			throw e;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (Exception e) {
			TLog.i(TAG, "Somethings wrong with your HTTP request. Maybe errors in SSL, certificate?");
			e.printStackTrace();
		}
		
		return null;
	}
}