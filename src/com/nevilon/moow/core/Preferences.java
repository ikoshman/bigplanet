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

	private static final String SOURCE_ID = "sourceId";

	private static SharedPreferences prefs;

	public static final String MAP_SOURCE = "MAP_SOURCE";
	
	public static final String NETWORK_MODE = "NETWORK_MODE";

	public static void init(Application app) {
		prefs = app.getSharedPreferences("global", Context.MODE_PRIVATE);
	}

	/**
	 * Сохраняет угловой тайл
	 * @param tile
	 */
	public static void putTile(RawTile tile){
		put("tilex", String.valueOf(tile.x));
		put("tiley", String.valueOf(tile.y));
		put("tilez", String.valueOf(tile.z));
	}
	
	/**
	 * Возвращает угловой тайл
	 * @return
	 */
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
	
	/**
	 * Сохраняет отступ
	 * @param offset
	 */
	public static void putOffset(Point offset){
		put("offsetX", String.valueOf(offset.x));
		put("offsetY",String.valueOf(offset.y));
	}
	
	/**
	 * Возвращает отступ
	 * @return
	 */
	public static Point getOffset(){
		int x = Integer.parseInt(get("offsetX"));
		int y = Integer.parseInt(get("offsetY"));
		return new Point(x,y);
	}
	
	public static void putSourceId(int sourceId){
		put(SOURCE_ID, String.valueOf(sourceId));
	}
	
	public static int getSourceId(){
		return Integer.parseInt(get(SOURCE_ID));
	}
	
	public static void putUseNet(boolean useNet){
		put("useNet", String.valueOf(useNet));
	}
	
	public static boolean getUseNet(){
		return Boolean.valueOf(get("useNet"));
	}
	
	private static String get(String name) {
		String data = prefs.getString(name, "0");
		System.out.println("get " +name + " : " + data);
		return  data;
	}
	
	

	private static void put(String name, String value) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(name, value);
		System.out.println("put "+name + " : " + value);
		editor.commit();
	}
}
