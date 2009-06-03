package com.nevilon.bigworld.core.loader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpStatus;

import com.nevilon.bigworld.core.RawTile;
import com.nevilon.bigworld.core.providers.MapStrategy;

public abstract class BaseLoader extends Thread {

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

	private boolean checkTile(RawTile tile) {
		return true;
	}

	private byte[] load(RawTile tile) throws Exception {

		try {
			HttpClient client = new HttpClient();

			client.getHttpConnectionManager().getParams().setSoTimeout(4000);
			client.getHttpConnectionManager().getParams().setConnectionTimeout(
					4000);

			GetMethod method = new GetMethod(getStrategy().getServer()
					+ getStrategy().getURL(tile.x, tile.y, tile.z));

			int statusCode = client.executeMethod(method);
			if (statusCode != -1 && method.getStatusCode() == HttpStatus.SC_OK) {
				byte data[] = method.getResponseBody();
				method.releaseConnection();
				return data;
			} else {
				System.out.println(method.getStatusLine());
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}

	}

	protected abstract void handle(RawTile tile, byte[] data, int meta);

	protected abstract MapStrategy getStrategy();

}
