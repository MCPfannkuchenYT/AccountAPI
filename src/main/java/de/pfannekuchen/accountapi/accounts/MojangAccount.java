package de.pfannekuchen.accountapi.accounts;

import java.io.IOException;
import java.util.UUID;

import org.json.simple.JSONObject;

import de.pfannekuchen.accountapi.AccountAPI;
import de.pfannekuchen.accountapi.utils.Utils;

public final class MojangAccount {

	private static final String AUTHSERVER = "https://authserver.mojang.com/";

	private final String accessToken;
	private final String username;
	private final UUID uuid;
	
	/**
	 * Create a Mojang Account via Email And Password.
	 * @param email E-Mail for Mojang Account
	 * @param password Raw Password for Mojang Account
	 * @throws Exception Throws an Exception when the Mojang Servers do
	 */
	public MojangAccount(final String email, final String password) throws Exception {
		/* Create Payload */
		final JSONObject payload = new JSONObject();
		final JSONObject payload_agent = new JSONObject();
		payload_agent.put("name", "Minecraft");
		payload_agent.put("version", 1);
		payload.put("agent", payload_agent);
		payload.put("username", email);
		payload.put("password", password);
		payload.put("requestUser", true);
		payload.put("clientToken", AccountAPI.getClientUUID().toString());
		
		/* Send Payload and Recieve new one */
		JSONObject response = null;
		try {
			response = Utils.sendAndRecieveJson(AUTHSERVER + "authenticate", payload, true);
			if (response.containsKey("error")) throw new Exception((String) response.get("error"));
		} catch (final IOException e) {
			System.err.println("Could not create Mojang Account.");
			e.printStackTrace();
		}
		
		this.accessToken = (String) response.get("accessToken");
		this.username = (String) ((JSONObject) response.get("selectedProfile")).get("name");
		this.uuid = UUID.fromString(((String) ((JSONObject) response.get("selectedProfile")).get("id")).replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
	}
	
	/**
	 * Refresh a Minecraft Account from it's accessToken
	 * @param accessToken Access Token of a Minecraft Account
	 * @throws Exception Throws an Exception when the Mojang Servers do
	 */
	public MojangAccount(final String accessToken) throws Exception {
		/* Create Payload */
		final JSONObject payload = new JSONObject();
		payload.put("accessToken", accessToken);
		payload.put("clientToken", AccountAPI.getClientUUID().toString());
		
		/* Send Payload and Recieve new one */
		JSONObject response = null;
		try {
			response = Utils.sendAndRecieveJson(AUTHSERVER + "refresh", payload, true);
			if (response.containsKey("error")) throw new Exception((String) response.get("error"));
		} catch (final IOException e) {
			System.err.println("Could not refresh Mojang Account.");
			e.printStackTrace();
		}
		
		this.accessToken = (String) response.get("accessToken");
		this.username = (String) ((JSONObject) response.get("selectedProfile")).get("name");
		this.uuid = UUID.fromString(((String) ((JSONObject) response.get("selectedProfile")).get("id")).replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
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
	
}
