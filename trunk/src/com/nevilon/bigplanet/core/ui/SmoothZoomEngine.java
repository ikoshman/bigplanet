package com.nevilon.bigplanet.core.ui;

import java.util.LinkedList;

import com.nevilon.bigplanet.core.AbstractCommand;
import com.nevilon.bigplanet.core.PhysicMap;
import com.nevilon.bigplanet.core.Utils;

public class SmoothZoomEngine {

	
	private static SmoothZoomEngine sze;
	
	private LinkedList<Integer> scaleQueue = new LinkedList<Integer>();

	private Float scaleFactor = 1000f;
	
	
	private AbstractCommand updateScreen;
	
	private AbstractCommand reloadMap;
	
	public static SmoothZoomEngine getInstance(){
		if(sze==null){
			sze = new SmoothZoomEngine();
		}
		return sze;
	}
	
	public void setUpdateScreenCommand(final AbstractCommand updateScreen){
		this.updateScreen = updateScreen;
	}
	
	public void setReloadMapCommand(final AbstractCommand reloadMap){
		this.reloadMap = reloadMap;
	}
	
	public void nullScaleFactor(){
		synchronized (scaleFactor) {
			scaleFactor = 1000f;
		} 
	}
	
	private  SmoothZoomEngine() {
		new Thread() {

			@Override
			public void run() {
				boolean isEmpty = true;
				if(updateScreen!=null){
					updateScreen.execute();
						
				}
				double endScaleFactor;
				int PAUSE = 10;
				while (true) {
					if (scaleQueue.size() > 0) {
						isEmpty = false;
						int scaleDirection = scaleQueue.removeFirst();
						endScaleFactor = scaleDirection == -1 ? scaleFactor / 2
								: scaleFactor * 2;
						int z  = PhysicMap.zoom ;
						System.out.println("zoom " + PhysicMap.zoom+" scaleFactor "  + Utils.getZoomLevel(scaleFactor/1000));
						if((scaleDirection==-1 && z<16) || (scaleDirection==1 && z>0)){
							if(!(endScaleFactor > 8000 || endScaleFactor < 125)){
								synchronized (scaleFactor) {

									do {
										try {
											Thread.sleep(PAUSE);
											scaleFactor = scaleFactor
													+ (scaleDirection) * 25;
											// обновить экран
							
											updateScreen.execute(new Float(
													scaleFactor / 1000));
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									} while (!(scaleFactor == (endScaleFactor)));	
								}
							}

						} if (!isEmpty && scaleQueue.size()==0) {
							System.out.println("reload");
							isEmpty = true;
							reloadMap.execute(new Float(scaleFactor / 1000));
						}

						}
				//	}
				}
			}

		}.start();
	}

	public void addToScaleQ(int direction) {
		scaleQueue.addLast(direction);
	}

}
