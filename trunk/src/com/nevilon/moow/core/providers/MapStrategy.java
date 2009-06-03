package com.nevilon.moow.core.providers;

public abstract class MapStrategy {

	public abstract String getURL(int x, int y, int z);

	public abstract String getServer();
	
	public abstract String getDescription();
}
