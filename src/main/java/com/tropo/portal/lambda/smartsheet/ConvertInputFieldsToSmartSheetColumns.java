package com.tropo.portal.lambda.smartsheet;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

import java.util.HashMap;

// this is a sample response from the form submission
/*
 * Message Body
00N6100000HSuwM=1
00N6100000HSuwN=1
00N6100000HSuwO=mysparkuser%40sparkbot.io
00N6100000HSuwP=http%3A%2F%2Fwww.myscreencast.com
00N6100000HSuwQ=jjddkkdkddkdk
00N6100000HSuwR=desiredusername
00N6100000HSuwW=1
00N6100000BT0HY=This+is+a+description+of+my+project
company=pjdjdjd
email=agnetkf@jdjdjd.com
employees=%3E+1000
first_name=AgentK
industry=Communications
last_name=AgentY
lead_source=Tropo.com+Webform&
oid=00D61000000dO6M
phone=6502459001
retURL=https%3A%2F%2Fwww.tropo.com%2Fthankyou
title=PM
URL=http%3A%2F%2Fwww.cisco.com
 * 
 */

// with this we can derive the following mapping
/*
 *  URL -> 'Company Website'
 *  title -> 'Person Job Title'
 *  retURL -> nothing - we don't have to log it
 *  phone -> 'Person Phone'
 *  oid -> org id - we don't need this anymore
 *  lead_source -> 'Lead Source'
 *  last_name -> 'Person Lastname'
 *  industry -> 'Company Industry'
 *  first_name -> 'Person Firstname'
 *  employees -> 'Company Size'
 *  email -> 'Person Email'
 *  company -> 'Company Name'
 *  00N6100000BT0HY -> 'Solution Description'
 *  00N6100000HSuwW=1
 *  00N6100000HSuwR -> 'Desired Username'
 *  00N6100000HSuwQ=kdjdjkdkjdkjk
 *  00N6100000HSuwP -> 'Solution URL'
 *  00N6100000HSuwO=test%40test.io
 */

public class ConvertInputFieldsToSmartSheetColumns {
	
	private static Map<String, String> fieldMapping = new HashMap<String, String>();
	
	static {
		fieldMapping.put("URL", "Company Website");
		fieldMapping.put("title", "Person Job Title");
		// retURL is going to be ignored
		fieldMapping.put("phone", "Person Phone");
		// oid=orgid - we don"t need this orgid anymore it"s SFDC specific
		fieldMapping.put("country", "Person Country");
		fieldMapping.put("salutation", "Person Salutation");
		fieldMapping.put("lead_source", "Lead Source");
		fieldMapping.put("last_name", "Person Lastname");
		fieldMapping.put("industry", "Company Industry");
		fieldMapping.put("first_name", "Person Firstname");
		fieldMapping.put("employees", "Company Size");
		fieldMapping.put("email", "Person Email");
		fieldMapping.put("company", "Company Name");
		fieldMapping.put("00N6100000BT0HY", "Solution Description");
		fieldMapping.put("00N6100000HSuwW", "Company Existing ISV");
		fieldMapping.put("00N6100000HSuwR", "Desired Tropo Username");
		fieldMapping.put("00N6100000HSuwQ", "Tropo Developer Account");
		fieldMapping.put("00N6100000HSuwP", "Solution URL");
		fieldMapping.put("00N6100000HSuwO", "Spark Developer Account");
		fieldMapping.put("00N6100000HSuwM", "Checkbox Developer Account");
		fieldMapping.put("00N6100000HSuwN", "Checkbox Spark Bot");
	}
	
	public static String getMappedField(String formField, LambdaLogger ll) {
		// we assume if we cannot find the value we leave it as is
		String mappedValue = fieldMapping.get(formField);
		ll.log("Mapped value " + formField + " to " +( (mappedValue!=null)?mappedValue:"?"));
		return mappedValue;
		// return ( (null!=mappedValue)?mappedValue: formField);
	}
}
