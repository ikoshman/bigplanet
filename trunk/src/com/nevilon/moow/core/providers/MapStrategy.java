package com.nevilon.moow.core.providers;


public abstract class MapStrategy {

	public abstract int getId();
	
	public abstract String getURL(int x, int y, int z);
	
	public abstract String getServer();
}
