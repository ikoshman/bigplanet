package com.nevilon.bigplanet.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import android.graphics.Bitmap;
import android.graphics.Point;

import com.nevilon.bigplanet.core.loader.TileLoader;
import com.nevilon.bigplanet.core.ui.MapControl;

public class PhysicMap {

	private static final int TILE_SIZE = 256;

	private TileResolver tileResolver;

	private Bitmap[][] cells = new Bitmap[3][3];

	//private HashMap<Tile,ArrayList<Bitmap>> cells = new HashMap<Tile,ArrayList<Bitmap>>(9);
	
	private RawTile defTile;

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
		//Tile t = new Tile();
		//t.x = x;
		//t.y = y;
		//Bitmap tmp =  cells.get(t).get(0);
		//System.out.println(tmp);
		//return tmp ;
		return cells[x][y];
	}
	
	private void setBitmap(Bitmap bmp, int x, int y){
		/*Tile t = new Tile();
		t.x = x;
		t.y = y;
		if(cells.get(t)==null){
			cells.put(t, new ArrayList<Bitmap>());
		}
		cells.get(t).add(0,bmp);
		*/
		//cells.get put(t, bmp);
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
		if (dx <= 2 && dy <= 2 && tile.z == defTile.z) {
			if (dx >= 0 && dy >= 0) {
				try {
					setBitmap(bitmap, dx, dy);
					//cells[dx][dy] = bitmap;
					updateMap();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void move(int dx, int dy) {
		System.gc();
		reload(defTile.x - dx, defTile.y - dy, defTile.z);

	}

	/*
	public Bitmap[][] getCells() {
		return cells;
	}
*/
	public void zoom(int x, int y, int z) {
		reload(x, y, z);
	}

	/**
	 * Уменьшение уровня детализации
	 */
	public void zoomOut() {
		if ((zoom) < 16) {
			int currentZoomX = getDefaultTile().x * TILE_SIZE - globalOffset.x
					+ getWidth() / 2;
			int currentZoomY = getDefaultTile().y * TILE_SIZE - globalOffset.y
					+ getHeight() / 2;

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
			// получение отступа он начала координат
			int currentZoomX = getDefaultTile().x * TILE_SIZE - globalOffset.x
					+ offsetX;
			int currentZoomY = getDefaultTile().y * TILE_SIZE - globalOffset.y
					+ offsetY;

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
	 * @param x
	 * @param y
	 */
	public void moveCoordinates(float x, float y) {
		previousMovePoint.set(nextMovePoint.x, nextMovePoint.y);
		nextMovePoint.set((int) x, (int) y);
		globalOffset.set(globalOffset.x
				+ (nextMovePoint.x - previousMovePoint.x), globalOffset.y
				+ (nextMovePoint.y - previousMovePoint.y));
		updateMap();
	}

	public Point getAbsoluteCenter() {
		// TODO не работает при изменении ориентации девайса
		Point centerPoint = new Point();
		centerPoint.x = getDistance(getDefaultTile().x) - globalOffset.x
				+ getWidth() / 2;
		centerPoint.y = getDistance(getDefaultTile().y) - globalOffset.y
				+ getHeight() / 2;
		return centerPoint;
	}

	
	private void updateMap(){
		//if(tileResolver.loaded ==9){
			updateScreenCommand.execute();
		//}
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

	private RawTile normalize(RawTile tile) {
		tile.x = normalizeX(tile.x, tile.z);
		tile.y = normalizeY(tile.y, tile.z);
		return tile;
	}

	private int normalizeX(int x, int z) {
		int tileCount = (int) Math.pow(2, 17 - z);
		while (x < 0) {
			x = tileCount + x;
		}
		while (x > tileCount - 1) {
			x = x - tileCount;
		}
		return x;
	}

	private int normalizeY(int y, int z) {
		int tileCount = (int) Math.pow(2, 17 - z);
		while (y < 0) {
			y = tileCount + y;
		}
		while (y > tileCount - 1) {
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
		//tileResolver.loaded = 0;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int x, y;
				x = (tile.x + i);
				x = normalizeX(x, tile.z);

				y = (tile.y + j);
				y = normalizeY(y, tile.z);
				setBitmap(MapControl.bp, i, j);
				//cells[i][j] = MapControl.bp;
				tileResolver.getTile(new RawTile(x, y, tile.z, tileResolver
						.getMapSourceId(),0));

				//tileResolver.getTile(new RawTile(x, y, tile.z, tileResolver
				//		.getMapSourceId(),1));
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
