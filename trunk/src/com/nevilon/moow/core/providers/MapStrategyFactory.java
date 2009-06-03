package com.nevilon.moow.core.providers;

public class MapStrategyFactory {

	private static MapStrategyFactory mapStrategyFactory;

	public enum MapSource {
		GOOGLE_VECTOR, GOOGLE_SATELLITE, YANDEX_VECTOR, YANDEX_SATELLITE, OPENSTREET_VECTOR
	};

	private MapStrategyFactory() {
	}

	public static MapStrategyFactory getInstance() {
		if (null == mapStrategyFactory) {
			mapStrategyFactory = new MapStrategyFactory();
		}
		return mapStrategyFactory;

	}

	public MapStrategy getStrategy(MapSource source) {
		switch (source) {

		case GOOGLE_VECTOR:
			return new GoogleVectorMapStrategy();

		case GOOGLE_SATELLITE:
			return new GoogleSatelliteMapStrategy();

		case YANDEX_SATELLITE:
			return new YandexSatelliteMapStrategy();

		case YANDEX_VECTOR:
			return new YandexVectorMapStrategy();

		case OPENSTREET_VECTOR:
			return new OpenStreetMapStrategy();

		default:
			return new GoogleVectorMapStrategy();
		}
	}

}
