package com.nevilon.moow;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.nevilon.moow.core.providers.MapStrategyFactory;
import com.nevilon.moow.core.ui.MapControl;

public class MoowMap extends Activity {

	private MapControl mapControl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mapControl = new MapControl(this);
		setContentView(mapControl, new ViewGroup.LayoutParams(320, 480));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, 0, 0, "Map source");
		menu.add(0, 1, 0, "Edit");
		menu.add(0, 2, 0, "Delete");
		return true;
	}

	/*
	 * @Override public boolean onPrepareOptionsMenu(Menu menu) { boolean
	 * isSelected = this.getSelectedItemId() >= 0;
	 * menu.findItem(1).setEnabled(isSelected);
	 * menu.findItem(2).setEnabled(isSelected); return true; }
	 */

	private RadioButton buildRadioButton(String label, int id) {
		RadioButton mapSource = new RadioButton(this);
		mapSource.setText(label);
		mapSource.setId(id);
		return mapSource;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:

			final Dialog dl = new Dialog(this);
			dl.setCanceledOnTouchOutside(true);
			dl.setCancelable(true);
			dl.setTitle("Select map source");
			
			final LinearLayout mainPanel = new LinearLayout(this);
			mainPanel.setLayoutParams(new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			mainPanel.setOrientation(LinearLayout.VERTICAL);

			RadioGroup sourcesRadioGroup = new RadioGroup(this);
						
			LinearLayout.LayoutParams layoutParams = new RadioGroup.LayoutParams(
					RadioGroup.LayoutParams.WRAP_CONTENT,
					RadioGroup.LayoutParams.WRAP_CONTENT);

			
			
			for(Integer id : MapStrategyFactory.strategies.keySet()) {
				sourcesRadioGroup.addView(
						buildRadioButton(
								MapStrategyFactory.strategies.get(id).getDescription(), id)
						, 0,
						layoutParams);
			}
			
			sourcesRadioGroup.check(mapControl.getMapSourceId());
			
			
			sourcesRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					mapControl.changeMapSource(checkedId);
					dl.hide();
				}
				
			});
			
			mainPanel.addView(sourcesRadioGroup);
			dl.setContentView(mainPanel);
			dl.show();

			return true;
		case 1:
			return true;
		}
		return false;

	}
}