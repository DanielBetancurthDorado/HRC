package configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class reads the properties that are required in the 
 * application, which can be changed by user.
 */
public class Configuration {
	private int port;
	private String virtualBoxHome;
	private String virtualMachinesHome;
	private String templatesHome;
	private String scriptsHome;
	private String othersHome;

	private String labelLogFile;
	private String pathLog;
	private String logFileName;
	private String logType;

	/**
	 * This is the constructor.
	 * 
	 * Load the configurations.
	 * 
	 * @param String
	 *            The properties filename.
	 */
	public Configuration(String filename) {
		Properties p = new Properties();
		InputStream is = null;

		try {
			is = new FileInputStream(filename);
			p.load(is);
			
			port = Integer.parseInt(p.getProperty("port"));

			virtualBoxHome = p.getProperty("virtualBoxHome");
			virtualMachinesHome = p.getProperty("virtualMachinesHome");
			templatesHome = p.getProperty("templatesHome");
			scriptsHome = p.getProperty("scriptsHome");
			othersHome = p.getProperty("othersHome");

			if (p.getProperty("labelLogFile") == null) {
				labelLogFile = "HRCServer";
			} else {
				labelLogFile = p.getProperty("labelLogFile");
			}

			if (p.getProperty("pathLog") == null) {
				pathLog = "Logs";
			} else {
				pathLog = p.getProperty("pathLog");
			}

			if (p.getProperty("logFileName") == null) {
				logFileName = "HRCServer.log";
			} else {
				logFileName = p.getProperty("logFileName");
			}

			if (p.getProperty("logType") == null) {
				logType = "info";
			} else {
				logType = p.getProperty("logType");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	/**
	 * This method returns the port number.
	 * 
	 * @return int The port number.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * This method returns the directory path where VirtualBox installed is.
	 * 
	 * @return String The directory path of VirtualBox.
	 */
	public String getVirtualBoxHome() {
		return virtualBoxHome;
	}

	/**
	 * This method returns the directory path where the virtual machines stored are.
	 * 
	 * @return String The directory path of the virtual machines repository.
	 */
	public String getVirtualMachinesHome() {
		return virtualMachinesHome;
	}
	
	/**
	 * This method returns the directory path where the templates stored are.
	 * 
	 * @return String The directory path of the templates repository.
	 */
	public String getTemplatesHome() {
		return templatesHome;
	}

	/**
	 * This method returns the directory path where the scripts stored are.
	 * 
	 * @return String The directory path of the scripts repository.
	 */
	public String getScriptsHome() {
		return scriptsHome;
	}

	/**
	 * This method returns the directory path where the other files stored are.
	 * 
	 * @return String The directory path of the files repository.
	 */
	public String getOthersHome() {
		return othersHome;
	}

	/**
	 * This method returns the label of each entry in the log file.
	 * 
	 * @return String The label.
	 */
	public String getLabelLogFile() {
		return labelLogFile;
	}

	/**
	 * This method returns the directory path where the log file will be stored.
	 * 
	 * @return String The pathLog.
	 */
	public String getPathLog() {
		return pathLog;
	}

	/**
	 * This method returns the log file name.
	 * 
	 * @return String The log file name.
	 */
	public String getLogFileName() {
		return logFileName;
	}
	
	/**
	 * This method returns the log type.
	 * 
	 * @return String The type of the log file.
	 */
	public String getLogType() {
		return logType;
	}
}
