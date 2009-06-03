package com.nevilon.moow.core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;

public class TileLoader implements Runnable{
	
	private TileProvider tileProvider;
	
	private int counter = 0;
	
	private LinkedList<RawTile> loadQueue = new LinkedList<RawTile>();

	public TileLoader(TileProvider tileProvider){
		this.tileProvider = tileProvider;
	//	new Thread(this).start();
	}
	
	public void load(RawTile tile){
		loadQueue.add(tile);
		//new ThreadLoader(tile).start();
	}

	public synchronized void tileLoaded(RawTile tile, byte[] data){
		this.tileProvider.putToStorage(tile,data);
		this.tileProvider.getTile(tile);
		counter--;
	}
	
	public void run() {
		while(true){
			try {
				Thread.sleep(500);
				if (counter<=5 && loadQueue.size()>0){
					System.out.println("blya");
					RawTile rt = loadQueue.poll();
					if (null!=rt){
						new ThreadLoader(rt).start();
						counter++;
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private  class ThreadLoader extends Thread{

		private RawTile tile;
		
		public ThreadLoader(RawTile tile) {
			super();
			this.tile = tile;
		}
		
		private  byte[] load() throws Exception {
	        URL u = new URL("http://mt1.google.com/mt?x="+tile.getX()+
	        		"&y="+tile.getY()+"&zoom="+tile.getZ());
	        URLConnection uc = u.openConnection();
	        String contentType = uc.getContentType();
	        int contentLength = uc.getContentLength();
	        if (contentType.startsWith("text/") || contentLength == -1) {
	            throw new IOException("This is not a binary file. "+tile.getX()+" "+ tile.getY()+" "+ tile.getZ());
	        }
	        InputStream raw = uc.getInputStream();
	        InputStream in = new BufferedInputStream(raw,65536);
	        byte[] data = new byte[contentLength];
	        int bytesRead = 0;
	        int offset = 0;
	        while (offset < contentLength) {
	            bytesRead = in.read(data, offset, data.length - offset);
	            if (bytesRead == -1)
	                break;
	            offset += bytesRead;
	        }
	        in.close();

	        if (offset != contentLength) {
	            throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
	        }

	        return data;
	        
	    }
		
		public void run(){
			try {
				TileLoader.this.tileLoaded(tile,load());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

	
	
}
