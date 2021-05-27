package de.pfannekuchen.accountapi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import de.pfannekuchen.accountapi.utils.Utils;

/**
 * This is an Implementation of the Microsoft Minecraft Account
 * @author Pancake
 */
public final class MicrosoftAccount {

	private final String accessToken;
	private final String username;
	private final UUID uuid;
	private final boolean ownsMinecraft;
	
	/**
	 * Create a Microsoft Account via Web View.
	 * @throws Exception Throws an Exception when the Microsoft Servers do
	 */
	public MicrosoftAccount() throws Exception {
		/* Obtain Auth Code from Login Process */
		Utils.openBrowser("https://login.live.com/oauth20_authorize.srf?client_id=f825dd16-a6d5-44f8-ab1c-836af116bfa3&prompt=select_account&response_type=code&scope=XboxLive.signin%20XboxLive.offline_access&redirect_uri=http://localhost:28562");
		final ServerSocket socket = new ServerSocket(28562); // Note: The Redirect address is localhost, so we setup a small http server
		final Socket s = socket.accept();
		final BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		final String authCode = in.readLine().substring(11).split(" ")[0];
		String output = "HTTP/1.0 200 OK\r\n" +
        "Server: AccountAPI Fake-Server/1.0\r\n" +
        "Content-type: text/html\r\n" +
        "\r\n"; 
		s.getOutputStream().write(output.getBytes(StandardCharsets.UTF_8));
        s.getOutputStream().flush();
		
        /* Connect to Xbox live servers to obtain XSTS */
        String localToken = null;
        try {
		    final String token = Utils.acquireAccessToken(authCode);
		    final String xblToken = Utils.getXBLToken(token);
		    final String xstsTokenJson = Utils.getXSTSToken(xblToken);

		    // Parse 2 instead of 1 variables from JSON
		    final String xstsToken = xstsTokenJson.split("Token\"")[1].split("\"")[1];
		    final String uhs = xstsTokenJson.split("uhs\"")[1].split("\"")[1];
		    
		    localToken = Utils.getAccessToken(xstsToken, uhs);
		    s.getOutputStream().write("Login finished, you can close this page now! :)\r\n".getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			s.getOutputStream().write("Something went wrong! :(\r\n".getBytes(StandardCharsets.UTF_8));
			e.printStackTrace();
		}
        accessToken = localToken;
        s.getOutputStream().flush();
        s.close();
        socket.close();
        
        /* Checking Game Ownership */
        final String ownershipJson = Utils.sendAndRecieveJson("https://api.minecraftservices.com/entitlements/mcstore", null, false, "Authorization", "Bearer " + accessToken);
        ownsMinecraft = !ownershipJson.replaceAll(" ", "").contains("[]");
        if (!ownsMinecraft) {
        	uuid = null;
        	username = null;
        	return;
        }
        
        /* Checking the Profile */
        final String profileJson = Utils.sendAndRecieveJson("https://api.minecraftservices.com/minecraft/profile", null, false, "Authorization", "Bearer " + accessToken);
        uuid = UUID.fromString(profileJson.split("id\"")[1].split("\"")[1].replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
        username = profileJson.split("name\"")[1].split("\"")[1];
	}
	
	/**
	 * Private Constructor used for Cloning
	 */
	MicrosoftAccount(String accessToken, String username, UUID uuid, boolean ownsMinecraft) {
		this.accessToken = accessToken;
		this.username = username;
		this.uuid = uuid;
		this.ownsMinecraft = ownsMinecraft;
	}
	
	/* Getters */
	
	public final String getAccessToken() {
		return accessToken;
	}

	public final String getUsername() {
		return username;
	}

	public final UUID getUuid() {
		return uuid;
	}

	public final boolean ownsMinecraft() {
		return ownsMinecraft;
	}
	
	/* General Java Stuff */
	
	/**
	 * Clones a Minecraft Account without Connecting to a Server again
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new MicrosoftAccount(accessToken, username, uuid, ownsMinecraft);
	}

	/**
	 * Create a Hash of the Player UUID
	 */
	@Override
	public int hashCode() {
		return uuid.hashCode();
	}
	
	/**
	 * Check whether two Accounts are equal
	 */
	@Override
	public boolean equals(Object o) {
		return o.hashCode() == hashCode();
	}
	
	/**
	 * To String support because why not
	 */
	@Override
	public String toString() {
		return uuid.toString();
	}
	
}
