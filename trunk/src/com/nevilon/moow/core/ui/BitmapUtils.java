package com.nevilon.moow.core.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;

/**
 * Утилиты для работы с битмапами
 * 
 * @author hudvin
 * 
 */
public class BitmapUtils {

	/**
	 * Цвет фона
	 */
	private static int BACKGROUND_COLOR = Color.argb(255, 240, 248, 255);
	
	/**
	 * Цвет линий
	 */
	private static int LINE_COLOR = Color.argb(255, 122,139,139);
	
	/**
	 * Рисует фон для карты( в клетку )
	 * 
	 * @param cellSize
	 * @param height
	 * @param widht
	 * @return
	 */
	public static Bitmap drawBackground(int cellSize, int height, int widht) {
		// создание битмапа по размеру экрана
		Bitmap bitmap = Bitmap.createBitmap(widht, height, Config.RGB_565);
		Canvas cv = new Canvas(bitmap);
		// прорисовка фона
		Paint background = new Paint();
		background.setColor(BACKGROUND_COLOR);
		cv.drawRect(0, 0, widht, height, background);
		background.setAntiAlias(true);
		// установка цвета линий
		background.setColor(LINE_COLOR);
		// продольные линии
		for (int i = 0; i < widht / cellSize; i++) {
			cv.drawLine(cellSize * i, 0, cellSize * i, height, background);
		}
		// поперечные линии
		for (int i = 0; i < height / cellSize; i++) {
			cv.drawLine(0, cellSize * i, widht, cellSize * i, background);
		}
		return bitmap;
	}

}
