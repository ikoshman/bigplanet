package com.nevilon.bigworld.core.providers;

import java.text.MessageFormat;

public class GoogleVectorMapStrategy extends MapStrategy {

	// private static final String REQUEST_PATTERN =
	// "http://mt1.google.com/mt?v=w2.99&x={0}&y={1}&zoom={2}";

	private static final String REQUEST_PATTERN = "mt?x={0}&y={1}&zoom={2}";

	@Override
	public String getURL(int x, int y, int z) {
		return MessageFormat.format(GoogleVectorMapStrategy.REQUEST_PATTERN,
				String.valueOf(x), String.valueOf(y), String.valueOf(z));

	}

	@Override
	public String getServer() {
		return "http://mt1.google.com/";
	}

	@Override
	public String getDescription() {
		return "Google Map";
	}

}
