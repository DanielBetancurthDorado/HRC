package main;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import util.Digest;
import util.LoggerUtil;

/*
 * El cliente HRC envía el archivo especificado. Debe incluir la ruta relativa a partir
 * de la carpeta donde está el cliente HRC, y el tipo: template, vm, script u other.
 */
public class HypervisorRemoteControlClient {
	private final int BUFFER_SIZE = 1024;
	private Socket clientSocket;
	private ObjectOutputStream outToServer;
	private ObjectInputStream inFromServer;

	private File file;
	
	private Logger log;

	public HypervisorRemoteControlClient(String ip, String port, String command) {
		init(ip, port, command);
	}

	private void init(String ip, String port, String command) {
		loggerSetUp();
		
		log.info("Hypervisor Remote Control Client"
				+ "\tLast update: March 1st, 2019, Integrity control in sendFile");

		log.info("Running a remote task: Ip address: " + ip
				+ " Port: " + port + " Command: " + command);

		try {
			clientSocket = new Socket(ip, Integer.parseInt(port));

			outToServer = new ObjectOutputStream(clientSocket.getOutputStream());

			inFromServer = new ObjectInputStream(clientSocket.getInputStream());

			if (command.startsWith("sendFile")) {
				String[] m = command.split(":");
				String filename = m[1];
				// command.split(":")[1];
				send(command); // no va salto de linea como cuando se envia String
				
				sendFile(filename);
				
				// calcula el md5
				// el folder es "" porque el cliente tiene el archivo en la carpeta actual.
				byte[] md5 = Digest.getDigestFile(filename, "", "MD5");
				
				Digest.printByteArrayHexadecimal(md5);
				
				// envía el md5				
				send(md5);	
				
			} else {
				send(command);
			}

			String receivedMessage = (String) receive();
			log.info("FROM SERVER: " + receivedMessage);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (inFromServer != null)
					inFromServer.close();
				if (outToServer != null)
					outToServer.close();
				if (clientSocket != null)
					clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void loggerSetUp() {
		// logger setup
		try {
			String logType = "info";
			if (logType.equals("info") == true) {
				log = LoggerUtil.getLoggerInfo("HRCClient",
						"Logs",
						"HRCClient.log");

			} else if (logType.equals("debug") == true) {
				log = LoggerUtil.getLoggerDebug(
						"HRCClient",
						"Logs",
						"HRCClient.log");

			} else if (logType.equals("both") == true) {
				log = LoggerUtil.getLoggers("HRCClient",
						"Logs",
						"HRCClient.log");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void send(Object o) throws IOException {
		outToServer.writeObject(o);
		outToServer.flush();
	}

	private Object receive() throws IOException, ClassNotFoundException {
		return inFromServer.readObject();
	}

	private void sendFile(String fileName) throws IOException {
		FileInputStream fileIn = null;

		try {
			file = new File(fileName);

			// Abre el archivo solicitado.
			fileIn = new FileInputStream(file);

			// Se obtiene el tamaño del archivo y se imprime en la consola.
			long size = file.length();
			log.info("Size: " + size);

			// Se calcula el número de bloques y el tamaño del ultimo bloque.
			int numberOfBlocks = (int) (size / BUFFER_SIZE);
			int sizeOfLastBlock = (int) (size % BUFFER_SIZE);

			// Si el archivo no se puede partir en bloques de igual tamaño queda
			// un bloque adicional, más pequeño.
			if (sizeOfLastBlock > 0) {
				numberOfBlocks++;
			}

			// Se imprimen en la consola el número de bloques y el tamaño del
			// último bloque.
			log.info("Number of blocks: " + numberOfBlocks);
			log.info("Size of last block: " + sizeOfLastBlock);

			// Se envía el número de bloques al cliente.
			send(new Integer(numberOfBlocks));

			// Si todos los bloques son de igual tamaño, no hay un bloque al
			// final, más pequeño.
			if (sizeOfLastBlock == 0) {
				for ( int i = 0; i < numberOfBlocks; i++ )
				{
					byte [ ] buffer = new byte [ BUFFER_SIZE ];
					fileIn.read ( buffer );


					send ( buffer );
				}
			} else {
				// Si queda un bloque más pequeño al final, se envían todos los bloques
				// excepto el último.
				for ( int i = 0; i < numberOfBlocks - 1; i++ )
				{
					byte [ ] buffer = new byte [ BUFFER_SIZE ];
					fileIn.read ( buffer );
					send ( buffer );
				}

				// El bloque restante se envía a continuación.
				byte [ ] lastBuffer = new byte [ sizeOfLastBlock ];
				fileIn.read ( lastBuffer);
				send ( lastBuffer );
			}
		}
		// Puede lanzar una excepción por un archivo no encontrado.
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// Puede lanzar una excepción de entrada y salida.
		catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileName != null)
				fileIn.close();
		}
	}

	public static void main(String args[]) {

		if (args.length == 3) {
			new HypervisorRemoteControlClient(args[0], args[1], args[2]);
		} else {
		}
	}
}
