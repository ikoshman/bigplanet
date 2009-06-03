package com.nevilon.moow.core.providers;

import java.text.MessageFormat;

public class YandexVectorMapStrategy  extends MapStrategy{

	private static final String REQUEST_PATTERN = "tiles?l=map&v=2.0.5&x={0}&y={1}d&z={2}";
	
	
	@Override
	public int getId() {
		return 2;
	}

	@Override
	public String getServer() {
		return "http://vec.maps.yandex.net/";
	}

	@Override
	public String getURL(int x, int y, int z) {
		return MessageFormat.format(YandexVectorMapStrategy.REQUEST_PATTERN,
				String.valueOf(x), String.valueOf(y), String
						.valueOf(17-z));
	}

}
