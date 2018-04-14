package com.tropo.portal.lambda.smartsheet;


import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.io.UnsupportedEncodingException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class LambdaFunctionHandler implements RequestHandler<Map<String, String>, String> {


	private static final String SMARTSHEETACCESSTOKEN = System.getenv("SMARTSHEETACCESSTOKEN");
	private static final String SMARTSHEETSHEETID = System.getenv("SMARTSHEETSHEETID");
	private static final String TROPOUSERNAME= System.getenv("TROPOUSERNAME");
	private static final String TROPOPASSWORD = System.getenv("TROPOPASSWORD");

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
	    
	       if ( null == m || m.isEmpty() ) {
		   throw new IllegalArgumentException("empty request received");
	       }
	       
		final String personEmailAsPrimaryKey = "Person Email";
		// assign the lambda logger
		ll = context.getLogger();
		ll.log("Input: " + m.toString());

		String retUrl = m.entrySet().stream().filter( e -> e.getKey().equals("retURL")).map(Map.Entry::getValue).findFirst().orElse(null);
		String desiredUsername = m.entrySet().stream().filter( e -> e.getKey().equals("00N6100000HSuwR")).map(Map.Entry::getValue).findFirst().orElse(null);
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
			desiredUsername = ( (m.get("email")!=null)?"tropoisv"+m.get("email").split("@")[0]:null);
		}



		// if the username is already in use we want to tell this application
		if ( null != desiredUsername )  {
			TropoUsernameConnector u = new TropoUsernameConnector(TROPOUSERNAME, TROPOPASSWORD, ll, "https://api.tropo.com/v1/users/");

			int n = 0;

			if ( 404 != ( n= u.getResponseCode(desiredUsername))) {
				ll.log("Username lookup returned " + n);
				return String.format(retErrorHTML, "The username " + desiredUsername + " is already in use.", "https://www.tropo.com/portal/register", retUrl, "5");
			}
		}

		// this filters all the entries we don't have columns for in smartsheet and also remapps the input field designation to the smartsheet column name
		m = m.entrySet().stream().
				filter( e -> ConvertInputFieldsToSmartSheetColumns.getMappedField(e.getKey(),ll)!=null).
				collect(Collectors.toMap (
						e -> ConvertInputFieldsToSmartSheetColumns.getMappedField(e.getKey(),ll),
						e-> decode(e.getValue())
						));


		// before we insert it we will add some custom fields
		m.put("Primary Column", m.get(personEmailAsPrimaryKey));
		m.put("Approval Status", "Yellow");


		SingleSmartSheet sss = new SingleSmartSheet(context);
		
		try { 
		    if ( null == sss.init(SMARTSHEETACCESSTOKEN,SMARTSHEETSHEETID) ) {
			throw new IllegalStateException("could not connec to smartsheet server");
		    }
		} catch ( Exception e ) {
			ll.log("Could not access smartsheet with the credentials provided");
			 // format is 1. error message 2. manual click usually just tropo.com 3. redirect website 4. delay Timer
			return String.format(retErrorHTML, "We apologize for this, but experienced some server side error. Please try again later", "https://www.tropo.com", retUrl, "5");
		}


		String emailAsPrimaryKey = m.get(personEmailAsPrimaryKey);

		if ( null!=emailAsPrimaryKey && sss.primaryKeyUsed(m.get(personEmailAsPrimaryKey))) {
			ll.log("Primary Key for  " + emailAsPrimaryKey + " is already used.");
			ll.log("Wont try to insert it. You can always change the sheet itself.");
			 // format is 1. error message 2. manual click usually just tropo.com 3. redirect website 4. delay Timer
			return (String.format(retErrorHTML, "You have already applied for this program with this email address.", "https://www.tropo.com", retUrl, "3"));
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
