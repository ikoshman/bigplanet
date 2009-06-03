package com.nevilon.moow;

import java.util.Stack;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

import com.nevilon.moow.core.InertionEngine;
import com.nevilon.moow.core.PhysicMap;
import com.nevilon.moow.core.RawTile;
import com.nevilon.moow.core.ui.DoubleClickHelper;

public class MoowMap extends Activity {

	public static final int MAP_HEIGHT = 480;

	private final static int BCG_CELL_SIZE = 16;

	/**
	 * Начальное значение зума
	 */
	private final static int START_ZOOM = 16; // whole world

	private Panel main;

	private ZoomPanel zoomPanel;

	private volatile boolean running = true;

	private PhysicMap pmap = new PhysicMap(
			new RawTile(0, 0, MoowMap.START_ZOOM));

	boolean inMove = false;

	private DoubleClickHelper dcDispatcher = new DoubleClickHelper();

	private Bitmap mapBg = drawBackground();
	
	// нужно ли запускать инерцию
	private boolean startInertion = false;
	
	// последнее время, когда происходило передвижение карты
	private long lastMoveTime = -1;
	
	private Stack<Point> moveHistory = new Stack<Point>();
	
	private InertionEngine iengine;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		System.out.println("created");
		super.onCreate(savedInstanceState);
		main = new Panel(this);
		setContentView(main, new ViewGroup.LayoutParams(320, MAP_HEIGHT));
		(new Thread(new CanvasUpdater())).start();
		zoomPanel = new ZoomPanel(this);
		addContentView(zoomPanel, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
	}

	/**
	 * Устанавливает состояние zoomIn/zoomOut контролов в зависимости от уровня
	 * зума
	 */
	private void updateZoomControls() {
		int zoomLevel = pmap.getZoomLevel();
		if (zoomLevel == 16) {
			zoomPanel.setIsZoomOutEnabled(false);
			zoomPanel.setIsZoomInEnabled(true);
		} else if (zoomLevel == 0) {
			zoomPanel.setIsZoomOutEnabled(true);
			zoomPanel.setIsZoomInEnabled(false);
		} else {
			zoomPanel.setIsZoomOutEnabled(true);
			zoomPanel.setIsZoomInEnabled(true);
		}
	}

	private void quickHack() {
		int dx = 0, dy = 0;
		int tdx, tdy;
		if (pmap.globalOffset.x > 0) {
			dx = Math.round((pmap.globalOffset.x + 320) / 256);
		} else {
			dx = Math.round((pmap.globalOffset.x) / 256);
		}

		if (pmap.globalOffset.y > 0) {
			dy = (int) Math.round((pmap.globalOffset.y + MAP_HEIGHT) / 256);
		} else {
			dy = (int) Math.round(pmap.globalOffset.y / 256);

		}

		pmap.globalOffset.x = pmap.globalOffset.x - dx * 256;
		pmap.globalOffset.y = pmap.globalOffset.y - dy * 256;

		tdx = dx;
		tdy = dy;

		if (pmap.globalOffset.x > 0) {
			dx = Math.round((pmap.globalOffset.x + 320) / 256);
		} else {
			dx = Math.round((pmap.globalOffset.x) / 256);
		}

		if (pmap.globalOffset.y > 0) {
			dy = (int) Math.round((pmap.globalOffset.y + MAP_HEIGHT) / 256);
		} else {
			dy = (int) Math.round(pmap.globalOffset.y / 256);

		}

		pmap.globalOffset.x = pmap.globalOffset.x - dx * 256;
		pmap.globalOffset.y = pmap.globalOffset.y - dy * 256;

		tdx += dx;
		tdy += dy;
		pmap.move(tdx, tdy);

	}

	/**
	 * Рисует фон для карты( в клетку )
	 * 
	 * @return
	 */
	private Bitmap drawBackground() {
		// создание битмапа по размеру экрана
		Bitmap bitmap = Bitmap.createBitmap(320, MAP_HEIGHT, Config.RGB_565);
		Canvas cv = new Canvas(bitmap);
		// прорисовка фона
		Paint background = new Paint();
		background.setARGB(255, 128, 128, 128);
		cv.drawRect(0, 0, 320, MAP_HEIGHT, background);
		background.setAntiAlias(true);
		// установка цвета линий
		background.setColor(Color.WHITE);
		// продольные линии
		for (int i = 0; i < 320 / MoowMap.BCG_CELL_SIZE; i++) {
			cv.drawLine(MoowMap.BCG_CELL_SIZE * i, 0,
					MoowMap.BCG_CELL_SIZE * i, MAP_HEIGHT, background);
		}
		// поперечные линии
		for (int i = 0; i < MAP_HEIGHT / MoowMap.BCG_CELL_SIZE; i++) {
			cv.drawLine(0, MoowMap.BCG_CELL_SIZE * i, 320,
					MoowMap.BCG_CELL_SIZE * i, background);
		}
		return bitmap;
	}

	private synchronized void doDraw(Canvas canvas, Paint paint) {
		Bitmap tmpBitmap;
		canvas.drawBitmap(mapBg, 0, 0, paint);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				tmpBitmap = pmap.getCells()[i][j];
				if (tmpBitmap != null) {
					canvas.drawBitmap(tmpBitmap, (i) * 256
							+ pmap.globalOffset.x, (j) * 256
							+ pmap.globalOffset.y, paint);
				}
			}
		}
	}

	class Panel extends View {
		Paint paint;

		public Panel(Context context) {
			super(context);
			paint = new Paint();
			// setDrawingCacheEnabled(true);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			doDraw(canvas, paint);
		}

		/**
		 * Обработка касаний
		 */
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				inMove = false;
				pmap.nextMovePoint.set((int) event.getX(), (int) event.getY());
				Point pxx = new Point();
				pxx.set((int)event.getX(), (int)event.getY());
				
				moveHistory.push(pxx);
				break;
			case MotionEvent.ACTION_MOVE:
				lastMoveTime = System.currentTimeMillis();
				inMove = true;
				pmap.moveCoordinates(event.getX(), event.getY());
				Point p = new Point();
				p.set((int)event.getX(), (int)event.getY());
				moveHistory.push(p);
				break;
			case MotionEvent.ACTION_UP:
				if (inMove) {
					pmap.moveCoordinates(event.getX(), event.getY());
					quickHack();
				} else {
					if (dcDispatcher.process(event)) {
						pmap.zoomIn((int) event.getX(), (int) event.getY());
						updateZoomControls();
					}
				}
				long interval = System.currentTimeMillis() - lastMoveTime;
				if(interval <200){
					iengine =  new InertionEngine(moveHistory, interval);
					iengine.x = pmap.globalOffset.x;
					iengine.y = pmap.globalOffset.y;
					startInertion = true;
				}
				break;
			}

			return true;
		}

	}
	
		


	class CanvasUpdater implements Runnable {

		double stepX;
		
		double stepY;
		
		double counter  = 1;
		
		public void run() {
			while (running) {
				try {
					Thread.sleep(10);
					if(startInertion){
						processInertion();
					}
				} catch (InterruptedException ex) {
				}
				main.postInvalidate();
			}
		}

		private void processInertion(){
		
			if(counter <=0.2){
				startInertion = false;
				quickHack();
				return;
			}
			
			stepX += (iengine.dx/iengine.getInterval())*(iengine.getInterval()*counter);
			iengine.x +=  stepX;
			
			stepY +=  (iengine.dy/iengine.getInterval())*(iengine.getInterval()*counter); 
		    iengine.y+= stepY;
			
		    System.out.println(stepX + " * "  + stepY);
		    
			pmap.globalOffset.x = (int)iengine.x;
			pmap.globalOffset.y = (int)iengine.y;
			counter = counter - counter*0.1;
			//System.out.println(counter);
		}

		
	}

	class ZoomPanel extends RelativeLayout {

		private ZoomControls zoomControls;

		public ZoomPanel(Context context) {
			super(context);
			zoomControls = new ZoomControls(getContext());
			zoomControls.setOnZoomOutClickListener(new OnClickListener() {
				public void onClick(View v) {
					pmap.zoomOut();
					updateZoomControls();
				}
			});
			zoomControls.setOnZoomInClickListener(new OnClickListener() {
				public void onClick(View v) {
					pmap.zoomInCenter();
					updateZoomControls();
				}
			});
			addView(zoomControls);
			setPadding(80, 368, 0, 0);
		}

		/**
		 * Устанавливает кнопку увеличения детализации в активное/неактивное
		 * состояние
		 * 
		 * @param isEnabled
		 */
		public void setIsZoomInEnabled(boolean isEnabled) {
			zoomControls.setIsZoomInEnabled(isEnabled);

		}

		/**
		 * Устанавливает кнопку уменьшения детализации в активное/неактивное
		 * состояние
		 * 
		 * @param isEnabled
		 */
		public void setIsZoomOutEnabled(boolean isEnabled) {
			zoomControls.setIsZoomOutEnabled(isEnabled);
		}

	}

	public void run() {
		// TODO Auto-generated method stub
		
	}

}