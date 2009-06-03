package com.nevilon.moow.core;

/**
 * Обработчик сообщений от потока
 * 
 * @author hudvin
 * 
 */
public abstract class Handler {

	public  void handle(Object object){};

	public  void handle(RawTile tile, byte[] data){};

}
