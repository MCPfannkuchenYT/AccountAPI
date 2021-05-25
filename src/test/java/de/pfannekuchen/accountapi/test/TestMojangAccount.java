package de.pfannekuchen.accountapi.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.junit.Test;

import de.pfannekuchen.accountapi.AccountAPI;
import de.pfannekuchen.accountapi.MojangAccount;
import junit.framework.Assert;

public class TestMojangAccount {

	@Test
	public void testMojangAccount() {
		AccountAPI.create();
		BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("account_credentials.dat")));
		try {
			MojangAccount logInAccount = AccountAPI.obtainMojangAccount(reader.readLine(), reader.readLine());
			MojangAccount refreshAccount = AccountAPI.obtainMojangAccount(logInAccount.getAccessToken());
			
			System.out.println(refreshAccount.getUsername());
			String username = reader.readLine();
			Assert.assertEquals(logInAccount.getUsername(), username);
			Assert.assertEquals(refreshAccount.getUsername(), username);
		} catch (IOException e) {
			System.err.println("Test failed!");
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Mojang Servers returned " + e.getMessage());
		}
	}
	
}
