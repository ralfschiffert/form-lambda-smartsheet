package com.tropo.portal.lambda.smartsheet;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

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
	public void RetrievalOfUsernameTest() {

		TropoUsernameConnector u = new TropoUsernameConnector(TROPOUSERNAME, TROPOPASSWORD, ll,"https://api.tropo.com/v1/users/");

		assertNotNull("Could not instantiate the tropo api connector",  u);
		assertTrue("wrong API response for non-existent username", 404 == u.checkUserName("93i34ridjddijjskjss9w999393d"));
		assertTrue("wrong API response for existing username", 403 == u.checkUserName("sanparik"));


	}


	@Test
	public void checkUrlException() {
		TropoUsernameConnector u = new TropoUsernameConnector(TROPOUSERNAME, TROPOPASSWORD, ll,"httpsapi.tropo.com/v1/users/");
		assertTrue("wrong API response for non-existent username", 0 == u.checkUserName("93i34ridjddijjskjss9w999393"));

	}

}
