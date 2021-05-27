package de.pfannekuchen.accountapi.utils;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This is where all the Magic happens, as an API-User you probably don't want to use any of this.
 * @author Pancake
 */
public final class Utils {

	/** JSON Parser */
	public static final JSONParser parser = new JSONParser();
	
	/**
	 * Method used to send Get or Post Requests to a server, and read the return as a JSON Object.
	 * @param url URL and/or Payload, if payload is null
	 * @param payload Payload that should be send if isPost is true
	 * @param isPost Whether Payload should be send or not
	 * @param headers Additional Headers for the Connection
	 * @return Returns the recieved JSON from the Server
	 * @throws IOException Something went wrong or the Server responded with an Error
	 */
	public static final JSONObject sendAndRecieveJson(final String url, final JSONObject payload, final boolean isPost, final String... headers) throws IOException {
		/* Open a Connection to the Server */
		final URL authServer = new URL(url);
		final HttpsURLConnection con = (HttpsURLConnection) authServer.openConnection();
		
		/* Set Headers*/
		if (isPost) con.setRequestMethod("POST");
		else con.setRequestMethod("GET");
		if (payload != null) con.setRequestProperty("Content-Type", "application/json; utf-8");
		else {
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; utf-8");
			con.setRequestProperty("Content-Length", "0");
		}
		con.setRequestProperty("Accept", "application/json");
		for (int i = 0; i < headers.length; i += 2) con.setRequestProperty(headers[i], headers[i + 1]);
		con.setDoOutput(true);
		
		/* Send Payload */
		if (isPost) {
			if (payload != null) {
				// Send the JSON Object as Payload
				try(final OutputStream os = con.getOutputStream()) {
					final byte[] input = payload.toJSONString().getBytes("utf-8");
				    os.write(input, 0, input.length);			
				}
			} else {
				// Split the URL to payload (which is after the ?) and send taht
				try(final OutputStream os = con.getOutputStream()) {
					final byte[] input = url.split("\\?", 2)[1].getBytes(StandardCharsets.UTF_8);
				    os.write(input, 0, input.length);			
				}
			}
		}
		
		/* Read Input from Connection and parse to Json */
		try(final BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
			String response = "";
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response += responseLine.trim();
			}
			return (JSONObject) parser.parse(response);
		} catch (ParseException e) {
			System.err.println("Could not parse input.");
			e.printStackTrace();
		}
		return null;
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
	public static final JSONObject acquireAccessToken(final String authCode) throws Exception {
		return sendAndRecieveJson("https://login.live.com/oauth20_token.srf?client_id=f825dd16-a6d5-44f8-ab1c-836af116bfa3&code=" + authCode + "&grant_type=authorization_code&redirect_uri=http://localhost:28562&scope=XboxLive.signin%20offline_access", null, true);
	}
	
	/**
	 * Check README.md
	 */
	public static final JSONObject getXBLToken(final String accessToken) throws IOException {
		final JSONObject properties = new JSONObject();
        properties.put("AuthMethod", "RPS");
        properties.put("SiteName", "user.auth.xboxlive.com");
        properties.put("RpsTicket", "d=" + accessToken);

        final JSONObject data = new JSONObject();
        data.put("Properties", properties);
        data.put("RelyingParty", "http://auth.xboxlive.com");
        data.put("TokenType", "JWT");

        return Utils.sendAndRecieveJson("https://user.auth.xboxlive.com/user/authenticate", data, true, "x-xbl-contract-version", "1");
	}
	
	/**
	 * Check README.md
	 */
	public static final JSONObject getXSTSToken(final String xblToken) throws IOException {
		final JSONObject properties = new JSONObject();
        properties.put("SandboxId", "RETAIL");
        
        final List<String> userToken = new ArrayList<String>();
        userToken.add(xblToken);
        properties.put("UserTokens", userToken);

        final JSONObject data = new JSONObject();
        data.put("Properties", properties);
        data.put("RelyingParty", "rp://api.minecraftservices.com/");
        data.put("TokenType", "JWT");

        return Utils.sendAndRecieveJson("https://xsts.auth.xboxlive.com/xsts/authorize", data, true, "x-xbl-contract-version", "1");
	}
	
	/**
	 * Check README.md
	 */
	public static final JSONObject getAccessToken(final JSONObject xstsToken) throws IOException {
        final JSONObject data = new JSONObject();
        data.put("identityToken", "XBL3.0 x=" + ((JSONObject) ((JSONArray) ((JSONObject) xstsToken.get("DisplayClaims")).get("xui")).get(0)).get("uhs") + ";" + xstsToken.get("Token"));

        return Utils.sendAndRecieveJson("https://api.minecraftservices.com/authentication/login_with_xbox", data, true);
	}
	
}
