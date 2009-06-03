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

public class MoowMap extends Activity {
	public static final int DIRECTION_RIGHT = 0, DIRECTION_LEFT = 1;

	private Panel main;

	private volatile boolean running = true;

	private Point previousMovePoint = new Point();
	private Point nextMovePoint = new Point();
	private Point globalOffset = new Point();

	private  int zoom = 12; // whole world

	PhysicMap pmap = new PhysicMap(new RawTile(9, 7, zoom));

	boolean moving = false;
	
	boolean canDraw = true;

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
			moveCoordinates(event.getX(), event.getY());
			break;
		case MotionEvent.ACTION_UP:
			canDraw = false;
			moveCoordinates(event.getX(), event.getY());
		    quickHack();
		    quickHack();
		    canDraw = true;
		    break;
		}

		return super.onTouchEvent(event);
	}

	private  void  quickHack(){
		int dx = 0,dy = 0;
	    if(globalOffset.x>0){
	    	dx = Math.round((globalOffset.x+320)/256);
	    } else {
	    	dx = Math.round((globalOffset.x)/256);
	    }
	    
	    
	    if(globalOffset.y>0){
	    	dy = (int)Math.floor((globalOffset.y+480)/256);  
	    } else {
	    	dy = (int)Math.floor(globalOffset.y/256);
		    
	    }
	    
	    globalOffset.x = globalOffset.x - dx*256 ;
	    globalOffset.y = globalOffset.y - dy*256;
	    
	    pmap.move(dx, dy);
	   
	}
	
	private void zoomIn(){
		//получение отступа он начала координат
		int currentZoomX = pmap.getDefaultTile().x*256-globalOffset.x+160;
		int currentZoomY = pmap.getDefaultTile().y*256-globalOffset.y+240;
		// получение координат углового тайла
		int tileX = (currentZoomX*2)/256;
		int tileY = (currentZoomY*2)/256;
		zoom-=1;
		pmap.zoom(tileX, tileY, zoom);
	}
	
	private void zoomOut(){
		if((zoom)<16){
			int currentZoomX = pmap.getDefaultTile().x*256-globalOffset.x+160;
			int currentZoomY = pmap.getDefaultTile().y*256-globalOffset.y+240;
			int tileX = (currentZoomX/2)/256;
			int tileY = (currentZoomY/2)/256;
			zoom+=1;
			pmap.zoom(tileX, tileY, zoom);

		}
		
	}
	
	
	
	private void moveCoordinates(float x, float y) {
		previousMovePoint.set(nextMovePoint.x, nextMovePoint.y);
		nextMovePoint.set((int) x, (int) y);
		globalOffset.set(globalOffset.x
				+ (nextMovePoint.x - previousMovePoint.x), globalOffset.y
				+ (nextMovePoint.y - previousMovePoint.y));

	}

	
	
	private synchronized void doDraw(Canvas canvas, Paint paint) {
		if(!canDraw){return;}
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