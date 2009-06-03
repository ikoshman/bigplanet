package com.nevilon.moow.core;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;

/**
 * Предназначен для хранения настроек
 * @author hudvin
 *
 */
public class Preferences {

	private static SharedPreferences prefs;

	public static final String MAP_SOURCE = "MAP_SOURCE";
	
	public static final String NETWORK_MODE = "NETWORK_MODE";

	public static void init(Application app) {
		prefs = app.getSharedPreferences("global", Context.MODE_PRIVATE);
	}

	
	public static void putTile(RawTile tile){
		put("tilex", String.valueOf(tile.x));
		put("tiley", String.valueOf(tile.y));
		put("tilez", String.valueOf(tile.z));
	}
	
	
	public static RawTile getTile(){
		int x, y, z;
		x = Integer.parseInt(get("tilex"));
		y = Integer.parseInt(get("tiley"));
        z = Integer.parseInt(get("tilez"));
        if(z == 0){
        	z = 16;
        }
		return new RawTile(x,y,z);
	}
	
	public static void putOffset(Point offset){
		put("offsetX", String.valueOf(offset.x));
		put("offsetY",String.valueOf(offset.y));
	}
	
	public static Point getOffset(){
		int x = Integer.parseInt(get("offsetX"));
		int y = Integer.parseInt(get("offsetY"));
		return new Point(x,y);
	}
	
	public static String get(String name) {
		return prefs.getString(name, "0");
	}

	public static void put(String name, String value) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(name, value);
		editor.commit();
	}
}
