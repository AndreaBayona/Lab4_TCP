package cliente;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Cliente extends Thread {

    //======================================
    // CONSTANTES
    //======================================
    private static String RUTA_ARCHIVOS = "./data/mediaCliente/";
    private static final int PUERTO = 9090;
    private static String RUTA_LOGS = "./data/logsCliente/";
    private static String RUTA_CANTIDAD_CLIENTES = "./data/logsCliente/ClienteCounter.txt";
    //======================================
    // COMUNICACION
    //======================================
    private static String SALUDO = "HELLO";
    private static String LISTO = "LISTO";
    private static String OK = "OK";
    private static String ERROR = "ERROR";
    private static int BUFFER_SIZE = 1024;
    private static String NOMBRE_RECIBIDO = "ArchRecibido";
    private static String SIZE_RECIBIDO = "SizeRecibido";
    private static String HASH_RECIBIDO = "HashRecibido";


    //======================================
    // ATRIBUTOS
    //======================================
    private Socket socketCliente;
    private BufferedReader lector;
    private PrintWriter escritor;
    private String nombreArchivo;
    private int idLog;
    private long tiempoEjecucion;
    private int tamanioArch;
    

    //======================================
    // CONSTRUCTORES
    //======================================

    /**
     *
     */
    public Cliente() {
        try {
            socketCliente = new Socket("localhost", PUERTO);
            idLog = numeroCliente();
            System.out.println("ID DEL CLIENTE " + idLog);
            nombreArchivo = "";
            
            tiempoEjecucion = 0;
            tamanioArch = 0;
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    //======================================
    // METODOS
    //======================================

 
    public void run() {
        try {
           
        	
            lector = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
            escritor = new PrintWriter(socketCliente.getOutputStream(), true);
            BufferedInputStream bis;
            String linea;
			
            escritor.println(SALUDO);
			System.out.println("CLIENTE " + idLog + ": Iniciando conexion con el servidor");
			
			linea = lector.readLine();
			
			if (linea.equals(SALUDO)) {
				System.out.println("CLIENTE " + idLog + ": Conectado al servidor");
				escritor.println(LISTO);
				
				linea = lector.readLine();
				nombreArchivo = linea;
				escritor.println(NOMBRE_RECIBIDO);
				System.out.println("CLIENTE " + idLog + ": nombre recibido: " + linea);

				linea = lector.readLine();
				escritor.println(SIZE_RECIBIDO);
				tamanioArch = Integer.parseInt(linea);
				System.out.println("CLIENTE " + idLog + ": tamanio recibido: " + linea);

			}
			
			//Fase 3: recibir archivo
			
			byte[] buffer = new byte[1024];
			bis = new BufferedInputStream(socketCliente.getInputStream());		
			File file = new File(RUTA_ARCHIVOS + "new_"+ idLog + "_"+nombreArchivo);
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

						
			String hash = lector.readLine();
			System.out.println("CLIENTE " + idLog + ": hash recibido: " + hash);

			
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			
			int count = 0;	
			
			long t1 = System.currentTimeMillis();
			
//			while ((count=bis.read(buffer)) != -1){
//				bos.write(buffer, 0, count);
//				digest.update(buffer, 0, count);
//			}
			int size = tamanioArch;
			
			while (size > 0 && (count = bis.read(buffer, 0, (int) Math.min(buffer.length, size))) > 0) {
	            System.out.println("bytes leidos " + count);
	            digest.update(buffer, 0, count);
	            bos.write(buffer, 0, count);
	            size -= count;
	        }
			
			long t2 = System.currentTimeMillis();
			
			tiempoEjecucion = t2 - t1;
			
			bos.close();
			
			boolean integridad = comprobarHash(digest, hash);
			
			if(integridad) {
				System.out.println("CLIENTE " + idLog + ": archivo recibido correctamente: ");
				 escritor.println(OK);
				
			}
			else {
				System.out.println("CLIENTE " + idLog + ": error en la recepcion del archivo: ");
				escritor.println(ERROR);
			}
			
			socketCliente.close();
			escritor.close();
			lector.close();
			System.out.println("Esta cerrado socket cliente " + socketCliente.isClosed());
			construirLog(integridad);

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        } 

    }
    
	
    public static void main(String[] args) {
        Cliente cliente = new Cliente();
        cliente.run();
    }
    
    
    /**
     * Compara el hash enviado por el servidor con el hash calculado al archivo enviado
     *
     * @param digest
     * @param hash
     */
    private boolean comprobarHash(MessageDigest digest, String hash) {
        byte[] bytes2 = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes2.length; i++) {
            sb.append(Integer.toString((bytes2[i] & 0xff) + 0x100, 16).substring(1));
        }
        String ss = sb.toString();
		System.out.println("CLIENTE " + idLog + ": hash calculado: " + ss);
		 
        boolean iguales = ss.equals(hash);
        System.out.println("ARCHIVOS SON IGUALES " + ss.equals(hash));

        if (iguales)
            return true;
        else
            return false;
    }

    


    /**
     * Registrar un evento en el Log
     *
     * @param msg mensaje que se desea registrar
     */
    public void registrarEnLog(String msg) {
        File f = new File(RUTA_LOGS + "log" + idLog + ".txt");
        try {
            FileWriter fwriter = new FileWriter(f, true);
            fwriter.append(msg);
            fwriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo encargado de construir un Log con una estructura dada para guardar y luego registrarlo por medio del metodo registrarEnLog
     */
    public void construirLog(boolean b) {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(date);


        String log = "---------------------- PRUEBA" + idLog + " ----------------------\n";
        log += "Fecha y Hora: " + strDate + "\n";
        log += "------------------ INFO ARCHIVO ENVIADO --------------------\n";
        log += "nombre: " + nombreArchivo + "\n";
        log += "tamanio archivo: " + tamanioArch + " bytes\n";
        log += "-------------------INFO CLIENTE-------------------------------\n";
        log += "id Cliente: " + idLog + " \n";
        log += "IP Cliente: " + socketCliente.getInetAddress() + " \n";
        log += "Port Cliente: " + socketCliente.getLocalPort() + " \n";
        log += "--------------------- CONEXIONES ----------------------\n";
        log += "Tiempo ejecucion: " + tiempoEjecucion + " milisegundos\n";
        log += "Integridad de los datos : " + b + "\n";
        log += "\n";

        registrarEnLog(log);
    }

    /**
     * Encargado de obtener el idCliente y admeas incrementarlo en el archivo
     *
     * @return el id del cliente
     * @throws IOException
     */
    public int numeroCliente() throws IOException {
        File file = new File(RUTA_CANTIDAD_CLIENTES);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String ss = br.readLine();
        int idLog = Integer.parseInt(ss);
        idLog++;
        FileWriter fw = new FileWriter(file);
        fw.append(String.valueOf(idLog));
        fw.close();

        return idLog;
    }

}
