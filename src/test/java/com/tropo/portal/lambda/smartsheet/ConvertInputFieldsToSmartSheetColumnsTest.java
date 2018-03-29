package com.tropo.portal.lambda.smartsheet;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.*;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class ConvertInputFieldsToSmartSheetColumnsTest {
	
	private String decode(String s) {
		
		String converted ="";
		try {
			converted = java.net.URLDecoder.decode(s, "UTF-8");
		} catch ( UnsupportedEncodingException e ) {
			System.out.println("Could not convert one of the values" + e.getMessage());
		}
		return converted;
	}


	@Test
	public void convertFormFieldsToSmartSheetColumnsTest() {	
		
		TestContext ctx = new TestContext();
		 LambdaLogger ll = ctx.getLogger();
	
		assertTrue("couldn't map field correctly " + "URL", ConvertInputFieldsToSmartSheetColumns.getMappedField("URL",ll).equals("Company Website"));
		assertTrue("couldn't map field correctly" + "title", ConvertInputFieldsToSmartSheetColumns.getMappedField("title",ll).equals("Person Job Title"));
		assertTrue("couldn't map field correctly" + "phone", ConvertInputFieldsToSmartSheetColumns.getMappedField("phone",ll).equals("Person Phone"));
		assertTrue("couldn't map field correctly" + "lead_source", ConvertInputFieldsToSmartSheetColumns.getMappedField("lead_source",ll).equals("Lead Source"));
		assertTrue("couldn't map field correctly" + "last_name", ConvertInputFieldsToSmartSheetColumns.getMappedField("last_name",ll).equals("Person Lastname"));
		assertTrue("couldn't map field correctly" + "industry", ConvertInputFieldsToSmartSheetColumns.getMappedField("industry",ll).equals("Company Industry"));
		assertTrue("couldn't map field correctly" + "first_name", ConvertInputFieldsToSmartSheetColumns.getMappedField("first_name",ll).equals("Person Firstname"));
		assertTrue("couldn't map field correctly" + "employees", ConvertInputFieldsToSmartSheetColumns.getMappedField("employees",ll).equals("Company Size"));
		assertTrue("couldn't map field correctly" + "email", ConvertInputFieldsToSmartSheetColumns.getMappedField("email",ll).equals("Person Email"));
		assertTrue("couldn't map field correctly" + "company",ConvertInputFieldsToSmartSheetColumns.getMappedField("company",ll).equals("Company Name"));
		assertTrue("couldn't map field correctly"+ "00N6100000BT0HY", ConvertInputFieldsToSmartSheetColumns.getMappedField("00N6100000BT0HY",ll).equals("Solution Description"));
		assertTrue("couldn't map field correctly"+ "00N6100000HSuwW", ConvertInputFieldsToSmartSheetColumns.getMappedField("00N6100000HSuwW",ll).equals( "Company Existing ISV"));
		assertTrue("couldn't map field correctly"+ "00N6100000HSuwR", ConvertInputFieldsToSmartSheetColumns.getMappedField("00N6100000HSuwR",ll).equals( "Desired Tropo Username"));
		assertTrue("couldn't map field correctly"+ "00N6100000HSuwQ", ConvertInputFieldsToSmartSheetColumns.getMappedField("00N6100000HSuwQ",ll).equals("Tropo Developer Account"));
		assertTrue("couldn't map field correctly"+ "00N6100000HSuwP", ConvertInputFieldsToSmartSheetColumns.getMappedField("00N6100000HSuwP",ll).equals("Solution URL"));
		assertTrue("couldn't map field correctly"+ "00N6100000HSuwO", ConvertInputFieldsToSmartSheetColumns.getMappedField("00N6100000HSuwO",ll).equals("Spark Developer Account"));
		assertTrue("couldn't map field correctly" + "country", ConvertInputFieldsToSmartSheetColumns.getMappedField("country",ll).equals("Person Country"));
		assertTrue("couldn't map field correctly" + "salutation", ConvertInputFieldsToSmartSheetColumns.getMappedField("salutation",ll).equals("Person Salutation"));
		assertTrue("couldn't map field correctly" + "00N6100000HSuwM", ConvertInputFieldsToSmartSheetColumns.getMappedField("00N6100000HSuwM",ll).equals("Checkbox Developer Account"));
		assertTrue("couldn't map field correctly" + "00N6100000HSuwN", ConvertInputFieldsToSmartSheetColumns.getMappedField("00N6100000HSuwN",ll).equals("Checkbox Spark Bot"));
	}
	
	
	
	private Context createContext() {
		// TODO Auto-generated method stub
		return null;
	}


	@Test
	public void testThatFormFieldsMapToColumnsInSmartSheet() {	
	
		TestContext context = new TestContext();
		// TODO: customize your context here if needed.
		context.setFunctionName("Your Function Name");
		LambdaLogger ll = context.getLogger();
		
		SingleSmartSheet sss = new SingleSmartSheet(context);
		sss.init(SingleSmartSheetTest.ACCESSTOKEN, SingleSmartSheetTest.SHEETID); // column mapping is part of the init
		
		assertFalse("column name check doesn't work", sss.doesColumnExist("ESELSOHR").isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist("Company Name").isPresent());
		
		// all these should exist in the sheet
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("URL",ll)).isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("title",ll)).isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("phone",ll)).isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("lead_source",ll)).isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("last_name",ll)).isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("industry",ll)).isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("first_name",ll)).isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("employees",ll)).isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("email",ll)).isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("company",ll)).isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("00N6100000BT0HY",ll)).isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("00N6100000HSuwW",ll)).isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("00N6100000HSuwR",ll)).isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("00N6100000HSuwQ",ll)).isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("00N6100000HSuwP",ll)).isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("00N6100000HSuwO",ll)).isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("country",ll)).isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("salutation",ll)).isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("00N6100000HSuwM",ll)).isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("00N6100000HSuwN",ll)).isPresent());
		assertTrue("column name check doesn't work", sss.doesColumnExist(ConvertInputFieldsToSmartSheetColumns.getMappedField("URL",ll)).isPresent());
	}
	
	
	@Test
	public void conversionOfMapFromInputFieldsToSmartSheetFieldsTest() throws UnsupportedEncodingException {
	
		TestContext context = new TestContext();
		// TODO: customize your context here if needed.
		context.setFunctionName("Your Function Name");
		LambdaLogger ll = context.getLogger();
		
		
		Map<String, String> m = new HashMap<String, String>();
		m.put("oid","00D61000000dO6M");
		m.put("retURL","https%3A%2F%2Fwww.sssssss.com%2Fthankyou");
		m.put("lead_source","jsjksjsjsj");
		m.put("first_name","sss");
		m.put("last_name","Scssssst");
		m.put("email","ragentk@ddjdjdkj.e");
		m.put("phone","6502459999");
		m.put("00N6100000HSuwR","desiredusername");
		m.put("title","PM");
		m.put("company","dkdklldkd");
		m.put("URL","http%3A%2F%2Fwww.dkdklldkd.com");
		m.put("employees","%3E+1000");
		m.put("industry","Communications");
		m.put("00N6100000HSuwP","http%3A%2F%2Fwww.myscreencast.com");
		m.put("00N6100000HSuwW","1");
		m.put("00N6100000HSuwN","1");
		m.put("00N6100000HSuwO","dddd%ddddd.io");
		m.put("00N6100000HSuwM","1");
		m.put("00N6100000HSuwQ","dddeejejJJKKJKJ");
		m.put("00N6100000BT0HY","This+is+a+description+of+my+project");
		
		System.out.println(m); 
		
		m = m.entrySet().parallelStream().filter( (e) -> ConvertInputFieldsToSmartSheetColumns.getMappedField(e.getKey(),ll)!=null).collect(
				Collectors.toMap (
						(e) -> ConvertInputFieldsToSmartSheetColumns.getMappedField(e.getKey(),ll),
						(e)-> decode(e.getValue())
						));
		
		System.out.println(m);
		
	}
	
	
	

}
