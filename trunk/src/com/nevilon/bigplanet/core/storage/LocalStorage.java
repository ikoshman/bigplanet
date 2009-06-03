package com.nevilon.bigplanet.core.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.nevilon.bigplanet.core.RawTile;

/**
 * Реализация файлового кеша Для хранения тайлов используется дерево
 * 
 * @author hudvin
 * 
 */
public class LocalStorage {

	private static final int BUFFER_SIZE = 4096;

	private static LocalStorage localStorage;
	
	private static String TILE_FILE_NAME = "tile";

	/**
	 * Корневой каталог для файлового кеша
	 */
	private static final String root_dir_location = "/sdcard/bigplanet/";

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
		//clear();
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

	public boolean isExists(RawTile tile) {
		String path = buildPath(tile);
		File tileFile = new File(path + TILE_FILE_NAME);
		return tileFile.exists();
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
	private String buildPath(RawTile tile) {
	    StringBuffer path = new StringBuffer();
		path.append(root_dir_location);
		path.append(tile.toString());
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
		String path = buildPath(tile);
		File fullPath = new File(path);
		fullPath.mkdirs();
		fullPath = new File(path + TILE_FILE_NAME);
		try {
			BufferedOutputStream outStream = new BufferedOutputStream(
					new FileOutputStream(fullPath), BUFFER_SIZE);
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
		String path = buildPath(tile);
		File tileFile = new File(path + TILE_FILE_NAME);
		if (tileFile.exists()) {
			try {
				BufferedInputStream io = new BufferedInputStream(
						new FileInputStream(tileFile), BUFFER_SIZE);
				return io;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
