package com.nevilon.moow.core;

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
	
	private List<Point> moveHistory;
	
	
	public InertionEngine(List<Point> moveHistory, long interval){
		this.interval = interval;
		this.moveHistory = moveHistory;
		findAB();
		
		dx = b.x - a.x;
		dy = b.y - a.y;
		ax = (dx/interval);
		ay = (dy/interval);
		
		while(Math.abs(dx)>5 || Math.abs(dy)>5){
			dx = dx/2;
			dy = dy/2;
		}
		System.out.println("bl " + dx + " " + dy);
		
	}
	
	
	public double getInterval(){
		return this.interval;
	}
	
	private void findAB(){
		a = moveHistory.get(0);
		b = moveHistory.get(moveHistory.size()/2);
		/*
		Point tmpPoint = new Point();
		for (Point pp : moveHistory){
			if(!pp.equals(tmpPoint) && tmpPoint.x!=0 && tmpPoint.y!=0){
				a = tmpPoint;
				b = pp;
				break;
			} else {
				tmpPoint = pp;
			}
		}
		*/
	}
	
	
}
