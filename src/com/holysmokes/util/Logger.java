package com.holysmokes.util;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.util.Log;

public class Logger {
	private static List<String> logList = new ArrayList<String>();
	private static StringWriter writer = new StringWriter();
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	public static void log(Object obj, String message) {
		log(obj.getClass(), message);
	}
	public static void log(Class<?> className, String message) {
		String log = dateFormat.format(Calendar.getInstance().getTime()) + " " + className.getSimpleName() + "\n" + message + "\n";
		logList.add(log);
		writer.append(log);
		
		Log.e(className.toString(), message);
	}
	
	public static String getLog() {
		return writer.toString();
	}
	
	public static List<String> getLogList() {
		return logList;
	}
	
	public static String[] getLogListAsArray() {
		return logList.toArray(new String[logList.size()]);
	}
	
}
