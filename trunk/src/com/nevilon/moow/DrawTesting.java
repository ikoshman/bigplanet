package com.nevilon.moow;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnLongClickListener;
import android.widget.TextView;
import android.graphics.*;
import android.content.*;

public class DrawTesting extends Activity {
	public static final int DIRECTION_RIGHT = 0, DIRECTION_LEFT = 1;

	private Panel main;

	private volatile boolean running = true;

	private boolean isLoaded = false;

	private long lastUpdates = System.currentTimeMillis();

	private float dx = 0;

	private float dy = 0;

	private float startX = 0;

	private float startY = 0;

	private float curerntX = 0;

	private float currentY = 0;

	boolean moving = false;

	Bitmap[][] tiles = new Bitmap[4][4];

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		main = new Panel(this);
		setContentView(main, new ViewGroup.LayoutParams(320, 480));
		(new Thread(new AnimationLoop())).start();

	}

	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			if (!moving) {
				startX = event.getX();
				startY = event.getY();
				System.out.println("startX " + startX + " startX " + startY);
			}
			dx = event.getX();
			dy = event.getY();
			moving = true;
			System.out.println("ACTION_MOVE");
			break;
		case MotionEvent.ACTION_UP:
			System.out.println("ACTION_UP");
			if (moving) {
			currentY = dy - startY;
			curerntX = dx - startX;
			startX = 0;
			startY = 0;
			dx = 0;
			dy = 0;

			 }
			moving = false;
			break;
		}

		return super.onTouchEvent(event);
	}

	private void loadTiles() {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				try {
					FileInputStream fl = new FileInputStream("/sdcard/map/"
							+ String.valueOf(i) + String.valueOf(j) + ".png");
					tiles[i][j] = BitmapFactory.decodeStream(fl);

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}

		}

	}

	private synchronized void doDraw(Canvas canvas, Paint paint) {
		if (!isLoaded) {
			isLoaded = true;
			loadTiles();
		}
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (moving) {
					canvas.drawBitmap(tiles[i][j], (i - 1) * 256 + dx - startX,
							(j - 1) * 256 + dy - startY, paint);
				} else {
					canvas.drawBitmap(tiles[i][j], (i - 1) * 256 + curerntX,
							(j - 1) * 256 + currentY, paint);

				}
			}

			// canvas.clipRect( (0)*256-dx, (0)*256, (0)*256+256, (0)*256+256);

		}

	}

	class Panel extends View implements OnLongClickListener {
		Paint paint;

		public Panel(Context context) {
			super(context);
			paint = new Paint();
			TextView txt = new TextView(getContext());
			txt.setText("xxxxxxxx");
			addContentView(txt, new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT));
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			doDraw(canvas, paint);
		}

		public boolean onLongClick(View arg0) {
			Log.e("long", "fuck fuck");
			return true;
		}
	}

	class AnimationLoop implements Runnable {
		public void run() {
			while (true) {
				while (running) {
					try {
						Thread.sleep(30);
					} catch (InterruptedException ex) {
					}
					main.postInvalidate();
				}
			}
		}
	}
}