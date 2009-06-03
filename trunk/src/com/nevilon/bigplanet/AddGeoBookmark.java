package com.nevilon.bigplanet;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AddGeoBookmark extends Activity {

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addgeobookmark);
		Button addGeoBookmarkBtn = (Button)findViewById(R.id.addGeoBookmarkBtn);
		addGeoBookmarkBtn.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
			
		});
	}
}
