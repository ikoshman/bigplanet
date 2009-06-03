package com.nevilon.bigplanet.core;

import android.app.Application;

public class BigPlanetApp extends Application {

	public BigPlanetApp() {
		super();
	}

	@Override
	public void onCreate() {
		Preferences.init(this);
	}

}
