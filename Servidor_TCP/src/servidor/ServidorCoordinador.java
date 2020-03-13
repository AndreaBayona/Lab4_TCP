package servidor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;

public class ServidorCoordinador {

    //======================================
    // CONSTANTES
    //======================================
    private static String RUTA_ARCHIVOS = "./data/media/";
    private static String RUTA_LOGS = "./data/logs/";
    private static final int PUERTO = 8080;
    private static String RUTA_NUMERO_PRUEBAS = "./data/logs/num.txt";

    //======================================
    // ATRIBUTOS
    //======================================
    private ServerSocket socketServidor;
    private int numeroConexiones;
    private  String nombreArchivo;
    private int idLog;

    public ServidorCoordinador (int numeroConexiones, String nombreArchivo, int idLog){
//        try {
            this.numeroConexiones = numeroConexiones;
            this.nombreArchivo = nombreArchivo;
            this.idLog = idLog;

//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void iniciar() throws IOException {

        crearLogPrueba();
        ArrayList<ServidorProtocolo> threadsServ = new ArrayList<ServidorProtocolo>();
        try {
            int count = 0;
            socketServidor = new ServerSocket(PUERTO);
            int idThread = 0;
            System.out.println(socketServidor.getInetAddress());

            while(true) {
                Socket sc = socketServidor.accept();
                ServidorProtocolo sp = new ServidorProtocolo(sc, idThread);
                idThread++;
                count++;
                threadsServ.add(sp);
                if(count >= numeroConexiones) {
                    count-=numeroConexiones;
                    for(int i = 0 ; i < numeroConexiones; i++){
                        ServidorProtocolo temp = threadsServ.remove(i);
                        temp.setLogID(idLog);
                        temp.setNombreArchivo(nombreArchivo);
                        temp.start();
                    }
                }
            }
        } catch (IOException e) {

            registrarEnLog("ERROR CERRANDO CONEXIONES...\n");
            registrarEnLog(e.getMessage() + "\n");
            e.printStackTrace();
        }



    }

    private void registrarEnLog(String s){
        File f = new File(RUTA_LOGS+"log"+idLog+".txt");
        try {
            FileWriter fwriter = new FileWriter(f,true);
            fwriter.append(s);
            fwriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void crearLogPrueba() throws IOException {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(date);
        File file = new File(RUTA_ARCHIVOS + nombreArchivo);

        String log = "---------------------- PRUEBA" + idLog +" ----------------------\n";
        log += "Fecha y Hora: " + strDate + "\n";
        log += "------------------ INFO ARCHIVO ENVIADO --------------------\n";
        log += "nombre: " + nombreArchivo + "\n";
        log += "tamaño: " + file.length() + " bytes\n";
        log += "--------------------- CONEXIONES ----------------------\n";
        log += "\n";

        registrarEnLog(log);
    }

    public static void main(String[] args) {


        try {

            Scanner scan = new Scanner(System.in);

            File file = new File(RUTA_NUMERO_PRUEBAS);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String ss = br.readLine();
            System.out.println(ss);
            int idLog = Integer.parseInt(ss);
            idLog++;

            FileWriter fw = new FileWriter(file);
            System.out.println(idLog);
            fw.append(String.valueOf(idLog));
            fw.close();


            System.out.println("Ingrese el archivo que desea enviar");
            System.out.println("1 = video 1 - 100 MB");
            System.out.println("2 = video 1 - 250 MB");

            String nombreArchivoScan = scan.nextLine();
            String nombreArchivo = "";

            if(nombreArchivoScan.equals("1")){
                nombreArchivo = "video1.mp4";
            }
            else{
                nombreArchivo = "video2.mp4";
            }
            System.out.println("Ingrese la cantidad conexiones (max 25)");
            int numCon = Integer.parseInt(scan.nextLine());


            ServidorCoordinador sc = new ServidorCoordinador(numCon, nombreArchivo, idLog);
            sc.iniciar();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
