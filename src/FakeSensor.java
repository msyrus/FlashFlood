import java.io.IOException;
import java.net.InetAddress;


public class FakeSensor extends Thread{
	private int sensorID, networkID, dIn, aIn, state, accuracy, delay;

	Client fakeClient;
	String data="7E";
	String s[];
	public FakeSensor(InetAddress ip, int port, int sID, int nID, int digital, int analog, int st, int acc, int delay){
		try {
			fakeClient = new Client(ip, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		sensorID = sID;
		networkID = nID;
		dIn = digital;
		aIn = analog;
		state = st;
		accuracy = acc;
		this.delay = delay;
		
		int len=1+8+2+1+1+2+1+((dIn>0)?2:0)+Library.countBit(aIn, 8)*2;
		data+=Library.addSpace(Integer.toHexString(len), 4);
		data+="92";
		data+=Library.addSpace(Integer.toHexString(sensorID),16);
		data+=Library.addSpace(Integer.toHexString(networkID),4);
		data+="0201";
		data+=Library.addSpace(Integer.toHexString(dIn), 4);
		data+=Library.addSpace(Integer.toHexString(aIn), 2);
		encode();
	}

	public void encode(){
		String d=data;
		System.out.println("State-->"+state);
		if(dIn<=0){
			return;
		}
		switch(state){
		case 0:
			d+="0000";
			break;
		case 1:
			d+="0001";
			break;
		case 2:
			d+="0003";
			break;
		case 3:
			d+="0007";
			break;
		case 4:
			d+="0015";
			break;
		default:			/// ERROR STATE or WRONG DATA
			d+="00"+Library.addSpace(""+(Math.round(Math.random()*100)%15),2);
			break;
		}
		s=new String[(d.length()/2)+1];
		for(int i=0; i<s.length-1; i++)
			s[i]=d.substring(i*2,i*2+2);
		s[s.length-1]=Library.calculateChkSum(s);
	}

	public void changeState(int st){
		state=st;
		encode();
	}

	public void sendData() {
		int cState=state;
		boolean fake = false;
		if((Math.random()*100)>accuracy){
			changeState(5);
			fake=true;
		}
		for(int i=0; i<s.length; i++)
			fakeClient.sendLine(s[i]);
		if(fake) changeState(cState);
	}
	
	public void run(){
		super.run();
		while(true){
			sendData();
			Library.stayCool(delay);
		}
	}

	public static void main(String[] args) throws IOException {

		FakeSensor fc=new FakeSensor(InetAddress.getByName("localhost"), 4242, 200, 200, 15, 0, 2, 50, 1000);
		fc.run();
	}
}
