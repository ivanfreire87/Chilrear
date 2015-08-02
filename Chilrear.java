import java.io.*; 
import java.net.*; 
import java.util.*;
import java.lang.*;

public class Chilrear{		
	
	public static void main(String args[]){
		String hostname = args[0];
		Integer port = Integer.parseInt(args[1]);
		Socket clientSocket = null;
		
		try{
			clientSocket = new Socket(hostname,port);
				
			Thread sender = new SenderThread(clientSocket);
			Thread receiver = new ReceiverThread(clientSocket);
			
			sender.start();
			receiver.start();
			
		}catch(Exception e){
			System.out.println("Client Error - " + e.getMessage());
		}
	}    
}
