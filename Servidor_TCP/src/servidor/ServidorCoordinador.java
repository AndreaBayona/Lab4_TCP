package servidor;

import java.io.*;
import java.net.ProtocolException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class ServidorCoordinador {

    //======================================
    // CONSTANTES
    //======================================
    private static String RUTA_ARCHIVOS = "./data/media/";
    private static String RUTA_LOGS = "./data/logs/";
    private static final int PUERTO = 9090;
    private static String RUTA_NUMERO_PRUEBAS = "./data/logs/num.txt";

    //======================================
    // ATRIBUTOS
    //======================================
    private static  ServerSocket socketServidor;
    private static int numeroConexiones;
    private  static String nombreArchivo;
    private static int idLog;
//    private static ExecutorService pool;
    static Object dormidos;

    public ServidorCoordinador (int numeroConexiones, String nombreArchivo, int idLog){

            dormidos = new Object();
            this.numeroConexiones = numeroConexiones;
            this.nombreArchivo = nombreArchivo;
            this.idLog = idLog;

//            pool = Executors.newFixedThreadPool(numeroConexiones);


    }

    public void iniciar() throws IOException {

        crearLogPrueba();
        ArrayList<Socket> sockets = new ArrayList<Socket>();
        try {
            int count = 0;
            socketServidor = new ServerSocket(PUERTO);
            int idThread = 0;
            System.out.println(socketServidor.getInetAddress());

            while(true) {
                Socket sc = socketServidor.accept();
                ServidorProtocolo sp = new ServidorProtocolo(sc, idThread, idLog, nombreArchivo);
 
                idThread++;
                sp.start();
//                count++;

                

//                synchronized (dormidos){
//                    if(count >= numeroConexiones)
//                        dormidos.notifyAll();
//                }


//                sockets.add(sc);
//                System.out.println("Size cola: " + sockets.size());
//
//                if(count >= numeroConexiones) {
//                    count-=numeroConexiones;
//                    System.out.println("Size count: " + count);
//                    for(int i = 0 ; i < numeroConexiones; i++){
//                        Socket temp = sockets.remove(0);
//
//
//                        pool.execute(sp);
//                    }
//                }
                
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
