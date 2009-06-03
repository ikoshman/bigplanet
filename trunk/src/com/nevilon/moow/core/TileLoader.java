package com.nevilon.moow.core;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.LinkedList;

import android.util.Log;

/**
 * Загрузчик тайлов с сервера
 * 
 * @author hudvin
 * 
 */
public class TileLoader implements Runnable {

	private static final String REQUEST_PATTERN = "http://mt1.google.com/mt?x={0}&y={1}&zoom={2}";

	//private static final String REQUEST_PATTERN = "http://mt1.google.com/mt?v=w2.99&x={0}&y={1}&zoom={2}";
	
	private Handler handler;

	private int counter = 0;

	private LinkedList<RawTile> loadQueue = new LinkedList<RawTile>();

	/**
	 * Конструктор
	 * 
	 * @param handler
	 *            обработчик результата загрузки
	 */
	public TileLoader(Handler handler) {
		this.handler = handler;
	}

	/**
	 * Добавляет в очередь на загрузку
	 * 
	 * @param tile
	 */
	public void load(RawTile tile) {
		addToQueue(tile);
	}

	public synchronized void addToQueue(RawTile tile) {
		loadQueue.add(tile);
	}

	public synchronized RawTile getFromQueue() {
		return loadQueue.poll();
	}

	public synchronized void tileLoaded(RawTile tile, byte[] data) {
		if (data != null) {
			handler.handle(tile, data);
		}
		counter--;
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(200);
				if (counter <= 6 && loadQueue.size() > 0) {
					RawTile rt = getFromQueue();
					Log.i("LOADER", "Tile " + rt + " start loading");
					if (null != rt) {
						new ThreadLoader(rt).start();
						counter++;
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private class ThreadLoader extends Thread {

		private static final String MIME_TEXT = "text/";
		private RawTile tile;

		public ThreadLoader(RawTile tile) {
			super();
			this.tile = tile;
		}

		private byte[] load() throws Exception {
			URL u = new URL(MessageFormat.format(TileLoader.REQUEST_PATTERN,
					String.valueOf(tile.x), String.valueOf(tile.y), String
							.valueOf(tile.z)));
			URLConnection uc = u.openConnection();
			String contentType = uc.getContentType();
			int contentLength = uc.getContentLength();
			if (contentType == null
					|| contentType.startsWith(ThreadLoader.MIME_TEXT)
					|| contentLength == -1) {
				Log.e("LOADER", "Can't load tile " + tile.x + " " + tile.y
						+ " " + tile.z);
				return null;
			}
			InputStream raw = uc.getInputStream();
			InputStream in = new BufferedInputStream(raw, 65536);
			byte[] data = new byte[contentLength];
			int bytesRead = 0;
			int offset = 0;
			while (offset < contentLength) {
				bytesRead = in.read(data, offset, data.length - offset);
				if (bytesRead == -1)
					break;
				offset += bytesRead;
			}
			in.close();
			if (offset != contentLength) {
				return null;
			}
			Log.i("LOADER", "Receive tile " + tile);
			return data;
		}

		public void run() {
			try {
				TileLoader.this.tileLoaded(tile, load());
				// TileLoader.this.tileLoaded(tile, null);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

}
