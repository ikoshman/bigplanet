package com.nevilon.bigplanet.core.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;

import com.nevilon.bigplanet.BigPlanet;
import com.nevilon.bigplanet.R;
import com.nevilon.bigplanet.core.db.DAO;
import com.nevilon.bigplanet.core.db.GeoBookmark;

public class AddBookmarkDialog {

	public static void show(final Context context, final GeoBookmark geoBookmark) {
		View v = View.inflate(context, R.layout.addgeobookmark, null);

		final EditText nameValue = (EditText) v.findViewById(R.id.nameValue);
		final EditText descriptionValue = (EditText) v
				.findViewById(R.id.descriptionValue);

		new AlertDialog.Builder(context).setView(v).setTitle("Add bookmark")

		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {

				geoBookmark.setName(nameValue.getText().toString());
				geoBookmark.setDescription(descriptionValue.getText()
						.toString());
				DAO d = new DAO(context);
				d.saveGeoBookmark(geoBookmark);

			}

		})

		.setNegativeButton("No", null)

		.show();

	}
}
