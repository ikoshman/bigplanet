package com.nevilon.bigplanet.core.providers;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class GoogleVectorMapStrategy extends MapStrategy {

	private List<Layer> layers = new ArrayList<Layer>();

    public GoogleVectorMapStrategy() {
		layers.add(new Layer() {

			private  String SERVER = "http://mt1.google.com/"; 
			
			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public int getId() {
				return 0;
			}

			@Override
			public String getURLPattern() {
				return SERVER+ "mt?x={0}&y={1}&zoom={2}";
			}


		});
		
		layers.add(new Layer() {

			private  String SERVER = "http://mt0.google.com/"; 
			
			@Override
			public String getDescription() {
				return "Traffic";
			}

			@Override
			public int getId() {
				return 1;
			}

			@Override
			public String getURLPattern() {
				return SERVER+ "mapstt?x={0}&y={1}&zoom={2}";
			}


		});


	}
	
	@Override
	public String getURL(int x, int y, int z,int layout) {
		Layer layer = layers.get(layout);
		return MessageFormat.format(layer.getURLPattern(),
				String.valueOf(x), String.valueOf(y), String.valueOf(z));

	}

	@Override
	public String getDescription() {
		return "Google Map";
	}

}
