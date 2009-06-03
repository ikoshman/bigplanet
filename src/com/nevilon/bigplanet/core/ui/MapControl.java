package com.nevilon.bigplanet.core.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.nevilon.bigplanet.core.AbstractCommand;
import com.nevilon.bigplanet.core.PhysicMap;
import com.nevilon.bigplanet.core.RawTile;

/**
 * Виджет, реализующий карту
 * 
 * @author hudvin
 * 
 */
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
	 * Детектор двойного тача
	 */
	private DoubleClickDetector dcDetector = new DoubleClickDetector();

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
	 * Битмап для канввы
	 */
	private Bitmap cvBitmap;

	/*
	 * Канва для отображения карты
	 */
	private Canvas cv;

	/*
	 * Размер ячейки фона
	 */
	private final static int BCG_CELL_SIZE = 16;

	/**
	 * Конструктор
	 * 
	 * @param context
	 * @param width
	 * @param height
	 * @param startTile
	 */
	public MapControl(Context context, int width, int height, RawTile startTile) {
		super(context);
		buildView(width, height, startTile);

	}

	/**
	 * Устанавливает размеры карты и дочерних контролов
	 * 
	 * @param width
	 * @param height
	 */
	public void setSize(int width, int height) {
		buildView(width, height, pmap.getDefaultTile());
	}

	/**
	 * Возвращает движок карты
	 * 
	 * @return
	 */
	public PhysicMap getPhysicalMap() {
		return pmap;
	}

	/**
	 * Строит виджет, устанавливает обработчики, размеры и др.
	 * 
	 * @param width
	 * @param height
	 * @param startTile
	 */
	private void buildView(int width, int height, RawTile startTile) {
		// создание фона
		mapBg = BitmapUtils.drawBackground(BCG_CELL_SIZE, height, width);
		// создание панели с картой
		main = new Panel(this.getContext());
		addView(main, 0, new ViewGroup.LayoutParams(width, height));
		// создание зум-панели
		if (zoomPanel == null) { // если не создана раньше
			zoomPanel = new ZoomPanel(this.getContext());
			// обработчик уменьшения
			zoomPanel.setOnZoomOutClickListener(new OnClickListener() {
				public void onClick(View v) {
					pmap.zoomOut();
					quickHack();
					updateZoomControls();
				}
			});
			// обработчик увеличения
			zoomPanel.setOnZoomInClickListener(new OnClickListener() {
				public void onClick(View v) {
					pmap.zoomInCenter();
					quickHack();
					updateZoomControls();
				}
			});

			addView(zoomPanel, new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));

		}
		zoomPanel.setPadding((width - 160) / 2, height - 112, 0, 0);
		
		if (pmap == null) { // если не был создан раньше
			pmap = new PhysicMap(startTile, new AbstractCommand() {

				/**
				 * Callback, выполняющий перерисовку карты по запросу
				 */
				@Override
				public synchronized void execute() {
					if (main != null) {
						main.postInvalidate();
					}
				}

			});
		}
		pmap.setHeight(height);
		pmap.setWidth(width);

	}

	private void quickHack() {
		int dx = 0, dy = 0;
		int tdx, tdy;
		Point globalOffset = pmap.getGlobalOffset();
		if (globalOffset.x > 0) {
			dx = Math.round((globalOffset.x + pmap.getWidth()) / 256);
		} else {
			dx = Math.round((globalOffset.x) / 256);
		}

		if (globalOffset.y > 0) {
			dy = Math.round((globalOffset.y + pmap.getHeight()) / 256);
		} else {
			dy = Math.round(globalOffset.y / 256);

		}

		globalOffset.x = globalOffset.x - dx * 256;
		globalOffset.y = globalOffset.y - dy * 256;

		tdx = dx;
		tdy = dy;

		if (globalOffset.x > 0) {
			dx = Math.round((globalOffset.x + pmap.getWidth()) / 256);
		} else {
			dx = Math.round((globalOffset.x) / 256);
		}

		if (globalOffset.y > 0) {
			dy = (int) Math.round((globalOffset.y + pmap.getHeight()) / 256);
		} else {
			dy = (int) Math.round(globalOffset.y / 256);

		}

		globalOffset.x = globalOffset.x - dx * 256;
		globalOffset.y = globalOffset.y - dy * 256;

		tdx += dx;
		tdy += dy;
		if (!(tdx == 0 && tdy == 0)) {
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

	/**
	 * Перерисовывает карту
	 * @param canvas
	 * @param paint
	 */
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
		// отрисовка тайлов
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				tmpBitmap = pmap.getCells()[i][j];
				if (tmpBitmap != null) {
					canvas.drawBitmap(tmpBitmap, (i) * 256
							+ pmap.getGlobalOffset().x, (j) * 256
							+ pmap.getGlobalOffset().y, paint);
				}
			}
		}

	}

	/**
	 * Панель, на которую выводится карта
	 * @author hudvin
	 *
	 */
	class Panel extends View {
		Paint paint;

		public Panel(Context context) {
			super(context);
			paint = new Paint();
		}

		@Override
		protected void onAttachedToWindow() {
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
				pmap.getNextMovePoint().set((int) event.getX(),
						(int) event.getY());
				break;
			case MotionEvent.ACTION_MOVE:
				inMove = true;
				pmap.moveCoordinates(event.getX(), event.getY());
				break;
			case MotionEvent.ACTION_UP:
				if (inMove) {
					pmap.moveCoordinates(event.getX(), event.getY());
					quickHack();
				} //else {
					if (dcDetector.process(event)) {
						pmap.zoomIn((int) event.getX(), (int) event.getY());
						updateZoomControls();
					}
			//	}

				break;
			}

			return true;
		}

	}

}
