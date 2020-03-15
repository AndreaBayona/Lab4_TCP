package servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
* @modified David Támara
* @author Telematico
*/
public class ServidorCMD {
	
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
                	int i=0;
                    while ((salida = cmdInput.readLine()) != null) {
                    	i++;
                    	response.add(salida);
                    	//System.out.println(i+" "+salida);
                    }
                }
                entrada.close();
                cmdInput.close();
            } catch (IOException ex) {
                ex.getStackTrace();
            }
            
        
    }

}
