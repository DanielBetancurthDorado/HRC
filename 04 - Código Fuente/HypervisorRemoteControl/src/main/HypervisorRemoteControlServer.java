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
	private int port;
	private ServerSocket welcomeSocket;
	private Socket connectionSocket;
	private ObjectOutputStream outToClient;
	private ObjectInputStream inFromClient;
	private Thread thread;

	public HypervisorRemoteControlServer() {
		port = 3601;
		init();
	}

	private void init() {
		System.out.println("Hypervisor Remote Control Server is running ... in ther port: "+ port);
		System.out.println("Usage: HRCClient " + myIP() + " " + port + " command\n");
		System.out.println("Supported command:");
		System.out.println("script.bat\t\tExecutes a .bat file\t\t\tExample: abc.bat,parameter1,parameter2, ... ");
		System.out.println("\t\t\t\t\t\t\t\tparameters are accepted, separated by ,");

		try {
			welcomeSocket = new ServerSocket(port);

			while (true) {
				connectionSocket = welcomeSocket.accept();
				thread = new Thread(new ServerThread(connectionSocket));
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
