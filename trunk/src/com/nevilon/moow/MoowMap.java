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

	private Panel main;

	private volatile boolean running = true;

	private Point previousMovePoint = new Point();
	private Point nextMovePoint = new Point();
	
	private  int zoom = 12; // whole world

	private PhysicMap pmap = new PhysicMap(new RawTile(9, 7, zoom));
	
	boolean inMove = false;
	
	private DoubleClickDispatcher dcDispatcher = new DoubleClickDispatcher();

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
						zoomCenter();
					}
				});
				addView(zc);
				setPadding(80, 368, 0, 0);
			}

		}
		addContentView(new ZoomPanel(MoowMap.this), new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}

	private static class DoubleClickDispatcher{
		
		/**
		 * Минимальный временной промежуток между двумя 
		 * отдельными касаниями, при котором они воспринимаются как 
		 * двойное касание
		 */
		private static int CLICK_INTERVAL = 400;
		
		/**
		 * Максимальное расстояние между касаниями,
		 * при котором они воспринимаются как двойное
		 */
		private static int CLICK_PRECISE = 3;
		
		/**
		 * Хранит предыдущее событие
		 */
		private Point previousPoint;
		
		/**
		 * Хранит время предыдущего события
		 */
		private long eventTime;
		
		public boolean process(MotionEvent currentEvent){
			if (previousPoint!=null 
					&& (System.currentTimeMillis()-eventTime)<DoubleClickDispatcher.CLICK_INTERVAL
					&& isNear((int)currentEvent.getX(), (int)currentEvent.getY())){
				return true;
			}
			previousPoint = new Point();
			previousPoint.x  = (int) currentEvent.getX();
			previousPoint.y  = (int) currentEvent.getY();
			eventTime = System.currentTimeMillis();
			return false;
		}
		
		/**
		 * Проверяет, находится ли первая точка вблизи второй
		 * @param event
		 * @return
		 */
		private boolean isNear(int x, int y){
			boolean checkX = Math.abs(previousPoint.x - x)<=DoubleClickDispatcher.CLICK_PRECISE;
			boolean checkY = Math.abs(previousPoint.y - y)<=DoubleClickDispatcher.CLICK_PRECISE; 
			return checkX == checkY && checkX == true;		
		}
		
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		// опускание клавиши
		case MotionEvent.ACTION_DOWN:
			inMove = false;
			nextMovePoint.set((int) event.getX(), (int) event.getY());	
			break;	
		// движение
		case MotionEvent.ACTION_MOVE:
			inMove = true;
			moveCoordinates(event.getX(), event.getY());
			break;
		// поднятие клавиши
		case MotionEvent.ACTION_UP:
			System.out.println("UP");
			if(inMove){
				moveCoordinates(event.getX(), event.getY());
			    quickHack();
			    quickHack();
			} else {
			   if(dcDispatcher.process(event)){
					zoomIn((int)event.getX(), (int)event.getY());				
				}
			}
			
			break;
		}

		return super.onTouchEvent(event);
	}

	
	private  void  quickHack(){
		int dx = 0,dy = 0;
	    if(pmap.globalOffset.x>0){
	    	dx = Math.round((pmap.globalOffset.x+320)/256);
	    } else {
	    	dx = Math.round((pmap.globalOffset.x)/256);
	    }
	    
	    
	    
	    
	    if(pmap.globalOffset.y>0){
	    	dy = (int)Math.floor((pmap.globalOffset.y+480)/256);  
	    } else {
	    	dy = (int)Math.floor(pmap.globalOffset.y/256);
		    
	    }
	    
	    pmap.globalOffset.x = pmap.globalOffset.x - dx*256 ;
	    pmap.globalOffset.y = pmap.globalOffset.y - dy*256;
	    
	    
	    pmap.move(dx, dy);
	    
	  
	    
	}
	
	private void zoomCenter(){
		zoomIn(160, 240);
	}
	
	/**
	 * Увеличение уровня детализации
	 * @param offsetX
	 * @param offsetY
	 */
   private void zoomIn(int offsetX, int offsetY){
		if(zoom>0){
			//получение отступа он начала координат
			int currentZoomX = pmap.getDefaultTile().x*256-pmap.globalOffset.x+offsetX;
			int currentZoomY = pmap.getDefaultTile().y*256-pmap.globalOffset.y+offsetY;
			// получение координат углового тайла
			int tileX = (currentZoomX*2)/256;
			int tileY = (currentZoomY*2)/256;
			zoom--;
			pmap.zoom(tileX, tileY, zoom);
		}
	}
	
	/**
	 * Уменьшение уровня детализации
	 */
	private void zoomOut(){
		if((zoom)<16){
			int currentZoomX = pmap.getDefaultTile().x*256-pmap.globalOffset.x+160;
			int currentZoomY = pmap.getDefaultTile().y*256-pmap.globalOffset.y+240;
			int tileX = (currentZoomX/2)/256;
			int tileY = (currentZoomY/2)/256;
			zoom++;
			pmap.zoom(tileX, tileY, zoom);

		}
		
	}
	
	
	
	private void moveCoordinates(float x, float y) {
		previousMovePoint.set(nextMovePoint.x, nextMovePoint.y);
		nextMovePoint.set((int) x, (int) y);
		pmap.globalOffset.set(pmap.globalOffset.x
				+ (nextMovePoint.x - previousMovePoint.x), pmap.globalOffset.y
				+ (nextMovePoint.y - previousMovePoint.y));
	}

	
	
	private synchronized void doDraw(Canvas canvas, Paint paint) {
		Bitmap tmpBitmap;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				tmpBitmap = pmap.getCells()[i][j];
				if (tmpBitmap != null) {
					canvas.drawBitmap(tmpBitmap, (i) * 256 + pmap.globalOffset.x,
							(j) * 256 + pmap.globalOffset.y, paint);
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