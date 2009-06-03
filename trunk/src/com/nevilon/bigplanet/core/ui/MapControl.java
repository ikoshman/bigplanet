package com.nevilon.bigplanet.core.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Bitmap.CompressFormat;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
	
	private int SMOOT_ZOOM_INTERVAL = 20;
	
	/*
	 * Панель с картой
	 */
	private Panel main;
	
	Canvas cs;

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
	
	private Bitmap cb = null;

	/*
	 * Размер ячейки фона
	 */
	private final static int BCG_CELL_SIZE = 16;

	private OnMapLongClickListener onMapLongClickListener;

	private MarkerManager markerManager;

	public static Bitmap CELL_BACKGROUND = BitmapUtils.drawBackground(
			BCG_CELL_SIZE, TILE_SIZE, TILE_SIZE);

	public static Bitmap EMPTY_BACKGROUND = BitmapUtils
			.drawEmptyBackground(TILE_SIZE);

	public Bitmap PLACE_MARKER = BitmapFactory.decodeResource(getResources(),
			R.drawable.marker);

	private Point scalePoint = new Point();

	public Handler h;

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
		scalePoint.set(width / 2, height / 2);
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
		if(main!=null){
			removeView(main);
		}
		//removeAllViews();
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
		h = new Handler() {
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
					scalePoint.set(pmap.getWidth() / 2, pmap.getHeight() / 2);
					new Thread() {

						@Override
						public void run() {
							while (!(pmap.scaleFactor <= 0.5)) {
								
								try {
									Thread.sleep(SMOOT_ZOOM_INTERVAL);
									pmap.scaleFactor -= 0.1f;
									postInvalidate();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							pmap.zoomOut();
							h.sendEmptyMessage(0);
						}

					}.start();
				}
			});

			// обработчик увеличения
			zoomPanel.setOnZoomInClickListener(new OnClickListener() {
				public void onClick(View v) {
					scalePoint.set(pmap.getWidth() / 2, pmap.getHeight() / 2);
					new Thread() {

						@Override
						public void run() {
							while (pmap.scaleFactor <= 2) {
								try {
									Thread.sleep(SMOOT_ZOOM_INTERVAL);
									pmap.scaleFactor += 0.1f;
									postInvalidate();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							pmap.zoomInCenter();
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
		pmap.quickHack();
	}

	private synchronized void updateScreen() {
		if (main != null) {
			main.postInvalidate();
		}
	}



	/**
	 * Устанавливает состояние zoomIn/zoomOut контролов в зависимости от уровня
	 * зума
	 */
	public void updateZoomControls() {
		pmap.getTileResolver().clearCache();
		System.gc();
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
	private synchronized void doDraw(Canvas c, Paint paint) {
		if(cb==null || cb.getHeight()!=pmap.getHeight()){
			cs = new Canvas();
			cb = Bitmap.createBitmap(pmap.getWidth(),
					pmap.getHeight(), Bitmap.Config.RGB_565); 	
		 	cs.setBitmap(cb); 
		}
		 	Bitmap tmpBitmap;
			for (int i = 0; i < 7; i++) {
				for (int j = 0; j < 7; j++) {
					if ((i > 1 && i < 5) && ((j > 1 && j < 5))) {
						tmpBitmap = pmap.getCell(i - 2, j - 2);
						if (tmpBitmap != null) {
							isNew = false;
							cs.drawBitmap(tmpBitmap, (i - 2) * TILE_SIZE
									+ pmap.getGlobalOffset().x, (j - 2)
									* TILE_SIZE + pmap.getGlobalOffset().y,
									paint);
						}
					} else {
						if (pmap.scaleFactor == 1) {
							cs.drawBitmap(CELL_BACKGROUND, (i - 2)
									* TILE_SIZE + pmap.getGlobalOffset().x,
									(j - 2) * TILE_SIZE
											+ pmap.getGlobalOffset().y, paint);
						} else {
							cs.drawBitmap(EMPTY_BACKGROUND, (i - 2)
									* TILE_SIZE + pmap.getGlobalOffset().x,
									(j - 2) * TILE_SIZE
											+ pmap.getGlobalOffset().y, paint);

						}
					}
				}
			}
		
		

		if (pmap.scaleFactor == 1) {
			// отрисовка маркеров
			for (int i = 0; i < 7; i++) {
				for (int j = 0; j < 7; j++) {
					if ((i > 1 && i < 5) && ((j > 1 && j < 5))) {
						RawTile tile = pmap.getDefaultTile();
						int z = getPhysicalMap().getZoomLevel();
						int tileX = PhysicMap.normalize(tile.x + (i - 2), z);
						int tileY = PhysicMap.normalize(tile.y + (j - 2), z);
						List<Marker> markers = markerManager.getMarkers(tileX,
								tileY, z);
						for (Marker marker : markers) {
							cs.drawBitmap(marker.getMarkerImage()
									.getImage(), (i - 2) * TILE_SIZE
									+ pmap.getGlobalOffset().x
									+ (int) marker.getOffset().x
									- marker.getMarkerImage().getOffsetX(),
									(j - 2)
											* TILE_SIZE
											+ pmap.getGlobalOffset().y
											+ (int) marker.getOffset().y
											- marker.getMarkerImage()
													.getOffsetY(), paint);
						}

					}
				}
			}
		}

		Matrix matr = new Matrix();
		matr.postScale((float) pmap.scaleFactor, (float) pmap.scaleFactor,
				scalePoint.x, scalePoint.y);
		c.drawColor(BitmapUtils.BACKGROUND_COLOR);
		c.drawBitmap(cb, matr, paint);
		//canvas.restore();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		new Thread() {
			@Override
			public void run() {
				while (isNew) {
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
		public  boolean onTouchEvent(final MotionEvent event) {
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
						scalePoint.set((int) event.getX(), (int) event.getY());
						float sx = (pmap.getWidth() / 2 - event.getX());
						float sy = (pmap.getHeight() / 2 - event.getY());
						final float dx = (sx / (1f / 0.2f));
						final float dy = (sy / (1f / 0.2f));
						
						new Thread() {

							@Override
							public void run() {
								float tx = 0;
								float ty = 0;
								int scaleX = scalePoint.x;
								int scaleY = scalePoint.y;
								
								float ox = pmap.getGlobalOffset().x;
								float oy = pmap.getGlobalOffset().y;
							    
								while (pmap.scaleFactor <= 2) {
									try {
										Thread.sleep(SMOOT_ZOOM_INTERVAL*2);
										
										pmap.scaleFactor += 0.2f;

										tx += dx;
										ty += dy;
										scalePoint.set((int) (scaleX + tx),
												(int) (scaleY + ty));
										ox += dx;
										oy += dy;
										
										pmap.getGlobalOffset().x = (int) ox;
										pmap.getGlobalOffset().y = (int) oy;
										postInvalidate();

										
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}

								try{
									Thread.sleep(200);
									pmap.zoomInCenter();
									h.sendEmptyMessage(0);
								} catch(InterruptedException e){
									e.printStackTrace();
								}
								 
							}

						}.start();
					} else {
						if (onMapLongClickListener != null) {
							onMapLongClickListener.onMapLongClick(0, 0);
						}
					}
				} else {
					if (inMove) {
						pmap.moveCoordinates(event.getX(), event.getY());
						pmap.quickHack();
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
