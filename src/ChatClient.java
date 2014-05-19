import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class ChatClient {
	private Socket clientSocket;
    PrintWriter out;
    BufferedReader in;
    int count;
    
	public ChatClient(String ip, int port){

		try {
			clientSocket = new Socket(ip, port);
			System.out.println("This is :"+java.net.Inet4Address.getLocalHost()+" Sock :"+clientSocket.getLocalPort());
			out = new PrintWriter(clientSocket.getOutputStream(), true);
	    	in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			count=0;
		} catch (UnknownHostException e) {
            System.err.println("Don't know about host: "+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	public void sendLine(String str){
		out.println(str);
		System.out.println("Sending --> "+str);
	}
    
    public void sendCmd(char ch) {
		out.write(ch);
		out.flush();
    	System.out.println(++count+"--> Sending : "+(int)ch);
		Library.stayCool(10);
	}
    
    public String readData() throws IOException{
    	if(in.ready())
    	return in.readLine();
    	return "";
    }
    
    public boolean ready2Read() throws IOException{
    	return (in.ready());
    }
    
    public void close() throws IOException{
    	in.close();
    	out.close();
    	clientSocket.close();
    }
}

class Main2{

    public void main(String []args) throws IOException{
    	ChatClient client=new ChatClient("localhost", 4242);
    	String line="";
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	while(!line.equalsIgnoreCase("quit")){
    		if(br.ready()){
        		line=br.readLine();
        		client.sendLine(line);
    		}
    		
    		if(client.ready2Read())
    		System.out.println(client.readData());
    	}
    	client.close();
    }
}
