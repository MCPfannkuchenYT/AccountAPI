package de.pfannekuchen.accountapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

final class HTTPSUtils {

	static final JSONObject sendAndRecieveJson(final String url, final JSONObject payload) throws IOException {
		final URL authServer = new URL(url);
		final HttpsURLConnection con = (HttpsURLConnection) authServer.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json; utf-8");
		con.setRequestProperty("Accept", "application/json");
		con.setDoOutput(true);
		
		try(final OutputStream os = con.getOutputStream()) {
			final byte[] input = payload.toJSONString().getBytes("utf-8");
		    os.write(input, 0, input.length);			
		}
		
		try(final BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
			String response = "";
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response += responseLine.trim();
			}
			return (JSONObject) new JSONParser().parse(response);
		} catch (ParseException e) {
			System.err.println("Could not parse input.");
			e.printStackTrace();
		}
		return null;
	}
	
}
