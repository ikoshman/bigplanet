package com.nevilon.bigplanet;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpStatus;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.nevilon.bigplanet.core.Place;
import com.nevilon.bigplanet.core.loader.BaseLoader;
import com.nevilon.bigplanet.core.ui.MapControl;
import com.nevilon.bigplanet.core.xml.GeoLocationHandler;

public class FindLocation extends ListActivity implements Runnable {

	private ProgressDialog waitDialog = null;

	private EditText searhText;

	private Handler handler;
	
	private List<Place> places = new ArrayList<Place>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(new SpeechListAdapter(FindLocation.this));
		getListView().setPadding(2, 52, 0, 0);
		
		LayoutParams p = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		View v = View.inflate(this, R.layout.searchpanel, null);
		addContentView(v, p);

		// text to search
		searhText = (EditText) findViewById(R.id.searcText);
		
		// search button
		ImageButton btn = (ImageButton) v.findViewById(R.id.searchBtn);
		btn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				startSearch();
			}

		});

		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				waitDialog.dismiss();
				waitDialog = null;
				setListAdapter(new SpeechListAdapter(FindLocation.this));
			}

		};
		
		getListView().setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent();
				intent.putExtra("place", places.get(position));
				setResult(BigPlanet.GO_TO_LOCATION,intent);
				finish();		
			}
		});

	}

	private void startSearch(){
		Thread t = new Thread(FindLocation.this);
		t.start();
		setListAdapter(null);
		waitDialog = ProgressDialog.show(FindLocation.this,
				"Please wait...", "Connecting to server", true);

	}
	
	/*
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent ev) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_ENTER:
			startSearch();	
			return false;
		default:
			return super.onKeyDown(keyCode, ev);
		}
	}
	
	*/
	public void run() {
		HttpClient client = new HttpClient();

		client.getHttpConnectionManager().getParams().setSoTimeout(
				BaseLoader.CONNECTION_TIMEOUT);
		client.getHttpConnectionManager().getParams().setConnectionTimeout(
				BaseLoader.CONNECTION_TIMEOUT);

		try {
			GetMethod method = new GetMethod(
					"http://maps.google.com/maps/geo?q="
							+ searhText.getText().toString() + "&output=xml");
			int statusCode = client.executeMethod(method);
			if (statusCode != -1 && method.getStatusCode() == HttpStatus.SC_OK) {
				String response = method.getResponseBodyAsString();
				
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
				GeoLocationHandler h = new GeoLocationHandler(); 
				xr.setContentHandler(h);
				xr.parse(new InputSource(new StringReader(response)));
				places = h.getPlaces();
				method.releaseConnection();
			} else {

			}
		} catch (Exception e) {

		} finally{
			handler.sendEmptyMessage(0);
		}

	}
	
	private class SpeechListAdapter extends BaseAdapter {

		public SpeechListAdapter(Context context) {
			mContext = context;
		}

		public int getCount() {
			return places.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			SpeechView sv;
			Place place = places.get(position);
			if (convertView == null) {

				sv = new SpeechView(mContext, place.getAddress(), place.getName());
			} else {
				sv = (SpeechView) convertView;
				sv.setName(place.getAddress());
				sv.setDescription(place.getName());
				sv.id = 0;
			}

			return sv;
		}

		private Context mContext;

	}

	private class SpeechView extends LinearLayout {
		public SpeechView(Context context, String name, String description) {
			super(context);
			View v = View.inflate(FindLocation.this, R.layout.geobookmark,
					null);
			nameLabel = (TextView) v.findViewById(android.R.id.text1);
			nameLabel.setText(name);

			descriptionLabel = (TextView) v.findViewById(android.R.id.text2);
			descriptionLabel.setText(description);
			addView(v);
		}

		public void setName(String name) {
			descriptionLabel.setText(name);
		}

		public void setDescription(String description) {
			descriptionLabel.setText(description);
		}

		protected long id;

		private TextView nameLabel;
		private TextView descriptionLabel;
	}


}
