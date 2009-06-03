package com.nevilon.moow;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.nevilon.moow.core.Preferences;
import com.nevilon.moow.core.providers.MapStrategyFactory;
import com.nevilon.moow.core.tools.savemap.MapSaverUI;
import com.nevilon.moow.core.ui.MapControl;

public class MoowMap extends Activity {

	private MapControl mapControl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		WindowManager wm = this.getWindowManager();
		Display display = wm.getDefaultDisplay();
		int height = display.getHeight();
		int width = display.getWidth();

		mapControl = new MapControl(this, width, height);

		setContentView(mapControl);
		setContentView(mapControl, new ViewGroup.LayoutParams(width,height));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, 0, 0, "Map source");

		SubMenu sub = menu.addSubMenu(0, 1, 0, "Tools");
		sub.add(2, 11, 1, "Cache map");

		
		menu.add(0, 3, 0, "Network mode");

		
		return true;
	}

	
	private RadioButton buildRadioButton(String label, int id) {
		RadioButton btn = new RadioButton(this);
		btn.setText(label);
		btn.setId(id);
		return btn;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 11:
			MapSaverUI mapSaverUI = new MapSaverUI(this, mapControl.getPhysicalMap()
					.getZoomLevel() , mapControl.getPhysicalMap()
					.getAbsoluteCenter());
			mapSaverUI.show();
			break;
		case 0:
			selectMapSource();
			break;
		case 3:
			selectNetworkMode();
			break;
		}
		return false;

	}

	private void selectNetworkMode(){
		final Dialog networkModeDialog;
		networkModeDialog = new Dialog(this);
		networkModeDialog.setCanceledOnTouchOutside(true);
		networkModeDialog.setCancelable(true);
		networkModeDialog.setTitle("Select network mode");

		final LinearLayout mainPanel = new LinearLayout(this);
		mainPanel.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		mainPanel.setOrientation(LinearLayout.VERTICAL);

		RadioGroup modesRadioGroup = new RadioGroup(this);

		LinearLayout.LayoutParams layoutParams = new RadioGroup.LayoutParams(
				RadioGroup.LayoutParams.WRAP_CONTENT,
				RadioGroup.LayoutParams.WRAP_CONTENT);

			modesRadioGroup.addView(buildRadioButton(
					"Offline",0), 0, layoutParams);

			modesRadioGroup.addView(buildRadioButton(
					"Online ",1), 0, layoutParams);

			
		modesRadioGroup.check(Integer.parseInt(Preferences.get(Preferences.NETWORK_MODE)));

		modesRadioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(RadioGroup group,
							int checkedId) {
						mapControl.getPhysicalMap().getTileResolver().setUseNet(checkedId == 1);
						Preferences.put(Preferences.NETWORK_MODE, String.valueOf(checkedId));
						networkModeDialog.hide();
					}

				});

		mainPanel.addView(modesRadioGroup);
		networkModeDialog.setContentView(mainPanel);
		networkModeDialog.show();	
		
		
	}
	
	private void selectMapSource() {
		final Dialog mapSourceDialog;
		mapSourceDialog = new Dialog(this);
		mapSourceDialog.setCanceledOnTouchOutside(true);
		mapSourceDialog.setCancelable(true);
		mapSourceDialog.setTitle("Select map source");

		final LinearLayout mainPanel = new LinearLayout(this);
		mainPanel.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		mainPanel.setOrientation(LinearLayout.VERTICAL);

		RadioGroup sourcesRadioGroup = new RadioGroup(this);

		LinearLayout.LayoutParams layoutParams = new RadioGroup.LayoutParams(
				RadioGroup.LayoutParams.WRAP_CONTENT,
				RadioGroup.LayoutParams.WRAP_CONTENT);

		for (Integer id : MapStrategyFactory.strategies.keySet()) {
			sourcesRadioGroup.addView(buildRadioButton(
					MapStrategyFactory.strategies.get(id).getDescription(),
					id), 0, layoutParams);
		}

		sourcesRadioGroup.check(mapControl.getMapSourceId());

		sourcesRadioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(RadioGroup group,
							int checkedId) {
						mapControl.changeMapSource(checkedId);
						mapSourceDialog.hide();
					}

				});

		mainPanel.addView(sourcesRadioGroup);
		mapSourceDialog.setContentView(mainPanel);
		mapSourceDialog.show();
	}

	
}