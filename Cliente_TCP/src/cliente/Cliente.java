package cliente;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Cliente extends Thread {

    //======================================
    // CONSTANTES
    //======================================
    private static String RUTA_ARCHIVOS = "./data/mediaCliente/";
    private static final int PUERTO = 8080;



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

    //======================================
    // CONSTRUCTORES
    //======================================

    public Cliente() {
        try {
            socketCliente = new Socket("localhost", PUERTO);
            lector = new DataInputStream(socketCliente.getInputStream());
            escritor = new DataOutputStream(socketCliente.getOutputStream());
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
        this.establecerConexion();
        boolean recepcionCorrecta = this.recibirArchivoYHash();
        if(recepcionCorrecta){
            this.confirmaRecepcionCorrectaArchivo();
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

    public void enviarError() throws IOException {
        escritor.writeUTF(ERROR);
    }

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


}
