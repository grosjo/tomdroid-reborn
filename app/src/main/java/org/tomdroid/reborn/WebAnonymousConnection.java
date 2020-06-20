package org.tomdroid.reborn;

import java.net.UnknownHostException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

public class WebAnonymousConnection extends WebConnection {

	@Override
	public String get(String uri) throws UnknownHostException
	{
		// Prepare a request object
		HttpGet httpGet = new HttpGet(uri);
		httpGet.addHeader("X-Tomboy-Client", Tomdroid.HTTP_HEADER);
		HttpResponse response = execute(httpGet);
		return parseResponse(response);
	}
	
	@Override
	public String put(String uri, String data) throws UnknownHostException {
		
		// Prepare a request object
		HttpPut httpPut = new HttpPut(uri);
		
		httpPut.setEntity(new StringEntity(data, "UTF-8"));

		httpPut.setHeader("Content-Type", "application/json");
		httpPut.addHeader("X-Tomboy-Client", Tomdroid.HTTP_HEADER);
		HttpResponse response = execute(httpPut);
		return parseResponse(response);
	}
}
