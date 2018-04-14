package com.tropo.portal.lambda.smartsheet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import com.amazonaws.services.lambda.runtime.Context;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class LambdaFunctionHandlerTest {

    private static Object input;
    
    
    @Rule
    public ExpectedException grabber = ExpectedException.none();

    @Before
    public void createInput() throws IOException {
        // TODO: set up your sample input object here.
        input = new String("This is a dummy string");
    }

    private Context createContext() {
        TestContext ctx = new TestContext();
        // TODO: customize your context here if needed.
        ctx.setFunctionName("Your Function Name");
        return ctx;
    }
    
    @Test
    public void returnErrorWhenSmartsheetCouldNotBeInitialized() {
	grabber.expect(IllegalArgumentException.class);
	LambdaFunctionHandler lfh = new LambdaFunctionHandler();
	assertThat(lfh.handleRequest(new HashMap<String, String>(), createContext()), equalTo("<!DOCTYPE html><html><head><title>Something went wrong</title></head><body><h1>Form Received with Errors</h1>There were some issues</br />We apologize for this, but experienced some server side error. Please try again later <br />If your browser does not support redirects, please click <a href='https://www.tropo.com'>here</a><script type='text/javascript'> var website = 'https://www.tropo.com/thankyou', timer = '5';function delayer() {window.location=website}; setTimeout('delayer()', 1000 * timer);</script></body></html>"));
    }

    @Test
    public void testLambdaFunctionHandler() {
        LambdaFunctionHandler handler = new LambdaFunctionHandler();
        Context ctx = createContext();
        
        String uniqueId = UUID.randomUUID().toString();
        
        Map<String, String> m = new HashMap<String, String>();
        m.put("oid","00D61000000dO6M");
        m.put("retURL","https%3A%2F%2Fwww.tropo.com%2Fthankyou");
        m.put("lead_source","Tropo Webform");
        m.put("first_name","Agent");
        m.put("last_name","K");
        m.put("email", uniqueId);
        m.put("phone","6502459999");
        m.put("00N6100000HSuwR","sanparik");
        m.put("title","PM");
        m.put("company","www.awesomecompany.com");
        m.put("URL","http%3A%2F%2Fwww.awesomecompany.com");
        m.put("employees","%3E+1000");
        m.put("industry","Communications");
        m.put("00N6100000HSuwP","http%3A%2F%2Fwww.myscreencast.com");
        m.put("00N6100000HSuwW","1");
        m.put("00N6100000HSuwN","1");
        m.put("00N6100000HSuwO","bot@bot.io");
        m.put("00N6100000HSuwM","1");
        m.put("00N6100000HSuwQ","KKKK");
        m.put("00N6100000BT0HY","This+is+a+description+of+my+project");
        m.put("salutation", "Mr.");
        m.put("country","Germany");
        
       String  output = handler.handleRequest(m, ctx);
        System.out.println(output);
        assertEquals("<!DOCTYPE html><html><head><title>Something went wrong</title></head><body><h1>Form Received with Errors</h1>There were some issues</br />The username sanparik is already in use. <br />If your browser does not support redirects, please click <a href='https://www.tropo.com/portal/register'>here</a><script type='text/javascript'> var website = 'https://www.tropo.com/thankyou', timer = '5';function delayer() {window.location=website}; setTimeout('delayer()', 1000 * timer);</script></body></html>", output);
        
        m.put("00N6100000HSuwR", uniqueId);
        output = handler.handleRequest(m, ctx);
        System.out.println(output);
        assertEquals("<!DOCTYPE html><html><head><script>var mylink='https://www.tropo.com/thankyou'; if (window.location.replace){ window.location.replace(mylink);} else {window.location.href ='mylink';} </script></head></html>", output);
        
        
        m.remove("retURL");
        m.remove("00N6100000HSuwR");
        m.put("email", UUID.randomUUID().toString());
        output = handler.handleRequest(m, ctx);
        System.out.println(output);
        assertEquals("<!DOCTYPE html><html><head><script>var mylink='https://www.tropo.com/thankyou'; if (window.location.replace){ window.location.replace(mylink);} else {window.location.href ='mylink';} </script></head></html>", output);
        
       
        
    }
}
