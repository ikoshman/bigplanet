package com.nevilon.bigplanet.core.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nevilon.bigplanet.BigPlanet;
import com.nevilon.bigplanet.R;
import com.nevilon.bigplanet.core.db.DAO;
import com.nevilon.bigplanet.core.db.GeoBookmark;

/**
 * Диалог для добавления/редактирования геозакладки
 * @author hudvin
 *
 */
public class AddBookmarkDialog {
	

	public static void show(final Context context, final GeoBookmark geoBookmark,
			final OnDialogClickListener onClickListener) {
		View v = View.inflate(context, R.layout.addgeobookmark, null);
		
		final AlertDialog dialog = new AlertDialog.Builder(context).create();
		dialog.setView(v);
		dialog.setTitle("Add bookmark");
		
		final EditText nameValue = (EditText) v.findViewById(R.id.nameValue);
		final EditText descriptionValue = (EditText) v
				.findViewById(R.id.descriptionValue);
		final TextView validationError = (TextView) v.findViewById(R.id.validationError); 
		
		final Button cancelBtn = (Button) v.findViewById(R.id.cancelBtn);
		cancelBtn.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				dialog.dismiss();
				onClickListener.onCancelClick();
			}
			
		});
		
		final Button addBtn = (Button) v.findViewById(R.id.addBtn);
		addBtn.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				String name = nameValue.getText().toString();
				String description = descriptionValue.getText().toString();
				if(name.trim().length() ==0){
					validationError.setText("Name cannot be empty");
				} else {				
					geoBookmark.setName(name);
					geoBookmark.setDescription(description);
					onClickListener.onOkClick(geoBookmark);
					dialog.dismiss();	
				}
			}
			
		});
		
		dialog.setCancelable(false);
		dialog.show();
	}
}
