package de.pfannekuchen.accountapi;

import java.io.IOException;
import java.util.UUID;

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
		final String payload = "{'agent': {'name': 'Minecraft','version':1},'username':'%USERNAME%','password':'%PASSWORD%','requestUser':'true','clientToken':'%CLIENTUUID%'}".replace("'", "\"")
				.replaceAll("%USERNAME%", email)
				.replaceAll("%PASSWORD%", password)
				.replaceAll("%CLIENTUUID%", (clientUuid = UUID.randomUUID()).toString());
		
		/* Send Payload and Recieve new one */
		String response = null;
		try {
			response = Utils.sendAndRecieveJson("https://authserver.mojang.com/authenticate", payload, true);
			if (response.contains("error")) throw new Exception(response);
		} catch (final IOException e) {
			System.err.println("Could not create Mojang Account.");
			e.printStackTrace();
		}
		
		this.accessToken = response.split("accessToken\"")[1].split("\"")[1];
		this.username = response.split("selectedProfile")[1].split("name\"")[1].split("\"")[1];
		this.uuid = UUID.fromString(response.split("selectedProfile")[1].split("id\"")[1].split("\"")[1].replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
	}
	
	/**
	 * Refresh a Minecraft Account from it's accessToken
	 * @param accessToken Access Token of a Minecraft Account
	 * @throws Exception Throws an Exception when the Mojang Servers do
	 */
	public MojangAccount(final String accessToken, final UUID clientUuid) throws Exception {
		/* Create Payload */
		final String payload = "{'accessToken':'%TOKEN%','clientToken':'%CLIENTUUID%'}".replace("'", "\"")
				.replaceAll("%TOKEN%", accessToken)
				.replaceAll("%CLIENTUUID%", clientUuid.toString());
		/* Send Payload and Recieve new one */
		String response = null;
		try {
			response = Utils.sendAndRecieveJson("https://authserver.mojang.com/refresh", payload, true);
			if (response.contains("error")) throw new Exception(response);
		} catch (final IOException e) {
			System.err.println("Could not refresh Mojang Account.");
			e.printStackTrace();
		}
		
		this.accessToken = response.split("accessToken\"")[1].split("\"")[1];
		this.username = response.split("selectedProfile")[1].split("name\"")[1].split("\"")[1];
		this.uuid = UUID.fromString(response.split("selectedProfile")[1].split("id\"")[1].split("\"")[1].replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
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
	
	public UUID getClientUuid() {
		return clientUuid;
	}
	
	/* General Java Stuff */
	
	/**
	 * Clones a Minecraft Account without Connecting to a Server again
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new MojangAccount(getClientUuid(), accessToken, username, uuid);
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
