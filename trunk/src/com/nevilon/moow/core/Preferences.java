package com.nevilon.moow.core;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

	private static SharedPreferences prefs;

	public static final String MAP_SOURCE = "MAP_SOURCE";

	public static void init(Application app) {
		prefs = app.getSharedPreferences("global", Context.MODE_PRIVATE);
	}

	public static String get(String name) {
		return prefs.getString(name, "");
	}

	public static void put(String name, String value) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(name, value);
		editor.commit();
	}
}
