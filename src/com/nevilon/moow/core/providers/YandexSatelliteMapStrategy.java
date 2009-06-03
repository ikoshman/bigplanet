package com.nevilon.moow.core.providers;

import java.text.MessageFormat;

public class YandexSatelliteMapStrategy extends MapStrategy {

	private static final String REQUEST_PATTERN = "tiles?l=sat&v=1.6.0&x={0}&y={1}&z={2}";

	@Override
	public String getServer() {
		return "http://sat01.maps.yandex.net/";
	}

	@Override
	public String getURL(int x, int y, int z) {
		return MessageFormat.format(YandexSatelliteMapStrategy.REQUEST_PATTERN,
				String.valueOf(x), String.valueOf(y), String.valueOf(17 - z));
	}

	@Override
	public String getDescription() {
		return "Yandex Satellite";
	}
}
