package org.tomdroid.reborn;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;

import org.jboss.com.sun.net.httpserver.HttpExchange;
import org.jboss.com.sun.net.httpserver.HttpHandler;
import org.jboss.com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;

import com.sun.net.*;
import com.sun.net.httpserver.*;

public class NextcloudConnection
{
    private static final String TAG = "OAuthConnection";
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final String USER_AGENT = "TomdroidReborn-1.0" ;
    private static final String OAUTH_CALLBACK_URL = "http://localhost:8000/tomdroid-web-sync/";

    private String accessTokenUrl, requestTokenUrl, authorizeUrl, NCKey, NCToken, NCTokenSecret, Verifier;

    private Activity activity;
    private HttpServer webserver;

    public NextcloudConnection(Activity a)
    {
        NCKey = Preferences.getString(Preferences.Key.NC_KEY);
        NCToken = Preferences.getString(Preferences.Key.NC_TOKEN);
        NCTokenSecret = Preferences.getString(Preferences.Key.NC_TOKEN_SECRET);

        activity = a;
    }

    String WebGet(String u,ArrayList<String> params) throws Exception
    {
        TLog.i(TAG,"Webget("+u+")");
        TLog.i(TAG,"Encoding "+StandardCharsets.UTF_8.name());

        // Adding params
        if(params != null)
        {
            int i = 0;
            while (i < params.size())
            {
                if (i == 0) { u = u + '?'; } else { u = u + '&'; }
                u = u + URLEncoder.encode(params.get(i), StandardCharsets.UTF_8.name()) + '=' + URLEncoder.encode(params.get(i + 1), StandardCharsets.UTF_8.name());
                i = i + 2;
            }
        }

        URL url = new URL(u);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.name());
        urlConnection.setUseCaches(false);
        urlConnection.setDoInput(true);
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("User-Agent", USER_AGENT);

        String result = "";

        int responseCode = urlConnection.getResponseCode();
        if(responseCode == HttpURLConnection.HTTP_OK)
        {
            BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
            String l;
            StringBuffer response = new StringBuffer();

            while ((l = r.readLine()) != null)
            {
                response.append(l);
            }
            r.close();
            result = response.toString();
        }
        urlConnection.disconnect();
        return result;
    }

    String WebPost(String u,ArrayList<String> params) throws Exception
    {
        TLog.i(TAG,"WebPost("+u+")");
        TLog.i(TAG,"Encoding "+StandardCharsets.UTF_8.name());

        String post = "";
        // Adding params
        if(params != null)
        {
            int i = 0;
            while (i < params.size())
            {
                if (i > 0)  { post = post + '&'; }
                post = post + params.get(i) + '=' + URLEncoder.encode(params.get(i + 1), StandardCharsets.UTF_8.name());
                i = i + 2;
            }
        }

        URL url = new URL(u);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.name());
        urlConnection.setUseCaches(false);
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("User-Agent", USER_AGENT);

        OutputStream os = urlConnection.getOutputStream();
        os.write(post.getBytes());
        os.flush();
        os.close();

        String result = "";

        int responseCode = urlConnection.getResponseCode();
        if(responseCode == HttpURLConnection.HTTP_OK)
        {
            BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
            String l;
            StringBuffer response = new StringBuffer();

            while ((l = r.readLine()) != null)
            {
                response.append(l);
            }
            r.close();
            result = response.toString();
        }
        urlConnection.disconnect();
        return result;
    }

    void OauthParamsSort(ArrayList<String> params)
    {
        ArrayList<String> p = new ArrayList<String>();

        int i = 0;
        while (i < params.size())
        {
            int j = 0;
            while ((j < p.size()) && (params.get(i).compareTo(p.get(j)) > 0))
            {
                j = j + 2;
            }

            if (j < p.size())
            {
                p.add(j, params.get(i));
                p.add(j + 1, params.get(i + 1));
            }
            else
            {
                p.add(params.get(i));
                p.add(params.get(i + 1));
            }
            i = i + 2;
        }

        params.clear();
        i = 0;
        while (i < p.size())
        {
            params.add(p.get(i));
            i = i + 1;
        }
    }

    String OauthTimestamp()
    {
        return Long.toString( System.currentTimeMillis()/1000);
    }

    String OauthNonce()
    {
        return Long.toString((long)(Math.random() * (9999999 - 123400) + 123400));
    }

    void OauthBaseParams(ArrayList<String> p, String Token, String Verifier)
    {
        p.clear();
        //OAuth setup
        p.add("oauth_version");
        p.add("1.0");
        p.add("oauth_signature_method");
        p.add("HMAC-SHA1");
        // NONCE
        p.add("oauth_nonce");
        p.add(OauthNonce());
        // TIMESTAMP
        p.add("oauth_timestamp");
        p.add(OauthTimestamp());
        // Key
        p.add("oauth_consumer_key");
        p.add(NCKey);
        // Token
        if ((Token.length()) > 0)
        {
            p.add("oauth_token");
            p.add(Token);
        }
        // Verifier
        if (Verifier.length() > 0)
        {
            p.add("oauth_verifier");
            p.add(Verifier);
        }
    }

    void OauthSign(String u, String mode, ArrayList<String> params,  String Secret) throws Exception
    {
        String p = "";
        int i =0;
        while(i<params.size())
        {
            if(i>0) { p = p + "&" ; }
            p = p + params.get(i) + '=' + URLEncoder.encode(params.get(i+1),StandardCharsets.UTF_8.name());
            i = i +2;
        }

        String hashkey = NCKey + "&" + Secret;
        String data = mode + "&" + URLEncoder.encode(u,StandardCharsets.UTF_8.name()) + "&" + p;

        SecretKeySpec signingKey = new SecretKeySpec(hashkey.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        byte[] hmac = mac.doFinal(data.getBytes());

        String signature = new String(Base64.getEncoder().encode(hmac));

        params.add("oauth_signature");
        params.add(signature);
    }

    Boolean NCAuth()
    {
        if(NCToken.length()>0) //Already auth
        {
            return true;
        }

        // GET OAUTH URLS
        TLog.i(TAG, "Get OAUTH URLs");
        try
        {
            String res = WebGet(Preferences.getString(Preferences.Key.SYNC_NC_URL) + "/api/1.0/", null);

            JSONObject jsonResponse = new JSONObject(res);

            accessTokenUrl = jsonResponse.getString("oauth_access_token_url");
            requestTokenUrl = jsonResponse.getString("oauth_request_token_url");
            authorizeUrl = jsonResponse.getString("oauth_authorize_url");

        }
        catch (Exception e)
        {
            e.printStackTrace();
            TLog.e(TAG, e.toString());
            return false;
        }

        // REQUEST TOKEN
        try
        {
            ArrayList<String> p = new ArrayList<String>();
            OauthBaseParams(p,"", "");
            p.add("oauth_callback");
            p.add(OAUTH_CALLBACK_URL);
            OauthParamsSort(p);
            OauthSign(requestTokenUrl, "POST", p, "");

            String res = WebPost(requestTokenUrl, p);
            TLog.i(TAG, "Request token reply : " + res);
            //Example : oauth_token=r36747eda81d3a14c&oauth_token_secret=s05507586554bb6bf&oauth_callback_confirmed=true
            String[] split = res.split("&");
            String[] s1 = split[0].split("=");
            String[] s2 = split[1].split("=");
            String[] s3 = split[2].split("=");
            if (s1[0].compareTo("oauth_token") != 0)
            {
                TLog.e(TAG, "OAuth: token invalid");
                return false;
            }
            if (s2[0].compareTo("oauth_token_secret") != 0)
            {
                TLog.e(TAG, "OAuth token Secret invalid");
                return false;
            }
            if (s3[0].compareTo("oauth_callback_confirmed") != 0)
            {
                TLog.e(TAG, "OAuth not confirmed");
                return false;
            }
            NCToken = s1[1];
            NCTokenSecret = s2[1];
        }
        catch (Exception e)
        {
            e.printStackTrace();
            TLog.e(TAG, e.toString());
            return false;
        }

        // AUTORIZE
        try
        {
            String u = authorizeUrl + "?oauth_token=" + NCToken + "&client=TomdroidReborn&oauth_callback=" + URLEncoder.encode(OAUTH_CALLBACK_URL, StandardCharsets.UTF_8.name());
            Verifier = "";

            webserver = HttpServer.create(new InetSocketAddress(8000), 0);
            webserver.createContext("/tomdroid-web-sync", new NCHandler());
            webserver.setExecutor(null); // creates a default executor
            webserver.start();

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(u));
            activity.startActivity(browserIntent);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            TLog.e(TAG, e.toString());
            return false;
        }
        return true;
    }

    class NCHandler implements HttpHandler
    {
        @Override
        public void handle(HttpExchange t) throws IOException
        {
            String params = t.getRequestURI().getRawQuery();
            String[] p2 = params.split("&");
            String restok = "";
            String restverif = "";

            int i=0;
            while(i<p2.length)
            {
                String[] s = p2[i].split("=");
                if(s[0].compareTo("oauth_token")==0) { restok = s[1]; }
                if(s[0].compareTo("oauth_verifier")==0) { restok = s[1]; }
                i++;
            }

            String response;
            if(restok.compareTo(NCToken)==0)
            {
                Verifier = restverif;
                response = "<h2>Congratulation, your Tomdroid Reborn is authenticated</h2><br>Please kindly return to the app";
            }
            else
            {
                response = "<h2>A error occured : URL is"+t.getRequestURI();
                TLog.i(TAG,"Auth Failure :(");
            }
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();

            t.getHttpContext().getServer().stop(1);

            if(restok.compareTo(NCToken)==0)
            {
                ArrayList<String> p = new ArrayList<String>();
                try
                {
                    OauthBaseParams(p,NCToken, Verifier);
                    OauthSign(accessTokenUrl, "POST", p, NCTokenSecret);
                    String res = WebPost(accessTokenUrl,p);

                    String[] s = res.split("&");
                    String[] s1 = s[0].split("=");
                    String[] s2 = s[1].split("=");

                    if(s1[0].compareTo("oauth_token")!=0) { TLog.e(TAG,"Auth success with Invalid response "+res); return; }
                    if(s2[0].compareTo("oauth_token_secret")!=0) { TLog.e(TAG,"Auth success with invalid response2 "+res); return; }

                    NCToken = s1[1];
                    NCTokenSecret = s2[1];
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    TLog.e(TAG, e.toString());
                }
            }
         }
    }
}
