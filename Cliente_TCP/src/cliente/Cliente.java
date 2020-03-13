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
    private static final int PUERTO = 8080;
    private static String RUTA_LOGS = "./data/logsCliente/";
    private static String RUTA_CANTIDAD_CLIENTES = "./data/logsCliente/ClienteCounter.txt";	
    //======================================
    // COMUNICACION
    //======================================
    private static String SALUDO = "HELLO";
    private static String ERROR = "ERROR";

    //======================================
    // ATRIBUTOS
    //======================================
    private Socket socketCliente;
    private DataInputStream lector;
    private DataOutputStream escritor;
    private String nombreArchivo;
    private  int idLog;
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
            lector = new DataInputStream(socketCliente.getInputStream());
            escritor = new DataOutputStream(socketCliente.getOutputStream());
            idLog=numeroCliente();
            nombreArchivo="";
            tiempoEjecucion=0;
            tamanioArch=0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //======================================
    // METODOS
    //======================================

    /**
     * Maneja la comunicacion entre el cliente y el servidor
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public void correrProtocoloCliente() throws IOException, NoSuchAlgorithmException {
    	long Tfin=0;
    	long Tinicio= System.currentTimeMillis();
        this.establecerConexion();
        boolean recepcionCorrecta = this.recibirArchivoYHash();
        if(recepcionCorrecta){
            this.confirmaRecepcionCorrectaArchivo();
            Tfin=System.currentTimeMillis();
            tiempoEjecucion=Tfin-Tinicio;
            construirLog(true);
        }
        else{
            this.enviarError();
        }
    }

    /**
     * Lee el hash enviado por el servidor, lee el archivo, calcula el hash sobre el archivo y compara
     * @return true si el archivo mantiene la integridad, false de lo contrario
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public boolean recibirArchivoYHash() throws IOException, NoSuchAlgorithmException {

        //Leer HASH -----------------------
        String hash = lector.readUTF();
        System.out.println("HASH: " + hash);
      
        //Leer archivo --------------------
        String nombreArch = lector.readUTF(); //Lee el nombre del archivo enviado y la extension
        System.out.println("Nombre Archivo : " + nombreArch);
        nombreArchivo= nombreArch;
        byte[] bytes = new byte[1024];
        OutputStream out = new FileOutputStream(RUTA_ARCHIVOS + "New_"+nombreArch);
        BufferedOutputStream bos = new BufferedOutputStream(out);

        System.out.println("pasa");
        
        int count;
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        long size = lector.readLong();
        System.out.println("SIZE ARCH " + size);
        while (size > 0 && (count = lector.read(bytes, 0, (int)Math.min(bytes.length, size))) > 0) {
             digest.update(bytes, 0, count);
            bos.write(bytes, 0, count);
            size -= count;
            System.out.println("bytes leidos " + count);
            tamanioArch+=count;
        }
        //cerrar stream
        bos.close();
        out.close();

        return comprobarHash(digest, hash);
    }

    /**
     * Compara el hash enviado por el servidor con el hash calculado al archivo enviado
     * @param digest
     * @param hash
     */
    private boolean comprobarHash(MessageDigest digest, String hash){
        byte[] bytes2 = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes2.length; i++) {
            sb.append(Integer.toString((bytes2[i] & 0xff) + 0x100, 16).substring(1));
        }
        System.out.println("Hash calculado sobre archivo "+sb);
        String ss = sb.toString();
        boolean iguales = ss.equals(hash);
        System.out.println("ARCHIVOS SON IGUALES " + ss.equals(hash));

        if(iguales)
            return true;
        else
            return false;
    }

    public void establecerConexion() throws IOException {
        this.escritor.writeUTF(SALUDO);
        String msg = lector.readUTF();
        if(!msg.equals(SALUDO)){
            this.enviarError();
            this.cerrarConexiones();
            this.socketCliente.close();
        }
        System.out.println("Conexion establecida "+msg.equals(SALUDO));
    }


    
    public void confirmaRecepcionCorrectaArchivo() throws IOException {
        escritor.writeUTF("OK");
    }

    /**
     * Encargado de verificar y notificar si hubo un error
     * @throws IOException
     */
    public void enviarError() throws IOException {
        escritor.writeUTF(ERROR);
        registrarEnLog("ERROR EN ESTABLECER UNA CONEXION CON EL SERVIDOR");
        
    }

    /**
     * Cierra todas las conexiones del escritor y lector
     * @throws IOException
     */
    public void cerrarConexiones() throws IOException {
        escritor.close();
        lector.close();
    }


    public static void main(String[] args) {
        Cliente cliente = new Cliente();
        try {
            cliente.correrProtocoloCliente();
            cliente.cerrarConexiones();
            System.out.println("Esta cerrado socket cliente" + cliente.socketCliente.isClosed());

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Registrar un evento en el Log
     * @param msg mensaje que se desea registrar 
     */
    public void registrarEnLog(String msg)
    {
    	File f = new File(RUTA_LOGS+"log"+idLog+".txt");
        try {
            FileWriter fwriter = new FileWriter(f,true);
            fwriter.append(msg);
            fwriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Metodo encargado de construir un Log con una estructura dada para guardar y luego registrarlo por medio del metodo registrarEnLog
     */
    public void construirLog(boolean b)
    {
    	Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(date);
        
        

        String log = "---------------------- PRUEBA" + idLog +" ----------------------\n";
        log += "Fecha y Hora: " + strDate + "\n";
        log += "------------------ INFO ARCHIVO ENVIADO --------------------\n";
        log += "nombre: " + nombreArchivo + "\n";
        log += "tamanio archivo: " + tamanioArch + " bytes\n";
        log += "-------------------INFO CLIENTE-------------------------------\n";
        log += "id Cliente: " + idLog + " \n";
        log += "IP Cliente: " + socketCliente.getInetAddress() +  " \n";
        log += "Port Cliente: " + socketCliente.getLocalPort() + " \n";
        log += "--------------------- CONEXIONES ----------------------\n";
        log += "Tiempo ejecucion: " + tiempoEjecucion + " milisegundos\n";
        log += "Integridad de los datos : " + b + "\n";
        log += "\n";

        registrarEnLog(log);
    }

    /**
     * Encargado de obtener el idCliente y admeas incrementarlo en el archivo
     * @return el id del cliente 
     * @throws IOException
     */
    public int numeroCliente() throws IOException
    {
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
