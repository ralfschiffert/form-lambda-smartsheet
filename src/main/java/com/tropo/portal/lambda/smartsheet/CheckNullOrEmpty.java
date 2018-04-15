package com.tropo.portal.lambda.smartsheet;

import java.util.Collection;
import java.util.Map;

public class CheckNullOrEmpty {

    private CheckNullOrEmpty() {
    }

    public static boolean isEmpty( Collection<?> collection ){
	boolean isEmpty = false;
	if( null == collection || collection.isEmpty() ) {
	    isEmpty = true;
	}
	return isEmpty;
    }


    public static boolean isEmpty( Map<?, ?> map ){
	boolean isEmpty = false;
	if( null == map  || map.isEmpty() ) {
	    isEmpty = true;
	}
	return isEmpty;
    }


    public static boolean isEmpty( Object object ){
	boolean isEmpty = false;
	if( null == object ) {
	    isEmpty = true;
	}
	return isEmpty;
    }


    public static boolean isEmpty( Object[] array ){
	boolean isEmpty = false;
	if(  null == array || array.length == 0 ) {
	    isEmpty = true;
	}
	return isEmpty;
    }


    public static boolean isEmpty( String string ){
	boolean isEmpty = false;
	if( null == string || string.trim().length() == 0 ){
	    isEmpty = true;
	}
	return isEmpty;
    }

}
