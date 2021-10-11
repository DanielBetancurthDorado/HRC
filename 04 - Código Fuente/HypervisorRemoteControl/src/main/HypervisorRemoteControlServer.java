package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

import util.LoggerUtil;
import configuration.Configuration;

/*
 * El servidor HRC recibe un archivo. El tipo de archivo (script, vm, template u other)
 * determina la carpeta en la que será guardado, de acuerdo con las propiedades.
 * 
 * Solo se pueden ejecutar scripts que estén almacenados en la carpeta correspondiente
 * a los scripts, de acuerdo con la propiedad de configuración.
 */
public class HypervisorRemoteControlServer {
	private Configuration configuration;
	private int port;

	private ServerSocket welcomeSocket;
	private Socket connectionSocket;

	private ObjectOutputStream outToClient;
	private ObjectInputStream inFromClient;

	private Thread thread;
	private Logger log;

	public HypervisorRemoteControlServer() {
		// reading the configuration properties
		configuration = new Configuration("hrc.properties");
		loggerSetUp();
		port = configuration.getPort();
		init();
	}

	private void init() {
		log.info("Hypervisor Remote Control Server is running ... in ther port: "
						+ port + "\tLast update: March 1st, 2019, Integrity control in sendFile");
		System.out.println("Usage: HRCClient " + myIP() + " " + port
				+ " command\n");
		System.out.println("Supported commands:");
		System.out
				.println("vmTurnOn:name\t\tStarts a VM\t\t\t\tExample: vmTurnOn:UbuntuServer16-4-01");
		System.out
				.println("vmTurnOnH:name\t\tStarts a headless VM\t\t\tExample: vmTurnOnH:UbuntuServer16-4-01");
		System.out
				.println("vmTurnOff:name\t\tStops a VM\t\t\t\tExample: vmTurnOff:UbuntuServer16-4-01");
		System.out
				.println("vmTurnOffACPI:name\tStops a VM with ACPI power button\tExample: vmTurnOffACPI:UbuntuServer16-4-01");
		System.out ////// CORREGIR, saving its state
				.println("vmTurnOffSave:name\tStops a VM saving the state of the VM\tExample: vmTurnOffSave:UbuntuServer16-4-01");
		System.out
				.println("vmClone:name \t\tClones a VM\t\t\t\tExample: vmClone:UbuntuServer16-4-01");
		System.out
				.println("vmRegister:name\t\tRegisters a VM\t\t\t\tExample: vmRegister:UbuntuServer16-4-01");
		System.out
				.println("vmUnregister:name\tUnregisters a VM\t\t\tExample: vmUnregister:UbuntuServer16-4-01");
		System.out
				.println("vmRestore:name \t\tResumes a VM in last snapshot\t\tExample: vmRestore:UbuntuServer16-4-01");
		System.out
				.println("vmMacReset:name\t\tResets the mac address of the first NIC\tExample: vmMacReset:UbuntuServer16-4-01");
		System.out.println("vmModify:name:profile\tModify the VM settings\t\t\tExample: vmModify:UbuntuServer16-4-01:2");
		System.out
		.println("sendFile:name:type\tSends a file\t\t\t\tExample: sendFile:script.bat:script");
		System.out
		.println("\t\t\t\t\t\t\t\tExample: sendFile:mv.vdi:vm");
		System.out
		.println("\t\t\t\t\t\t\t\ttypes accepted: script, template, vm, other");
		
		System.out
		.println("serverOff\t\tShutdowns the HRC server\t\tExample: serverOff");
		System.out
				.println("script.bat\t\tExecutes a .bat file\t\t\tExample: abc.bat;parameter1;parameter2, ... ");
		System.out
		.println("\t\t\t\t\t\t\t\tparameters are accepted, separated by ;");

		try {
			welcomeSocket = new ServerSocket(port);

			while (true) {
				connectionSocket = welcomeSocket.accept();

				thread = new Thread(new ServerThread(connectionSocket,
						configuration, log));
				thread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (outToClient != null)
					outToClient.close();
				if (inFromClient != null)
					inFromClient.close();
				if (connectionSocket != null)
					connectionSocket.close();
				if (welcomeSocket != null)
					welcomeSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void loggerSetUp() {
		// logger setup
		try {
			String logType = configuration.getLogType();
			if (logType.equals("info") == true) {
				log = LoggerUtil.getLoggerInfo(configuration.getLabelLogFile(),
						configuration.getPathLog(),
						configuration.getLogFileName());

			} else if (logType.equals("debug") == true) {
				log = LoggerUtil.getLoggerDebug(
						configuration.getLabelLogFile(),
						configuration.getPathLog(),
						configuration.getLogFileName());

			} else if (logType.equals("both") == true) {
				log = LoggerUtil.getLoggers(configuration.getLabelLogFile(),
						configuration.getPathLog(),
						configuration.getLogFileName());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String myIP() {
		String ip = "";

		try {
			URL whatismyip = new URL("http://checkip.amazonaws.com/");
			URLConnection connection = whatismyip.openConnection();
			connection.addRequestProperty("Protocol", "Http/1.1");
			connection.addRequestProperty("Connection", "keep-alive");
			connection.addRequestProperty("Keep-Alive", "1000");
			connection.addRequestProperty("User-Agent", "Web-Agent");

			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));

			ip = in.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ip;
	}

	public static void main(String args[]) {
		new HypervisorRemoteControlServer();
	}
}
