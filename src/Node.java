import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Vector;


public class Node {

	Integer id;
	Vector <Integer> friendsList, neighboursList;
	Vector <Client> friends, neighbours;
	Vector <InetAddress> ipList;
	
	XbeeServer xbeeServer;
	Server server;
	
	boolean state;
	
	Node(Integer Id, Vector <Integer> FriendList, Vector <Integer> NeighbourList, Vector <InetAddress> IpList) throws ArrayIndexOutOfBoundsException, IOException, UnknownHostException, Exception{
		if(Id<=0 || IpList == null || Id>=IpList.size())
			throw new Exception();
		else if(IpList.elementAt(Id) != InetAddress.getLocalHost())
			throw new Exception();
		
		for(Integer friend: FriendList){
			if(NeighbourList.indexOf(friend)!=-1){
				System.out.print("Friend can't be Neighbour.");
				throw new Exception();
			}
		}
		///******** Check and Remove Duplicate Elements from Friends and Neighbors *******///
		if(FriendList.indexOf(Id)!=-1) throw new Exception();
		if(NeighbourList.indexOf(Id)!=-1) throw new Exception();
		
		Integer maxId = Collections.max(FriendList);
		maxId = Math.max(maxId, Collections.max(NeighbourList));
		if(maxId>=IpList.size()) throw new ArrayIndexOutOfBoundsException(maxId);
		
		this.id = Id;
		this.friendsList = FriendList;
		this.neighboursList = NeighbourList;
		this.ipList = IpList;
		
		server = new Server(id, new ServerSocket(4242));
		(new Thread(server)).start();

		xbeeServer = new XbeeServer(id, new ServerSocket(4241), "xbee", "flashflood");
		(new Thread(xbeeServer)).start();
		
		state = false;
	}
	
	void buildNetwork() throws IOException{
		for(Integer friend: friendsList){
			Client client = new Client(friend,ipList.elementAt(friend), 4242);
			System.out.println("\nWaiting for response from friend#"+friend+" ("+ipList.elementAt(friend)+")");
			while (!client.ready2Read()) System.out.print('.');
			String msg = client.readData();
			if (msg.equalsIgnoreCase("Who?")) client.sendLine(""+id);
			else System.err.println("Unexpected Msg: "+msg);
			friends.add(client);
		}
		for(Integer neighbour: neighboursList){
			Client client = new Client(neighbour,ipList.elementAt(neighbour), 4242);
			System.out.println("\nWaiting for response from friend#"+neighbour+" ("+ipList.elementAt(neighbour)+")");
			while (!client.ready2Read()) System.out.print('.');
			String msg = client.readData();
			if (msg.equalsIgnoreCase("Who?")) client.sendLine(""+id);
			else System.err.println("Unexpected Msg: "+msg);
			neighbours.add(client);
		}
		state = true;
	}
	
	boolean addFriend(Integer id){
		if (id == this.id) return false;
		if(neighboursList.indexOf(id)!=-1) return false;
		if(state){
			Client client;
			try {
				client = new Client(id,ipList.get(id), 4242);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			friends.add(client);
		}
		friendsList.add(id);
		return true;
	}
	
	boolean addNeighbour(Integer id){
		if (id == this.id) return false;
		if(friendsList.indexOf(id)!=-1) return false;
		if(state){
			Client client;
			try {
				client = new Client(id,ipList.get(id), 4242);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			neighbours.add(client);
		}
		neighboursList.add(id);
		return true;
	}
	
	void sendMsgToFriends(String msg){
		if(!state) return;
		Vector <InetAddress> flist = new Vector<InetAddress>(0);
		for(Integer friend: friendsList)
			flist.add(ipList.elementAt(friend));
		
		server.broadcast(msg, flist);
	}

	void sendMsgToNeighbours(String msg){
		if(!state) return;
		Vector <InetAddress> nlist = new Vector<InetAddress>(0);
		for(Integer neighbour: neighboursList)
			nlist.add(ipList.elementAt(neighbour));
		
		server.broadcast(msg, nlist);
	}
	
	Vector<Pair> receiveMsg(){
		Vector<Pair> msgs = new Vector<Pair>(0);
		if(!state) return msgs;
		for(Client friend: friends){
			try {
				if(!friend.ready2Read()) continue;
				String msg = friend.readData();
				if(msg.equals("")) continue;
				Pair pair = new Pair(friend.getId(),msg);
				msgs.add(pair);
			} catch (IOException e) {
				System.err.println("Unable to read from "+friend.getId());
				e.printStackTrace();
			}
		}
		
		for(Client neighbour: neighbours){
			try {
				if(!neighbour.ready2Read()) continue;
				String msg = neighbour.readData();
				if(msg.equals("")) continue;
				Pair pair = new Pair(neighbour.getId(),msg);
				msgs.add(pair);
			} catch (IOException e) {
				System.err.println("Unable to read from "+neighbour.getId());
				e.printStackTrace();
			}
		}
		return msgs;
	}
	
	Vector<Data> getSensorData(){
		return xbeeServer.getData();
	}
	
	boolean getNodeState(){
		return state;
	}
}
