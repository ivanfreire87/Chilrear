import java.io.*; 
import java.net.*; 
import java.util.*;
import java.util.Hashtable;
import java.text.*;

public class ServerUserThread extends Thread{
	//records of users name + password pair
	private Hashtable<String, String> usersRecords = new Hashtable<String, String>();
	//login status of each user (true or false)
	private Hashtable<String, Boolean> usersLoginStatus = new Hashtable<String, Boolean>();
	//users list of interested
	private Hashtable<String, ArrayList<String>> usersInterested = new Hashtable<String, ArrayList<String>>();
	//users list of received messages
	private Hashtable<String, ArrayList<String>> usersMessages = new Hashtable<String, ArrayList<String>>();

	private Socket threadConnectionSocket = null;
	private String userName = null;	
	private DataOutputStream outToClient = null;
	
	public ServerUserThread(Socket socket, Hashtable<String, String> usersRecords1, Hashtable<String, Boolean> usersLoginStatus1, Hashtable<String, ArrayList<String>> usersInterested1, Hashtable<String, ArrayList<String>> usersMessages1){
		threadConnectionSocket = socket;
		usersRecords = usersRecords1;
		usersLoginStatus = usersLoginStatus1;
		usersInterested = usersInterested1;
		usersMessages = usersMessages1;		
	}
			
	public void run(){	
		String[] clientRequest = null;	
		BufferedReader inFromClient=null;
		
		String clientSentence = null;
		
		while(true) { 
			try{					
				inFromClient = new BufferedReader(new InputStreamReader(threadConnectionSocket.getInputStream())); 
				outToClient = new DataOutputStream(threadConnectionSocket.getOutputStream());				
								
			}catch(Exception e){
				System.out.println("Server (user thread - port: " + threadConnectionSocket.getPort() + ") Error: " + e.getMessage());
				break;
			}
			try {
				clientRequest = inFromClient.readLine().split(" ");
				System.out.println("New " + clientRequest[0] + " request (port: " + threadConnectionSocket.getPort() + ").");
				
			}catch(Exception e){
				System.out.println("In from client (port: " + threadConnectionSocket.getPort() + ") error: " + e.getMessage());
				break;
			}

			try{
				switch(clientRequest[0]){
					case "register":		
											if(clientRequest.length < 3)
												sendResponse("Missing name or password");
											else
												registerClient(clientRequest[1],clientRequest[2]);
											break;		
						
					case "login":			if(clientRequest.length < 3)
												sendResponse("Missing name or password");
											else
												loginClient(clientRequest[1],clientRequest[2]);
											break;					
						

					case "interest":		if(clientRequest.length < 2)
												sendResponse("Missing name");
											else	
												interestInUser(clientRequest[1]);
											break;
						
					case "post":			if(clientRequest.length < 2)
												sendResponse("Missing message");
											else{
												String message="";
												for(int i = 1; i < clientRequest.length; i++){
													if(i == clientRequest.length-1)
														message = message.concat(clientRequest[i]);
													else
														message = message.concat(clientRequest[i] + " ");
												}
												post(message);
											}
											break;				
						
					case "exit":			clientExit();
											return;
											
				}					
			}catch(NullPointerException e){
				e.printStackTrace();
			}catch(Exception e){
				System.out.println("Requests from client (port: " + threadConnectionSocket.getPort() + ") error: " + e.getMessage());
				break;
			}
		}
			
    }
		
	public void registerClient(String name, String password){
		if(userExists(name))
			sendResponse("Name already in use.");
		else{	
			registerUser(name,password);
			setLoginStatus(name,false);
			usersInterested.put(name, new ArrayList<String>());
			usersMessages.put(name, new ArrayList<String>());
			sendResponse("Register successful.");
		}
	}
	
	public void loginClient(String name, String password){
		if(userExists(name)){
			if(correctPassword(name,password)){
				userName = name;
				setLoginStatus(name,true);
				sendResponse("Login successful. Welcome " + name + "!");
				sendMessages(userName);
			}
			else
				sendResponse("Wrong password.");
		}
		else
			sendResponse("User not found.");
	}
	
	public void interestInUser(String name){
		if(userName == null)
			sendResponse("You must be logged in to be able to be interested in a user.");
		else{
			if(!userExists(name))
				sendResponse("User not found.");
			else if(alreadyInterestedIn(userName,name))
				sendResponse("You are already interested in " + name + ".");
			else{
				insertInterestIn(userName,name);
				sendResponse("You are now interested in " + name + ".");
			}
		}
	}
	
	public void post(String message){
		String date;
		Calendar calendar = Calendar.getInstance();

		SimpleDateFormat timeFormat = new SimpleDateFormat("dd/MM/YYYY HH:mm");

		date = timeFormat.format(calendar.getTime());
		 
		if(userName == null)
			sendResponse("You must be logged in to be able to post messages.");
		else{
			insertUserMessage(userName,date,message);
			sendResponse("Message posted successfully.");
		}
	}
	
	public void sendResponse(String response){
		try{				
			outToClient.writeBytes(response + "\n");	
		}catch(Exception e){
			System.out.println("Connection Error: " + e.getMessage());
		}
	}
	
	public void clientExit(){
		if(userName != null)
			setLoginStatus(userName,false);	
		System.out.println("Connection closed (port: " + threadConnectionSocket.getPort() + ").");
		sendResponse("Connection closed");
		try{
			threadConnectionSocket.close();	
		}catch(Exception e){
			System.out.println("Connection Error: " + e);
		}
	}
	
	public synchronized void setLoginStatus(String name, Boolean status){
		usersLoginStatus.put(name,status);
	}
	
	public synchronized void registerUser(String name, String password){
		usersRecords.put(name,password);
	}
	
	public synchronized Boolean userExists(String name){
		return usersRecords.containsKey(name);
	}
	
	public synchronized Boolean correctPassword(String name, String password){
		return usersRecords.get(name).equals(password);
	}

	public synchronized Boolean loggedIn(String name){
		return usersLoginStatus.get(name);
	}
	
	public synchronized Boolean alreadyInterestedIn(String user, String interestName){
		ArrayList<String> userInterested = usersInterested.get(interestName);
		return userInterested.contains(user);		
	}
		
	public synchronized void insertInterestIn(String interestedName, String interestName){
		ArrayList<String> userInterested = usersInterested.get(interestName);
		userInterested.add(interestedName);
		usersInterested.put(interestName,userInterested);	
	}
	
	public synchronized void sendMessages(String name){
		if(!(usersMessages.get(name).isEmpty())){
			for(String userMessage : usersMessages.get(name)){
				System.out.println("Sending message: " + userMessage);
				sendResponse(userMessage);	
			}	
		}
	}
	
	public synchronized void insertUserMessage(String senderName, String date, String message){
		
		ArrayList<String> messages = usersMessages.get(senderName);
		ArrayList<String> interestedMessages;
		String openBracket = "[";
		String closeBracket = "] ";
		messages.add(openBracket + date + closeBracket +
		             openBracket + senderName + closeBracket +
					 openBracket + message + closeBracket);			 
		usersMessages.put(senderName,messages);
		
		if(!usersInterested.get(senderName).isEmpty()){
			for(String interestedName : usersInterested.get(senderName)){
				if(usersLoginStatus.get(interestedName)){
					interestedMessages = usersMessages.get(interestedName);
					interestedMessages.add(openBracket + date + closeBracket +
										   openBracket + senderName + closeBracket +
										   openBracket + message + closeBracket);			 
					usersMessages.put(interestedName,interestedMessages);
				}
			}
		}				
	}
}
