package main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import util.Constants;
import util.Digest;
import configuration.Configuration;

public class ServerThread implements Runnable {
	private Socket socket;
	private ObjectOutputStream outToClient;
	private ObjectInputStream inFromClient;
	private FileOutputStream out;
	private File file;
	private String command;
	private String name;
	private String parameter;
	private String answer;
	private String space;
	
	private String commandToExecute;
	
	public ServerThread(Socket socket) {
		space = Constants.SPACE;
		answer="";
		this.socket = socket;
		try {
			createStreams();
		} catch (IOException e) { e.printStackTrace(); }
	}

	public void run() {
		try {
			System.out.println("Connection incoming ...");

			String clientMessage = (String) receive();

			clientMessage = clientMessage.replace(Constants.COMMA, Constants.SPACE);

			System.out.println("clientMessage");

			System.out.println(clientMessage);

			execute(clientMessage);


			send(answer + "\n");
		} catch (IOException e) { e.printStackTrace(); }
		catch (ClassNotFoundException e) { e.printStackTrace(); }
		finally {
			try {
				if (outToClient != null) outToClient.close();
				if (inFromClient != null) inFromClient.close();
			} catch (IOException e) { e.printStackTrace(); }
		}
	}


	/**
	 * Execute command in the operative system.
	 * 
	 * @param command
	 *            To execute
	 */
	private String execute(String command) {
		System.out.println("Running: " + command);
		Runtime r = Runtime.getRuntime();
		String answer = "";
		try{
			Process process = Runtime.getRuntime().exec(
					new String[]{"cmd", "/c", command},
					null,
					new File("C:\\Program Files\\Oracle\\VirtualBox\\setupunacloud"));

			printResults(process);
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return answer;
	}
	public static void printResults(Process process) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = "";
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
	}
	private void send(Object o) throws IOException {
		outToClient.writeObject(o);
		outToClient.flush ( );
	}

	private Object receive() throws IOException, ClassNotFoundException {
		return inFromClient.readObject ( );
	}

	private void createStreams() throws IOException {
		inFromClient = new ObjectInputStream(socket.getInputStream());
		outToClient = new ObjectOutputStream(socket.getOutputStream());
	}
}
