package server;

public class ClientChat {
    int id;
    String name;
    String chat;

    public ClientChat(int id, String name){
        this.id = id;
        this.chat = new String();
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getChat() {
        return chat;
    }

    public String getName() {
        return name;
    }

    public void setChat(String chat) {
        this.chat = chat;
    }

    public void addMessage(String message){
        this.chat = this.chat + message + "\n";
    }
}
