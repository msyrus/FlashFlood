import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;


public class Client {
	int id;
	private Socket clientSocket;
    PrintWriter out;
    BufferedReader in;
    Integer count;
    
	public Client(Integer Id, InetAddress ip, int port) throws IOException{

		clientSocket = new Socket(ip, port);
		System.out.println("I am: "+clientSocket.getLocalAddress()+" Connected to: "+clientSocket.getRemoteSocketAddress()+" Port :"+clientSocket.getLocalPort());
		out = new PrintWriter(clientSocket.getOutputStream(), true);
    	in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		count=0;
		id = Id;
	}
	
	Integer getId(){
		return id;
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
    	Client client=new Client(1,InetAddress.getByName("localhost"), 4242);
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
