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
	 * 
	 * @param bitmap
	 * @param tile
	 */
	public void update(Bitmap bitmap, RawTile tile) {
		int dx = tile.getX() - defTile.getX();
		int dy = tile.getY() - defTile.getY();
		if (dx <= 2 && dy <= 2 && tile.getZ()==defTile.getZ()) {
			if (dx >= 0 && dy >= 0) {
				try {
					cells[dx][dy] = bitmap;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void reload(int dx, int dy){
		defTile = new RawTile(defTile.getX()-dx, defTile.getY()-dy, defTile.getZ());
		loadCells(defTile);
	}
	
	public Bitmap[][] getCells() {
		return cells;
	}

	public void zoom(int x, int y, int z) {
		defTile = new RawTile(x-1, y-1, z);
		loadCells(defTile);
	}

	
	/**
	 * Запрос на загрузку тайлов для данной группы ячеек (определяется по
	 * крайней левой верхней)
	 * 
	 * @param tile
	 */
	private void loadCells(RawTile tile) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int x, y;
				x = (tile.getX() + i);
				y = (tile.getY() + j);
				cells[i][j] = null;
				tileProvider.getTile(new RawTile(x, y, tile.getZ()));
			}
		}
	}

}
