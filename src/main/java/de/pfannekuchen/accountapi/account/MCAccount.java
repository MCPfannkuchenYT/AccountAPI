package de.pfannekuchen.accountapi.account;

import java.util.UUID;

/**
 * Header for Minecraft Accounts
 * @author Pancake
 */
public abstract class MCAccount {

	protected String accessToken;
	protected String username;
	protected UUID uuid;
	
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
	
	@Override
	protected abstract Object clone() throws CloneNotSupportedException;
	
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
