package com.nevilon.bigplanet.core;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Point;

import com.nevilon.bigplanet.core.geoutils.GeoUtils;
import com.nevilon.bigplanet.core.storage.BitmapCacheWrapper;
import com.nevilon.bigplanet.core.ui.MapControl;
import com.nevilon.bigplanet.core.ui.SmoothZoomEngine;

public class PhysicMap {

	private static final int TILE_SIZE = 256;

	private static Random random = new Random();

	private TileResolver tileResolver;

	private Bitmap[][] cells = new Bitmap[3][3];

	public RawTile defTile;

	public float scaleFactor = 1.00f;

	public static int zoom;

	private Point globalOffset = new Point();

	private Point previousMovePoint = new Point();

	private Point nextMovePoint = new Point();

	private int width;

	private int height;

	private int correctionX;

	private int correctionY;

	public int inZoom = 0;
	
	/*
	 * Передвигается ли карта
	 */
	public boolean inMove = false;

	private AbstractCommand updateScreenCommand;

	public PhysicMap(RawTile defTile, AbstractCommand updateScreenCommand) {
		this.defTile = defTile;
		this.updateScreenCommand = updateScreenCommand;
		this.zoom = defTile.z;
		tileResolver = new TileResolver(this);
		loadCells(defTile);
	}

	public Bitmap getCell(int x, int y) {
		return cells[x][y];
	}

	private void setBitmap(Bitmap bmp, int x, int y) {
		cells[x][y] = bmp;
	}

	public Point getNextMovePoint() {
		return this.nextMovePoint;
	}

	public Point getGlobalOffset() {
		return this.globalOffset;
	}

	public void setGlobalOffset(Point globalOffset) {
		this.globalOffset = globalOffset;
	}

	public RawTile getDefaultTile() {
		return this.defTile;
	}

	public int getZoomLevel() {
		return this.zoom;
	}

	/**
	 * Callback method
	 * 
	 * @param bitmap
	 * @param tile
	 */
	public synchronized void update(Bitmap bitmap, RawTile tile) {
		if(!inMove){
		int dx = tile.x - defTile.x;
		int dy = tile.y - defTile.y;
		if (dx <= 2 && dy <= 2 && tile.z == defTile.z) {
			if (dx >= 0 && dy >= 0) {
				if (bitmap == null) {
					System.out.println("null");
				} else {

					try {
						setBitmap(bitmap, dx, dy);
						updateMap();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	  }
	}

	
	public void loadFromCache(){
		Bitmap tmpBitmap;
		
		//int x = defTile.x;
		//int y = defTile.y;
		for(int i=2;i<5;i++){
			for(int j=2;j<5;j++){
				int tx = i-2;
				int ty = j-2;
				RawTile tile = new RawTile(defTile.x+tx,defTile.y+ty, defTile.z, defTile.s);
				
				tmpBitmap = tileResolver.loadTile(tile);
				if(tmpBitmap!=null){
					//System.out.println(tile);
					cells[tx][ty] = tmpBitmap;	
				}
				
			}
		}
		
		/*
		 * 
		 * 
		 * int x, y;
				x = (tile.x + i);
				y = (tile.y + j);
				if (scaleFactor == 1) {
					setBitmap(MapControl.CELL_BACKGROUND, i, j);
				}
				// setBitmap(null, i, j);
				tileResolver.getTile(new RawTile(x, y, zoom, tileResolver
						.getMapSourceId()));
		 * 
		 */
		/*
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				y = defTile.y;
				if ((i > 1 && i < 5) && ((j > 1 && j < 5))) {
					RawTile tile = new RawTile(defTile.x,defTile.y, defTile.z, defTile.s);
					tile.x = x;
					tile.y = y;
					tmpBitmap = tileResolver.loadTile(tile);
					if(tmpBitmap!=null){
						System.out.println((x-defTile.x) + " " + (y-defTile.y));
						cells[x-defTile.x][y-defTile.y] = tmpBitmap;	
					}
					
				}
				x++;
			}
			y++;
		}
		*/
	}
	
	public void move(int dx, int dy) {
		System.gc();
		reload(defTile.x - dx, defTile.y - dy, defTile.z);
	}

	public void goTo(int x, int y, int z, int offsetX, int offsetY) {
		int fullX = x * 256 + offsetX;
		int fullY = y * 256 + offsetY;
		// коориданаты углового тайла
		int tx = fullX - getWidth() / 2;
		int ty = fullY - getHeight() / 2;
		globalOffset.x = -(tx - Math.round(tx / 256) * 256);
		globalOffset.y = -(ty - Math.round(ty / 256) * 256);
		reload(tx / 256, ty / 256, z);
	}

	public void zoom(int x, int y, int z) {
		tileResolver.gcCache();
		reload(x, y, z);
	}

	/**
	 * Уменьшение уровня детализации
	 */
	public void zoomOut(int n) {
		if ((zoom) < 16) {
			int currentZoomX = (int) (getDefaultTile().x * TILE_SIZE
					- globalOffset.x + getWidth() / 2);
			int currentZoomY = (int) (getDefaultTile().y * TILE_SIZE
					- globalOffset.y + getHeight() / 2);

			// получение координат точки предудущем уровне
			int nextZoomX = currentZoomX / 2;
			int nextZoomY = currentZoomY / 2;

			// получение координат угла экрана на новом уровне
			nextZoomX = nextZoomX - getWidth() / 2;
			nextZoomY = nextZoomY - getHeight() / 2;

			// получение углового тайла
			int tileX = (nextZoomX / TILE_SIZE);
			int tileY = nextZoomY / TILE_SIZE;

			// отступ всегда один - точка должна находится в центре экрана
			correctionX = -(nextZoomX - tileX * TILE_SIZE);
			correctionY = -(nextZoomY - tileY * TILE_SIZE);

			inZoom = -1;
			zoom++;
			zoom(tileX, tileY, zoom);

		}

	}

	public void zoomS(double dz){
		int offsetX = getWidth() / 2;
		int offsetY = getHeight() / 2;
		int zoomTo=Utils.getZoomLevel(dz);
		if(dz>1){
		
			int currentZoomX = (int) (getDefaultTile().x * TILE_SIZE
					- globalOffset.x + offsetX);
			int currentZoomY = (int) (getDefaultTile().y * TILE_SIZE
					- globalOffset.y + offsetY);

			// получение координат точки на новом уровне
			int nextZoomX = (int)(currentZoomX * dz);
			int nextZoomY = (int)(currentZoomY * dz);

			// получение координат угла экрана на новом уровне
			nextZoomX = nextZoomX - offsetX;
			nextZoomY = nextZoomY - offsetY;

			// получение углового тайла
			int tileX = nextZoomX / TILE_SIZE;
			int tileY = nextZoomY / TILE_SIZE;

			// отступ всегда один - точка должна находится в центре экрана
			correctionX = nextZoomX - tileX * TILE_SIZE;
			correctionY = nextZoomY - tileY * TILE_SIZE;

			inZoom = 1;
			zoom=zoom-zoomTo;
			zoom(tileX, tileY, zoom);	
		} else {
			
			int currentZoomX = (int) (getDefaultTile().x * TILE_SIZE
					- globalOffset.x +offsetX);
			int currentZoomY = (int) (getDefaultTile().y * TILE_SIZE
					- globalOffset.y + offsetY);

			// получение координат точки предудущем уровне
			int nextZoomX = (int) (currentZoomX / (1/dz));
			int nextZoomY = (int) (currentZoomY / (1/dz));

			// получение координат угла экрана на новом уровне
			nextZoomX = nextZoomX - offsetX;
			nextZoomY = nextZoomY - offsetY;

			// получение углового тайла
			int tileX = (nextZoomX / TILE_SIZE);
			int tileY = nextZoomY / TILE_SIZE;

			// отступ всегда один - точка должна находится в центре экрана
			correctionX = -(nextZoomX - tileX * TILE_SIZE);
			correctionY = -(nextZoomY - tileY * TILE_SIZE);

			inZoom = -1;
			zoom=zoom+zoomTo;
			zoom(tileX, tileY, zoom);

			
		}
	}
	
	/**
	 * Увеличение уровня детализации с центрированием
	 */
	public void zoomInCenter() {
		zoomIn(getWidth() / 2, getHeight() / 2);
	}

	/**
	 * Увеличение уровня детализации
	 * 
	 * @param offsetX
	 * @param offsetY
	 */
	public void zoomIn(int offsetX, int offsetY) {
		if (zoom > 0) {
			// получение отступа он начала координат

			int currentZoomX = (int) (getDefaultTile().x * TILE_SIZE
					- globalOffset.x + offsetX);
			int currentZoomY = (int) (getDefaultTile().y * TILE_SIZE
					- globalOffset.y + offsetY);

			// получение координат точки на новом уровне
			int nextZoomX = currentZoomX * 2;
			int nextZoomY = currentZoomY * 2;

			// получение координат угла экрана на новом уровне
			nextZoomX = nextZoomX - offsetX;
			nextZoomY = nextZoomY - offsetY;

			// получение углового тайла
			int tileX = nextZoomX / TILE_SIZE;
			int tileY = nextZoomY / TILE_SIZE;

			// отступ всегда один - точка должна находится в центре экрана
			correctionX = nextZoomX - tileX * TILE_SIZE;
			correctionY = nextZoomY - tileY * TILE_SIZE;

			inZoom = 1;
			zoom--;
			

			zoom(tileX, tileY, zoom);
		}
	}

	
	private int getMaxTile(int z){
		int tileCount = (int) Math.pow(2, 17 - z);
		return tileCount;
	}
	
	/**
	 * Установка текущего отступа
	 * 
	 * @param (int)x - координата x тача
	 * @param (int)y - координата y тача
	 */
	public void moveCoordinates(final float x, final float y) {
		previousMovePoint.set(nextMovePoint.x, nextMovePoint.y);
		nextMovePoint.set((int) x, (int) y);
		
		int offsetX = globalOffset.x + (nextMovePoint.x - previousMovePoint.x);
		int offsetY = globalOffset.y + (nextMovePoint.y - previousMovePoint.y);

		if (nextMovePoint.x > previousMovePoint.x) {
			if(getDefaultTile().x == 0){
			int sing = defTile.x < 0 ? -1 : 1;
			int tx = defTile.x * 256 + sing * offsetX;
				if (globalOffset.x <= 0 && tx >= 0) {
					offsetX = 0;
				}
			}
		// ограничение по правому краю	
		}  else if(nextMovePoint.x < previousMovePoint.x) {
			int sing = defTile.x < 0 ? -1 : 1;
			int tx =  (getMaxTile(defTile.z)-defTile.x) * 256  + sing * offsetX-getWidth();;
			if(tx<=0){
				offsetX = getWidth()-512;
			}		
		}

		if (nextMovePoint.y > previousMovePoint.y) {
			if(getDefaultTile().y == 0){
			int sing = defTile.y < 0 ? -1 : 1;
			int ty = defTile.y * 256 + sing * offsetY;
				if (globalOffset.y <= 0 && ty >= 0) {
					offsetY = 0;
				}
			}
		} else if(nextMovePoint.y < previousMovePoint.y) {
			int sing = defTile.y < 0 ? -1 : 1;
			int ty =  (getMaxTile(defTile.z)-defTile.y) * 256  + sing * offsetY-getHeight();;
			if(ty<=0){
				offsetY = getHeight()-512;
			}
		}

		globalOffset.set(offsetX, offsetY);
		updateMap();
	}

	public Point getAbsoluteCenter() {
		Point centerPoint = new Point();
		centerPoint.x = getDistance(getDefaultTile().x) - globalOffset.x
				+ getWidth() / 2;
		centerPoint.y = getDistance(getDefaultTile().y) - globalOffset.y
				+ getHeight() / 2;
		return centerPoint;
	}

	public void quickHack() {
		int dx = 0, dy = 0;
		int tdx = 0, tdy = 0;
		Point globalOffset = getGlobalOffset();

		for (int i = 0; i < 2; i++) {
			if (globalOffset.x > 0) {
				dx = Math.round((globalOffset.x + getWidth()) / TILE_SIZE);
			} else {
				dx = Math.round((globalOffset.x) / TILE_SIZE);
			}

			if (globalOffset.y > 0) {
				dy = Math.round((globalOffset.y + getHeight()) / TILE_SIZE);
			} else {
				dy = Math.round(globalOffset.y / TILE_SIZE);
			}

			globalOffset.x = globalOffset.x - dx * TILE_SIZE;
			globalOffset.y = globalOffset.y - dy * TILE_SIZE;

			tdx += dx;
			tdy += dy;
		}

		if (!(tdx == 0 && tdy == 0)) {
			move(tdx, tdy);
		}

	}

	private void updateMap() {
	  if (tileResolver.loaded == 9) {
			if (inZoom != 0) {
				globalOffset.x = (-1) * inZoom * (correctionX);
				globalOffset.y = (-1) * inZoom * (correctionY);
				inZoom = 0;
				scaleFactor = 1;
			}
			updateScreenCommand.execute();
			int r = random.nextInt(10);
			if (r > 7) {
				BitmapCacheWrapper.getInstance().gc();
			}
			SmoothZoomEngine.getInstance().nullScaleFactor();
		}
	}

	private int getDistance(int tileCount) {
		return tileCount * TILE_SIZE;
	}

	private void reload(int x, int y, int z) {
		defTile.x = x;
		defTile.y = y;
		defTile.z = z;
		loadCells(defTile);
	}

		/**
	 * Запрос на загрузку тайлов для данной группы ячеек (определяется по
	 * крайней левой верхней)
	 * 
	 * @param tile
	 */
	private synchronized void loadCells(RawTile tile) {
		tileResolver.loaded = 0;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int x, y;
				x = (tile.x + i);
				y = (tile.y + j);
				if (scaleFactor == 1) {
					setBitmap(MapControl.CELL_BACKGROUND, i, j);
				}
				if(GeoUtils.isValid(tile)){
					tileResolver.getTile(new RawTile(x, y, zoom, tileResolver
							.getMapSourceId()));	
				} else {
					tileResolver.loaded++;
				}
			}
		}
	}

	public void reloadTiles() {
		loadCells(defTile);
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public TileResolver getTileResolver() {
		return this.tileResolver;

	}

	public void setDefTile(RawTile defTile) {
		this.defTile = defTile;
		this.zoom = defTile.z;
	}

}
