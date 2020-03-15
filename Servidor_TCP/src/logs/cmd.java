package logs;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 *
 * @author Telematico
 */
public class cmd{

    private static String query = "" ;
    private static ArrayList<String> response;

    public static void giveLine(String comand) {
    	query = comand;
    }
    
    public static ArrayList<String> getResponse() {
    	return response;
    }

    public static void correr() {
        
            Runtime r = Runtime.getRuntime();
            String salida = null;
            try {
                Process p = r.exec("cmd /c " + query);
                InputStreamReader entrada = new InputStreamReader(p.getInputStream());
                BufferedReader cmdInput = new BufferedReader(entrada);
                response = new ArrayList<String>();
                //mostramos la salida del comando
                if ((salida = cmdInput.readLine()) != null) {
                    while ((salida = cmdInput.readLine()) != null) {
                    	response.add(salida);
                    }
                }
            } catch (IOException ex) {
                ex.getStackTrace();
            }
        
    }
    
    public static void main(String[] args) {
    	//Caja de comandos| Se deben hacer estos 3 comandos para poder obtener la informacion del comando
        cmd.giveLine("ipconfig");
        cmd.correr();
        ArrayList<String> resp = cmd.getResponse();
        //Transformacion de resp para tranformarlo en un String y poder guardarlo como log
        String info = resp.clone().toString();
        String fin = "";
        try {
        	//Guardar Un Log -info se refiere a que se desea guardar (info debe ser el log completo)
			Logs.guardar(info);
			//Leer un log -log recive como parametro la direccion de un log para acceder a la informacion de este
			fin = Logs.leer("log\\log0");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println(fin);
    }
}
