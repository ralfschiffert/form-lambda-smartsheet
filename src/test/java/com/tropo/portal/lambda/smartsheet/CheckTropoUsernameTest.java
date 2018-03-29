package com.tropo.portal.lambda.smartsheet;

import static org.junit.Assert.*;
import java.net.MalformedURLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class CheckTropoUsernameTest {
	
	static LambdaLogger ll = null;
	
	@BeforeClass
	public static void createContext() {
		TestContext context = new TestContext();
		context.setFunctionName("SmartSheetFunctionJunitTest");
		ll =context.getLogger();
	}

	@Test
	public void RetrievalOfUsernameTest() {
	
		TropoUsernameConnector u = new TropoUsernameConnector("xxxx", "xxxxx", ll,"https://api.tropo.com/v1/users/");
		
		assertNotNull("Could not instantiate the tropo api connector",  u);	
		assertTrue("wrong API response for non-existent username", 404 == u.checkUserName("93i34ridjddijjskjss9w999393"));
		assertTrue("wrong API response for existing username", 403 == u.checkUserName("existing"));
		
	
	}
	
	
	@Test
	public void checkUrlException() {
		TropoUsernameConnector u = new TropoUsernameConnector("xxxx", "xxxxx", ll,"https://api.tropo.com/v1/users/");
		assertTrue("wrong API response for non-existent username", 0 == u.checkUserName("93i34ridjddijjskjss9w999393"));
		
	}

}
