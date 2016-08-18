package edu.upenn.cis455.webserver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * This class is to create and maintain a logger file for HTTP server
 * @author user
 *
 */
public class ServerLogger {
	
	private File logFile;
	private BufferedWriter writer;
	private static ServerLogger logger;
	
	public ServerLogger (String fileName){
		logFile = new File(fileName);
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile),"UTF-8"));
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			logger.writeFile(e.toString());
		}
	}
	
	public static ServerLogger getLogger(String fileName) {
		if (logger == null) {
			logger = new ServerLogger(fileName);
		}		
		return logger;
	}
	
	public void writeFile(String log) {
		try {
			writer.write(log + "\r\n");
		} catch (IOException e) {
			logger.writeFile(e.toString());
		}
	}
	
	public void flushWriter() {
        try {
            writer.flush();
        } catch (IOException e) {
        	logger.writeFile(e.toString());
        }
    }

    /**
     * Close the log file
     */
    public void closeLogFile() {
        try {
            writer.close();
        } catch (IOException e) {
        	logger.writeFile(e.toString());
        }
    }
	
	public void getWriterString(String string) {
		writer.toString();
	}

}
