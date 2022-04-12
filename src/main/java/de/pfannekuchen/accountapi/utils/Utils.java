package de.pfannekuchen.accountapi.utils;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

/**
 * This is where all the Magic happens, as an API-User you probably don't want to use any of this.
 * @author Pancake
 */
public final class Utils {
	
	/**
	 * Method used to send Get
	 * @param url URL and/or Payload, if payload is null
	 * @param headers Additional Headers for the Connection
	 * @return Returns the recieved JSON from the Server
	 * @throws Exception Something went wrong or the Server responded with an Error
	 */
	public static final String sendGet(final String url, final String... headers) throws Exception {
		/* Open a Connection to the Server */
		final URL authServer = new URL(url);
		final HttpURLConnection con = (HttpURLConnection) authServer.openConnection();

		/* Set Headers */
		con.setRequestMethod("GET");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; utf-8");
		con.setRequestProperty("Content-Length", "0");
		con.setRequestProperty("Accept", "application/json");
		for (int i = 0; i < headers.length; i += 2)
			con.setRequestProperty(headers[i], headers[i + 1]);
		con.setDoOutput(true);

		/* Read Input from Connection and parse to Json */
		try (final BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
			String response = "";
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response += responseLine.trim();
			}
			return response;
		}
	}
	
	/**
	 * Method used to send a Post request with Basic Name Value Pairs
	 * @param url URL and/or Payload, if payload is null
	 * @param headers Additional Headers for the Connection
	 * @return Returns the recieved JSON from the Server
	 * @throws Exception Something went wrong or the Server responded with an Error
	 */
	public static final String sendPost1(final String url, final String... pairs) throws Exception {
		/* Open a Connection to the Server */
		CloseableHttpClient httpclient = HttpClients.createDefault();
		final HttpPost client = new HttpPost(url);
		
		List <NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (String pair : pairs)
			nvps.add(new BasicNameValuePair(pair.split("\\=", 2)[0], pair.split("\\=", 2)[1]));
		client.setEntity(new UrlEncodedFormEntity(nvps));
		
		/* Set Headers*/
		client.setHeader("Content-Type", "application/x-www-form-urlencoded; utf-8");
		client.setHeader("Accept", "application/json");
		
		CloseableHttpResponse request = httpclient.execute(client);
		HttpEntity entity1 = request.getEntity();
		
		/* Read Input from Connection and parse to Json */
		try(final BufferedReader br = new BufferedReader(new InputStreamReader(entity1.getContent()))) {
			String response = "";
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response += responseLine.trim();
			}
			return response;
		}
	}
	
	/**
	 * Method used to send a Post request with Basic Name Value Pairs
	 * @param url URL and/or Payload, if payload is null
	 * @param headers Additional Headers for the Connection
	 * @return Returns the recieved JSON from the Server
	 * @throws Exception Something went wrong or the Server responded with an Error
	 */
	public static final String sendPost2(final String url, final String payload, final String... headers) throws Exception {
		/* Open a Connection to the Server */
		CloseableHttpClient httpclient = HttpClients.createDefault();
		final HttpPost client = new HttpPost(url);
		
		client.setEntity(new StringEntity(payload));
		
		/* Set Headers*/
		client.setHeader("Content-Type", "application/json; utf-8"); 
		client.setHeader("Accept", "application/json");
		for (int i = 0; i < headers.length; i += 2)
			client.setHeader(headers[i], headers[i + 1]);
		
		CloseableHttpResponse request = httpclient.execute(client);
		HttpEntity entity1 = request.getEntity();
		
		/* Read Input from Connection and parse to Json */
		try(final BufferedReader br = new BufferedReader(new InputStreamReader(entity1.getContent()))) {
			String response = "";
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response += responseLine.trim();
			}
			return response;
		}
	}
	
	/**
	 * Open a URL in a Browser
	 * @param url URL to open in a browser
	 */
	public static final void openBrowser(final String url) throws IOException, URISyntaxException {
		if (Desktop.isDesktopSupported()) {
			Desktop.getDesktop().browse(new URI(url));
		} else {
			Runtime.getRuntime().exec("xdg-open " + url);
		}
	}
	
	/**
	 * Check README.md
	 */
	public static final String acquireAccessToken(final String authCode) throws Exception {
		return sendPost1("https://login.live.com/oauth20_token.srf", "client_id=f825dd16-a6d5-44f8-ab1c-836af116bfa3", "code=" + authCode, "grant_type=authorization_code", "redirect_uri=http://localhost:28562", "scope=XboxLive.signin");
	}
	
	/**
	 * Same as above, but for refreshing an existing Token
	 */
	public static final String refreshAccessToken(final String oldToken) throws Exception {
		return sendPost1("https://login.live.com/oauth20_token.srf", "client_id=f825dd16-a6d5-44f8-ab1c-836af116bfa3", "refresh_token=" + oldToken, "grant_type=refresh_token", "redirect_uri=http://localhost:28562", "scope=XboxLive.signin%20offline_access").split("access_token\"")[1].split("\"")[1];
	}
	
	/**
	 * Check README.md
	 */
	public static final String getXBLToken(final String accessToken) throws Exception {
        return sendPost2("https://user.auth.xboxlive.com/user/authenticate", "{'Properties':{'AuthMethod':'RPS','SiteName':'user.auth.xboxlive.com','RpsTicket':'d=%TOKEN%'},'RelyingParty':'http://auth.xboxlive.com','TokenType':'JWT'}".replace("'", "\"").replaceAll("%TOKEN%", accessToken), "x-xbl-contract-version", "1").split("Token\"")[1].split("\"")[1];
	}
	
	/**
	 * Check README.md
	 */
	public static final String getXSTSToken(final String xblToken) throws Exception {
        return sendPost2("https://xsts.auth.xboxlive.com/xsts/authorize", "{'Properties':{'SandboxId':'RETAIL','UserTokens':['%XBL%']},'RelyingParty':'rp://api.minecraftservices.com/','TokenType':'JWT'}".replace("'", "\"").replaceAll("%XBL%", xblToken), "x-xbl-contract-version", "1");
	}
	
	/**
	 * Check README.md
	 */
	public static final String getAccessToken(final String xstsToken, final String hash) throws Exception {
        return sendPost2("https://api.minecraftservices.com/authentication/login_with_xbox", "{\"identityToken\":\"XBL3.0 x=" + hash + ";" + xstsToken + "\"}").split("access_token\"")[1].split("\"")[1];
	}
	
}
