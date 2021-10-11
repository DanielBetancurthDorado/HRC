package util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Digest {
	final static String SPACE = " ";
	// static String destino = SPACE + "\\\\Desktop-vahu1hi\\Compartida\\";
	final static String destino = SPACE + "Destination\\";

	private static BufferedReader br;

	public static byte[] getDigest(byte[] input, String algorithm) {
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			md.update(input);
			return md.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] getDigestFile(String filename, String folder, String algorithm) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(algorithm);
			FileInputStream in = new FileInputStream(folder + filename);
			byte[] buffer = new byte[1024];

			int length;
			while ((length = in.read(buffer)) != -1) {
				md.update(buffer, 0, length);
			}
			in.close();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return md.digest();
	}

	public static boolean verifyDigest(byte[] digestA, byte[] digestB) {
		if (digestA.length != digestB.length) {
			return false;
		}

		for (int i = 0; i < digestA.length; i++) {
			if (digestA[i] != digestB[i]) {
				return false;
			}
		}
		return true;
	}

	public static void printByteArrayHexadecimal(byte[] input) {
		System.out.println(byteArrayHexadecimalToString(input));
	}

	public static String byteArrayHexadecimalToString(byte[] input) {
		String out = "";
		for (int i = 0; i < input.length; i++) {
			if ((input[i] & 0xff) <= 0xf) {
				out += "0";
			}
			out += Integer.toHexString(input[i] & 0xff) + " ";
		}
		return out;
	}

	public static String execute(String command) {
		Runtime r = Runtime.getRuntime();
		Process p = null;
		String text = "";

		try {
			p = r.exec(command);

			String line;

			BufferedReader input = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			while ((line = input.readLine()) != null) {
				text += line + "\r\n";
			}
			input.close();
		} catch (Exception e) {
			System.err.println("Error ejecutando " + command);
		}
		return text;
	}

	public static void main(String[] args) {
		
	}
}
