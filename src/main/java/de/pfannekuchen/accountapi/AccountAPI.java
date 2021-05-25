package de.pfannekuchen.accountapi;

import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public final class AccountAPI {

	static UUID CLIENTUUID;
	
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
	
	public static final MojangAccount obtainMojangAccount(final String email, final String password) throws Exception {
		return new MojangAccount(email, password);
	}
	
	public static final MojangAccount obtainMojangAccount(final String authtoken) throws Exception {
		return new MojangAccount(authtoken);
	}
	
}


