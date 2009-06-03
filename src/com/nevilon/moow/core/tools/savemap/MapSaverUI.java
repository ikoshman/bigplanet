package com.nevilon.moow.core.tools.savemap;

import java.text.MessageFormat;
import java.util.Stack;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.nevilon.moow.R;
import com.nevilon.moow.core.Preferences;
import com.nevilon.moow.core.RawTile;
import com.nevilon.moow.core.geoutils.GoogleTileUtils;
import com.nevilon.moow.core.providers.MapStrategyFactory;
import com.nevilon.moow.core.storage.LocalStorageWrapper;

public class MapSaverUI {

	private static final String DOWNLOAD_MESSAGE = "{0} tile(s) of {1} downloaded({2}KB), {3} error(s)";

	final String MESSAGE_PATTERN = DOWNLOAD_MESSAGE;

	private Context context;

	private int zoomLevel;

	private Point absoluteCenter;

	private MapSaver mapSaver;

	private Stack<RawTile> tiles;

	
	private TextView downloadInfo;
	
	
	public MapSaverUI(Context context, int zoomLevel, Point absoluteCenter) {
		this.context = context;
		this.absoluteCenter = absoluteCenter;
		this.zoomLevel = zoomLevel;
	}

	public void show() {
		showParamsDialog();
	}

	private void showParamsDialog() {
		final Dialog paramsDialog = new Dialog(context);
		paramsDialog.setTitle("Select radius of region");
		paramsDialog.setCanceledOnTouchOutside(true);
		paramsDialog.setCancelable(true);

		View v = View.inflate(context, R.layout.savemap, null);

		downloadInfo = (TextView) v
		.findViewById(R.id.downloadInfo);
		final EditText radiusValue = (EditText) v
				.findViewById(R.id.radiusValue);
		
	
		radiusValue.addTextChangedListener(new TextWatcher(){

			public void afterTextChanged(Editable arg0) {
				int radius = Integer.parseInt(arg0.toString());
				updateLabels(radius);
								
			}

			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {}

			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {}
			
		});
		
		radiusValue.setText("0");
		updateLabels(0);
		final Button startButton = (Button) v.findViewById(R.id.startButton);
		startButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				paramsDialog.dismiss();
				showProgressDialog();
				// запуск загрузки, отображение индикатора
			}

		});

		final Button cancelButton = (Button) v.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				paramsDialog.dismiss();
			}

		});

		SeekBar radiusSeek = (SeekBar) v.findViewById(R.id.radiusSeekbar);
		radiusSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromTouch) {

				int radius = (int) Math.pow(2, progress);
				radiusValue.setText(String.valueOf(radius));
				updateLabels(radius);
			}

		});
		paramsDialog.setContentView(v);
		paramsDialog.show();

	}

	
	private void updateLabels(int radius){
		tiles = getTiles(radius);
		downloadInfo.setText(String.valueOf(tiles.size())
				+ " tile(s) / "
				+ String.valueOf(tiles.size() * 20000 / 1024) + " KB");

	}
	
	private void showProgressDialog() {
		final ProgressDialog downloadDialog = new ProgressDialog(context);
		downloadDialog.setTitle("Downloading...");
		downloadDialog.setCancelable(true);
		downloadDialog.setButton("Cancel",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						mapSaver.stopDownload();
						downloadDialog.dismiss();
					}

				});

		downloadDialog.show();

		mapSaver = new MapSaver(tiles, MapStrategyFactory
				.getStrategy(MapStrategyFactory.GOOGLE_VECTOR), new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					if (mapSaver.getTotalSuccessful()
							+ mapSaver.getTotalUnsuccessful() == tiles.size()) {
						downloadDialog.dismiss();
					} else {
						String message = MessageFormat.format(DOWNLOAD_MESSAGE,
								mapSaver.getTotalSuccessful(), tiles.size(),
								mapSaver.getTotalKB(), mapSaver
										.getTotalUnsuccessful());
						downloadDialog.setMessage(message);
					}
					break;
				}
				super.handleMessage(msg);
			}

		});

		String message = MessageFormat.format(DOWNLOAD_MESSAGE, mapSaver
				.getTotalSuccessful(), tiles.size(), mapSaver.getTotalKB(),
				mapSaver.getTotalUnsuccessful());
		downloadDialog.setMessage(message);
		mapSaver.download();
	}

	private Stack<RawTile> getTiles(int radius) {
		Point latLon = GoogleTileUtils.getLatLong(absoluteCenter.x / 256,
				absoluteCenter.y / 256, zoomLevel);
		double resolution = (Math.cos(latLon.x * Math.PI / 180) * 2 * Math.PI * 6378137)
				/ (256 * Math.pow(2, 17 - zoomLevel));

		// радиус в тайлах
		int dTile;
		dTile = (int) Math.rint((((radius * 1000) / resolution) / 256));
		// коордитаны центровой тайла
		int cx = absoluteCenter.x / 256;
		int cy = absoluteCenter.y / 256;

		int topX = cx - dTile;
		int topY = cy - dTile;

		
		final Stack<RawTile> tiles = new Stack<RawTile>();
		for (int i = topX; i <= topX + 2 * dTile; i++) {
			for (int j = topY; j <= topY + 2 * dTile; j++) {
				RawTile tile = new RawTile(i, j, zoomLevel);
				tiles.add(tile);
			}
		}
		return tiles;
	}

}
