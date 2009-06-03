package com.nevilon.moow;

import java.util.ArrayList;
import java.util.List;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

import com.nevilon.moow.core.PhysicMap;
import com.nevilon.moow.core.RawTile;
import com.nevilon.moow.core.ui.DoubleClickHelper;
import com.nevilon.moow.core.ui.InertionEngine;
import com.nevilon.moow.core.ui.ZoomPanel;

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
	
	private List<Point> moveHistory = new ArrayList<Point>();

	private InertionEngine iengine;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		main = new Panel(this);
		setContentView(main, new ViewGroup.LayoutParams(320, MAP_HEIGHT));
		(new Thread(new CanvasUpdater())).start();
		
		zoomPanel = new ZoomPanel(this);
		zoomPanel.setOnZoomOutClickListener(new OnClickListener() {
			public void onClick(View v) {
				pmap.zoomOut();
				quickHack();	
				updateZoomControls();
			}
		});
		
		zoomPanel.setOnZoomInClickListener(new OnClickListener() {
			public void onClick(View v) {
				pmap.zoomInCenter();
				quickHack();
				updateZoomControls();
			}
		});
		
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
			dy = Math.round((pmap.globalOffset.y + MAP_HEIGHT) / 256);
		} else {
			dy = Math.round(pmap.globalOffset.y / 256);

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

	private  void doDraw(Canvas canvas, Paint paint) {
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

	
	private void addPointToHistory(float x, float y){
		Point tmpPoint = new Point();
		tmpPoint.set((int) x, (int) y);
		moveHistory.add(tmpPoint);
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

		/**
		 * Обработка касаний
		 */
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				inMove = false;
				moveHistory.clear();
				pmap.nextMovePoint.set((int) event.getX(), (int) event.getY());
				lastMoveTime = 0;
				addPointToHistory(event.getX(), event.getY());
				break;
			case MotionEvent.ACTION_MOVE:
				lastMoveTime = System.currentTimeMillis();
				inMove = true;
				pmap.moveCoordinates(event.getX(), event.getY());
				addPointToHistory(event.getX(), event.getY());
				break;
			case MotionEvent.ACTION_UP:
				//if(startInertion){
				//	stopInertion();
				//}
				//long interval = System.currentTimeMillis() - lastMoveTime;
				//if (interval < 100 && !startInertion) {
				//	lastMoveTime = 0;
				//	startInertion(moveHistory, interval);				
				//	return false;
				//}

				if (inMove) {
					quickHack();					
					pmap.moveCoordinates(event.getX(), event.getY());
				} else {
					if (dcDispatcher.process(event)) {
						pmap.zoomIn((int) event.getX(), (int) event.getY());
						updateZoomControls();
					}
				}

				break;
			}

			return true;
		}

	}
	
	private void startInertion(List<Point> moveHistory, long interval){
		iengine = new InertionEngine(moveHistory, interval);
		startInertion = true;
	}
	
	private void stopInertion(){
		startInertion = false;
	}

	class CanvasUpdater implements Runnable {

		private static final int UPDATE_INTERVAL = 35;

		int step = 0;

		int d = 3;

		public void run() {
			while (running) {
				try {
					Thread.sleep(CanvasUpdater.UPDATE_INTERVAL);
					//if (startInertion) {
						//processInertion();
					//}
					main.postInvalidate();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}

		private void processInertion() {
			if (step % 10 == 0) {
				iengine.reduceSpeed();
			}

			if (step > iengine.step/7 || d < 0) {
				startInertion = false;
				//quickHack();
				step = 0;
				return;
			}

			step++;

			pmap.globalOffset.x += iengine.dx;
			pmap.globalOffset.y += iengine.dy;

		}

	}

	
}