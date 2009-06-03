package com.nevilon.moow;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

import com.nevilon.moow.core.PhysicMap;
import com.nevilon.moow.core.RawTile;
import com.nevilon.moow.core.utils.TileUtils;
import com.nevilon.moow.core.utils.TileUtils.GeoLocation;
import com.nevilon.moow.core.utils.TileUtils.GeoPoint;

public class MoowMap extends Activity {
	public static final int DIRECTION_RIGHT = 0, DIRECTION_LEFT = 1;

	private Panel main;

	private volatile boolean running = true;

	private boolean inMove = false;

	private int nL = -1;
	private int nR = -1;
	private int nB = -1;
	private int nT = -1;

	private Point previousMovePoint = new Point();
	private Point nextMovePoint = new Point();
	private Point globalOffset = new Point();

	private long lastMove = -1;

	int zoom = 16; // whole world

	PhysicMap pmap = new PhysicMap(new RawTile(0, 0, zoom));

	boolean moving = false;

	Bitmap[][] tiles = new Bitmap[4][4];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		main = new Panel(this);

		setContentView(main, new ViewGroup.LayoutParams(320, 480));
		(new Thread(new AnimationLoop())).start();

		class ZoomPanel extends RelativeLayout {

			
			private void zoom(int direction){
				double x = 320 / 2 + Math.abs(globalOffset.x);
				double y = 480 / 2 + Math.abs(globalOffset.y);
				// получение сдвига в полных тайлах
				int tx = (int) Math.floor(x / 256);
				int ty = (int) Math.floor(y / 256);

				// получение отступа в для последнего тайла
				int otx = (int) (x - 256 * tx);
				int oty = (int) (y - 256 * ty);
				//otx = 0;
			//oty =0;
				GeoLocation cpoint = TileUtils.getBoundingBox(tx
						+ pmap.getDefaultTile().getX(), ty
						+ pmap.getDefaultTile().getY(), otx, oty, zoom);

				/*
				cpoint = TileUtils.getLatLong(tx+pmap.getDefaultTile().getX(),
						                      ty+pmap.getDefaultTile().getY(),
						                      otx,
						                      oty,
						                      zoom);
				*/
				// увеличение
				if (direction == 1){
					zoom--;
	
					// уменьшение
				} else {
					if (zoom!=16){
						zoom++;	
					} else {
						return;
					}
				}
				  
				
				GeoPoint xy = TileUtils.getTileXY(cpoint.lat,
						cpoint.lon, zoom);
				GeoPoint offset =TileUtils.getPixelXY(cpoint.lon, cpoint.lat, zoom);
				MoowMap.this.pmap.zoom((int) xy.x, (int) xy.y, zoom);

				previousMovePoint = new Point();
				nextMovePoint = new Point();
				globalOffset = new Point();
				globalOffset.x = (int) (-160+offset.x);
				globalOffset.y =  (int) (240-offset.y);
				
				
				//moveCoordinates(globalOffset.x, globalOffset.y);
		

			}
			
			public ZoomPanel(Context context) {
				super(context);
				ZoomControls zc = new ZoomControls(getContext());
				zc.setOnZoomOutClickListener(new OnClickListener(){
					public void onClick(View v) {
						zoom(-1);
					}
					
				});
				zc.setOnZoomInClickListener(new OnClickListener() {

					public void onClick(View v) {
						zoom(1);
					}

				});
				addView(zc);
				setPadding(80, 368, 0, 0);
			}

		}
		addContentView(new ZoomPanel(MoowMap.this), new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}

	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:

			nextMovePoint.set((int) event.getX(), (int) event.getY());
		
			break;
		case MotionEvent.ACTION_MOVE:
			moveCoordinates(event.getX(), event.getY());
			lastMove = System.currentTimeMillis();
		
			break;
		case MotionEvent.ACTION_UP:
			// inMove = false;
			if (lastMove != -1 && System.currentTimeMillis() - lastMove > 1000) {
			} else {
				// (new Thread(new InertionMoover())).start();
			}
			lastMove = -1;
		
			break;
		}

		return super.onTouchEvent(event);
	}

	private void moveCoordinates(float x, float y) {
		inMove = true;
		previousMovePoint.set(nextMovePoint.x, nextMovePoint.y);
		nextMovePoint.set((int) x, (int) y);
		globalOffset.set(globalOffset.x
				+ (nextMovePoint.x - previousMovePoint.x), globalOffset.y
				+ (nextMovePoint.y - previousMovePoint.y));

	}

	
	
	private synchronized void doDraw(Canvas canvas, Paint paint) {
		System.out.println(globalOffset.x);
		updatePhysicMap();
		Bitmap tmpBitmap;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				tmpBitmap = pmap.getCells()[i][j];
				if (tmpBitmap != null) {
					canvas.drawBitmap(tmpBitmap, (i) * 256 + globalOffset.x,
							(j) * 256 + globalOffset.y, paint);
				}
			}
		}

	}

	private void updatePhysicMap() {
		boolean movedR = false;
		boolean movedL = false;
		boolean movedB = false;
		boolean movedT = false;

		// обработка перемещения вправо
		if (isMovedRight()) {
			if (previousMovePoint.x < nextMovePoint.x) {
				globalOffset.x -= 256;
				pmap.moveLeft();
				movedR = true;
			}
		}
		if (!movedR) {
			nR = (int) Math.ceil((globalOffset.x - 256) / 256);
		} else {
			nR = 0;
		}

		// обработка перемещения влево
		if (isMovedLeft()) {
			if (previousMovePoint.x > nextMovePoint.x) {
				globalOffset.x += (256);
				pmap.moveRight();
				movedL = true;
			}
		}
		if (!movedL) {
			nL = (int) Math.ceil((globalOffset.x - 256 + 320) / 256);
			// System.out.println(nL);
		} else {
			nL = 0;
		}

		// обработка перемещения вниз
		if (isMovedBottom()) {
			if (previousMovePoint.y < nextMovePoint.y) {
				globalOffset.y -= (256);
				pmap.moveBottom();
				movedB = true;
			}

		}
		if (!movedB) {
			nB = (int) Math.ceil((globalOffset.y - (256)) / 256);
		} else {
			nB = 0;
		}

		// обработка перемещения вверх
		if (isMovedTop()) {
			if (previousMovePoint.y > nextMovePoint.y) {
				globalOffset.y += (256);
				pmap.moveTop();
				movedT = true;
			}

		}
		if (!movedT) {
			nT = (int) Math.ceil((globalOffset.y + 256 + 480) / 256);
		} else {
			nT = 0;
		}
	}

	// проверка, передвинуто ли вправо
	private boolean isMovedRight() {
		return inMove && nR != -1
				&& Math.abs(Math.ceil((globalOffset.x + 256) / 256) - nR) == 1;
	}

	// проверка, передвинуто ли влево
	private boolean isMovedLeft() {
		return Math.abs(Math.ceil((globalOffset.x - 256 + 320) / 256) - nL) == 1;

	}

	// проверка, передвино ли вниз
	private boolean isMovedBottom() {
		return inMove && nB != -1
				&& Math.abs(Math.ceil((globalOffset.y + 256) / 256) - nB) == 1;
	}

	// проверка, передвино ли вниз
	private boolean isMovedTop() {
		return inMove
				&& nT != -1
				&& Math.abs(Math.ceil((globalOffset.y - 256 + 480) / 256) - nT) == 1;
	}

	class Panel extends View {
		Paint paint;

		public Panel(Context context) {
			super(context);
			paint = new Paint();
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			doDraw(canvas, paint);
		}
	}

	class InertionMoover implements Runnable {

		private int counter = 400;

		public void run() {

			int x = nextMovePoint.x + 1;
			int x1 = previousMovePoint.x;
			int x2 = nextMovePoint.x;
			int y1 = previousMovePoint.y;
			int y2 = nextMovePoint.y;

			// if(x1 == 0){
			// x1 = x2-2;
			// }

			System.out.println(previousMovePoint.x + "^" + previousMovePoint.y);
			System.out.println(nextMovePoint.x + "*" + nextMovePoint.y);
			while (counter > 0) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int y = ((x - x1) * (y2 - y1) / (x2 - x1)) + y1;

				moveCoordinates(x, y);
				x -= x2 > x1 ? -1 : 1;
				counter -= 1;
			}
		}
	}

	class AnimationLoop implements Runnable {

		// private int counter = 400;

		public void run() {
			// while (true) {
			while (running) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException ex) {
				}
				// processInertion();
				main.postInvalidate();
			}
			// }
		}

		private void processInertion() {

		}
	}
}