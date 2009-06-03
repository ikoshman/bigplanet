package com.nevilon.moow.core;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class PhysicMap {

	private TileProvider tileProvider;

	private Bitmap[][] cells = new Bitmap[3][3];
	
	private RawTile defTile = new RawTile(1042,1520,5);
	
	public PhysicMap(){
		tileProvider = new TileProvider(this);
		loadCells(defTile);
	}
	
	public RawTile getDefaultTile(){
		return this.defTile;
	}
	
	/**
	 * Callback method
	 * @param bitmap
	 * @param tile
	 */
	public void update(Bitmap bitmap, RawTile tile){
		if(tile.getX()-defTile.getX()<=2 &&
				tile.getY()-defTile.getY()<=2){
			cells[tile.getX()-defTile.getX()][tile.getY()-defTile.getY()] = bitmap;		
		}
		
	}
	
	public Bitmap[][] getCells(){
		return cells;
	}
	
	public void moveTop(){
		defTile = new RawTile(defTile.getX(), defTile.getY()+1,defTile.getZ());
		loadCells(defTile);
	}
	
	public void moveBottom(){
		defTile = new RawTile(defTile.getX(), defTile.getY()-1,defTile.getZ());
		loadCells(defTile);
	}
	
	public void moveLeft(){
		defTile = new RawTile(defTile.getX()-1, defTile.getY(),defTile.getZ());
		loadCells(defTile);
	}
	
	public void moveRight(){
		defTile = new RawTile(defTile.getX()+1, defTile.getY(),defTile.getZ());
		loadCells(defTile);
	}
	
	private void loadCells(RawTile tile){
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				int x,y;
				x = (tile.getX()+i);
				y = (tile.getY()+j);
				cells[i][j] = null;
				tileProvider.getTile(new RawTile(x,y,tile.getZ()));
			}
		}
	}
	
}
