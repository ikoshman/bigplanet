package com.nevilon.bigplanet.core.providers;

import java.text.MessageFormat;

public class GoogleSatelliteMapStrategy extends MapStrategy {

	private static final String REQUEST_PATTERN = "kh?v=36&hl=ru&x={0}&y={1}&z={2}&s=Gal";

	@Override
	public String getServer() {
		return "http://khm3.google.com/";
	}

	@Override
	public String getURL(int x, int y, int z) {
		return MessageFormat.format(GoogleSatelliteMapStrategy.REQUEST_PATTERN,
				String.valueOf(x), String.valueOf(y), String.valueOf(17 - z));
	}

	@Override
	public String getDescription() {
		return "Google Satellite";
	}

}
