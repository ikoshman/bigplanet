package com.nevilon.moow;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

import com.nevilon.moow.core.ui.MapControl;

public class MoowMap extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MapControl c = new MapControl(this);
		setContentView(c, new ViewGroup.LayoutParams(320, 480));
	}

}