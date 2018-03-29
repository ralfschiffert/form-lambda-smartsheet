package com.tropo.portal.lambda.smartsheet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class TropoUsernameConnector {
	
	private class MyAuthenticator extends Authenticator {
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(requestUser, requestPass.toCharArray());
		};
	}
	
	private String  requestUser;
	private String requestPass;
	private LambdaLogger ll;
	private String baseUrl;

	public TropoUsernameConnector(String requestUser, String requestPass, LambdaLogger ll, String baseUrl) {
		this.requestUser = requestUser;
		this.requestPass = requestPass;
		this.ll = ll;
		this.baseUrl = baseUrl;
	}

	public Integer checkUserName(String tropoUserName)  {
		
		Authenticator.setDefault(new MyAuthenticator());

		int returnCode = 0;
		
		try {
			URL url = new URL(baseUrl + tropoUserName);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			returnCode = connection.getResponseCode();
			connection.disconnect();
		} catch (MalformedURLException e) {
			// there is little value in exposing this one
			ll.log("Malformed URL: " + e.getMessage());
			returnCode = 0;
		} catch (IOException e) {
			ll.log("I/O Error: " + e.getMessage());
			returnCode = 0;
		}
	
		return returnCode;
	}
}