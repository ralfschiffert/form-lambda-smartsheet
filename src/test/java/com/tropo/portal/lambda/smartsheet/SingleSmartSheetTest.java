package com.tropo.portal.lambda.smartsheet;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import java.lang.reflect.*;
import java.util.ArrayList;
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

	public static String SMARTSHEETACCESSTOKEN = System.getProperty("SMARTSHEETACCESSTOKEN");
	public static String SMARTSHEETSHEETID = System.getProperty("SMARTSHEETSHEETID");
	private static Context ctx = null;
	private static LambdaLogger ll = null;

	@BeforeClass
	public static void createContext() {
		TestContext context = new TestContext();
		context.setFunctionName("SmartSheetFunctionJunitTest");
		ll =context.getLogger();
		ctx = context;
	}


	private Map<String, String> testData(boolean uniquePrimaryColumn) {
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


	@Test
	public void ATestSuccessfulInitOfSmartSheet() {
		// the init wants an AWS context for logging
		SingleSmartSheet sss = new SingleSmartSheet(ctx);
		// this should not throw an exception
		assertNotNull("The init with the right credentials didn't work",sss.init(SMARTSHEETACCESSTOKEN, SMARTSHEETSHEETID));
	}


	@Test
	public void ChainingOfMethodsToInitTest() {
		// the init wants an AWS context for logging
		SingleSmartSheet sss = new SingleSmartSheet(ctx);
		// this should not throw an exception
		assertNotNull("The init with the right credentials didn't work",sss.init(SMARTSHEETACCESSTOKEN, SMARTSHEETSHEETID).doesPrimaryColumnExist());

	}

	@Test(expected=IllegalStateException.class )
	public void ABUnSuccessfulInitOfSmartSheetDueToWrongSheetId() {
		// the init wants an AWS context for logging
		SingleSmartSheet sss = new SingleSmartSheet(ctx);
		// this should not throw an exception
		assertNotNull("This sheet should not be existing",sss.init(SMARTSHEETACCESSTOKEN, SMARTSHEETSHEETID+"0"));
	}

	@Test (expected=IllegalStateException.class )
	public void BUnsuccessfulInitOfSmartSheeDueToWrongAccessToken() {
		// the init wants an AWS context for logging
		SingleSmartSheet sss = new SingleSmartSheet(ctx);
		assertNotNull("couldn't create a SingleSmartSheet class",sss);
		sss.init(SMARTSHEETACCESSTOKEN+"o", SMARTSHEETSHEETID);
	}


	@Test(expected=IllegalArgumentException.class)
	public void CUnsuccessfulInitOfSmartSheetDueEmptyAccessToken() {
		// the init wants an AWS context for logging
		SingleSmartSheet sss = new SingleSmartSheet(ctx);
		assertNotNull("couldn't create a SingleSmartSheet class",sss);
		// this should throw the exception
		sss.init(null, SMARTSHEETSHEETID);
	}


	@Test(expected=InvocationTargetException.class)
	public void DUnSuccessfulOpeningOfSheetDueToNoInit() throws Exception {
		// the init wants an AWS context for logging
		SingleSmartSheet sss = new SingleSmartSheet(ctx);
		// method is private, so we need to do some trickery
		Method method = sss.getClass().getDeclaredMethod("openOrReloadSheet");
		method.setAccessible(true);
		assertNotNull("failed to open and return sheet after init",method.invoke(sss));
	}


	@Test
	public void ESuccessfulReopeningOfSheet() throws Exception {
		// the init wants an AWS context for logging
		SingleSmartSheet sss = new SingleSmartSheet(ctx);
		sss.init(SMARTSHEETACCESSTOKEN, SMARTSHEETSHEETID);
		// method is private, so we need to do some trickery
		Method method = sss.getClass().getDeclaredMethod("openOrReloadSheet");
		method.setAccessible(true);
		Optional<Sheet> s = (Optional<Sheet>) method.invoke(sss);
		assertTrue("failed to open and return sheet after successful init", s.isPresent());
	}

	@Test
	public void getCellValueByColumnName() {
		SingleSmartSheet sss = new SingleSmartSheet(ctx);
		sss.init(SMARTSHEETACCESSTOKEN, SMARTSHEETSHEETID);
		Optional<Sheet> os = sss.getSheetRepresentation();
		assertTrue("failed to open and return sheet after init",os.isPresent());

		Row r =  sss.readyRowForInsertion(testData(true)).get();
		assertNotNull("couldn't build the row from test data", r);
		assertTrue("rowContainer does not contain 1 row", 1==sss.getNofRowsReadyToInsert());
		try {
			Method getCellMethod = sss.getClass().getDeclaredMethod("getCellValueByColumnName",Row.class,String.class);
			getCellMethod.setAccessible(true);
			Optional<String> s =  (Optional<String>)getCellMethod.invoke(sss, r, "Person Lastname");
			assertTrue("couldn't get the Person Lastname cell from the test data set", s.isPresent());
			assertTrue("Person Lastname contained the wrong value", "K".equals(s.get()));
		}
		catch ( Exception e) {
			assertTrue("issue with the reflection access", false);
		}
		assertTrue("could not empty the cache container", 1==sss.clearRowCacheContainer());

	}


	@Test
	public void GSuccessfuRetrievalOfRowFromSheetAndCellFromRow() throws Exception {
		final int rowNb = 1;
		// the init wants an AWS context for logging
		SingleSmartSheet sss = new SingleSmartSheet(ctx);
		sss.init(SMARTSHEETACCESSTOKEN, SMARTSHEETSHEETID);

		Optional<Sheet> os = sss.getSheetRepresentation();
		assertTrue("failed to open and return sheet after init",os.isPresent());

		Row r = os.get().getRowByRowNumber(os.get().getTotalRowCount());
		assertNotNull("could not get row from sheet",r);
		Method getCellMethod = sss.getClass().getDeclaredMethod("getCellByColumnName",Row.class,String.class);
		assertNotNull("couldn't find method getCellByColumnName in class", getCellMethod);
		getCellMethod.setAccessible(true);
		Optional<Cell> c =  (Optional<Cell>)getCellMethod.invoke(sss, r, "Person Lastname");
		assertTrue("could not retrieve Person Lastname for last row", c.isPresent());
		ll.log("Display Value " + c.get().getDisplayValue());
		assertTrue("display lastname was empty ", ! c.get().getDisplayValue().isEmpty());
	}


	@Test
	public void HBuildListOfCellsFromDataMapTest() throws Exception {
		// let's build a data map
		Map<String, String> data = new HashMap<String, String>(); // column name followed by value
		data.put("Person Firstname", "Value A");
		data.put("Solution Description", "Value B");
		data.put("Tropo Developer Account", "Value C");

		SingleSmartSheet sss = new SingleSmartSheet(ctx);
		sss.init(SMARTSHEETACCESSTOKEN, SMARTSHEETSHEETID);
		Method buildCellListMethod = sss.getClass().getDeclaredMethod("buildListOfCellsFromDataMap", Map.class);
		assertNotNull("couldn't find method buildListOfCellsFromDataMap in class", buildCellListMethod);
		buildCellListMethod.setAccessible(true);
		List<Cell> l = (List<Cell>)buildCellListMethod.invoke(sss, data);
		assertNotNull("could not get list of cells from data", l);
		// we should access a cell
		// we could overwrite the containsAll, but this seems too much action for this simple test
		// instead let's just iterate over the list and check every time we get a correct value back
		// to sharpen our skills we use some java 8 stuff
		int found = 0;
		// l.stream().map( (Cell)e ->  { if ( e.getDisplayValue().matches("Value [ABC]")) { ++found; }}  );
		// l.stream().map( e -> e.getDisplayValue().matches("Value [ABC]"));
		for ( Cell cell : l ) {
			String tmp = (String) cell.getValue();
			if (tmp.matches("Value [ABC]")) {
				++found;
			}
		}
		assertTrue("building the cell returned the wrong values", found==3);
	}

	@Test
	public void ITestRowPreparationAndInsertion() {
		SingleSmartSheet sss = new SingleSmartSheet(ctx);
		sss.init(SMARTSHEETACCESSTOKEN, SMARTSHEETSHEETID);

		Row r2 =  sss.readyRowForInsertion(testData(true)).get();
		Row r3 =  sss.readyRowForInsertion(testData(true)).get();
		assertNotNull("could NOT build row despite having a unique primary key", r2);
		assertNotNull("could NOT build row despite having a unique primary key", r3);
		assertTrue("prepped 2 rows for insertion but didn't receive 2 rows back", sss.getNofRowsReadyToInsert()==2);
		// let's now insert the rows
		int previousRowCount = sss.getNofRowsInSmartSheet();
		assertTrue("could not insert the two prepped rows", 2==sss.insertRowOrRows());
		assertTrue("current row count is not 2 more than previous row count", sss.getNofRowsInSmartSheet()-2==previousRowCount);
		assertTrue("deletion of last inserted rows failed", 2==sss.deleteLastInserted());
	}


	@Test
	public void KRedundantPrimaryKeyDetectionTest() {
		SingleSmartSheet sss = new SingleSmartSheet(ctx, true);
		sss.init(SMARTSHEETACCESSTOKEN, SMARTSHEETSHEETID);

		int previousRowCount = sss.getNofRowsInSmartSheet();
		Map<String, String> data = testData(true);
		Row r1 = sss.readyRowForInsertion(data).get();
		assertNotNull("Couldn't ready the row for insertion", r1);
		assertTrue("Could not insert row", 1==sss.insertRowOrRows());
		assertTrue("Something went wrong with the row insertion", sss.getNofRowsInSmartSheet() == 1+previousRowCount);
		Optional<Row> r2 = sss.readyRowForInsertion(data);
		assertTrue("Could insert redundant row",  !r2.isPresent() );
		assertTrue("could not delete the last inserted row", 1==sss.deleteLastInserted());
		sss.clearRowCacheContainer();

	}



	@Test
	public void JFindPrimaryKeyTest() {
		SingleSmartSheet sss = new SingleSmartSheet(ctx);
		sss.init(SMARTSHEETACCESSTOKEN, SMARTSHEETSHEETID);

		final String existingKey = "hangchneg@cisco.com";
		final String nonExistentKey = UUID.randomUUID().toString();
		assertTrue("primary key test for key " + existingKey, sss.primaryKeyUsed(existingKey));
		assertFalse("primary key test for key " + nonExistentKey, sss.primaryKeyUsed(nonExistentKey));
	}


	@Test
	public void initCheckTest() {
		SingleSmartSheet sss = new SingleSmartSheet(ctx);

		// Get the private field
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
			assertTrue("our reflection mechanism didn't work", false);
		}


		assertTrue("This should work since we call the init under the hood", sss.getSheetRepresentation().isPresent());
	}


	@Test(expected = IllegalArgumentException.class)
	public void testPreconditionInInit() {
		SingleSmartSheet sss = new SingleSmartSheet(ctx);
		sss.init(SMARTSHEETACCESSTOKEN, null);

	}


	@Test
	public void KCleanupRoutineTest() {
		SingleSmartSheet sss = new SingleSmartSheet(ctx);
		sss.init(SMARTSHEETACCESSTOKEN, SMARTSHEETSHEETID);

		final int nofRows = sss.getNofRowsInSmartSheet();
	    Optional<Row> rowToInsert =  sss.readyRowForInsertion(testData(true));
		assertTrue("Could not create a row from the testData", rowToInsert.isPresent());
		assertTrue("Could not insert testrow into smartsheet", 1==sss.insertRowOrRows());

        // now let's clear our row again
		assertTrue("could not delete 1 row", 1==sss.deleteLastInserted());
		assertFalse("number of rows in smartsheet changed", nofRows != sss.getNofRowsInSmartSheet());
	}
}
