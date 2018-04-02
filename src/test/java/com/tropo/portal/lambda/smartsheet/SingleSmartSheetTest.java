package com.tropo.portal.lambda.smartsheet;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.smartsheet.api.models.Cell;
import com.smartsheet.api.models.Row;
import com.smartsheet.api.models.Sheet;

public class SingleSmartSheetTest {

	public static String SMARTSHEETACCESSTOKEN = System.getenv("SMARTSHEETACCESSTOKEN");
	public static String SMARTSHEETSHEETID = System.getenv("SMARTSHEETSHEETID");
	private static Context ctx = null;
	private static LambdaLogger ll = null;
	private SingleSmartSheet sss = null;
	
	
	private Map<String, String> setupTestData(boolean uniquePrimaryColumn) {
		Map<String, String> data = new HashMap<String, String>();

		if ( !uniquePrimaryColumn ) {
			data.put("Primary Column",  "hangchneg@cisco.com");
		} else {
			data.put("Primary Column",  UUID.randomUUID().toString());
		}
		data.put("Approval Status", "Yellow");
		data.put("Company Name", "Hancheng Demo");
		data.put("Company Website","http://www.mycomapnywebsite.com/init.html");
		data.put("Company Size", "11113");
		data.put("Company Industry", "Networking2");
		data.put("Company Existing ISV", "no");
		data.put("Solution Description", "This is my solution description");
		data.put("Solution URL", "http://www.mysolutionurl.com/phase.html");
		data.put("Person Salutation", " Dr.");
		data.put("Person Firstname", "Agent");
		data.put("Person Lastname", "K");
		data.put("Person Job Title", "Man In Black");
		data.put("Person Email", "agentk@mib.movie");
		data.put("Person Phone", "(650)245-9999");
		data.put("Person Country", "U.S.A");
		data.put("Desired Tropo Username", "AgentKRocks");
		data.put("Spark Developer Account", "agentk@mib.com");
		data.put("Tropo Developer Account", "511511511");

		return data;
	}

	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	@BeforeClass
	public static void createContext() {
		TestContext context = new TestContext();
		context.setFunctionName("SmartSheetFunctionJunitTest");
		ll =context.getLogger();
		ctx = context;
	}

	@Before
	public void SuccessfulInitOfSmartSheetWtihCorrectTokenAndSheetId() {
		// the init wants an AWS context for logging
		sss = new SingleSmartSheet(ctx);
		// this should not throw an exception
		assertThat("The init with the right credentials didn't work", sss.init(SMARTSHEETACCESSTOKEN, SMARTSHEETSHEETID), is(notNullValue()));
	}
	

	@Test
	public void UnSuccessfulInitOfSmartSheetDueToWrongSheetId() {
		// this should not throw an exception
		String invalidSmartSheetId =  SMARTSHEETSHEETID+"0";
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Couldn't get the sheet. Could be token, network or sheetID:" + invalidSmartSheetId);
		assertThat("This sheet should not be existing", sss.init(SMARTSHEETACCESSTOKEN, invalidSmartSheetId), is(nullValue()));
	}

	
	@Test
	public void UnsuccessfulInitOfSmartSheeDueToWrongAccessToken() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Couldn't get the sheet. Could be token, network or sheetID:" + SMARTSHEETSHEETID);
		assertThat(sss.init(SMARTSHEETACCESSTOKEN+"o", SMARTSHEETSHEETID), is(nullValue()));
	}


	@Test
	public void UnsuccessfulInitOfSmartSheetDueEmptyAccessToken() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("no access token provided or access token empty");
		assertThat(sss.init(null, SMARTSHEETSHEETID), is(nullValue()));
	}


	@Test
	public void UnsuccessfulOpeningOfSheetDueToNoInit() throws Exception {
		// method is private, so we need to do some trickery
		SingleSmartSheet sssLocal = new SingleSmartSheet(ctx);
		Method method = sssLocal.getClass().getDeclaredMethod("openOrReloadSheet");
		method.setAccessible(true);
		assertThat("failed to open and return sheet after init",method.invoke(sss), is(notNullValue()));
	}


	@Test
	public void SuccessfulReopeningOfSheeWIthCorrectTokenAndSheetId() throws Exception {
		// method is private, so we need to do some trickery
		Method method = sss.getClass().getDeclaredMethod("openOrReloadSheet");
		method.setAccessible(true);
		Optional<Sheet> s = (Optional<Sheet>) method.invoke(sss);
		assertThat("failed to open and return sheet after successful init", s.isPresent(), is(true));
	}

	@Test
	public void SuccessfulCreationOfRowFromTestData() {
		Row r =  sss.readyRowForInsertion(setupTestData(true)).get();
		assertThat("couldn't build the row from test data", r, is(notNullValue()));
		assertThat("rowContainer does not contain 1 row", sss.getNofRowsReadyToInsert(), equalTo(1));
	}
	
	
	@Test
	public void SuccessfulRetrievalOfPersonLastNameFromCreatedRowByColumnName() {
		Row r =  sss.readyRowForInsertion(setupTestData(true)).get();
		assertThat("couldn't build the row from test data", r, is(notNullValue()));
		assertThat("rowContainer does not contain 1 row", sss.getNofRowsReadyToInsert(), equalTo(1));
		
		Optional<String> s = sss.getCellValueByColumnName(r, "Person Lastname");
		assertThat("couldn't get the Person Lastname cell from the test data set", s.isPresent(), is(true));
		assertThat("Person Lastname contained the wrong value", s.get(), equalTo("K"));
		assertThat("could not empty the cache container", sss.clearRowCacheContainer(), equalTo(1));
	}


	@Test
	public void SuccessfuRetrievalOfRowFromSheetAndLastNameCellFromRow() throws Exception {
		final int rowNb = 1;
		Optional<Sheet> os = sss.getSheetRepresentation();
		assertThat("failed to open and return sheet after init",os.isPresent(), is(true));

		Row r = os.get().getRowByRowNumber(os.get().getTotalRowCount());
		assertThat("could not get first row from sheet",r,is(notNullValue()));
		
		Method getCellMethod = sss.getClass().getDeclaredMethod("getCellByColumnName",Row.class,String.class);
		assertThat("couldn't find method getCellByColumnName in class", getCellMethod, is(notNullValue()));
		getCellMethod.setAccessible(true);
		
		Optional<Cell> c =  (Optional<Cell>)getCellMethod.invoke(sss, r, "Person Lastname");
		assertThat("could not retrieve Person Lastname for last row", c.isPresent(), is(true));
		assertThat("display lastname was empty ", c.get().getDisplayValue(), not(isEmptyOrNullString()));
	}


	@Test
	public void SuccessfulBuildListOfCellsFromDataMapWithRetrieval() throws Exception {
		// let's build a data map
		Map<String, String> data = new HashMap<String, String>(); // column name followed by value
		data.put("Person Firstname", "Value A");
		data.put("Solution Description", "Value B");
		data.put("Tropo Developer Account", "Value C");

		Method buildCellListMethod = sss.getClass().getDeclaredMethod("buildListOfCellsFromDataMap", Map.class);
		assertThat("couldn't find method buildListOfCellsFromDataMap in class", buildCellListMethod, is(notNullValue()));
		buildCellListMethod.setAccessible(true);
		
		List<Cell> l = (List<Cell>)buildCellListMethod.invoke(sss, data);
		assertThat("could not get list of cells from data", l, is(notNullValue()));
		// we should access a cell
		// we could overwrite the containsAll, but this seems too much action for this simple test
		// instead let's just iterate over the list and check every time we get a correct value back
		// to sharpen our skills we use some java 8 stuff
		// l.stream().map( (Cell)e ->  { if ( e.getDisplayValue().matches("Value [ABC]")) { ++found; }}  );
		// l.stream().map( e -> e.getDisplayValue().matches("Value [ABC]"));
		Long found = l.stream().filter(e -> ((String)(e.getValue())).matches("Value [ABC]")).count();
		assertThat("building the cell returned the wrong values", found, equalTo(3L));
	}
	


	@Test
	public void SuccessfulPrepationOfUniquePrimaryKeysWithInsertionAndCleamup() {
		Row r2 =  sss.readyRowForInsertion(setupTestData(true)).get();
		Row r3 =  sss.readyRowForInsertion(setupTestData(true)).get();
		assertThat("could NOT build row despite having a unique primary key", r2, is(notNullValue()));
		assertThat("could NOT build row despite having a unique primary key", r3, is(notNullValue()));
		assertThat("prepped 2 rows for insertion but didn't receive 2 rows back", sss.getNofRowsReadyToInsert(), equalTo(2));
		// let's now insert the rows
		int previousRowCount = sss.getNofRowsInSmartSheet();
		assertThat("could not insert the two prepped rows", sss.insertRowOrRows(), equalTo(2));
		assertThat("current row count is not 2 more than previous row count", sss.getNofRowsInSmartSheet()-2, equalTo(previousRowCount));
		assertThat("deletion of last inserted rows failed", sss.deleteLastInserted(), equalTo(2));
	}


	@Test
	public void PreventRedundantPrimaryKey() {
	
		int previousRowCount = sss.getNofRowsInSmartSheet();
		Map<String, String> data = setupTestData(true);
		Row r1 = sss.readyRowForInsertion(data).get();
		
		assertThat("Couldn't ready the row for insertion", r1, is(notNullValue()));
		assertThat("Could not insert row", sss.insertRowOrRows(), equalTo(1));
		assertThat("Something went wrong with the row insertion", sss.getNofRowsInSmartSheet(), equalTo(1+previousRowCount));
		
		Optional<Row> r2 = sss.readyRowForInsertion(data);
		assertThat("Could insert redundant row",  r2.isPresent() , is(true));
		assertThat("could not delete the last inserted row", sss.deleteLastInserted(), equalTo(1));
		sss.clearRowCacheContainer();
	}



	@Test
	public void SuccessfulIdentificationOfExistingAndNonExistingPrimaryKey() {
		final String existingKey = "hangchneg@cisco.com";
		final String nonExistentKey = UUID.randomUUID().toString();
		assertThat("primary key test for key " + existingKey, sss.primaryKeyUsed(existingKey), is(true));
		assertThat("primary key test for key " + nonExistentKey, sss.primaryKeyUsed(nonExistentKey), is(false));
	}


	@Test
	public void SucccessulIntelligentInitEvenWhenPublicInitiDoesntWork() {
		try {
			final Field tokenField = SingleSmartSheet.class.getDeclaredField("accessToken");
			final Field sheetIdField = SingleSmartSheet.class.getDeclaredField("sheetId");
			// Allow modification on the field
			tokenField.setAccessible(true);
			sheetIdField.setAccessible(true);
			// Return the Obect corresponding to the field
			tokenField.set(sss, SMARTSHEETACCESSTOKEN);
			sheetIdField.set(sss, Long.parseLong(SMARTSHEETSHEETID));
		}
		catch ( Exception e ) {
			assertThat("our reflection mechanism didn't work", is(false));
		}
		assertThat("This should work since we call the init under the hood", sss.getSheetRepresentation().isPresent(), is(true));
	}


	@Test
	public void SuccessfulCleanupRoutineAfterTest() {
	
		final int nofRows = sss.getNofRowsInSmartSheet();
	    Optional<Row> rowToInsert =  sss.readyRowForInsertion(setupTestData(true));
		assertThat("Could not create a row from the testData", rowToInsert.isPresent(), is(true));
		assertThat("Could not insert testrow into smartsheet",sss.insertRowOrRows(),equalTo(1));

        // now let's clear our row again
		assertThat("could not delete 1 row",sss.deleteLastInserted(), equalTo(1));
		assertThat("number of rows in smartsheet changed", sss.getNofRowsInSmartSheet(), is(nofRows));
	}
}
