package com.nevilon.bigplanet.core.loader;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.nevilon.bigplanet.core.RawTile;
import com.nevilon.bigplanet.core.providers.MapStrategy;

public abstract class BaseLoader extends Thread {

	public static final int CONNECTION_TIMEOUT = 10000;

	private RawTile[] tiles;

	private boolean stop = false;

	public BaseLoader(RawTile tile) {
		super();
		this.tiles = new RawTile[1];
		this.tiles[0] = tile;
	}

	public BaseLoader(RawTile[] tiles) {
		super();
		this.tiles = tiles;
	}

	public void stopLoader() {
		stop = true;
	}

	public void run() {
		for (RawTile tile : tiles) {
			if (stop) {
				return;
			}
			if (checkTile(tile)) {
				try {
					System.out.println(tile);
					byte[] data = load(tile);
					handle(tile, data, 0);
				} catch (Exception e) {
					e.printStackTrace();
					handle(tile, null, 0);
				}
			} else {
				handle(tile, null, 1);
			}
		}

	}

	protected boolean checkTile(RawTile tile) {
		return true;
	}

	private byte[] load(RawTile tile) {
		HttpURLConnection connection = null;
		try {
			URL u = new URL(getStrategy().getURL(tile.x, tile.y, tile.z, 0));
			connection = (HttpURLConnection) u
					.openConnection();
			connection.setRequestMethod("GET");
			connection.setReadTimeout(BaseLoader.CONNECTION_TIMEOUT);
			connection.setConnectTimeout(BaseLoader.CONNECTION_TIMEOUT);
			connection.connect();
			int responseCode = connection.getResponseCode();
			if (responseCode!= HttpURLConnection.HTTP_OK) {
				System.out.println(connection.getResponseMessage());
				return null;
			}
			int contentLength = connection.getContentLength();
			InputStream raw = connection.getInputStream();
			InputStream in = new BufferedInputStream(raw, 4096);
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
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			connection.disconnect();
		}
		return null;

	}

	protected abstract void handle(RawTile tile, byte[] data, int meta);

	protected abstract MapStrategy getStrategy();

}
