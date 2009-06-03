package com.nevilon.moow.core;

import android.graphics.Bitmap;

public class PhysicMap {

	private TileProvider tileProvider;

	private Bitmap[][] cells = new Bitmap[3][3];
	
	private RawTile defTile = new RawTile(29,59,9);
	
	public PhysicMap(){
		tileProvider = new TileProvider(this);
		loadCells(defTile);
	}
	
	public void update(Bitmap bitmap, RawTile tile){
		cells[tile.getX()-defTile.getX()][tile.getY()-defTile.getY()] = bitmap;
		System.out.println("updated");
	}
	
	public Bitmap[][] getCells(){
		return cells;
	}
	
	private void loadCells(RawTile tile){
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				tileProvider.getTile(
						new RawTile(
								tile.getX()+i,
								tile.getY()+j,
								tile.getZ()),
								true);
			}
		}
	}
	
}
