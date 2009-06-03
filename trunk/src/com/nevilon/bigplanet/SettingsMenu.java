package com.nevilon.bigplanet;

import java.util.HashMap;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SettingsMenu extends ListActivity {

	private static String[] menuLabels = { "Double touch" };

	private static HashMap<String, Class<? extends Activity>> menuItems;

	static {
		menuItems = new HashMap<String, Class<? extends Activity>>();
		menuItems.put("Double touch", DoubleTouchSettings.class);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, menuLabels));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Object o = this.getListAdapter().getItem(position);
		String keyword = o.toString();
		Class<? extends Activity> activityToRun = menuItems.get(keyword);
		if (null != activityToRun) {
			Intent intent = new Intent();
			intent.setClass(this, activityToRun);
			startActivity(intent);
		}

	}
}
