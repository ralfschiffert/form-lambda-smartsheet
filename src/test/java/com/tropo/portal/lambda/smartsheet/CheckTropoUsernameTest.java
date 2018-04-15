package com.tropo.portal.lambda.smartsheet;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class CheckTropoUsernameTest {

	static LambdaLogger ll = null;
	static String TROPOUSERNAME = null;
	static String TROPOPASSWORD = null;
	
	@BeforeClass
	public static void createContext() {
		TestContext context = new TestContext();
		context.setFunctionName("SmartSheetFunctionJunitTest");
		ll =context.getLogger();

		TROPOUSERNAME =System.getenv("TROPOUSERNAME");
		TROPOPASSWORD = System.getenv("TROPOPASSWORD");
	}

	@Test
	public void checkAvailableUsername() {
		TropoUsernameConnector u = new TropoUsernameConnector(TROPOUSERNAME, TROPOPASSWORD, ll,"https://api.tropo.com/v1/users/");

		assertThat("could not get the Authenticator to work to access url with basic auth", u, is(notNullValue())); 
		assertThat(u.getResponseCode("93i34ridjddijjskjss9w999393d"),equalTo(404));
	}

	
	@Test
	public void checkUnavailableUsername() {
		TropoUsernameConnector u = new TropoUsernameConnector(TROPOUSERNAME, TROPOPASSWORD, ll,"https://api.tropo.com/v1/users/");

		assertThat("could not get the Authenticator to work to access url with basic auth", u, is(notNullValue())); 
		assertThat(u.getResponseCode("sanparik"), equalTo(403));
	}
	
	
	@Test
	public void checkNegativeReturnValueForMalformedURL() {
		TropoUsernameConnector u = new TropoUsernameConnector(TROPOUSERNAME, TROPOPASSWORD, ll,"httpsapi.tropo.com/v1/users/");
		
		assertThat( u.getResponseCode("sanparik"), equalTo(-1));
		}
}
