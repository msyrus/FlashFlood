
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Multithreaded chat xbeeServer
 * Clients connect on port 4242 and give their name
 * Then the xbeeServer accepts input and broadcasts it to the other clients
 * Connect by running Client.java
 * 
 * @author Syrus
 */

public class XbeeServer extends Thread {
	private Integer id;
	private ServerSocket listen;								// for accepting connections
	private ArrayList<ClientHandler> handlers;					// all the connections with clients

	DatabaseConnect db;

	public XbeeServer(Integer id, ServerSocket listen, String database, String table) {
		this.id=id;
		this.listen = listen;
		handlers = new ArrayList<ClientHandler>();
		try {
			db=new DatabaseConnect(database,table);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Integer getServerId() {
		return id;
	}

	/**
	 * The usual loop of accepting connections and firing off new threads to handle them
	 */
	public void run() {
		while (true) {
			Socket sc;
			ClientHandler handler;
			try {
				sc=listen.accept();
				sc.getInputStream();
				handler = new ClientHandler(listen.accept(), this);
				handler.setDaemon(true);
				addHandler(handler);
				handler.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
	public synchronized void broadcast(ClientHandler from, String msg) {
		for (ClientHandler h : handlers) {
			if (h != from) {
				h.out.println(msg);
			}
		}
	}
	
	public synchronized Vector<Data> getData(){
		Vector<Data> data = new Vector<Data>();
		for(ClientHandler handler: handlers)
			data.addElement(handler.sensorData);
		return data;
	}


	/**
	 * Handles communication between a xbeeServer and one client
	 */
	public class ClientHandler extends Thread {
		private Socket sock;					// each instance is in a different thread and has its own socket
		private XbeeServer xbeeServer;				// the main xbeeServer instance
		private PrintWriter out;
		public Data sensorData;

		public ClientHandler(Socket sock, XbeeServer xbeeServer) {
			super("ClientHandler");
			this.sock = sock;
			this.xbeeServer = xbeeServer;
			
			JFrame frame = new JFrame("This is it");
			frame.setVisible(true);
			String str=JOptionPane.showInputDialog(frame, "Coordinate for SensorNode"+(xbeeServer.handlers.size()+1), "Location", JOptionPane.QUESTION_MESSAGE);
			String tok[] = str.split("[,]");
			int x = Integer.parseInt(tok[0]);
			int y = Integer.parseInt(tok[1]);
			sensorData = new Data(-1, x+","+y);
		}

		/**
		 * Insert or Update item in Database
		 */
		public void insertXbeeData(XbeeData v){
			Integer data[] = v.analogData;
			if(data==null || data.length==0) return;
			if(sensorData.getId()==-1)
				sensorData.setId(v.id);
			else if(sensorData.getId()!=v.id){
				System.err.println("Error ID doesn't match.\n");
				return;
			}
			sensorData.updateValue(data);
			if(sensorData.getCount()==0) {
				db.insertItem(sensorData);
				try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(""+sensorData.getId(), true)))) {
					data = sensorData.getValue();
					for(int j=0; j<data.length; j++)
						out.print(data[j]+",");
				}catch (IOException e) {
					//exception handling left as an exercise for the reader
				}
			}
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

						insertXbeeData(v);
					}
				}
				// Done
				System.out.println(" hung up");

				// Clean up -- note that also remove self from xbeeServer's list of handlers so it doesn't broadcast here
				xbeeServer.removeHandler(this);
				out.close();
				in.close();
				sock.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}