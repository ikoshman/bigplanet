package com.nevilon.bigworld.core.providers;

import java.text.MessageFormat;

public class OpenStreetMapStrategy extends MapStrategy {

	private static final String REQUEST_PATTERN = "{2}/{0}/{1}.png";

	@Override
	public String getServer() {
		return "http://b.tile.openstreetmap.org/";
	}

	@Override
	public String getURL(int x, int y, int z) {
		return MessageFormat.format(OpenStreetMapStrategy.REQUEST_PATTERN,
				String.valueOf(x), String.valueOf(y), String.valueOf(17 - z));
	}

	@Override
	public String getDescription() {
		return "OpenStreet";
	}

}
