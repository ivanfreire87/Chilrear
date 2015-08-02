import java.io.*; 
import java.net.*; 
import java.util.*;

public class Server{
	//records of users name + password pair
	static Hashtable<String, String> usersRecords = new Hashtable<String, String>();
	//login status of each user (true or false)
	static Hashtable<String, Boolean> usersLoginStatus = new Hashtable<String, Boolean>();
	//users list of interested
	static Hashtable<String, ArrayList<String>> usersInterested = new Hashtable<String, ArrayList<String>>();
	//users list of received messages
	static Hashtable<String, ArrayList<String>> usersMessages = new Hashtable<String, ArrayList<String>>();
	
	public static void main(String[] args){
		Integer port = Integer.parseInt(args[0]);
		ServerSocket welcomeSocket = null;	
		
		try{
			welcomeSocket = new ServerSocket(port); 
			System.out.println("Welcome socket created. Awaiting connections.\n");
			
		}catch(Exception e){
			System.out.println("Server (welcome socket creation) Error: " + e.getMessage());
		}
		
		try{
			Socket connectionSocket1;
			while(true) { 					
					connectionSocket1 = welcomeSocket.accept();
					System.out.println("Connection socket created. Source Port: " + connectionSocket1.getPort());
					ServerUserThread userThread = new ServerUserThread(connectionSocket1,usersRecords,usersLoginStatus,usersInterested,usersMessages);
					userThread.start();						
			}
		}catch(Exception e){
			System.out.println("Server (connection creation) Error: " + e.getMessage());
		}
			
    }
		
}
