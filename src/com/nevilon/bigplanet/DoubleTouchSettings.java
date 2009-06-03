package com.nevilon.bigplanet;

import com.nevilon.bigplanet.core.ui.DoubleClickDetector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class DoubleTouchSettings extends Activity {
	
	private DoubleClickDetector dcDetector = new DoubleClickDetector();

	private boolean inMove = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View v = View.inflate(this, R.layout.doubletouchsettings, null);
		
		//interval label
		final TextView intervalLabel = (TextView)v.findViewById(R.id.intervalLabel);
		intervalLabel.setText("Interval(ms): ");
		
		// interval seekbar
		SeekBar intervalBar = (SeekBar) v.findViewById(R.id.interval);
		intervalBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStartTrackingTouch(SeekBar seekBar) {}

			public void onStopTrackingTouch(SeekBar seekBar) {}

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromTouch) {
				intervalLabel.setText("Interval(ms): " + progress*100);
				dcDetector.setInterval(progress*100);
			}

		});
		
		
		final TextView accuracyLabel = (TextView)v.findViewById(R.id.accuracyLabel);
		accuracyLabel.setText("Accuracy(px): ");
		
		// interval seekbar
		SeekBar accuracyBar = (SeekBar) v.findViewById(R.id.accuracy);
		accuracyBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStartTrackingTouch(SeekBar seekBar) {}

			public void onStopTrackingTouch(SeekBar seekBar) {}

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromTouch) {
				dcDetector.setPrecise(progress);
				accuracyLabel.setText("Accuracy(px): " + progress);
			}

		});

		
		setContentView(v);
	}
	
	

	/**
	 * Обработка касаний
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			inMove = false;
			break;
		case MotionEvent.ACTION_MOVE:
			inMove = true;
			break;
		case MotionEvent.ACTION_UP:
			if (inMove) {
				
				new AlertDialog.Builder(this)
				.setMessage("In move")
				.setPositiveButton("Ok", null)
				.setCancelable(true)
				.show();
				
			} if (dcDetector.process(event)) {
					new AlertDialog.Builder(this)
					.setMessage("Double click detected! Congratulations!")
					.setPositiveButton("Ok", null)
					.setCancelable(true)
					.show();
				}
	
			break;
		}

		return true;
	}
	
	

}
