import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Multithreaded chat server
 * Clients connect on port 4242 and give their name
 * Then the server accepts input and broadcasts it to the other clients
 * Connect by running Client.java
 * 
 * @author Syrus
 */

public class Server {
	private Integer id;
	private ServerSocket listen;								// for accepting connections
	private ArrayList<ClientHandler> handlers;					// all the connections with clients

	DatabaseConnect db;

	public Server(Integer id, ServerSocket listen, String database, String table) {
		this.id=id;
		this.listen = listen;
		handlers = new ArrayList<ClientHandler>();
		try {
			db=new DatabaseConnect(database,table);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Integer getId() {
		return id;
	}

	/**
	 * The usual loop of accepting connections and firing off new threads to handle them
	 */
	public void getConnections() throws IOException {
		while (true) {
			Socket sc=listen.accept();
			sc.getInputStream();
			ClientHandler handler = new ClientHandler(listen.accept(), this);
			handler.setDaemon(true);
			addHandler(handler);
			handler.start();
		}
	}

	/**
	 * Adds the handler to the list of current client handlers
	 */
	public synchronized void addHandler(ClientHandler handler) {
		handlers.add(handler);
	}

	/**
	 * Removes the handler from the list of current client handlers
	 */
	public synchronized void removeHandler(ClientHandler handler) {
		handlers.remove(handler);
	}

	/**
	 * Sends the message from the one client handler to all the others (but not echoing back to the originator)
	 */
	public synchronized void broadcast(String msg, Vector<InetAddress> list) {
		for (ClientHandler h : handlers) {
			for(InetAddress member: list)
				if (h.getSocket().getInetAddress() == member) {
					h.out.println(msg);
				}
		}
	}

	/**
	 * Insert or Update item in Database
	 */
	public synchronized void insertItem(ClientHandler from, XbeeData v){
		boolean found=false;
		Integer data[] = v.analogData;
		if(data==null || data.length==0) return;
		for(int i=0; i<from.Nodes.size(); i++)
			if(from.Nodes.elementAt(i).getId()==v.id){
				found=true;
				from.Nodes.elementAt(i).updateValue(data);
				if(from.Nodes.elementAt(i).getCount()==0) {
					db.insertItem(from.Nodes.elementAt(i));
					try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(""+from.Nodes.elementAt(i).getId(), true)))) {
						data = from.Nodes.elementAt(i).getValue();
						for(int j=0; j<data.length; j++)
							out.print(data[j]+",");
						out.println();
					}catch (IOException e) {
						//exception handling left as an exercise for the reader
					}
				}
				break;
			}
		if(!found){
			JFrame frame = new JFrame("This is it");
			frame.setVisible(true);
			String str=JOptionPane.showInputDialog(frame, "Coordinate for Node"+(from.Nodes.size()+1), "Location", JOptionPane.QUESTION_MESSAGE);
			String tok[] = str.split("[,]");
			int x=Integer.parseInt(tok[0]);
			int y=Integer.parseInt(tok[1]);
			Data it=new Data(v.id, data, x+","+y);
			from.Nodes.addElement(it);
		}
	}

	/**
	 * Handles communication between a server and one client
	 */
	public class ClientHandler extends Thread {
		private Socket sock;					// each instance is in a different thread and has its own socket
		private Server server;				// the main server instance
		private PrintWriter out;
		public Vector<Data> Nodes;

		public ClientHandler(Socket sock, Server server) {
			super("ClientHandler");
			this.sock = sock;
			this.server = server;
			Nodes = new Vector<Data>();
		}
		
		public Socket getSocket(){
			return sock;
		}

		public void run() {
			try {
				System.out.println("someone connected");

				// Communication channel
				out = new PrintWriter(sock.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				out.write("Who?");
				out.flush();
				int id=in.read();
				if(id<0){
					;
				}
				else{
					while(sock.isConnected()){
						XbeeData v=Library.decode(in);
						System.out.println("ID: "+v.id);

						System.out.println("Digital Data :");
						for(int j=0; j<v.digitalData.length; j++)
							System.out.print(v.digitalData[j]+" ");
						System.out.println();

						System.out.println("Analog Data :");
						for(int j=0; j<v.analogData.length; j++)
							System.out.print(v.analogData[j]+" ");
						System.out.println();

						server.insertItem(this,v);
					}
				}
				// Done
				System.out.println(" hung up");

				// Clean up -- note that also remove self from server's list of handlers so it doesn't broadcast here
				server.removeHandler(this);
				out.close();
				in.close();
				sock.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("waiting for connections");
		new Server(1, new ServerSocket(4242), "xbee", "flashflood").getConnections();
	}
}