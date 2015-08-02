import java.io.*; 
import java.net.*; 
import java.util.*;
import java.lang.*;

public class SenderThread extends Thread{		
	private Socket clientSocket = null;

	public SenderThread(Socket socket){
		clientSocket=socket;
	}
	
	public void run(){
		
		String command;		 
		List<String> validCommands = Arrays.asList("register", "login", "interest", "post", "exit");
		BufferedReader inFromUser = null;
		DataOutputStream outToServer = null;		
						
		/*LISTA DE COMANDOS*/
		System.out.println("AVAILABLE COMMANDS:");
		System.out.println("  register name password");
		System.out.println("  login name password");
		System.out.println("  interest name");
		System.out.println("  post message");
		System.out.println("  exit");
							
		inFromUser = new BufferedReader(new InputStreamReader(System.in));
				
		while(true){
			System.out.print("\nCommand: ");
			try{								
				command = inFromUser.readLine();
				outToServer = new DataOutputStream(clientSocket.getOutputStream());
	
				if(!validCommands.contains(command.split(" ")[0]))
					System.out.println("Invalid Command\n");				
				else
					outToServer.writeBytes(command + '\n');	
				Thread.sleep(100);	
			}catch(Exception e){
				System.out.println("\nClient Thread (sender) Error - " + e.getMessage());
				System.exit(0);
			}				
		}			
	}    
}
