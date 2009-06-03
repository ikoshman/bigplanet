package com.nevilon.moow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.nevilon.moow.core.LocalStorage;
import com.nevilon.moow.core.PhysicMap;
import com.nevilon.moow.core.TileLoader;
import com.nevilon.moow.core.TileProvider;

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
	
	private float dx = 0;

	private float dy = 0;

	private float startX = 0;

	private float startY = 0;

	private float curerntX = 0;

	private float currentY = 0;
	
	private Point previousMovePoint = new Point();
	private Point nextMovePoint = new Point();
	private Point globalOffset = new Point();
	

	boolean moving = false;

	Bitmap[][] tiles = new Bitmap[4][4];

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		main = new Panel(this);
		setContentView(main, new ViewGroup.LayoutParams(320, 480));
		(new Thread(new AnimationLoop())).start();
		
		
	//	TileProvider tileProvider = new TileProvider();
	//	for(int i=10;i<100;i++){
	//		tileProvider.getTile(i,172, 8);
			
//		}
		
		
	}

	
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		
		
		
		case MotionEvent.ACTION_DOWN:
			System.out.println("DOWN");
			break;
		case MotionEvent.ACTION_MOVE:

			if (!moving) {
				// начато движение
				startX = event.getX();
				startY = event.getY();				
			} else {
				globalOffset.x = globalOffset.x + (nextMovePoint.x-previousMovePoint.x) ; // сохранение сдвига
				globalOffset.y = globalOffset.y + (nextMovePoint.y-previousMovePoint.y) ; // сохранение сдвига

			}
			
			
			previousMovePoint.x = nextMovePoint.x;
			previousMovePoint.y = nextMovePoint.y;
			nextMovePoint.x = (int)event.getX();
			nextMovePoint.y = (int)event.getY();
			
			System.out.println(globalOffset.x);
			System.out.println("startX " + startX + " startX " + startY);
			dx = event.getX(); // 
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
		
		PhysicMap pmap = new PhysicMap();
		for(int i=0;i<3;i++){
			canvas.drawBitmap(pmap.getCells()[i][0], (i - 1) * 256,0 * 256, paint);
		}

		
//		for (int i = 0; i < 4; i++) {
//			for (int j = 0; j < 4; j++) {
				//if (moving) {
//					canvas.drawBitmap(tiles[i][j], (i - 1) * 256 + globalOffset.x,
//							(j - 1) * 256 + globalOffset.y, paint);
			//	} else {
			//		canvas.drawBitmap(tiles[i][j], (i - 1) * 256 + curerntX,
			//				(j - 1) * 256 + currentY, paint);
			//	}
	//		}

//		}

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