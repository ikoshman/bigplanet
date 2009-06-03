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
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

import com.nevilon.moow.core.InertionEngine;
import com.nevilon.moow.core.PhysicMap;
import com.nevilon.moow.core.RawTile;
import com.nevilon.moow.core.ui.DoubleClickHelper;
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
		zoomPanel.setOnZoomOutClickListener(new OnClickListener() {
			public void onClick(View v) {
				pmap.zoomOut();
				updateZoomControls();
			}
		});
		
		zoomPanel.setOnZoomInClickListener(new OnClickListener() {
			public void onClick(View v) {
				pmap.zoomInCenter();
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
				pxx.set((int) event.getX(), (int) event.getY());
				lastMoveTime = 0;
				moveHistory.push(pxx);
				break;
			case MotionEvent.ACTION_MOVE:
				lastMoveTime = System.currentTimeMillis();
				inMove = true;
				pmap.moveCoordinates(event.getX(), event.getY());
				Point p = new Point();
				p.set((int) event.getX(), (int) event.getY());
				System.out.println(p);
				moveHistory.push(p);
				break;
			case MotionEvent.ACTION_UP:
				System.out.println("up " + event.getX() + " " + event.getY());
				long interval = System.currentTimeMillis() - lastMoveTime;
				System.out.println(interval);
				if (interval < 1000) {
					iengine = new InertionEngine(moveHistory, interval);
					lastMoveTime = 0;
					// iengine.x = pmap.globalOffset.x;
					// iengine.y = pmap.globalOffset.y;
					startInertion = true;
					return false;
				}

				if (inMove) {
					pmap.moveCoordinates(event.getX(), event.getY());
					quickHack();
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

	class CanvasUpdater implements Runnable {

		private static final int UPDATE_INTERVAL = 40;

		int step = 0;

		int d = 3;

		public void run() {
			while (running) {
				try {
					Thread.sleep(CanvasUpdater.UPDATE_INTERVAL);
					if (startInertion) {
						processInertion();
					}
					main.postInvalidate();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}

		private void processInertion() {
			if (step % 25 == 0) {
				d--;
			}

			if (step > 100 || d < 0) {
				startInertion = false;
				quickHack();
				step = 0;
				d = 3;
				return;
			}

			step++;

			pmap.globalOffset.x += d;
			pmap.globalOffset.y += d;

		}

	}

	
}