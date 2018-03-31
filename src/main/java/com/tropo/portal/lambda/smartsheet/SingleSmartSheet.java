package com.tropo.portal.lambda.smartsheet;

//Add Maven library "com.smartsheet:smartsheet-sdk-java:2.2.3" to access Smartsheet Java SDK
import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.models.Cell;
import com.smartsheet.api.models.Column;
import com.smartsheet.api.models.Row;
import com.smartsheet.api.models.Sheet;
import java.util.*;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.Context;

public class SingleSmartSheet {

	// the following should be set even if the init is not true yet
	private String accessToken = null;
	private Context context = null;
	private LambdaLogger ll = null;
	private boolean enforceUniquePrimaryKey = false; // this still can be enforced from the outside
	
	
	// the following fields should be set if the init==true
	private boolean initSuccess = false;
	private  Long sheetId = null;
	private Map<String, Long> columnMap = new HashMap<String, Long>();
	private Smartsheet client = null;
	private Sheet smartsheetNative = null;
	private String primaryColumnName = null;	
	private Long primaryColumnId = -1L;
	
	// the following fields will be set as the program progresses
	private List<Row> rowContainer = new ArrayList<Row>();
	private List<Row> lastInserted = new ArrayList<Row>();
	

	/*static {
        // These lines enable logging to the console
        System.setProperty("Smartsheet.trace.parts", "RequestBodySummary,ResponseBodySummary");
        System.setProperty("Smartsheet.trace.pretty", "true");
	} */

	// default parameter-free constructor
	public SingleSmartSheet(Context ctx)  {
		
		this.context = ctx;
		// we should get a logger so we can do some diagnostics 		
		ll = context.getLogger();
		ll.log("smartsheet constructor called");
	}
	

	// false for null objects and empty Strings - true otherwise
	private boolean passPrecondition( Object o ) {
		if ( null == o ) { 
			ll.log("received a null value for a parameter most likely");
			return false;
		} else if ( o instanceof String && o.toString().isEmpty() ) {
				ll.log("received an empty string as input");
				return false;
		} else if ( o instanceof Collection && ((Collection) o).isEmpty()) {
			ll.log("received an empty collection as input where we didn't expect one");
			return false;
		} 
		
		return true;
	}
	
	// in this version the object itself takes care not to insert duplicate primary keys
	public SingleSmartSheet(Context ctx, boolean enforceUniquePrimaryKey ) {
		
		this.context = ctx;
		// we should get a logger so we can do some diagnostics 		
		ll = context.getLogger();
		ll.log("smartsheet constructor called");
		
		this.enforceUniquePrimaryKey = enforceUniquePrimaryKey;
		ll.log("object enforces primary key uniqueness " + ((true==enforceUniquePrimaryKey)?"yes":"no" ));
	}

	
	private void checkIfSmartSheetInitialized() {
		if ( !initSuccess ) {
			ll.log("Not successfully initialized before trying to access sheet");
			ll.log("We will try to init it again");

			
			// Optional doesn;t give us much here, but we want to train on JAVA 8
			Optional<SingleSmartSheet> sss = Optional.ofNullable(this.init(this.accessToken,Long.toString(this.sheetId)));
			if ( !sss.isPresent() ) {
				ll.log("Finally giving up. Could not initialize the smartsheet at all.");
				throw new IllegalStateException("not successfully initialized before trying to access sheet");
			}
		}	
	}
	
	
	public Optional<Sheet> getSheetRepresentation() {
		checkIfSmartSheetInitialized();
		
		return Optional.ofNullable(smartsheetNative);
	}


	// the LONG is the Column ID if the column Exists - this seems to be the correct way to use the Optional 
	// as a return value from a public method
	public Optional<Long> doesColumnExist(String columnName) {
		// the map is built during intialization
		checkIfSmartSheetInitialized();

		ll.log("Checking for column " + columnName);
		Optional<Long> l = Optional.ofNullable(columnMap.get(columnName));

		return l;
	}
	
	
	public Optional<Long> doesPrimaryColumnExist() {
		return(doesColumnExist(primaryColumnName));
	}

	
	// smartsheets cannot have more than 5000 rows 
	// returns a negative number of number of rows cannot be retrieved
	public int getNofRowsInSmartSheet() {
		checkIfSmartSheetInitialized();

		// get the current version
		// I have seen some issues with caching
		reloadSheet();
	
		// check for null pointer here
		if (! passPrecondition(smartsheetNative )) {
			return -1;
		}
		
		Integer n = smartsheetNative.getTotalRowCount();
		ll.log("Smartsheet has " + n + " rows");
		return n;
	}


	// receives info about the sheet to read and the access token
	// checks for sheet accessibility and if accessible maps the column id's to the column names
	public SingleSmartSheet init( String token, String sheetId ) throws IllegalArgumentException, IllegalStateException {

		if ( ! passPrecondition(token) ) {
			ll.log("no access token provided or access token empty");
			initSuccess = false;
			throw new IllegalArgumentException("no access token provided or access token empty");
		}

		if ( ! passPrecondition(sheetId) ) {
			ll.log("no sheetId provided or sheetId is empty");
			initSuccess = false;
			throw new IllegalArgumentException("no sheetId provided or sheetId is empty");
		}

		this.sheetId = Long.parseLong(sheetId);
		this.accessToken = token;

		// Initialize client - throws an exception if we cannot use this token
		synchronized (this) {
			try {
				client = new SmartsheetBuilder().setAccessToken(accessToken).build();

				if ( null == client ) {
					ll.log("Could not get SDK client to access smartsheet");
					throw new IllegalStateException("Could not get SDK client to access smartsheet");
				}
			}
			catch ( IllegalStateException e ) {
				ll.log("Could not connect to the smartsheet. Likely access token invalid or not set at all. Giving up.");
				ll.log(e.getMessage());
				initSuccess = false;
				client = null;
				throw e;
			}

			// in the init we also want to map the ColumnID's to ColumnNames
			// this will make it much easier later
			try {
				smartsheetNative = client.sheetResources().getSheet(this.sheetId,  null,  null,  null,  null,  null,  null,  null);
				if ( null == smartsheetNative ) {
					// not sure if we want to throw an exception here
					// then again this is not a general purpose library and we need the smartsheet
					initSuccess = false;
					throw new IllegalStateException("Couldn't get the sheet sheetid " + this.sheetId);
				}
			}
			catch ( SmartsheetException e ) {
				ll.log("Could not get to sheet with sheetID  " + sheetId);
				ll.log("Could be sheetID or AccessToken. Will have to give up");
				initSuccess = false;
				throw new IllegalStateException("Couldn't get the sheet sheetid " + this.sheetId);
			}

			ll.log("Loaded sversion " + smartsheetNative.getVersion() + " with " + smartsheetNative.getRows().size() + " rows from sheet: " + smartsheetNative.getName());

			// Build the column map for later reference
			ll.log("Building map for column names and column ID's");
			List<Column> columns = smartsheetNative.getColumns();
			if ( ! passPrecondition(columns)) {
				// in this particular case we cannot work with empty sheets
				// they need to have the columns already set for the rest of the program to work
				ll.log("Could not get columns. Will have to give up");
				initSuccess = false;
				throw new  IllegalArgumentException("Could not access columns in sheet. We need the columns configured for this program to work");
			}
			
			columnMap = columns.stream().collect(Collectors.toMap(Column::getTitle,Column::getId));
			Column primaryColumn = columns.stream().filter( (e) -> null!=e.getPrimary()).findFirst().orElse(null);
			
			primaryColumnName = primaryColumn.getTitle();
			primaryColumnId = primaryColumn.getId();
		} // end synchronized block

		ll.log(columnMap.toString());

		if ( null == primaryColumnName || primaryColumnName.isEmpty() || primaryColumnId < 0) {
			ll.log("Could not identify the primary column");
			throw new IllegalArgumentException("Could not identify the primary column. Maybe wrong sheet");
		} else {
			ll.log("Primary column name is " + primaryColumnName);
		}
		
		// if we made it until here that's good. We should return this object to allow for some chaining of methods.
		this.initSuccess = true;
		return this;
	}


	
	public void reloadSheet() {
		ll.log("Reloading smartsheet. Usually done after changes to it");
		Optional<Sheet> s = this.openOrReloadSheet();
		
		if ( s.isPresent() ) {
			smartsheetNative = s.get();
		} else {
			ll.log("Problems when reloading smartsheet. Now a null object");
		}
	}


	private Optional<Sheet> openOrReloadSheet() {
		checkIfSmartSheetInitialized() ;
		
		// load sheet
		Sheet s = null;
		try {
			s = this.client.sheetResources().getSheet(this.sheetId, null, null, null, null, null, null, null);
		}
		catch ( SmartsheetException e) {
			ll.log("Could not open sheet or reload the sheet");
			return Optional.empty();
		}

		ll.log("Reloaded " + s.getName()  + " with " + s.getRows().size() + " rows from sheet: " + s.getName());
		// this will throw an exception when s==null
		return Optional.of(s);
	}


	// Helper function to find cell in a row
	private Optional<Cell> getCellByColumnName(Row row, String columnName) throws IllegalArgumentException {
		checkIfSmartSheetInitialized();

		// receive a row and a columnName and return the right cell
		if ( ! passPrecondition(columnName) ) { 
			ll.log("No column name supplied in lookup");
			return Optional.empty();
		}

		if ( ! passPrecondition(row) ) { 
			ll.log("No column name supplied in lookup");
			throw new IllegalArgumentException("row supplied was null");
		}

		Optional<Long> colId = doesColumnExist(columnName);

		if ( !colId.isPresent() || colId.get() < 0 ) {
			ll.log("No column with name " + columnName + " found in map." );
			throw new IllegalArgumentException("No column with " + columnName + " could be found");
		}

		final Long searchColumn = colId.get();
		Cell found = row.getCells().stream().
				filter(cell -> (searchColumn.longValue()==cell.getColumnId().longValue()) ).
				findFirst().orElse(null);
		
		return Optional.ofNullable(found);
	}
	
	
	
	private Optional<String> getCellValueByColumnName(Row r, String columnName)  {
			
			Optional<Cell> c = getCellByColumnName(r, columnName);
			
			if ( c.isPresent() ) {
					String s = ( c.get().getDisplayValue() != null)?c.get().getDisplayValue():(String)c.get().getValue();
					return Optional.of(s);
			}
			
			return Optional.empty();
	}


// I tried with Optional<List<Cell>> first, but it gets too unwieldy and there is little  purpose anyways
// since this is a private method
	private List<Cell> buildListOfCellsFromDataMap( Map<String, String> data ) {

		final int maxCellEntrySize = 2400;

		if ( ! passPrecondition(data) ) {
			ll.log("source data has problems. Shamelessly avoiding to build List of cells");
			return null;
		}

		// we get data in the form of a map with <columnHeader, value>
		List<Cell> list = new ArrayList<Cell>();

		// iterate over the data
		for ( String key : data.keySet() ) {
			Cell cell = new Cell();

			 
			if ( ! doesColumnExist(key).isPresent() ) {
				ll.log("Could not find the columnID for key " + key);
				ll.log("Will skip over it");
				cell = null;
				continue;
			}

			cell.setColumnId(doesColumnExist(key).get());
			cell.setStrict(false);
			String s = data.get(key);
			if ( null !=s && s.length() > maxCellEntrySize) {
				ll.log( "Size of data input " + s + " exceeds allowed max value of " + maxCellEntrySize);
				ll.log("Will truncate entry");
				// we could return an error
				// it's unlikely that form alone will lead to a decision so better to not make to much trouble for the user
				s=s.substring(0, maxCellEntrySize);
			}
			
			if ( s != null ) {
			cell.setValue(s);
			list.add( cell);
			}
			cell = null;
		}

		if  (! passPrecondition(list) ) {
			ll.log("Could not build list of cells from data");
			ll.log("Data is " + data.toString());
			return null;
		}
		ll.log("Successfully built a list of cells from the data map");
		return list;
	}


	public int getNofRowsReadyToInsert() {
		
		Integer i = rowContainer.size();
		ll.log("Number of rows to insert is " + i);
		return i;
	}


	public Optional<Row> readyRowForInsertion(Map<String, String> data) {
		// this access the smartsheet to check the primary key column
		// so we should ensure the object was initialized
		// this should also ensure that smartSheetNative is not null

		checkIfSmartSheetInitialized();

		if ( enforceUniquePrimaryKey ) {
			if ( primaryKeyConflict(data)) return Optional.empty();
		}
		
		if (  null != buildListOfCellsFromDataMap(data) ) {
			Row r = new Row();
			r.setToTop(true); // we insert rows at the top
			r.setCells(buildListOfCellsFromDataMap(data));
			this.rowContainer.add(r);
			ll.log("Added a new row to the insertion object");
			ll.log("New rows will be inserted at the top of the sheet");
			return Optional.of(r);
		}
		
		return Optional.empty();
	}
	
	
	public int clearRowCacheContainer() {
		 int i = getNofRowsReadyToInsert();
		 rowContainer.clear();
		 return i;
	}


	public int insertRowOrRows() {
		checkIfSmartSheetInitialized();

		try {
			ll.log("About to add the rows from the row object to the sheet");
			ll.log("There are " + this.rowContainer.size() + " rows to insert");
			
			// we are using this addRows which is an all or nothing operation
			lastInserted = client.sheetResources().rowResources().addRows(sheetId.longValue() , this.rowContainer);	
		}
		catch ( SmartsheetException e ) {
			ll.log("Could not insert the rows from the rows object");
			ll.log(e.getMessage());
			return -1;
		}
		
		
		ll.log("Added the rows to the sheet succesfully");
		
		// since we used addRows not addRowsAllowPartialSuccess we can clear the whole container if we made it that far
		rowContainer.clear();
	

		return (  (null!=lastInserted)?lastInserted.size():-1 );
	}


	// we want to use the deletion mostly for the unit tests so as not to leave anything behind in the sheet
	public Integer deleteLastInserted() {

		checkIfSmartSheetInitialized() ;
		int rowsDeleted = 0;
		Set<Long> rowIds = new HashSet<Long>(lastInserted.stream().map((e) -> e.getId()).collect(Collectors.toList()));
		ll.log("About to delete " + rowIds.size() + " rows");

		try {
			rowsDeleted = client.sheetResources().rowResources().deleteRows(sheetId, rowIds, true).size();
			this.reloadSheet();
			return rowsDeleted;
		}  catch ( SmartsheetException e) {
			ll.log("Could not delete any or all rows from the sheet");
			ll.log(e.getMessage());
			this.reloadSheet();
			return rowsDeleted;
		}
	}



	private boolean primaryKeyValueExistsInList(String value,  List<Row> input ) {
		// the primary key name is set during the init
		checkIfSmartSheetInitialized();

		// some basic checks upfront
		if ( ! passPrecondition(value)) {
			throw new IllegalArgumentException("Key to search for was null or empty");
		}

		if ( null == input) {  // the precondition checks for empty as well, but empty is perfectly valid
			throw new IllegalArgumentException("List to search for was null");
		}

		// there may very well be a case where we get passed an empty row list 
		// the right thing to do is to say, no! your element is not here
		if ( 0 == input.size()) {
			return false;
		}

		ll.log("Checking for the existence of the primary key " + value);

		if ( !passPrecondition(primaryColumnName) ) {
			ll.log("Could not find the name of the primary column. Init may not have been called or wrong sheet");
			throw new IllegalStateException("Could not find the name of the primary column. Init may not have been called or wrong sheet");
		}


		for (Row row : input) {
			Cell c = row.getCells().stream()
					.filter(cell -> (primaryColumnId.equals((Long) cell.getColumnId()) &&   cell.getDisplayValue().equalsIgnoreCase(value)))
					.findFirst()
					.orElse(null);

			if ( null !=c  ) {
				ll.log("The Primary Key " + value + " already exists");
				ll.log("We want to avoid duplicate primary keys");
				return true;
			}
		}

		ll.log("Haven't found the primary key value" + value + " in the sheet");
		ll.log("Should be able to add the row");
		return false;
	}




	private boolean primaryKeyExistsInSheet(String value) {
		// Load the entire sheet
		checkIfSmartSheetInitialized();

		if ( null == smartsheetNative ) {
			ll.log("The native smartsheet object was null.");
			ll.log("We will try to initialize it again");
			this.reloadSheet();
		}

		if ( smartsheetNative.getTotalRowCount() > 0 ) {
			return primaryKeyValueExistsInList(value, smartsheetNative.getRows());
		} else {
			// empty smartsheet
			return false;
		}

	}


	private boolean primaryKeyExistsInContainer(String value) {

		ll.log("Checking for the existence of the primary key " + value);

		return primaryKeyValueExistsInList(value, rowContainer);
	}



	public boolean primaryKeyUsed(String dataPrimaryKey) {

		if ( !passPrecondition(dataPrimaryKey) ) {
			ll.log("Someone supplied an empty or null primary key to check for in the sheet");
			return false;
		}

		// our primary key is an email address - so we can make it lowercase and delete spaces
		return (primaryKeyExistsInSheet(dataPrimaryKey.trim()) || primaryKeyExistsInContainer(dataPrimaryKey.trim()) );
	}



	public boolean primaryKeyConflict( Map<String, String> sourceData) {

		// find the primary keys value first from the data set
		Map.Entry<String, String> entry = sourceData.entrySet().stream().filter( (e) -> e.getKey().equals(primaryColumnName)).findFirst().orElse(null);

		// we need to check if this key was already used in the smartsheet
		return (primaryKeyUsed(entry.getValue()));
	}
	
}


