package de.pfannekuchen.accountapi.utils;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * This is where all the Magic happens, as an API-User you probably don't want to use any of this.
 * @author Pancake
 */
public final class Utils {
	
	/**
	 * Method used to send Get or Post Requests to a server, and read the return as a JSON Object.
	 * @param url URL and/or Payload, if payload is null
	 * @param payload Payload that should be send if isPost is true
	 * @param isPost Whether Payload should be send or not
	 * @param headers Additional Headers for the Connection
	 * @return Returns the recieved JSON from the Server
	 * @throws IOException Something went wrong or the Server responded with an Error
	 */
	public static final String sendAndRecieveJson(final String url, final String payload, final boolean isPost, final String... headers) throws IOException {
		/* Open a Connection to the Server */
		final URL authServer = new URL(url);
		final HttpURLConnection con = (HttpURLConnection) authServer.openConnection();
		
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
					final byte[] input = payload.getBytes("utf-8");
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
		return sendAndRecieveJson("https://login.live.com/oauth20_token.srf?client_id=f825dd16-a6d5-44f8-ab1c-836af116bfa3&code=" + authCode + "&grant_type=authorization_code&redirect_uri=http://localhost:28562&scope=XboxLive.signin%20offline_access", null, true);
	}
	
	/**
	 * Same as above, but for refreshing an existing Token
	 */
	public static final String refreshAccessToken(final String oldToken) throws Exception {
		return sendAndRecieveJson("https://login.live.com/oauth20_token.srf?client_id=f825dd16-a6d5-44f8-ab1c-836af116bfa3&refresh_token=" + oldToken + "&grant_type=refresh_token&redirect_uri=http://localhost:28562&scope=XboxLive.signin%20offline_access", null, true).split("access_token\"")[1].split("\"")[1];
	}
	
	/**
	 * Check README.md
	 */
	public static final String getXBLToken(final String accessToken) throws IOException {
        return sendAndRecieveJson("https://user.auth.xboxlive.com/user/authenticate", "{'Properties':{'AuthMethod':'RPS','SiteName':'user.auth.xboxlive.com','RpsTicket':'d=%TOKEN%'},'RelyingParty':'http://auth.xboxlive.com','TokenType':'JWT'}".replace("'", "\"").replaceAll("%TOKEN%", accessToken), true, "x-xbl-contract-version", "1").split("Token\"")[1].split("\"")[1];
	}
	
	/**
	 * Check README.md
	 */
	public static final String getXSTSToken(final String xblToken) throws IOException {
        return sendAndRecieveJson("https://xsts.auth.xboxlive.com/xsts/authorize", "{'Properties':{'SandboxId':'RETAIL','UserTokens':['%XBL%']},'RelyingParty':'rp://api.minecraftservices.com/','TokenType':'JWT'}".replace("'", "\"").replaceAll("%XBL%", xblToken), true, "x-xbl-contract-version", "1");
	}
	
	/**
	 * Check README.md
	 */
	public static final String getAccessToken(final String xstsToken, final String hash) throws IOException {
        return sendAndRecieveJson("https://api.minecraftservices.com/authentication/login_with_xbox", "{\"identityToken\":\"XBL3.0 x=" + hash + ";" + xstsToken + "\"}", true).split("access_token\"")[1].split("\"")[1];
	}
	
}
