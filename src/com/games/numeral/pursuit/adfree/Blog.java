package com.games.numeral.pursuit.adfree;

import android.util.Log;

public class Blog{
	public static void i(Object... objects){
		String tag=getTag();
		if(objects==null) Log.i(tag,"null");
		StringBuilder builder=new StringBuilder();
		for(Object o:objects){
			String add=(o==null?"null":o.toString());
			builder.append(add);
		}
		Log.i(tag,builder.toString());
	}
	public static void v(Object... objects){
		String tag=getTag();
		if(objects==null) Log.v(tag,"null");
		StringBuilder builder=new StringBuilder();
		for(Object o:objects){
			String add=(o==null?"null":o.toString());
			builder.append(add);
		}
		Log.v(tag,builder.toString());
	}
	public static void e(Object... objects){
		String tag=getTag();
		if(objects==null) Log.e(tag,"null");
		StringBuilder builder=new StringBuilder();
		for(Object o:objects){
			String add=(o==null?"null":o.toString());
			builder.append(add);
		}
		Log.e(tag,builder.toString());
	}
	public static void w(Object... objects){
		String tag=getTag();
		if(objects==null) Log.w(tag,"null");
		StringBuilder builder=new StringBuilder();
		for(Object o:objects){
			String add=(o==null?"null":o.toString());
			builder.append(add);
		}
		Log.w(tag,builder.toString());
	}
	public static void d(Object... objects){
		String tag=getTag();
		if(objects==null) Log.d(tag,"null");
		StringBuilder builder=new StringBuilder();
		for(Object o:objects){
			String add=(o==null?"null":o.toString());
			builder.append(add);
		}
		Log.d(tag,builder.toString());
	}
	
	private static String getTag() {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		return stackTraceElements[4].getClassName()+"."+stackTraceElements[4].getMethodName();
	}
	
}
