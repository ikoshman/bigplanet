package com.nevilon.bigworld.core.ui;

import android.graphics.Point;
import android.view.MotionEvent;

/**
 * Предназначен для обработки двойного клика
 * 
 * @author hudvin
 * 
 */
public class DoubleClickHelper {

	/**
	 * Минимальный временной промежуток между двумя отдельными касаниями, при
	 * котором они воспринимаются как двойное касание
	 */
	private static int CLICK_INTERVAL = 500;

	/**
	 * Максимальное расстояние между касаниями, при котором они воспринимаются
	 * как двойное
	 */
	private static int CLICK_PRECISE = 7;

	/**
	 * Хранит предыдущее событие
	 */
	private Point previousPoint;

	/**
	 * Хранит время предыдущего события
	 */
	private long eventTime;

	public boolean process(MotionEvent currentEvent) {
		if (previousPoint != null
				&& (System.currentTimeMillis() - eventTime) < DoubleClickHelper.CLICK_INTERVAL
				&& isNear((int) currentEvent.getX(), (int) currentEvent.getY())) {
			return true;
		}
		previousPoint = new Point();
		previousPoint.x = (int) currentEvent.getX();
		previousPoint.y = (int) currentEvent.getY();
		eventTime = System.currentTimeMillis();
		return false;
	}

	/**
	 * Проверяет, находится ли первая точка вблизи второй
	 * 
	 * @param event
	 * @return
	 */
	private boolean isNear(int x, int y) {
		boolean checkX = Math.abs(previousPoint.x - x) <= DoubleClickHelper.CLICK_PRECISE;
		boolean checkY = Math.abs(previousPoint.y - y) <= DoubleClickHelper.CLICK_PRECISE;
		return checkX == checkY && checkX == true;
	}

}
