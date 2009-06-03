package com.nevilon.moow.core.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.nevilon.moow.core.AbstractCommand;
import com.nevilon.moow.core.PhysicMap;
import com.nevilon.moow.core.RawTile;

public class MapControl extends RelativeLayout {

	/*
	 * Панель с картой
	 */
	private Panel main;

	/*
	 * Передвигается ли карта
	 */
	private boolean inMove = false;

	/*
	 * Время, когда карта перемещалась последний раз
	 */
	private long lastMoveTime = -1;

	/*
	 * Детектор двойного тача
	 */
	private DoubleClickHelper dcDispatcher = new DoubleClickHelper();

	/*
	 * Начальное значение зума
	 */
	private final static int START_ZOOM = 16;

	/*
	 * Перерисовка панели с картой
	 */
	private boolean running = true;

	/*
	 * Движок карты
	 */
	private PhysicMap pmap;

	/*
	 * Панель с зум-контролами
	 */
	private ZoomPanel zoomPanel;

	/*
	 * Фон
	 */
	private Bitmap mapBg;

	/*
	 * Координаты точек, история перемещения
	 */
	private List<Point> moveHistory = new ArrayList<Point>();

	/*
	 * Битмап для канввы
	 */
	private Bitmap cvBitmap;

	/*
	 * Канва для отображения карты
	 */
	private Canvas cv;

	/*
	 * Движок, реалищующий инерцию
	 */
	private InertionEngine iengine;

	// нужно ли запускать инерцию
	private boolean startInertion = false;

	/*
	 * Размер ячейки фона
	 */
	private final static int BCG_CELL_SIZE = 16;
	
	public MapControl(Context context, int width, int height) {
		super(context);

		mapBg = BitmapUtils.drawBackground(BCG_CELL_SIZE, height, width);
	
		// панель с картой
		main = new Panel(context);
		addView(main, 0, new ViewGroup.LayoutParams(width, height));

		//(new Thread(new CanvasUpdater())).start();

		zoomPanel = new ZoomPanel(context);
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

		addView(zoomPanel, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		 pmap = new PhysicMap(new RawTile(0, 0,
					MapControl.START_ZOOM), new AbstractCommand(){

						@Override
						public synchronized void execute() {
							if(main!=null){
								System.out.println("call");
								main.postInvalidate();
							} else {
								System.out.println("FFFF");
							}
						}
			 
		 });
		pmap.setHeight(height);
		pmap.setWidth(width);
			
	}

	public void changeMapSource(int sourceId){
		pmap.changeMapSource(sourceId);
	}
	
	public int getMapSourceId(){
		return pmap.getMapSourceId();
	}
	
	public PhysicMap getPhysicalMap(){
		return pmap;
	}
	
	
	private void quickHack() {
		int dx = 0, dy = 0;
		int tdx, tdy;
		if (pmap.globalOffset.x > 0) {
			dx = Math.round((pmap.globalOffset.x + pmap.getWidth()) / 256);
		} else {
			dx = Math.round((pmap.globalOffset.x) / 256);
		}

		if (pmap.globalOffset.y > 0) {
			dy = Math.round((pmap.globalOffset.y + pmap.getHeight()) / 256);
		} else {
			dy = Math.round(pmap.globalOffset.y / 256);

		}

		pmap.globalOffset.x = pmap.globalOffset.x - dx * 256;
		pmap.globalOffset.y = pmap.globalOffset.y - dy * 256;

		tdx = dx;
		tdy = dy;

		if (pmap.globalOffset.x > 0) {
			dx = Math.round((pmap.globalOffset.x + pmap.getWidth()) / 256);
		} else {
			dx = Math.round((pmap.globalOffset.x) / 256);
		}

		if (pmap.globalOffset.y > 0) {
			dy = (int) Math.round((pmap.globalOffset.y + pmap.getHeight()) / 256);
		} else {
			dy = (int) Math.round(pmap.globalOffset.y / 256);

		}

		pmap.globalOffset.x = pmap.globalOffset.x - dx * 256;
		pmap.globalOffset.y = pmap.globalOffset.y - dy * 256;

		tdx += dx;
		tdy += dy;
		if(!(tdx==0 && tdy==0)){
			pmap.move(tdx, tdy);
		}

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

	private void doDraw(Canvas canvas, Paint paint) {
	
		if (cvBitmap == null) {
			cvBitmap = Bitmap.createBitmap(768, 768, Bitmap.Config.RGB_565);
		}
		

		if (cv == null) {
			cv = new Canvas();
			canvas = cv;
			canvas.setBitmap(cvBitmap);
		}

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
	

	private void addPointToHistory(float x, float y) {
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
		protected void onAttachedToWindow(){
			super.onAttachedToWindow();
			postInvalidateDelayed(500);
			
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
				// moveHistory.clear();
				pmap.nextMovePoint.set((int) event.getX(), (int) event.getY());
				lastMoveTime = 0;
				// addPointToHistory(event.getX(), event.getY());
				break;
			case MotionEvent.ACTION_MOVE:
				lastMoveTime = System.currentTimeMillis();
				inMove = true;
				pmap.moveCoordinates(event.getX(), event.getY());
				// addPointToHistory(event.getX(), event.getY());
				break;
			case MotionEvent.ACTION_UP:
				// if(startInertion){
				// stopInertion();
				// }
				// long interval = System.currentTimeMillis() - lastMoveTime;
				// if (interval < 100 && !startInertion) {
				// lastMoveTime = 0;
				// startInertion(moveHistory, interval);
				// return false;
				// }

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

	private void startInertion(List<Point> moveHistory, long interval) {
		iengine = new InertionEngine(moveHistory, interval);
		startInertion = true;
	}

	private void stopInertion() {
		startInertion = false;
	}

}
