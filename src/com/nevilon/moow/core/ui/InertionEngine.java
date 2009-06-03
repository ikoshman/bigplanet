package com.nevilon.moow.core.ui;

import java.util.List;
import java.util.Stack;

import android.graphics.Point;

public class InertionEngine {

	private long interval;
	
	private Point a;
	
	private Point b;
	
	public double x;
	
	public double y;

	public double dx;
	
	public double dy;
	
	// величины ускорения
	public double ax;
	
	public double ay;
	
	public int step;
	
	private List<Point> moveHistory;
	
	
	public InertionEngine(List<Point> moveHistory, long interval){
		this.interval = interval;
		this.moveHistory = moveHistory;
		findAB();
		
		dx = b.x - a.x;
		dy = b.y - a.y;
		
		int absDx =(int) Math.abs(dx);
		int absDy = (int) Math.abs(dy); 
		
		if(absDx > absDy ){
			step = absDx;
		} else {
			step = absDy;
		}
		
		while(Math.abs(dx)>10 || Math.abs(dy)>10){
			dx = dx/2;
			dy = dy/2;
		}
		
	}
	
	public void reduceSpeed(){
		if(this.dx > (this.dx-0.5)){
			this.dx= this.dx - 0.5;
		} else if(this.dx<0 && this.dx<(this.dx+0.5)){
			this.dx= this.dx + 0.5;
		}
		
		if(this.dy > (this.dy-0.5)){
			this.dy= this.dy - 0.5;
		} else if(this.dy<0 && this.dy<(this.dy+0.5)){
			this.dy= this.dy + 0.5;
		}
	}
	
	
	
	private void findAB(){
		a = moveHistory.get(0);
		b = moveHistory.get(moveHistory.size()/2);
		moveHistory = null;
	}
	
	
}
