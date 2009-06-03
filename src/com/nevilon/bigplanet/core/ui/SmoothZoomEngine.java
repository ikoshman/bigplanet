package com.nevilon.bigplanet.core.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.nevilon.bigplanet.core.AbstractCommand;

public class SmoothZoomEngine {

	
	private static SmoothZoomEngine sze;
	
	private LinkedList<Integer> scaleQueue = new LinkedList<Integer>();

	private Double scaleFactor = 1000d;
	
	
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
			scaleFactor = 1000d;
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
						if (!(endScaleFactor > 8000 || endScaleFactor < 125)) {
							System.out.println(scaleFactor + " "
									+ endScaleFactor);
							synchronized (scaleFactor) {

								do {
									try {
										Thread.sleep(PAUSE);
										scaleFactor = scaleFactor
												+ (scaleDirection) * 25;
										// обновить экран
						
										updateScreen.execute(new Double(
												scaleFactor / 1000));
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								} while (!(scaleFactor == (endScaleFactor)));	
							}
						}

						} else if (!isEmpty && scaleQueue.size()==0) {
							isEmpty = true;
							reloadMap.execute(new Double(scaleFactor / 1000));
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
