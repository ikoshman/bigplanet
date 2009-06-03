package com.nevilon.bigplanet.core;

import android.app.Application;

public class BigPlanetApp extends Application {

	public static final boolean isDemo = true;
	
	public BigPlanetApp() {
		super();
	}

	@Override
	public void onCreate() {
		Preferences.init(this);
	}

}
