import java.io.*; 
import java.net.*; 
import java.util.*;
import java.lang.*;

public class ReceiverThread extends Thread{		
	private Socket clientSocket = null;

	public ReceiverThread(Socket socket){
		clientSocket=socket;
	}
	
	public void run(){
		
		String serverResponse;
		BufferedReader inFromServer = null;
	
		while(true){
			try{
				inFromServer = new BufferedReader ( new InputStreamReader(clientSocket.getInputStream()));
				serverResponse = inFromServer.readLine(); 
				System.out.println(serverResponse);
				if(serverResponse.equals("Connection closed")){
					clientSocket.close();
					System.exit(0);
				}				
														
			}catch(Exception e){
				System.out.println("\nClient Thread (receiver) Error - " + e.getMessage());
				System.exit(0);
			}
		}
	}
}
