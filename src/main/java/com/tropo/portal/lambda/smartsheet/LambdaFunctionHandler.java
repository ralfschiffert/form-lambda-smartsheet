package com.tropo.portal.lambda.smartsheet;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.impl.crypto.MacProvider;
//import io.jsonwebtoken.impl.DefaultClock;
import java.security.Key;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.util.Base64;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.models.Row;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class LambdaFunctionHandler implements RequestHandler<Map<String, String>, String> {


	private static final String SMARTSHEETACCESSTOKEN = System.getenv("SMARTSHEETACCESSTOKEN");
	private static final String SMARTSHEETSHEETID = System.getenv("SMARTSHEETSHEETID");
	private static final String TROPOUSERNAME= System.getenv("TROPOUSERNAME");
	private static final String TROPOPASSWORD = System.getenv("TROPOPASSWORD");



	private String retUrl = null;
	private String desiredUsername = null;

	// formatting this string via 'error message', website, website, timeToWait
	private String retErrorHTML = "<!DOCTYPE html><html><head><title>Something went wrong</title>"
	 		+ "</head><body><h1>Form Received with Errors</h1>"
	 		+ "There were some issues</br />"
	 		+ "%s <br />" // error message
	 		+ "If your browser does not support redirects, please click <a href='%s'>here</a>" // manual click
	 		+ "<script type='text/javascript'> var website = '%s', timer = '%s';function delayer() {window.location=website}; setTimeout('delayer()', 1000 * timer);</script>"
	 		+ "</body></html>";
	       // format is 1. error message 2. manual click usually just tropo.com 3. redirect website 4. delay Timer

	private String retSuccessHTML = "<!DOCTYPE html><html><head>"
			+ "<script>var mylink='%s'; if (window.location.replace){ window.location.replace(mylink);} else {window.location.href ='mylink';} </script>"
	 		+ "</head></html>";

	private LambdaLogger ll = null;

	private String decode(String s) {
		String converted ="";
		try {
			converted = java.net.URLDecoder.decode(s, "UTF-8");
		} catch ( UnsupportedEncodingException e ) {
			ll.log("Could not convert one of the values" + s);
			ll.log(e.getMessage());
		}
		return converted;
	}

	@Override
	public String handleRequest(Map<String, String> m, Context context) {
		// assign the lambda logger
		ll = context.getLogger();
		ll.log("Input: " + m.toString());

	    retUrl = m.entrySet().stream().filter( (e) -> e.getKey().equals("retURL")).map(Map.Entry::getValue).findFirst().orElse(null);
	    desiredUsername = m.entrySet().stream().filter( (e) -> e.getKey().equals("00N6100000HSuwR")).map(Map.Entry::getValue).findFirst().orElse(null);
	    ll.log("retUrl is " + retUrl);
	    if ( retUrl != null && ! retUrl.isEmpty()) {
	    	retUrl = decode(retUrl);
	    } else {
	    	retUrl = "https://www.tropo.com/thankyou";
	    }

		ll.log("desiredUsername is " + desiredUsername);
		if ( null != desiredUsername ) {
			desiredUsername = decode(desiredUsername);
		} else {
			desiredUsername = "tropoisv" + m.get("email").split("@")[0];
		}



		// if the username is already in use we want to tell this application
		if ( null != desiredUsername )  {
			TropoUsernameConnector u = new TropoUsernameConnector(TROPOUSERNAME, TROPOPASSWORD, ll, "https://api.tropo.com/v1/users/");

			int n = 0;

			if ( 404 != ( n= u.checkUserName(desiredUsername))) {
				ll.log("Username lookup returned " + n);
				return String.format(retErrorHTML, "The username " + desiredUsername + " is already in use.", "https://www.tropo.com/portal/register", retUrl, "5");
			}
		}

		// this filters all the entries we don't have columns for in smartsheet and also remapps the input field designation to the smartsheet column name
		m = m.entrySet().stream().
				filter( (e) -> ConvertInputFieldsToSmartSheetColumns.getMappedField(e.getKey(),ll)!=null).
				collect(Collectors.toMap (
						(e) -> ConvertInputFieldsToSmartSheetColumns.getMappedField(e.getKey(),ll),
						(e)-> decode(e.getValue())
						));


		// before we insert it we will add some custom fields
		m.put("Primary Column", m.get("Person Email"));
		m.put("Approval Status", "Yellow");


		SingleSmartSheet sss = new SingleSmartSheet(context).init(SMARTSHEETACCESSTOKEN,SMARTSHEETSHEETID);;
		if ( null == sss ) {
			System.out.println("Could not work with SmartSheet");
			ll.log("Could not access smartsheet with the credentials provided");
			 // format is 1. error message 2. manual click usually just tropo.com 3. redirect website 4. delay Timer
			return String.format(retErrorHTML, "We apologize for this, but expeirenced some server side error. Please try again later", "https://www.tropo.com", retUrl, "5");
		}


		String emailAsPrimaryKey = m.get("Person Email");

		if ( null!=emailAsPrimaryKey && sss.primaryKeyUsed(m.get("Person Email"))) {
			ll.log("Primary Key for  " + emailAsPrimaryKey + " is already used.");
			ll.log("Wont try to insert it. You can always change the sheet itself.");
			 // format is 1. error message 2. manual click usually just tropo.com 3. redirect website 4. delay Timer
			String htmlBody = String.format(retErrorHTML, "You have already applied for this program with this email address.", "https://www.tropo.com", retUrl, "3");
			// return "{ 'errorMessage':'clienterror', 'htmlbody':'" + htmlBody +"'}";
			return htmlBody;
		}

		sss.readyRowForInsertion(m);
		final int inserted = sss.insertRowOrRows();
		if ( 1 != inserted ) {
			ll.log("Couldn't insert the row here");
			ll.log("Got a return for nof rows " + inserted);
		}

		return String.format(retSuccessHTML, retUrl);

	}
}
