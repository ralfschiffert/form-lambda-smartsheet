package com.tropo.portal.lambda.smartsheet;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.Any;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import static org.powermock.api.mockito.PowerMockito.*;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.tropo.portal.lambda.smartsheet.TropoUsernameConnector")
public class CheckTropoUsernameTestWithPowerMockito {
	
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
	
		private URL  url = mock(URL.class);
		private HttpURLConnection httpUrlConnection = mock(HttpURLConnection.class);
		
	
		@Test
		public void test() throws Exception {
			PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(url);
			when(url.openConnection()).thenReturn(httpUrlConnection);
			doNothing().when(httpUrlConnection).setRequestMethod("GET");
			when(httpUrlConnection.getResponseCode()).thenThrow(new IOException("Mock network error"));
			
			TropoUsernameConnector u = new TropoUsernameConnector(TROPOUSERNAME,TROPOPASSWORD,ll,"https://api.tropo.com/v1/users");
			// when(httpUrlConnection.getResponseCode()).thenThrow(RuntimeException.class);
			
			System.out.println(u.getResponseCode("kjsjsjsjs"));
		}
}
