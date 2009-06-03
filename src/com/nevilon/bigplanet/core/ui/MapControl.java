package com.nevilon.bigplanet.core.ui;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;

import com.nevilon.bigplanet.R;
import com.nevilon.bigplanet.core.AbstractCommand;
import com.nevilon.bigplanet.core.MarkerManager;
import com.nevilon.bigplanet.core.PhysicMap;
import com.nevilon.bigplanet.core.RawTile;
import com.nevilon.bigplanet.core.MarkerManager.Marker;

/**
 * Виджет, реализующий карту
 * 
 * @author hudvin
 * 
 */
public class MapControl extends RelativeLayout {

	private static final int TILE_SIZE = 256;

	public static final int ZOOM_MODE = 0;

	public static final int SELECT_MODE = 1;

	private int mapMode = ZOOM_MODE;

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

	
	private boolean isNew = true;
	
	/*
	 * Размер ячейки фона
	 */
	private final static int BCG_CELL_SIZE = 16;

	private OnMapLongClickListener onMapLongClickListener;

	private MarkerManager markerManager;

	public static Bitmap CELL_BACKGROUND = BitmapUtils.drawBackground(
			BCG_CELL_SIZE, TILE_SIZE, TILE_SIZE);

	public Bitmap PLACE_MARKER = BitmapFactory.decodeResource(getResources(),
			R.drawable.marker);

	/**
	 * Конструктор
	 * 
	 * @param context
	 * @param width
	 * @param height
	 * @param startTile
	 */
	public MapControl(Context context, int width, int height,
			RawTile startTile, MarkerManager markerManager) {
		super(context);
		this.markerManager = markerManager;
		buildView(width, height, startTile);
	}

	public int getMapMode() {
		return mapMode;
	}

	/**
	 * Устанавливает режим карты и состояние зум-контролов(выбор объекта для
	 * добавления в закладки либо навигация)
	 * 
	 * @param mapMode
	 */
	public void setMapMode(int mapMode) {
		this.mapMode = mapMode;
		updateZoomControls();
	}

	public void setOnMapLongClickListener(
			OnMapLongClickListener onMapLongClickListener) {
		this.onMapLongClickListener = onMapLongClickListener;
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

	public void goTo(int x, int y, int z, int offsetX, int offsetY) {
		getPhysicalMap().goTo(x, y, z, offsetX, offsetY);
		updateZoomControls();
		updateScreen();
	}

	/**
	 * Строит виджет, устанавливает обработчики, размеры и др.
	 * 
	 * @param width
	 * @param height
	 * @param startTile
	 */
	private void buildView(int width, int height, RawTile startTile) {
		final Handler h = new Handler(){
            @Override
            public void handleMessage(Message msg) {
               updateZoomControls();
            }
        }; 
		
		
		// создание панели с картой
		main = new Panel(this.getContext());
		addView(main, 0, new ViewGroup.LayoutParams(width, height));
		// создание зум-панели
		if (zoomPanel == null) { // если не создана раньше
			zoomPanel = new ZoomPanel(this.getContext());
			// обработчик уменьшения
			zoomPanel.setOnZoomOutClickListener(new OnClickListener() {
				public void onClick(View v) {
                       new Thread(){
						
						@Override
						public void run(){
							while(!(pmap.scaleFactor<=0.5)){
								try {
									Thread.sleep(20);
									pmap.scaleFactor-=0.05f;
									postInvalidate();
								//	quickHack();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							pmap.zoomOut();
							
							//quickHack();
							h.sendEmptyMessage(0);
						}
						
					}.start();
					
					//quickHack();
					//updateZoomControls();
				}
			});
			
			
			// обработчик увеличения
			zoomPanel.setOnZoomInClickListener(new OnClickListener() {
				public void onClick(View v) {
					new Thread(){
						
						@Override
						public void run(){
							while(pmap.scaleFactor<=2){
								try {
									Thread.sleep(20);
									pmap.scaleFactor+=0.05f;
									postInvalidate();
								//	quickHack();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							pmap.zoomInCenter();
							//quickHack();
							h.sendEmptyMessage(0);
						}
						
					}.start();
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
				public void execute() {
					updateScreen();
				}

			});
		}
		pmap.setHeight(height);
		pmap.setWidth(width);

	}

	private synchronized void updateScreen() {
		//scaleFactor=1;
		if (main != null) {
			main.postInvalidate();
		}
	}

	private void quickHack() {
		int dx = 0, dy = 0;
		int tdx=0, tdy=0;
		Point globalOffset = pmap.getGlobalOffset();
		
		
		for(int i=0;i<2;i++){
		if (globalOffset.x > 0) {
			dx = Math.round((globalOffset.x + pmap.getWidth()) / TILE_SIZE);
		} else {
			dx = Math.round((globalOffset.x) / TILE_SIZE);
		}

		if (globalOffset.y > 0) {
			dy = Math.round((globalOffset.y + pmap.getHeight()) / TILE_SIZE);
		} else {
			dy = Math.round(globalOffset.y / TILE_SIZE);
		}

		globalOffset.x = globalOffset.x - dx * TILE_SIZE;
		globalOffset.y = globalOffset.y - dy * TILE_SIZE;
		

		tdx+=dx;
		tdy+=dy;
		}
		
		if (!(tdx == 0 && tdy == 0)) {
			pmap.move(tdx, tdy);
		}

	}

	/**
	 * Устанавливает состояние zoomIn/zoomOut контролов в зависимости от уровня
	 * зума
	 */
	public void updateZoomControls() {
		markerManager.updateAll(pmap.getZoomLevel());
		int zoomLevel = pmap.getZoomLevel();
		if (getMapMode() == MapControl.SELECT_MODE) {
			zoomPanel.setVisibility(View.INVISIBLE);
		} else {
			zoomPanel.setVisibility(View.VISIBLE);
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
	}

	/**
	 * Перерисовывает карту
	 * 
	 * @param canvas
	 * @param paint
	 */
	private synchronized void doDraw(Canvas canvas, Paint paint) {
		Bitmap tmpBitmap;
		Matrix matr = new Matrix();
		
		matr.postScale((float)pmap.scaleFactor, (float)pmap.scaleFactor,getWidth()/2,getHeight()/2);
		canvas.setMatrix(matr);
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				if ((i > 1 && i < 5) && ((j > 1 && j < 5))) {
					tmpBitmap = pmap.getCell(i - 2, j - 2);
					if (tmpBitmap != null) {
						isNew = false;
						canvas.drawBitmap(tmpBitmap, (i - 2) * TILE_SIZE
								+ pmap.getGlobalOffset().x, (j - 2) * TILE_SIZE
								+ pmap.getGlobalOffset().y, paint);
					} 
				} else {
					canvas.drawBitmap(CELL_BACKGROUND, (i - 2) * TILE_SIZE
							+ pmap.getGlobalOffset().x, (j - 2) * TILE_SIZE
							+ pmap.getGlobalOffset().y, paint);
				}
			}
		}
		// отрисовка маркеров
	/*	for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				if ((i > 1 && i < 5) && ((j > 1 && j < 5))) {
					RawTile tile = pmap.getDefaultTile();
					int z = getPhysicalMap().getZoomLevel();
					int tileX = PhysicMap.normalize(tile.x + (i - 2), z);
					int tileY = PhysicMap.normalize(tile.y + (j - 2), z);
					List<Marker> markers = markerManager.getMarkers(tileX,
							tileY, z);
					for (Marker marker : markers) {
						canvas.drawBitmap(marker.getMarkerImage().getImage(), (i - 2) * TILE_SIZE
								+ pmap.getGlobalOffset().x
								+ (int) marker.getOffset().x - marker.getMarkerImage().getOffsetX(), (j - 2)
								* TILE_SIZE + pmap.getGlobalOffset().y
								+ (int) marker.getOffset().y - marker.getMarkerImage().getOffsetY(), paint);
					}

				}
			}
		}
		*/
		canvas.restore();
	}
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		new Thread(){
			@Override
			public void run(){
				while(isNew){
					try {
						Thread.sleep(200);
						postInvalidate();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		}.start();
	}

	/**
	 * Панель, на которую выводится карта
	 * 
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
				if (dcDetector.process(event)) {
					if (mapMode == MapControl.ZOOM_MODE) {
						pmap.zoomIn((int) event.getX(), (int) event.getY());
						updateZoomControls();
					} else {
						if (onMapLongClickListener != null) {
							onMapLongClickListener.onMapLongClick(0, 0);
						}
					}
				} else {
					if (inMove) {
						pmap.moveCoordinates(event.getX(), event.getY());
						quickHack();
					}
				}
				break;
			}

			return true;
		}

	}

	public void setMapSource(int sourceId) {
		getPhysicalMap().getTileResolver().setMapSource(sourceId);
		getPhysicalMap().reloadTiles();
		updateScreen();
	}

}
