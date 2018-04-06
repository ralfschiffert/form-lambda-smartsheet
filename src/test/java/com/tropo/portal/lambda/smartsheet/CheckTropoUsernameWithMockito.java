package com.tropo.portal.lambda.smartsheet;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.verify;
import java.net.HttpURLConnection;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.LambdaLogger;


public class CheckTropoUsernameWithMockito {
	
	private static String TROPOUSERNAME=null,TROPOPASSWORD=null;
	private static LambdaLogger ll;
	
	@BeforeClass
		public static void createContext() {
		TestContext context = new TestContext();
		context.setFunctionName("SmartSheetFunctionJunitTest");
		ll =context.getLogger();

		TROPOUSERNAME =System.getenv("TROPOUSERNAME");
		TROPOPASSWORD = System.getenv("TROPOPASSWORD");
	}
	

	@Mock
	private HttpURLConnection httpUrlConnection;
	
	@InjectMocks
	private TropoUsernameConnector u = new TropoUsernameConnector(TROPOUSERNAME,TROPOPASSWORD,ll,"https://api.tropo.com/v1/users");
	
	@Before
	public void setup() throws Exception {
		
	}
	
	
	@Test
	public void testSomething() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(httpUrlConnection.getResponseCode()).thenReturn(544);
		
		assertThat(u.getResponseCode("sjsjsjs"),equalTo(544));
	}
}
