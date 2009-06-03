package com.nevilon.bigplanet;

import java.util.List;

import com.nevilon.bigplanet.core.db.DAO;
import com.nevilon.bigplanet.core.db.GeoBookmark;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AllGeoBookmarks extends ListActivity{

	private List<GeoBookmark> geoBookmarks; 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DAO dao = new DAO(this);
		geoBookmarks  =  dao.getBookmarks();
		setListAdapter(new SpeechListAdapter(this));
	}

	
	private class SpeechListAdapter extends BaseAdapter {

		public SpeechListAdapter(Context context) {
			mContext = context;
		}

		public int getCount() {
			return geoBookmarks.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			SpeechView sv;
			GeoBookmark bookmark = geoBookmarks.get(position);
			if (convertView == null) {

				sv = new SpeechView(mContext, bookmark.getName(), bookmark.getDescription());
			} else {
				sv = (SpeechView) convertView;
				sv.setName(bookmark.getName());
				sv.setDescription(bookmark.getDescription());
				sv.id = bookmark.getId();
			}

			return sv;
		}

		private Context mContext;

	}

	private class SpeechView extends LinearLayout {
		public SpeechView(Context context, String name, String description) {
			super(context);
			View v = View.inflate(AllGeoBookmarks.this, R.layout.geobookmark, null);
			TextView txt1 = (TextView) v.findViewById(android.R.id.text1);
			txt1.setText(name);

			TextView txt2 = (TextView) v.findViewById(android.R.id.text2);
			txt2.setText(description);
			addView(v);
		}

		public void setName(String name) {
			mTitle.setText(name);
		}

		public void setDescription(String description) {
			mDialogue.setText(description);
		}

		protected long id;

		private TextView mTitle;
		private TextView mDialogue;
	}
	
}
