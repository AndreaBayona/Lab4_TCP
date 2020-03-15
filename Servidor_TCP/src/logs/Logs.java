package logs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;

public class Logs {
	
	private static int logN = 0;
	
	public static void guardar(String informacion) throws IOException, NoSuchAlgorithmException {
		FileOutputStream archivo = new FileOutputStream("log\\log"+logN);
		ObjectOutputStream oos = new ObjectOutputStream(archivo);
		oos.writeObject(informacion);
		oos.close();
		logN++;
	}
	
	public static String leer(String dir) throws IOException, ClassNotFoundException {
		FileInputStream archivo = new FileInputStream(dir);
		ObjectInputStream ois = new ObjectInputStream(archivo);
		String texto = (String) ois.readObject();
		ois.close();
		return texto;
	}
}
