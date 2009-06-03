package com.nevilon.moow;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nevilon.moow.core.PhysicMap;

public class DrawTesting extends Activity {
	public static final int DIRECTION_RIGHT = 0, DIRECTION_LEFT = 1;

	private Panel main;

	private volatile boolean running = true;

	private boolean isLoaded = false;
	
	
	
	private Point previousMovePoint = new Point();
	private Point nextMovePoint = new Point();
	private Point globalOffset = new Point();
	
	PhysicMap pmap = new PhysicMap();

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
	
		case MotionEvent.ACTION_DOWN:
			nextMovePoint.set((int)event.getX(), (int)event.getY());
			//System.out.println("DOWN");
			break;
		case MotionEvent.ACTION_MOVE:
			previousMovePoint.set(nextMovePoint.x, nextMovePoint.y);
			nextMovePoint.set((int)event.getX(),(int)event.getY());
			

			globalOffset.set(globalOffset.x+(nextMovePoint.x-previousMovePoint.x),
					globalOffset.y+(nextMovePoint.y-previousMovePoint.y));
				break;
		case MotionEvent.ACTION_UP:
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
		
		
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				canvas.drawBitmap(pmap.getCells()[i][j],
						(i - 1) * 256+globalOffset.x,
						(j-1) * 256+globalOffset.y, paint);
			}
		}

	}

	class Panel extends View {
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