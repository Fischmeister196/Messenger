package client;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;


public class Client implements Runnable{

	private String name;
	private Socket socket = null;
	private DataOutputStream outputStream = null;
	private Scanner input = null;
	private Thread thread = null;
	private ClientThread clientThread = null;
	private volatile boolean isDone = false;
	
	public Client(String ip, int port, String name){
		this.name = name;
		System.out.println("Verbindung zum Server wird aufgebaut...");
		try {
			socket = new Socket(ip, port);
			System.out.println("Verbunden: " + port);
			start();
			
		} catch (IOException e) {
			System.out.println(e);
		}
		
	}
	
	public void handle(String message){
		if (message.equals("ENDE!")){
			System.out.println("Drücke ENTER zum beenden...");
			stop();
		}else{
			if(message.equals("askName")) {
				try{
					outputStream.writeUTF("askName:" + this.getName());
				}catch(SocketException se){
					//socket is closed
				}catch(Exception e){
					e.printStackTrace();
				}
			}

			System.out.println(message);
		}
	}
	
	@Override
	public void run(){
		while(!isDone){
			String line = null;
			try{
				line = input.nextLine();
				outputStream.writeUTF(this.getName() + ":" + line);
			}catch(SocketException se){
				//socket is closed
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
		
	public void start() throws IOException{
		input = new Scanner(System.in);
		outputStream = new DataOutputStream(socket.getOutputStream());
		
		if(thread == null){
			clientThread = new ClientThread(this, socket);
			thread = new Thread(this);
			thread.start();	
		}
			 
	}
	
	public void close() throws IOException{
		isDone = true;
		outputStream.close();
		socket.close();
		input.close();
		clientThread.close();
		clientThread.interrupt();		
	}

	public void stop(){
		if(thread != null){
			isDone = true;
			thread = null;
		}
		try {
			close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getName() {
		return name;
	}
	
	public static void main(String [] args) throws IOException{
		Scanner initialInput = new Scanner(System.in);
		System.out.println("Dein Name:");
		String name = initialInput.nextLine();
		System.out.println("Server Name:");
		String host = initialInput.nextLine();
		System.out.println("Port:");
		int port = Integer.parseInt(initialInput.nextLine());
		if(host != null) {
			Client client = new Client(host, port, name);
		} else {
			Client client = new Client("localhost", port, name);
		}

	}


}
