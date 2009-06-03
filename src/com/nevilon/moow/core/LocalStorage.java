package com.nevilon.moow.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Реализация файлового кеша Для хранения тайлов используется дерево
 * 
 * @author hudvin
 * 
 */
public class LocalStorage {

	private static LocalStorage localStorage;

	/**
	 * Корневой каталог для файлового кеша
	 */
	private static final String root_dir_location = "/sdcard/moow/";

	public static LocalStorage getInstance() {
		if (localStorage == null) {
			localStorage = new LocalStorage();
		}
		return localStorage;
	}

	/**
	 * Конструктор Инициализация файлового кеша(если необходимо)
	 */
	private LocalStorage() {
		init();
	}

	/**
	 * Очистка файлового кеша
	 */
	public void clear() {
		deleteDir(new File(root_dir_location));
	}

	/**
	 * Инициализация файлового кеша
	 */
	private void init() {
		File dir = new File(root_dir_location);
		if (!(dir.exists() && dir.isDirectory())) {
			dir.mkdirs();
		}
	}

	/**
	 * Удаляет (рекурсивно) каталог и все его содержимое
	 * 
	 * @param dir
	 * @return
	 */
	private boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

	/**
	 * Построение пути сохранения для тайла
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private String buildPath(int x, int y, int z) {
		String intPath = String.valueOf(z) + String.valueOf(x)
				+ String.valueOf(y);
		StringBuffer path = new StringBuffer();
		path.append(root_dir_location);
		for (int i = 0; i < intPath.length(); i++) {
			path.append(intPath.charAt(i));
			path.append("/");
		}
		return path.toString();
	}

	/**
	 * Сохраняет тайл в файловый кеш
	 * 
	 * @param tile
	 *            параметры тайла
	 * @param data
	 *            параметры тайла
	 */
	public void put(RawTile tile, byte[] data) {
		String path = buildPath(tile.x, tile.y, tile.z);
		File fullPath = new File(path);
		fullPath.mkdirs();
		fullPath = new File(path + "tile.tl");
		try {
			BufferedOutputStream outStream = new BufferedOutputStream(
					new FileOutputStream(fullPath), 4096);
			outStream.write(data);
			outStream.flush();
			outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Возвращает заданный тайл или null(если не найден)
	 * 
	 * @param tile
	 *            параметры тайла
	 * @return тайл
	 */
	public BufferedInputStream get(RawTile tile) {
		String path = buildPath(tile.x, tile.y, tile.z);
		File tileFile = new File(path + "/tile.tl");
		//TODO try to use decodeFile
		if (tileFile.exists()) {
			try {
				BufferedInputStream io = new BufferedInputStream(new FileInputStream(tileFile),
						4096);
				return io ;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
