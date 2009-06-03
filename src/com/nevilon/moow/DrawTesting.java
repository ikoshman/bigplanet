package com.nevilon.moow;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnLongClickListener;
import android.graphics.*;
import android.content.*;

public class DrawTesting extends Activity {
	public static final int DIRECTION_RIGHT = 0, DIRECTION_LEFT = 1;

	private Panel main;

	private volatile boolean running = true;

	private float oldX = 0;

	private float oldY = 0;

	private float currentX = 0;

	private float currentY = 0;

	private float totalX = 0;
	private float totalY = 0;
	
	private boolean isLoaded = false;

	private long lastUpdates = System.currentTimeMillis();

	Bitmap[][] tiles = new Bitmap[4][4];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		main = new Panel(this);
		setContentView(main, new ViewGroup.LayoutParams(320, 480));
		(new Thread(new AnimationLoop())).start();

	}

	public boolean onTouchEvent(MotionEvent event) {
		boolean moving = false;
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:

			if (oldX > currentX) {
				// вправо
				totalX -= oldX-currentX;
			} else {
				//влево
				totalX += currentX-oldX;
			}

			if (oldY > currentY) {
				// вниз
				totalY -= oldY-currentY;
			} else {
				//вверх
				totalY += currentY-oldY;
			}

			oldX = currentX;
			oldY = currentY;
			currentX = event.getX() - 40;
			currentY = event.getY() - 100;

			moving = true;
			System.out.println("ACTION_MOVE");
			System.out.println(event.getX());
			break;
		case MotionEvent.ACTION_UP:
			System.out.println("ACTION_UP");
			if (moving) {
			}
			moving = false;
			break;
		}

		return super.onTouchEvent(event);
		/*
		 * boolean isMove = event.getAction() == MotionEvent.ACTION_MOVE;
		 * 
		 * if (isMove) { //totalX+=Math.abs(currentX-oldX); if(currentX>oldX){
		 * totalX-=8; //totalX = totalX+(currentX-oldX); } else { totalX+=8;
		 * //totalX=totalX+(currentX+oldX); } totalX-=8; oldX = currentX; oldY =
		 * currentY; currentX = event.getX(); //- 40; currentY = event.getY();
		 * //- 100; Log.e("ACTION", String.valueOf(event.getAction())); return
		 * super.onTouchEvent(event); } return super.onTouchEvent(event);
		 */
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
				canvas
						.drawBitmap(tiles[i][j], (i-1) * 256 + totalX, (j-1) * 256+totalY,
								paint);

			}

			
			 // canvas.clipRect( (0)*256-totalX, (0)*256, (0)*256+256, (0)*256+256);
			 

		}
		float x1 = currentX;
		float y1 = currentY;
		float x2 = (currentX) + 256;
		float y2 = (currentY) + 256;
		String mess = String.valueOf(x1) + " " + String.valueOf(y1) + " "
				+ String.valueOf(x2) + " " + String.valueOf(y2);
		// Log.i("MAP",mess);

		// canvas.clipRect(x1, y1, x2, y2);
		// Bitmap btp = BitmapFactory
		// .decodeResource(getResources(), R.drawable.mt);
		// canvas.drawBitmap(btp, currentX - 20, currentY + 20, paint);

		// canvas.drawBitmap(btp, currentX - 20, currentY + 20, paint);
		// }
	}

	class Panel extends View implements OnLongClickListener {
		Paint paint;

		public Panel(Context context) {
			super(context);
			paint = new Paint();
			// this.setOnLongClickListener(this);
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