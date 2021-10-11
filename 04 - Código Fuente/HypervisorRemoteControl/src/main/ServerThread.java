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
	private File             file;

	private String command;
	private String name;
	private String parameter;
	
	private String answer;
	
	private Configuration configuration;
	private String vmsHome;
	private String vboxHome;
	private String scriptsHome;
	private String templatesHome;
	private String othersHome;
	
	private Logger log;
	
	private String space;
	
	private String commandToExecute;
	
	public ServerThread(Socket socket, Configuration configuration, Logger log) {
		this.configuration = configuration;
		this.log = log;
		vmsHome = configuration.getVirtualMachinesHome();
		vboxHome = configuration.getVirtualBoxHome();
		scriptsHome = configuration.getScriptsHome();
		templatesHome = configuration.getTemplatesHome();
		othersHome = configuration.getOthersHome();

		space = Constants.SPACE;
		answer="";
		this.socket = socket;
		try {
			createStreams();
		} catch (IOException e) { e.printStackTrace(); }
	}

	public void run() {
		try {
			log.info("Connection incoming ...");

			String clientMessage = (String) receive();

			log.info(clientMessage);
			
			String commandToExecute = "";
			
			// nuevo
			if (clientMessage.indexOf(":")!= -1){ // Tiene :
				String[] m = clientMessage.split(":");

				command = m[0];
				name = m[1];				
				
				if (m.length == 3) {
					parameter = m[2];					
				} else {
					parameter = "";
				}
				
				if (command.trim().equalsIgnoreCase("vmTurnOn")) {
					vmTurnOn();
				} else if (command.trim().equalsIgnoreCase("vmTurnOnH")) {
					vmTurnOnH();
				} else if (command.trim().equalsIgnoreCase("vmTurnOffACPI")) {
					vmTurnOffACPI();
				} else if (command.trim().equalsIgnoreCase("vmTurnOff")) {
					vmTurnOff();
				} else if (command.trim().equalsIgnoreCase("vmTurnOffSave")) {
					vmTurnOffSave();
				} else if (command.trim().equalsIgnoreCase("vmClone")) {
					vmClone();
				} else if (command.trim().equalsIgnoreCase("vmRegister")) {
					vmRegister();
				} else if (command.trim().equalsIgnoreCase("vmUnregister")) {
					vmUnregister();
				} else if (command.trim().equalsIgnoreCase("vmRestore")) {
					vmRestore();
				} else if (command.trim().equalsIgnoreCase("vmMacReset")) {
					vmMacReset();
				} else if (command.trim().equalsIgnoreCase("sendFile")) {
					// parameter is the type of the file
					// 
					sendFile(parameter);
				} else if (command.trim().equalsIgnoreCase("vmModify")) {
					vmModify();
				} 				
			} else { // No tiene :
				
				if (clientMessage.trim().equalsIgnoreCase("serverOff")) {
					serverOff();
				} else {
					// parameters of a bat file are separated by ;
					clientMessage = clientMessage.replace(Constants.SEMICOLON, Constants.SPACE);
					command(clientMessage);					
				}
			}
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

	private void command(String clientMessage) {
		answer = execute(scriptsHome+clientMessage);
		log.info(answer);
	}

	private void sendFile(String type) throws IOException, ClassNotFoundException {
//		String [] array = name.split(Constants.COLON);
//		String fileName = array[0];
//		String fileType = array[1];
		
		commandToExecute = "sendFile:" + name;
		log.info("Running: " + commandToExecute);
		
		String folder = "";
		if (type.equals("script")) {
			folder = scriptsHome;
		} else if (type.equals("template")) {
			folder = templatesHome;
		} else if (type.equals("vm")) {
			folder = vmsHome;
		} else {
			folder = othersHome;
		}
		
		long ta = System.nanoTime();
		receiveFile (name, folder);
		
		// recibe el md5
		byte[] receivedMd5 = (byte[]) receive();
		
		log.info("Received MD5: " + Digest.byteArrayHexadecimalToString(receivedMd5));

		// calcula el md5
		folder+="\\";
		byte[] calculatedMd5 = Digest.getDigestFile(name, folder, "MD5");

		log.info("Calculated MD5: " + Digest.byteArrayHexadecimalToString(calculatedMd5));

		// verifica el md5
		if (Digest.verifyDigest(calculatedMd5, receivedMd5)) {
			log.info("File received successfully.");
		} else {
			log.info("Integrity error in the received file.");
		}

		long tb = System.nanoTime();
		
		long transferTime = tb-ta;
		long size = file.length();
		
		answer += "File size: " + size + "bytes\t";
		answer += "File transfer time: " + transferTime/1000000000.0 + " s";
		log.info(answer);
	}

	private void serverOff() {
		System.exit(0);
	}
	
	private void vmModify() {
		if (parameter != null) {
			int cpu = 0;
			int mem = 0;
			if (Integer.parseInt(parameter)==1) {
				cpu = 1;
				mem = 1;
			} else if (Integer.parseInt(parameter)==2) {
				cpu = 2;
				mem = 2;			
			} else if (Integer.parseInt(parameter)==3) {
				cpu = 4;
				mem = 4;		
			} else if (Integer.parseInt(parameter)==4) {
				cpu = 6;
				mem = 8;
			}
			
			commandToExecute = vboxHome 
					+ "VBoxManage" + space
					+ "modifyvm" + space 
					+ name + space 
					+ "--cpus" + space
					+ cpu + space
					+ "--memory" + space
					+ mem * 1024;
			answer = execute(commandToExecute);
		} else {
			answer = "Error. Perfil de hardware no identificado.";
		}
	}
	
	private void vmRestore() {
		commandToExecute = vboxHome 
				+ "VBoxManage" + space
				+ "snapshot" + space 
				+ name + space 
				+ "restorecurrent";
		answer = execute(commandToExecute);
	}

	// VBoxManage unregistervm VMb
	private void vmUnregister() {
		commandToExecute = vboxHome 
				+ "VBoxManage" + space
				+ "unregistervm" + space 
				+ name;
		answer = execute(commandToExecute);
	}

	private void vmMacReset() {
		commandToExecute = vboxHome 
				+ "VBoxManage" + space
				+ "modifyvm" + space 
				+ name + space 
				+ "--macaddress1" + space 
				+ "auto";
		answer = execute(commandToExecute);
	}

	// VBoxManage registervm "D:\VMs\Debian8.4-01\Debian8.4-01.vbox"
	private void vmRegister() {
		commandToExecute = vboxHome 
				+ "VBoxManage" + space
				+ "registervm" + space
//				+ "\""
				+ vmsHome + name
				+ File.separator + name + ".vbox"
//				+ "\""
				;
		answer = execute(commandToExecute);
	}

	private void vmClone() {
		commandToExecute = vboxHome 
				+ "VBoxManage" + space
				+ "clonevm" + space 
				+ name + space
				+ "--name" + space 
				+ name + "-clone" + space
				+ "--mode machine" + space 
				+ "--register";
		answer = execute(commandToExecute);
	}

	private void vmTurnOffACPI() {
		commandToExecute = vboxHome 
				+ "VBoxManage" + space
				+ "controlvm" + space 
				+ name + space
				+ "acpipowerbutton";
		answer = execute(commandToExecute);
	}

	private void vmTurnOff() {
		commandToExecute = vboxHome 
				+ "VBoxManage" + space
				+ "controlvm" + space 
				+ name + space
				+ "poweroff";
		answer = execute(commandToExecute);
	}

	private void vmTurnOffSave() {
		commandToExecute = vboxHome 
				+ "VBoxManage" + space
				+ "controlvm" + space 
				+ name + space
				+ "savestate";
		answer = execute(commandToExecute);
	}

	private void vmTurnOnH() {
		commandToExecute = vboxHome 
				+ "VBoxManage" + space
				+ "startvm" + space 
				+ name + space 
				+ "--type headless";
		answer = execute(commandToExecute);
	}

	private void vmTurnOn() {
		commandToExecute = vboxHome 
				+ "VBoxManage" + space
				+ "startvm" + space 
				+ name;
		answer = execute(commandToExecute);
	}

	/**
	 * Execute command in the operative system.
	 * 
	 * @param command
	 *            To execute
	 */
	private String execute(String command) {
		log.info("Running: " + command);
		Runtime r = Runtime.getRuntime();
		Process p = null;
		String line = "";
		String answer = "";
		try {
			p = r.exec(command);

			BufferedReader input = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));

			while ((line = input.readLine()) != null) {
				log.info(line);
				answer += line + "\n";
			}
			input.close();
		} catch (Exception e) {
			log.info("Error while executing " + command);
		}
		return answer;
	}

	private void send(Object o) throws IOException {
		outToClient.writeObject(o);
		outToClient.flush ( );
	}

	private Object receive() throws IOException, ClassNotFoundException {
		return inFromClient.readObject ( );
	}

	private void receiveFile(String fileName, String folder) throws IOException {
		try {
			// Se crea el archivo con el nombre especificado en la carpeta
			// especificada.
			// Esta carpeta debe existir en el host del servidor.
			file = new File(folder + File.separator + fileName);
			out = new FileOutputStream(file);
			log.info("Creando el archivo " + fileName);

			// El servidor recibe el número de bloques que compone el archivo.
			int numberOfBlocks = ((Integer) receive()).intValue();

			// Se reciben uno a uno los bloques que conforman el archivo y se
			// almacenan en el archivo.
			byte[] buffer = null;
			for (int i = 0; i < numberOfBlocks; i++) {
				buffer = (byte[]) receive();
				out.write(buffer, 0, buffer.length);
				buffer = null;
			}
		}
		catch (ClassNotFoundException e) { e.printStackTrace(); }
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) {	e.printStackTrace(); }
		finally {
			if (out != null) out.close();
		}
	}

	private void createStreams() throws IOException {
		inFromClient = new ObjectInputStream(socket.getInputStream());
		outToClient = new ObjectOutputStream(socket.getOutputStream());
	}
}
