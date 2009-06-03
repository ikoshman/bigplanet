package com.nevilon.bigplanet.core;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Point;

import com.nevilon.bigplanet.core.storage.BitmapCacheWrapper;
import com.nevilon.bigplanet.core.ui.MapControl;

public class PhysicMap {

	private static final int TILE_SIZE = 256;
	
	private static Random random = new Random();

	private TileResolver tileResolver;

	private Bitmap[][] cells = new Bitmap[3][3];
	
	public RawTile defTile;

	private int zoom;

	private Point globalOffset = new Point();

	private Point previousMovePoint = new Point();

	private Point nextMovePoint = new Point();

	private int width;

	private int height;
	
	private AbstractCommand updateScreenCommand;

	public PhysicMap(RawTile defTile, AbstractCommand updateScreenCommand) {
		this.defTile = defTile;
		this.updateScreenCommand = updateScreenCommand;
		this.zoom = defTile.z;
		tileResolver = new TileResolver(this);
		loadCells(defTile);
	}

	public Bitmap getCell(int x, int y){
		return cells[x][y];
	}
	
	private void setBitmap(Bitmap bmp, int x, int y){
		cells[x][y] = bmp;
	}
	
	public Point getNextMovePoint(){
		return this.nextMovePoint;
	}
	
	public Point getGlobalOffset(){
		return this.globalOffset;
	}
	
	public void setGlobalOffset(Point globalOffset){
		this.globalOffset = globalOffset;
	}

	public RawTile getDefaultTile() {
		return normalize(this.defTile);
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
		int dx = tile.x - defTile.x;
		int dy = tile.y - defTile.y;
		//System.out.println("dx " + dx + " dy: " +dy);
		//dx = normalize(dx,getZoomLevel());
		//dy = normalize(dy,getZoomLevel());
		
		if (dx <= 2 && dy <= 2 && tile.z == defTile.z) {
			if (dx >= 0 && dy >= 0) {
				try {
					setBitmap(bitmap, dx, dy);
					updateMap();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void move(int dx, int dy) {
		System.gc();
		reload(defTile.x - dx,
				defTile.y -dy,
				defTile.z);
	}

	
	
	public void goTo(int x,int y, int z, int offsetX, int offsetY){
		int fullX = x*256+offsetX;
		int fullY = y*256 +offsetY;
		// коориданаты углового тайла
		int tx = fullX - getWidth()/2;
		int ty = fullY - getHeight()/2;
		
		this.zoom = z;
		globalOffset.x = -(tx-Math.round(tx/256)*256);

		globalOffset.y = -(ty-Math.round(ty/256)*256);
		reload(tx/256, ty/256, z);
	}
	
	public void zoom(int x, int y, int z) {
	
		reload(x, y, z);
	}

	/**
	 * Уменьшение уровня детализации
	 */
	public void zoomOut() {
		if ((zoom) < 16) {
			int currentZoomX = (int) (getDefaultTile().x * TILE_SIZE - globalOffset.x
					+ getWidth() / 2);
			int currentZoomY = (int) (getDefaultTile().y * TILE_SIZE - globalOffset.y
					+ getHeight() / 2);

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
			int correctionX = nextZoomX - tileX * TILE_SIZE;
			int correctionY = nextZoomY - tileY * TILE_SIZE;

			globalOffset.x = -(correctionX);
			globalOffset.y = -(correctionY);
			zoom++;
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
			System.out.println("zoomImap " + zoom);
			// получение отступа он начала координат
			int currentZoomX = (int) (getDefaultTile().x * TILE_SIZE - globalOffset.x
					+ offsetX);
			int currentZoomY = (int) (getDefaultTile().y * TILE_SIZE - globalOffset.y
					+ offsetY);

			// получение координат точки на новом уровне
			int nextZoomX = currentZoomX * 2;
			int nextZoomY = currentZoomY * 2;

			// получение координат угла экрана на новом уровне
			nextZoomX = nextZoomX - getWidth() / 2;
			nextZoomY = nextZoomY - getHeight() / 2;

			// получение углового тайла
			int tileX = (nextZoomX / TILE_SIZE);
			int tileY = nextZoomY / TILE_SIZE;

			// отступ всегда один - точка должна находится в центре экрана
			int correctionX = nextZoomX - tileX * TILE_SIZE;
			int correctionY = nextZoomY - tileY * TILE_SIZE;

			globalOffset.x = -(correctionX);
			globalOffset.y = -(correctionY);
			zoom--;
			zoom(tileX, tileY, zoom);
		}
	}

	/**
	 * Установка текущего отступа
	 * 
	 * @param (int)f
	 * @param (int)g
	 */
	public void moveCoordinates(float x, float y) {
		previousMovePoint.set(nextMovePoint.x, nextMovePoint.y);
		nextMovePoint.set( (int)x,  (int)y);
		globalOffset.set(globalOffset.x
				+ (nextMovePoint.x - previousMovePoint.x), globalOffset.y
				+ (nextMovePoint.y - previousMovePoint.y));
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

	
	private void updateMap(){
		if(tileResolver.loaded ==9){
			updateScreenCommand.execute();
			int r = random.nextInt(10);
			if(r>7){
				BitmapCacheWrapper.getInstance().gc();
			}
			System.out.println(r);
		}
	}
	
	private int getDistance(int tileCount) {
		return tileCount * TILE_SIZE;
	}

	private void reload(int x, int y, int z) {
		defTile.x = x;
		defTile.y = y;
		defTile.z = z;
		//defTile = normalize(defTile);
		loadCells(defTile);
	}

	public static RawTile normalize(RawTile tile) {
		int x = normalize(tile.x, tile.z);
		int y = normalize(tile.y, tile.z);
		int z = tile.z;
		RawTile newTile = new RawTile(x,y,z,tile.s);
		return newTile;
	}

	public static int normalize(int y, int z) {
		int tileCount = (int) Math.pow(2, 17 - z);
		while (y < 0) {
			y = tileCount + y;
		}
		while (y >= tileCount) {
			y = y - tileCount;
		}
		return y;
	}

	/**
	 * Запрос на загрузку тайлов для данной группы ячеек (определяется по
	 * крайней левой верхней)
	 * 
	 * @param tile
	 */
	private synchronized void  loadCells(RawTile tile) {
		tileResolver.loaded = 0;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int x, y;
				x = (tile.x + i);
				x = normalize(x, tile.z);

				y = (tile.y + j);
				y = normalize(y, tile.z);
				setBitmap(MapControl.CELL_BACKGROUND, i, j);
				tileResolver.getTile(new RawTile(x, y, zoom, tileResolver
						.getMapSourceId()));
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
