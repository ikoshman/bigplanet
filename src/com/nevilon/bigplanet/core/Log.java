package com.nevilon.bigplanet.core;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {

	private static Logger logger = Logger.getLogger("MAIN");
	
	static{
		/*
		try {
			logger.setLevel(Level.ALL);
			FileHandler fileHandler = new FileHandler("/sdcard/bigplanet/log.txt");
			fileHandler.setFormatter(new SimpleFormatter());
			logger.addHandler(fileHandler);
			 ConsoleHandler conHdlr = new ConsoleHandler();
			 conHdlr.setFormatter(new SimpleFormatter());
			 logger.addHandler(conHdlr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}
	
	public static void message(String tag,String msg){
		String message = tag+":"+ msg; 
		System.out.println(message	);
		logger.info(message);
	}
	
		
}
