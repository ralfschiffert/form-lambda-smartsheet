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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.InjectMocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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


public class LambdaFunctionHandlerTestTestWithMockito {

    private static Context ctx;
    private static List<Column> l = new ArrayList<>();
    
    
    @Rule
    public ExpectedException grabber = ExpectedException.none();

    @Mock
    private SingleSmartSheet sss = new SingleSmartSheet(ctx);

    @InjectMocks
    private LambdaFunctionHandler handler = new LambdaFunctionHandler(); 


    @BeforeClass
    public static void createContext() {
        TestContext context = new TestContext();
        context.setFunctionName("SmartSheetFunctionJunitMockTest");
        ctx = context;
    }

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
    }
    

    @Test
    public void returnErrorWhenSmartsheetCouldNotBeInitialized() {
	grabber.expect(IllegalArgumentException.class);
	grabber.expectMessage("empty request received");
	System.out.println(handler.handleRequest(new HashMap<String, String>(), ctx));
    }

}
