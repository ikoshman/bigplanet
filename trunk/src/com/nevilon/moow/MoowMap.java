package com.nevilon.moow;

import org.junit.Test;

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

		int zoom = 12; // whole world

	PhysicMap pmap = new PhysicMap(new RawTile(9, 7, zoom));

	boolean moving = false;

	Bitmap[][] tiles = new Bitmap[4][4];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		main = new Panel(this);

		setContentView(main, new ViewGroup.LayoutParams(320, 480));
		(new Thread(new CanvasUpdater())).start();

		class ZoomPanel extends RelativeLayout {

			
						
			public ZoomPanel(Context context) {
				super(context);
				ZoomControls zc = new ZoomControls(getContext());
				zc.setOnZoomOutClickListener(new OnClickListener(){
					public void onClick(View v) {
						zoomOut();
					}
					
				});
				zc.setOnZoomInClickListener(new OnClickListener() {

					public void onClick(View v) {
						zoomIn();
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
			System.out.println("move");
			moveCoordinates(event.getX(), event.getY());
			break;
		case MotionEvent.ACTION_UP:
		    inMove = false;
		    moveCoordinates(event.getX(), event.getY());
		    break;
		}

		return super.onTouchEvent(event);
	}

	private void zoomIn(){
		int currentZoomX = pmap.getDefaultTile().getX()*256-globalOffset.x+160;
		int currentZoomY = pmap.getDefaultTile().getY()*256-globalOffset.y+240;
		int tileX = (currentZoomX*2)/256;
		int tileY = (currentZoomY*2)/256;
		zoom-=1;
		pmap.zoom(tileX, tileY, zoom);
	}
	
	private void zoomOut(){
		if((zoom)<16){
			int currentZoomX = pmap.getDefaultTile().getX()*256-globalOffset.x+160;
			int currentZoomY = pmap.getDefaultTile().getY()*256-globalOffset.y+240;
			int tileX = (currentZoomX/2)/256;
			int tileY = (currentZoomY/2)/256;
			zoom+=1;
			pmap.zoom(tileX, tileY, zoom);

		}
		
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
		} else {
			nL = 0;
		}

		// обработка перемещения вниз
		if (isMovedBottom()) {
			if (previousMovePoint.y < nextMovePoint.y) {
				System.out.println("move botton");
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
		return inMove && nL!=-1 && Math.abs(Math.ceil((globalOffset.x - 256 + 320) / 256) - nL) == 1;

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

	
	class CanvasUpdater implements Runnable {

		public void run() {
			while (running) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException ex) {
				}
				main.postInvalidate();
			}
		}

	}
}