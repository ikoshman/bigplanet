package com.nevilon.bigworld.core.providers;

import java.text.MessageFormat;

public class GoogleLandscapeMapStrategy extends MapStrategy {

	private static final String REQUEST_PATTERN = "mt?v=w2p.87&hl=ru&x={0}&y={1}&z={2}&s=Galil";

	@Override
	public String getServer() {
		return "http://mt1.google.com/";
	}

	@Override
	public String getURL(int x, int y, int z) {
		return MessageFormat.format(GoogleLandscapeMapStrategy.REQUEST_PATTERN,
				String.valueOf(x), String.valueOf(y), String.valueOf(17 - z));
	}

	@Override
	public String getDescription() {
		return "Google Landscape";
	}

}
