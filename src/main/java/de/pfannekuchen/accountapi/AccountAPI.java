package de.pfannekuchen.accountapi;

import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import de.pfannekuchen.accountapi.accounts.MojangAccount;

public final class AccountAPI {

	static UUID CLIENTUUID;
	
	// TODO: Get rid of this
	public static void create() {
		/* Try to Load or Generate a UUID for the Account API Client */
		try {
			final Preferences pref = Preferences.userRoot();
			if (!pref.get("MC_CLIENT_UUID", "").isEmpty()) CLIENTUUID = UUID.fromString(pref.get("MC_CLIENT_UUID", null));
			else pref.put("MC_CLIENT_UUID", (CLIENTUUID = UUID.randomUUID()).toString());
			pref.flush();
		} catch (BackingStoreException e) {
			System.err.println("Could not initialize Account API");
			e.printStackTrace();
		}
	}

	public static UUID getClientUUID() {
		return CLIENTUUID;
	}
	
}


