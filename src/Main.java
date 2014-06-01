import java.sql.SQLException;


public class Main {

	public static void main(String[] args) throws SQLException {
		DatabaseConnect db=new DatabaseConnect("xbee","firealarm");
		Integer te[]=new Integer[3];
		for(int i=0; i<3; i++) te[i]=100;
		Data it=new Data(600,te,"15,80");
		db.insertItem(it);
		Data x=db.findItem(10);
//		for(int i=0; i<x.length; i++)
			System.out.println(x);
	}

}
