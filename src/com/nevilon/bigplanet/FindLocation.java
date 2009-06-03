package com.nevilon.bigplanet;

import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpStatus;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.nevilon.bigplanet.core.Place;
import com.nevilon.bigplanet.core.loader.BaseLoader;
import com.nevilon.bigplanet.core.xml.GeoLocationHandler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class FindLocation extends Activity implements Runnable {

	private ProgressDialog waitDialog = null;

	private EditText searhText;

	private Handler handler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
				Thread t = new Thread(FindLocation.this);
				t.start();
				waitDialog = ProgressDialog.show(FindLocation.this,
						"Please wait...", "Connecting to server", true);
			}

		});

		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				waitDialog.dismiss();
			}

		};

	}

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
				//System.out.println(response);
				
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
				GeoLocationHandler h = new GeoLocationHandler(); 
				xr.setContentHandler(h);
				xr.parse(new InputSource(new StringReader(response)));
				
				List<Place> p = h.places;
				System.out.println(p);
				method.releaseConnection();
			} else {

			}
		} catch (Exception e) {

		} finally{
			handler.sendEmptyMessage(0);
		}

	}

}
