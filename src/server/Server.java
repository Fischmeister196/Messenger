package server;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Server implements Runnable{
	
	private ServerSocket server = null;
	private Thread thread = null;
	private ArrayList<ServerThread> threadList = null;
	
	public Server(int portNumber){
	
		try{
			System.out.println("SERVER=====================================");
			
			System.out.println("Server-Port: " + portNumber + ", Warte auf Clients...");
			server = new ServerSocket(portNumber);
			server.setReuseAddress(true);
			System.out.println("Server erstellt: " + server);
			
			System.out.println("===========================================\n");

			threadList = new ArrayList<ServerThread>();
			
			start();
			
		}catch(IOException e){
			System.out.println(e);
		}
	}

	/**
	 * Startet einen neuen Thread
	 */
	public void start(){
		if(thread == null)
			thread = new Thread(this); thread.start();
	}
	
	public void stop(){
		if(thread != null)
			thread.interrupt(); thread = null;
	}

	/**
	 * Solange ein Thread läuft eitf run sudgeführt
	 */
	@Override
	public void run() {
		while(thread != null){
			try{
				System.out.println("CLIENT=====================================");
				System.out.println("Warte auf Clients...");
				addThread(server.accept());
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	/**
	 * Hinzufügen einen neuen Thread aka Client
	 * @param socket Verbindung zum neuen Client
	 */
	public void addThread(Socket socket){
		System.out.println("Verbunden mit Client: " + socket);
		System.out.println("===========================================\n");

		threadList.add(new ServerThread(this, socket));
		startThread();
	}

	/**
	 * Nach Hinzufügen des Threads durch addThread() wird der neune Thread gestartet
	 */
	public void startThread(){
		try{
			int newThread = threadList.size() - 1;
			threadList.get(newThread).open();
			threadList.get(newThread).start();
		}catch(IOException ioe){
			System.out.println("Thread konnte nicht gestartet werden: " + ioe);
		}
	}

	/**
	 * Methode zum finden von Thread ID in threadList
	 * @param id
	 * @return
	 */
	public int findThread(int id){
		
		int positionID = -1;
		
		for(ServerThread st : threadList){
			if(st.getID() == id){
				positionID = threadList.indexOf(st);
			}
		}
		
		return positionID;
	}

	/**
	 * Löschen eines Thread
	 * @param id
	 */
	public void removeThread(int id){
		
		int removePosID = findThread(id);
		
		if(removePosID != -1){
			ServerThread removeThread = threadList.get(removePosID);
			threadList.remove(removePosID);
			
			try{
				System.out.println("Removing client: " + id);
				removeThread.close();
			}catch(IOException ioe){
				System.out.println("Could not close thread: " + ioe);
			}
		}		
	}

	/**
	 * Senden von input an alle Clients
	 * @param id
	 * @param input
	 */
	public synchronized void handle(int id, String input){
		for(ServerThread c : threadList)
			if(c.getID() != id)
				c.send(id + ": " + input);
		
		if(input.equalsIgnoreCase("ENDE!")){
			threadList.get((findThread(id))).send("Tschüss vom Server!");
			removeThread(id);
		}			 
	}
	
	public static void main(String [] args){
		Scanner input = new Scanner(System.in);
		System.out.println("Server Port:");
		int port = input.nextInt();
		
		try {
			System.out.println("IP Adresse: " + InetAddress.getLocalHost() + "\n");
		} catch (UnknownHostException e) {
			System.out.println(e);
		}
		
		Server server = new Server(port);
		input.close();
	}

	
}

