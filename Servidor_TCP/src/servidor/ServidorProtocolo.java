package servidor;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ServidorProtocolo extends Thread {

    //======================================
    // CONSTANTES
    //======================================
    private static String RUTA_ARCHIVOS = "./data/media/";
    private static String RUTA_LOGS = "./data/logs/";


    //======================================
    // COMANDOS DE COMUNICACIÓN
    //======================================
    private static String SALUDO = "HELLO";
    private static String ERROR = "ERROR";
    private static String OK = "OK";

    //======================================
    // ATRIBUTOS
    //======================================
    private static Socket socketServidor;
    private DataInputStream reader;
    private DataOutputStream writer;
    private int idThread;
    private int logID;
    private String nombreArchivo;


    private long tiempo1;
    private  long tiempo2;

    //======================================
    // CONSTRUCTORES
    //======================================

    public ServidorProtocolo(Socket sc, int idThread) {
        try {
            this.idThread = idThread;
            socketServidor = sc;
            logID = -1;
            nombreArchivo = "";
            reader = new DataInputStream(socketServidor.getInputStream());
            writer = new DataOutputStream(socketServidor.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //======================================
    // METODOS
    //======================================

    @Override
    public void run() {
        try {
            recibirConexionCliente();
            enviarArchivoYHash(nombreArchivo);
            boolean res = confirmacionIntegridadArchivo();
            registrarInfoClienteLog(res);
            cerrarConexiones();

            System.out.println("Esta cerrado " + socketServidor.isClosed());
            this.socketServidor.close();

        } catch (IOException | NoSuchAlgorithmException e) {
            registrarEnLog("ERROR EN LA CONEXION DEL CLIENTE " + idThread + "\n");
            registrarEnLog(e.getMessage() + "\n");
            e.printStackTrace();

        }


    }

    /**
     * Recibe y establece la conexion con el cliente
     *
     * @throws IOException
     */
    private void recibirConexionCliente() throws IOException {
        String msg = reader.readUTF();
        System.out.println("mensaje del cliente: " + msg);
        if (msg.equals(SALUDO)) {
            writer.writeUTF(SALUDO);
            System.out.println("Saludo enviado");
        } else {
            cerrarConexiones();
            socketServidor.close();
            System.out.println("Protocolo incorrecto: " + msg);
        }
    }

    /**
     * Calcula el hash de un archivo, lo envia y tambien el archivo original en bytes
     *
     * @param nombreArch
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private void enviarArchivoYHash(String nombreArch) throws IOException, NoSuchAlgorithmException {

        System.out.println("NOMBRE  ---------"+nombreArchivo);
        File file = new File(RUTA_ARCHIVOS + nombreArch);


        //ENVIAR HASH-----------------------
        String hash = getFileHash(file);
        System.out.println("SERVIDOR: Hash enviado al cliente: " + hash);
        writer.writeUTF(hash);
        //----------------------------------

        //ENVIAR ARCHIVO -------------------
        writer.writeUTF(file.getName()); //envia el nombre y la extension del archivo
        writer.writeLong(file.length()); //envia el tamanio del archivo
        System.out.println("SERVIDOR: tamanioArc " + file.length());

        byte[] bytes = new byte[1024];
        InputStream in = new FileInputStream(file);
        OutputStream out = socketServidor.getOutputStream();

        int count;
        tiempo1 = System.currentTimeMillis();
        while ((count = in.read(bytes)) > 0) {
            out.write(bytes, 0, count);
        }
        //----------------------------------

        in.close();
        out.flush();
    }

    private void cerrarConexiones() throws IOException {
        writer.close();
        reader.close();
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
        String msg = reader.readUTF();
        if (msg.equals(OK)) {
            System.out.println("Archivo recibido con exito");
            tiempo2 = System.currentTimeMillis();
            return true;
        } else {
            System.out.println("Error en la lectura del archivo");
            return false;
        }
    }

    private void registrarEnLog(String s) {
        File f = new File(RUTA_LOGS + "log" + logID + ".txt");
        try {
            FileWriter fwriter = new FileWriter(f, true);
            fwriter.append(s);
            fwriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void registrarInfoClienteLog(boolean integridad){
        String log = "";
        long t = tiempo2 - tiempo1;
        log = "------------- Cliente " + idThread + "-------------\n";
        log += "Tiempo de transferencia del archivo " + nombreArchivo + ": " + String.valueOf(t) + " segundos\n";
        log += "Entrega exitosa: " + String.valueOf(integridad) + "\n";
        log += "------------------------------------";
        log += "\n";
        registrarEnLog (log);

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
}
