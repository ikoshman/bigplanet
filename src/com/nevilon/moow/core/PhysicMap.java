package com.nevilon.moow.core;

import android.graphics.Bitmap;

public class PhysicMap {

	private TileProvider tileProvider;

	private Bitmap[][] cells = new Bitmap[3][3];

	private RawTile defTile;

	public PhysicMap(RawTile defTile) {
		this.defTile = defTile;
		tileProvider = new TileProvider(this);
		loadCells(defTile);
	}

	public RawTile getDefaultTile() {
		return this.defTile;
	}

	/**
	 * Callback method 
	 * @param bitmap
	 * @param tile
	 */
	public synchronized  void update(Bitmap bitmap, RawTile tile) {
		int dx = tile.x - defTile.x;
		int dy = tile.y - defTile.y;
		if (dx <= 2 && dy <= 2 && tile.z==defTile.z) {
			if (dx >= 0 && dy >= 0) {
				try {
					cells[dx][dy] = bitmap;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void move(int dx, int dy){
		//System.out.println(dx);
		//if(defTile.x-dx<=0){
		//	dx = (int) (Math.pow(2, 17-defTile.z) - Math.abs(dx))-1;
		//	reload(dx, defTile.y-dy, defTile.z);
		//} else {
			reload(defTile.x -dx,defTile.y - dy, defTile.z);

		//}
	}
	
	public Bitmap[][] getCells() {
		return cells;
	}

	public void zoom(int x, int y, int z) {
		reload(x-1,y-1,z);
	}

	
	private void reload(int x, int y, int z){
		/**
		 * использование загруженных битмапов
		 * 
		 */
		if(defTile.z == z){
			//for(int i=)
		}
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
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int x, y;
				x = (tile.x + i);
				y = (tile.y + j);
				Bitmap tmpBitmap = tileProvider.inMemoryCache.get(new RawTile(x,y,tile.z));
				if(tmpBitmap!=null){
					cells[i][j] = tmpBitmap;
				} else {
					System.out.println("null");
					cells[i][j] = null;
					tileProvider.getTile(new RawTile(x, y, tile.z));
				}
			}
		}
	}
	

}
