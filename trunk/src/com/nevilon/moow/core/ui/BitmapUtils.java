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
	 * Рисует фон для карты( в клетку )
	 * 
	 * @param cellSize
	 * @param height
	 * @param widht
	 * @return
	 */
	public static Bitmap drawBackground(int cellSize, int height, int widht) {
		// создание битмапа по размеру экрана
		Bitmap bitmap = Bitmap.createBitmap(320, height, Config.RGB_565);
		Canvas cv = new Canvas(bitmap);
		// прорисовка фона
		Paint background = new Paint();
		background.setARGB(255, 240, 248, 255);
		cv.drawRect(0, 0, 320, height, background);
		background.setAntiAlias(true);
		// установка цвета линий
		background.setARGB(255, 122,139,139);
		// продольные линии
		for (int i = 0; i < 320 / cellSize; i++) {
			cv.drawLine(cellSize * i, 0, cellSize * i, height, background);
		}
		// поперечные линии
		for (int i = 0; i < height / cellSize; i++) {
			cv.drawLine(0, cellSize * i, 320, cellSize * i, background);
		}
		return bitmap;
	}

}
