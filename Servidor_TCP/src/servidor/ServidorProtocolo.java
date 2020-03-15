package servidor;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ServidorProtocolo extends Thread {

	//======================================
	// CONSTANTES
	//======================================
	private static String RUTA_ARCHIVOS = "./data/media/";
	private static String RUTA_LOGS = "./data/logs/";
	private static int TAMANO_PAQUETE = 1024;


	//======================================
	// COMANDOS DE COMUNICACIÓN
	//======================================
	private static String SALUDO = "HELLO";
	private static String ERROR = "ERROR";
	private static String LISTO = "LISTO";
	private static String OK = "OK";
	private static int BUFFER_SIZE = 1024;
	private static String NOMBRE_RECIBIDO = "ArchRecibido";
	private static String SIZE_RECIBIDO = "SizeRecibido";
	private static String HASH_RECIBIDO = "HashRecibido";


	//======================================
	// ATRIBUTOS
	//======================================
	private static Socket socketCliente;
	private BufferedReader reader;
	private PrintWriter writer;
	private int idThread;
	private int logID;
	private String nombreArchivo;
	private File file;


	private long tiempo1;
	private long tiempo2;

	private int packages;

	//======================================
	// CONSTRUCTORES
	//======================================

	public ServidorProtocolo(Socket sc, int idThread, int logId, String nombreArchivo) {

		this.idThread = idThread;
		socketCliente = sc;
		this.logID = logId;
		this.nombreArchivo = nombreArchivo;
		file = new File(RUTA_ARCHIVOS+nombreArchivo);
	}

	//======================================
	// METODOS
	//======================================

	@Override
	public void run() {
		try {
			String linea;
			reader = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
			writer = new PrintWriter(socketCliente.getOutputStream(), true);
			InputStream bis;
			OutputStream bos;


			//ETAPA 1 ------- Recibir saludo del cliente
			linea = reader.readLine();

			if(linea.equals(SALUDO)) {
				System.out.println("SERVIDOR " + idThread + ": Saludo recibido del cliente");

				writer.println(SALUDO);
				linea = reader.readLine();

				if(linea.equals(LISTO)) {
					System.out.println("SERVIDOR " + idThread + ": Enviando nombre archivo al cliente " + nombreArchivo);
					writer.println(nombreArchivo);
					linea = reader.readLine();
					if(linea.equals(NOMBRE_RECIBIDO)) {
						writer.println(file.length());

						linea = reader.readLine();
						System.out.println("SERVIDOR " + idThread + ": El cliente recibio el tamanio del archivo " + linea);

					}
				}
			}
			else {
				System.out.println("SERVIDOR " + idThread + ": No se pudo establecer la conexion con el cliente");
				socketCliente.close();
			}


			bis = new FileInputStream(file) ;
			bos =  socketCliente.getOutputStream(); 

			String s= getFileHash(file);


			ServidorCMD.giveLine("netstat -s");
			ServidorCMD.correr();
			String send=ServidorCMD.getResponse().get(88);

			writer.println(s);
			System.out.println("SERVIDOR " + idThread + ": Hash del archivo enviado: " + s);

			tiempo1 = System.currentTimeMillis();

			int in;
			byte[] byteArray = new byte[1024];	
			while ((in = bis.read(byteArray)) > 0){
				bos.write(byteArray,0,in);
			}	
			tiempo2 = System.currentTimeMillis();

			bos.flush();
			bis.close();


			linea = reader.readLine();
			boolean integridad = false;
			if(linea.equals(OK))
				integridad = true;


			ServidorCMD.correr();
			String get=ServidorCMD.getResponse().get(88);
			getPackages(send,get);

			System.out.println(idThread + " SERVIDOR: Esta cerrado " + socketCliente.isClosed());
			socketCliente.close();
			System.out.println("Esta cerrado " + socketCliente.isClosed());

			registrarInfoClienteLog(integridad);

		} catch (IOException e ) {
			registrarEnLog(idThread + "SERVIDOR: ERROR EN LA CONEXION DEL CLIENTE " + idThread + "\n");
			registrarEnLog(e.getMessage() + "\n");
			cerrarConexiones();
			e.printStackTrace();

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void cerrarConexiones()  {

		try {
			reader.close();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Calcula el hash del archivo que se pasa por parametro con SHA-1
	 *
	 * @param archivo
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	private String getFileHash(File archivo) throws IOException, NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-1");
		FileInputStream inputStream = new FileInputStream(archivo);
		byte[] byteArray = new byte[1024];
		int count = 0;
		while ((count = inputStream.read(byteArray)) != -1) {
			digest.update(byteArray, 0, count);
		}
		inputStream.close();
		byte[] bytes = digest.digest();
		StringBuilder stringB = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			stringB.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		digest.reset();

		return stringB.toString();
	}

	public boolean confirmacionIntegridadArchivo() throws IOException {
		String msg = reader.readLine();
		System.out.println(msg);
		while (!msg.equals(OK) && !msg.equals(ERROR)) {
			msg = reader.readLine();
		}

		if (msg.equals(OK)) {
			System.out.println("Archivo recibido con exito");
			tiempo2 = System.currentTimeMillis();
			return true;
		} else {
			System.out.println("Error en la lectura del archivo");
			return false;
		}
	}

	private synchronized void registrarEnLog(String s) {
		File f = new File(RUTA_LOGS + "log" + logID + ".txt");
		try {
			FileWriter fwriter = new FileWriter(f, true);
			fwriter.append(s);
			fwriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void registrarInfoClienteLog(boolean integridad) {
		String log = "";
		long t = tiempo2 - tiempo1;
		int pesoFile = (int) Math.round((file.length()/TAMANO_PAQUETE)+0.5);
		int perdidos = packages - pesoFile;


		log = "------------- Cliente " + idThread + "-------------\n";
		log += "Tiempo de transferencia del archivo " + nombreArchivo + ": " + String.valueOf(t) + " milisegundos\n";
		log += "Entrega exitosa: " + String.valueOf(integridad) + "\n";
		log += "Paquetes Enviados: "+packages+"\n";
		log += "Paquetes Transmitidos: "+pesoFile+"\n";
		log += "Paquetes Perdidos: "+perdidos+"\n";
		log += "------------------------------------";
		log += "\n";
		registrarEnLog(log);

	}

	public int getLogID() {
		return logID;
	}

	public void setLogID(int logID) {
		this.logID = logID;
	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

	public void setNombreArchivo(String nombreArchivo) {
		this.nombreArchivo = nombreArchivo;
	}

	public void getPackages(String send, String get) {
		int sendInfo = Integer.parseInt((send.split("="))[1].trim());
		int getInfo = Integer.parseInt((get.split("="))[1].trim());
		packages = getInfo - sendInfo;
	}
}
