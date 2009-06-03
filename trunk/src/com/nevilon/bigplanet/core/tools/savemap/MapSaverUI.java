package com.nevilon.bigplanet.core.tools.savemap;

import java.text.MessageFormat;
import java.util.Stack;
import java.util.Timer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.nevilon.bigplanet.R;
import com.nevilon.bigplanet.core.RawTile;
import com.nevilon.bigplanet.core.geoutils.GoogleTileUtils;
import com.nevilon.bigplanet.core.providers.MapStrategyFactory;

public class MapSaverUI {

	private static final String DOWNLOAD_MESSAGE = "{0} tile(s) of {1} downloaded({2}KB), {3} error(s)";

	final String MESSAGE_PATTERN = DOWNLOAD_MESSAGE;

	private Context context;

	private int zoomLevel;
	
  	private boolean alreadyCreated = false;

	private Point absoluteCenter;

	private MapSaver mapSaver;

	private Stack<RawTile> tiles = new Stack<RawTile>();

	private int sourceId;

	private int radius = 0;

	private TextView downloadInfo;

	private Timer timer = new Timer();

	public MapSaverUI(Context context, int zoomLevel, Point absoluteCenter,
			int sourceId) {
		this.context = context;
		this.absoluteCenter = absoluteCenter;
		this.zoomLevel = zoomLevel;
		this.sourceId = sourceId;
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

		downloadInfo = (TextView) v.findViewById(R.id.downloadInfo);

		updateLabels();
		final Button startButton = (Button) v.findViewById(R.id.startButton);
		startButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				getTiles(radius, false);
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

				radius = (int) Math.pow(2, progress);
				updateLabels();
			}

		});
		paramsDialog.setContentView(v);
		paramsDialog.show();

	}

	private void updateLabels() {
		int tilesCount = getTiles(radius, true);
		downloadInfo.setText(String.valueOf(radius) + " km, "
				+ String.valueOf(tilesCount) + " tile(s) / "
				+ String.valueOf(tilesCount * 1100 / 1024) + " KB");

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
						timer.cancel();
					}

				});

		downloadDialog.show();

		final Thread updateThread;
		mapSaver = new MapSaver(tiles,
				MapStrategyFactory.getStrategy(sourceId), new Handler() {
					
			
			     @Override
					public void handleMessage(Message msg) {
						
			    	 
			    	 
			    	 
			    	 switch (msg.what) {
						case 0:
							System.out.println("case");
							if (!alreadyCreated && mapSaver.getTotalSuccessful()
									+ mapSaver.getTotalUnsuccessful() == tiles
									.size()) {
								alreadyCreated = true;
								downloadDialog.dismiss();
								
								System.out.println("create");
								
								Builder completeDialog = new AlertDialog.Builder(
										context)
								.setTitle("Download complete")
										.setMessage("All tiles were saved")
											.setPositiveButton(
													"Ok",
													new DialogInterface.OnClickListener() {

													public void onClick(
															DialogInterface dialog,
															int which) {
														downloadDialog.cancel();
														System.out.println("close");
													}

												});
								completeDialog.show();
							}

							break;
						}
						super.handleMessage(msg);
					}

				});

		final Handler handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				String message = MessageFormat.format(DOWNLOAD_MESSAGE,
						mapSaver.getTotalSuccessful(), tiles.size(), mapSaver
								.getTotalKB(), mapSaver.getTotalUnsuccessful());
				downloadDialog.setMessage(message);
			}

		};

		updateThread = new Thread() {


			@Override
			public void run() {

				while (downloadDialog != null && downloadDialog.isShowing()) {
					try {
						Thread.sleep(1000);
						handler.sendEmptyMessage(0);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
				System.out.println("fuck");

			}

		};

		updateThread.start();

		String message = MessageFormat.format(DOWNLOAD_MESSAGE, mapSaver
				.getTotalSuccessful(), tiles.size(), mapSaver.getTotalKB(),
				mapSaver.getTotalUnsuccessful());
		downloadDialog.setMessage(message);
		mapSaver.download();
	}

	private int getTiles(int radius, boolean onlyCount) {
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

		tiles.clear();
		int count = 0;
		for (int i = topX; i <= topX + 2 * dTile; i++) {
			for (int j = topY; j <= topY + 2 * dTile; j++) {
				if (onlyCount) {
					count++;
				} else {
					RawTile tile = new RawTile(i, j, zoomLevel, sourceId);
					if (GoogleTileUtils.isValid(tile)) {
						tiles.add(tile);
					}

				}
			}
		}
		return count;
	}

}
