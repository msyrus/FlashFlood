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
		
		if(FriendList.indexOf(Id)!=-1) throw new Exception();
		if(NeighbourList.indexOf(Id)!=-1) throw new Exception();
		
		Integer maxId = Collections.max(FriendList);
		maxId = Math.max(maxId, Collections.max(NeighbourList));
		if(maxId>=IpList.size()) throw new ArrayIndexOutOfBoundsException(maxId);
		
		this.id = Id;
		this.friendsList = FriendList;
		this.neighboursList = NeighbourList;
		this.ipList = IpList;
		
		server = new Server(id, new ServerSocket(4242), "xbee", "flashflood");
		server.getConnections();
		
		xbeeServer = new XbeeServer(id, new ServerSocket(4241), "xbee", "flashflood");
		server.getConnections();
		
		state = false;
	}
	
	void buildNetwork() throws IOException{
		for(Integer friend: friendsList){
			Client client = new Client(ipList.elementAt(friend), 4242);
			friends.add(client);
		}
		for(Integer neighbour: neighboursList){
			Client client = new Client(ipList.elementAt(neighbour), 4242);
			neighbours.add(client);
		}
	}
	
	boolean addFriend(Integer id){
		if (id == this.id) return false;
		if(neighboursList.indexOf(id)!=-1) return false;
		Client client;
		try {
			client = new Client(ipList.get(id), 4242);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		friends.add(client);
		friendsList.add(id);
		return true;
	}
	
	boolean addNeighbour(Integer id){
		if (id == this.id) return false;
		if(friendsList.indexOf(id)!=-1) return false;
		Client client;
		try {
			client = new Client(ipList.get(id), 4242);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		neighbours.add(client);
		neighboursList.add(id);
		return true;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
