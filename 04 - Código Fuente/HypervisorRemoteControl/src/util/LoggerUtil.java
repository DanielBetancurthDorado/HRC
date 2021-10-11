package util;

/**
 * This class provides several methods related to the usage of the loggers.
 *
 * @author Carlos Eduardo Gómez Montoya
* @author Jose Gabriel Tamura Lara
* @author Harold Enrique Castro Barrera
*
* 2017
*/

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class LoggerUtil {
	
	/**
	 * This method return a logger to write in two files (debug and info) and the console.
	 * 
	 * @param classname It is the name of the class that appears in each entry of the log file.
	 * @param directoryPath It is the path of the directory where the log file will be stored.
	 * @param name It is the name of the log file.
	 * @return Logger It is the configured logger to use in any part of the source code.
	 * @throws IOException
	 */
	public static Logger getLoggers(String classname, String directoryPath, String name) throws IOException {
		Logger rootLogger = Logger.getLogger(classname);
		Layout pattern = new PatternLayout("%d{MM-dd-yyyy HH:mm:ss,SSS} %-5p %c:%m%n");
		
		String filename1 = directoryPath + File.separator + name + "-Debug";
		Appender fileAppender1 = new FileAppender(pattern, filename1, true);
		rootLogger.removeAllAppenders();
		rootLogger.setAdditivity(false);
		rootLogger.setLevel(Level.DEBUG);
		rootLogger.addAppender(fileAppender1);

		String filename2 = directoryPath + File.separator + name + "-Info";
		Appender fileAppender2 = new FileAppender(pattern, filename2, true);
		rootLogger.setLevel(Level.INFO);
		rootLogger.addAppender(fileAppender2);
		
		Appender consoleAppender = new ConsoleAppender(pattern);
		rootLogger.addAppender(consoleAppender);
				
		return rootLogger;
	}

	/**
	 * This method return a logger to write in a debug file and the console.
	 * 
	 * @param classname It is the name of the class that appears in each entry of the log file.
	 * @param directoryPath It is the path of the directory where the log file will be stored.
	 * @param name It is the name of the log file.
	 * @return Logger It is the configured logger to use in any part of the source code.
	 * @throws IOException
	 */
	public static Logger getLoggerDebug(String classname, String directoryPath, String name) throws IOException {
		Logger rootLogger = Logger.getLogger(classname);
		Layout pattern = new PatternLayout("%d{MM-dd-yyyy HH:mm:ss,SSS} %-5p %c:%m%n");
		String filename = directoryPath + File.separator + name + "-Debug";
		Appender fileAppender = new FileAppender(pattern, filename, true);
		rootLogger.removeAllAppenders();
		rootLogger.setAdditivity(false);
		rootLogger.setLevel(Level.DEBUG);
		rootLogger.addAppender(fileAppender);
		
		Appender consoleAppender = new ConsoleAppender(pattern);
		rootLogger.addAppender(consoleAppender);
				
		return rootLogger;
	}

	/**
	 * This method return a logger to write in an info file and the console.
	 * 
	 * @param classname It is the name of the class that appears in each entry of the log file.
	 * @param directoryPath It is the path of the directory where the log file will be stored.
	 * @param name It is the name of the log file.
	 * @return Logger It is the configured logger to use in any part of the source code.
	 * @throws IOException
	 */
	public static Logger getLoggerInfo(String classname, String directoryPath, String name) throws IOException {
		Logger rootLogger = Logger.getLogger(classname);
		Layout pattern = new PatternLayout("%d{MM-dd-yyyy HH:mm:ss,SSS} %-5p %c:%m%n");
		String filename = directoryPath + File.separator + name + "-Info";
		Appender fileAppender = new FileAppender(pattern, filename, true);
		rootLogger.removeAllAppenders();
		rootLogger.setAdditivity(false);
		rootLogger.setLevel(Level.INFO);
		rootLogger.addAppender(fileAppender);
		
		Appender consoleAppender = new ConsoleAppender(pattern);
		rootLogger.addAppender(consoleAppender);
				
		return rootLogger;
	}
}
