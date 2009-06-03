package com.nevilon.bigplanet;

import com.nevilon.bigplanet.core.db.DAO;
import com.nevilon.bigplanet.core.db.GeoBookmark;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddGeoBookmark extends Activity {

	private GeoBookmark geoBookmark;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addgeobookmark);
		
		Bundle extras = getIntent().getExtras();
	     geoBookmark = (GeoBookmark)extras.getSerializable("bookmark");

	     
	     final EditText nameValue = (EditText)findViewById(R.id.nameValue);
	     final EditText descriptionValue = (EditText)findViewById(R.id.descriptionValue);
		
		Button addGeoBookmarkBtn = (Button)findViewById(R.id.addGeoBookmarkBtn);
		addGeoBookmarkBtn.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				
				geoBookmark.setName(nameValue.getText().toString());
				geoBookmark.setDescription(descriptionValue.getText().toString());
				DAO d = new DAO(AddGeoBookmark.this);
				d.saveGeoBookmark(geoBookmark);
				setResult(RESULT_OK);
				finish();
			}
			
		});
	}
}
