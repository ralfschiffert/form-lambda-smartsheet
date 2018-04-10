package com.tropo.portal.lambda.smartsheet;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.models.Column;
import com.smartsheet.api.models.Sheet;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;


public class SingleSmartSheetTestWithMockito {

    private static LambdaLogger ll;
    private static Context ctx;
    private static List<Column> l = new ArrayList<>();

    @Mock ( answer = Answers.RETURNS_DEEP_STUBS )
    private Smartsheet smartsheet;

    @Mock
    private Sheet sheet;


    @Mock ( answer = Answers.RETURNS_DEEP_STUBS )
    private SmartsheetBuilder builder;


    @InjectMocks
    private SingleSmartSheet sss = new SingleSmartSheet(ctx);


    @BeforeClass
    public static void createContext() {
        TestContext context = new TestContext();
        context.setFunctionName("SmartSheetFunctionJunitMockTest");
        ll = context.getLogger();
        ctx = context;
    }


    @BeforeClass
    public static void createListOfColumns() {
        for ( int i = 0; i < 3; i++ ) {
            Column c = Mockito.mock(Column.class);
            given(c.getTitle()).willReturn(String.valueOf( (char) (i + 'A') ));
            given(c.getId()).willReturn((long)i);
            l.add(c);
        }
    }


    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void anNullObjectShouldReturnFalse() {
        Object o1 = null;
        Object o2 = "this is a test";

        assertThat(SingleSmartSheet.passPrecondition( o1 ), is(equalTo(false)));
        assertThat(SingleSmartSheet.passPrecondition( o2 ), is(equalTo(true)));
    }


    @Test
    public void anEmptyOrNullStringShouldReturnFalse() {
        String s1 = null;
        String s2 = "";
        String s3 = "not empty";

        assertThat(SingleSmartSheet.passPrecondition( s1), is(equalTo(false)));
        assertThat(SingleSmartSheet.passPrecondition( s2 ), is(equalTo(false)));
        assertThat(SingleSmartSheet.passPrecondition( s3 ), is(equalTo(true)));
    }


    @Test
    public void anEmptyOrNullCollectionShouldReturnFalse() {
        Collections c1 = null;
        java.util.List<String> c2 = new ArrayList<>();
        int[] a = { 1,2};
        java.util.List c3= java.util.Arrays.asList( a );


        assertThat(SingleSmartSheet.passPrecondition( c1), is(equalTo(false)));
        assertThat(SingleSmartSheet.passPrecondition( c2 ), is(equalTo(false)));
        assertThat(SingleSmartSheet.passPrecondition( c3 ), is(equalTo(true)));
    }


    @Test
    public void givenTheCorrectMockObjectsTheInitShouldWork() throws Exception {
        // setup a mock builder
        given(builder.setAccessToken( "1234" ).build()).willReturn( smartsheet );
        // setup a mock smartsheet-client
        given(smartsheet.sheetResources().getSheet(4321L, null, null, null, null, null, null, null)).willReturn(sheet);
        // setup a mock sheet
        given(sheet.getColumns()).willReturn(l);


        // act
        SingleSmartSheet s = sss.init("1234", "4321");
        assertThat(sss.isConnected(), is(true));
        assertThat(s, instanceOf(SingleSmartSheet.class));
    }



    @Test
    public void givenAnInitialInitFailureTheSmartSheetShouldTryAgain() throws Exception {
        // setup a mock builder
        given(builder.setAccessToken("1235").build()).willReturn(smartsheet);
        // setup a mock smartsheet-client
        given(smartsheet.sheetResources().getSheet(5321L, null, null, null, null, null, null, null)).willReturn(null).willReturn(sheet);
        // setup a mock sheet
        given(sheet.getColumns()).willReturn(l);


        // act
        try {
            SingleSmartSheet s=sss.init("1235", "5321");
            assertThat(sss.isConnected(), is(false));
            assertThat(s, isNull());
        } catch (Exception e) {
            System.out.println("got an exception during the first time around");
        }
        // this will initialize it under the hood
        assertThat(sss.getSheetRepresentation().isPresent(), is(true));
        verify(builder.setAccessToken(anyString()), times(2)).build();
        verify(smartsheet.sheetResources(), times(2)).getSheet(anyLong(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
    }








    @Test
    public void getSheetRepresentation() {
    }

    @Test
    public void doesColumnExist() {
    }

    @Test
    public void doesPrimaryColumnExist() {
    }

    @Test
    public void getNofRowsInSmartSheet() {
    }

    @Test
    public void init() {
    }

    @Test
    public void reloadSheet() {
    }

    @Test
    public void getCellValueByColumnName() {
    }

    @Test
    public void getNofRowsReadyToInsert() {
    }

    @Test
    public void readyRowForInsertion() {
    }

    @Test
    public void clearRowCacheContainer() {
    }

    @Test
    public void insertRowOrRows() {
    }

    @Test
    public void deleteLastInserted() {
    }

    @Test
    public void primaryKeyUsed() {
    }

    @Test
    public void primaryKeyConflict() {
    }
}