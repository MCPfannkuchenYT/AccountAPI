package de.pfannekuchen.accountapi.accounts;

import java.io.IOException;
import java.util.UUID;

import org.json.simple.JSONObject;

import de.pfannekuchen.accountapi.utils.Utils;

/**
 * This is an Implementation of the old Mojang Minecraft Account
 * @author Pancake
 */
public final class MojangAccount {
	
	/** UUID for specific Account */
	private final UUID clientUuid;
	
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
		payload.put("clientToken", (clientUuid = UUID.randomUUID()).toString());
		
		/* Send Payload and Recieve new one */
		JSONObject response = null;
		try {
			response = Utils.sendAndRecieveJson("https://authserver.mojang.com/authenticate", payload, true);
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
	public MojangAccount(final String accessToken, final UUID clientUuid) throws Exception {
		/* Create Payload */
		final JSONObject payload = new JSONObject();
		payload.put("accessToken", accessToken);
		payload.put("clientToken", clientUuid.toString());
		
		/* Send Payload and Recieve new one */
		JSONObject response = null;
		try {
			response = Utils.sendAndRecieveJson("https://authserver.mojang.com/refresh", payload, true);
			if (response.containsKey("error")) throw new Exception((String) response.get("error"));
		} catch (final IOException e) {
			System.err.println("Could not refresh Mojang Account.");
			e.printStackTrace();
		}
		
		this.accessToken = (String) response.get("accessToken");
		this.username = (String) ((JSONObject) response.get("selectedProfile")).get("name");
		this.uuid = UUID.fromString(((String) ((JSONObject) response.get("selectedProfile")).get("id")).replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
		this.clientUuid = clientUuid;
	}
	
	/**
	 * Private Constructor used for Cloning without connecting to Servers
	 */

	MojangAccount(UUID clientUuid, String accessToken, String username, UUID uuid) {
		this.clientUuid = clientUuid;
		this.accessToken = accessToken;
		this.username = username;
		this.uuid = uuid;
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
	
	/* General Java Stuff */
	
	/**
	 * Clones a Minecraft Account without Connecting to a Server again
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new MojangAccount(clientUuid, accessToken, username, uuid);
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
