package server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerThread extends Thread{
	
	private Server server = null;
	private Socket socket = null;
	private final int ID;
	private DataInputStream streamInput = null;
	private DataOutputStream streamOutput = null;
	private boolean isDone = false;
	
	public ServerThread(Server server, Socket socket){
		this.server = server;
		this.socket = socket;
		this.ID = socket.getPort();
	}
	
	@Override
	public void run(){
		while(!isDone){
			try {
				server.handle(ID, streamInput.readUTF());
			} catch (IOException ioe) {

			} 
		}		
	}
	
	public void send(String message){
		try{
			streamOutput.writeUTF(message);
			streamOutput.flush();
			System.out.println(message + " gesendet zu Client: " + ID);
		}catch(IOException e){
			e.printStackTrace();
			server.removeThread(ID);
		}
	}

	public void askName() {
		try{
			streamOutput.writeUTF("askName");
			streamOutput.flush();
			System.out.println("Name von Client angefragt: " + ID);
		}catch(IOException e){
			e.printStackTrace();
			server.removeThread(ID);
		}
	}
	
	public void open() throws IOException{
		streamInput = new DataInputStream(socket.getInputStream());
		streamOutput = new DataOutputStream(socket.getOutputStream());
	}
	
	public void close() throws IOException{
		isDone = true;
		socket.close();
		streamInput.close();
	}

	public int getID(){
		return ID;
	}
	
}
