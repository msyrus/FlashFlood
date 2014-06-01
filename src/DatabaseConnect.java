/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.mysql.jdbc.Driver;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Syrus
 */

public class DatabaseConnect {
	String connectionURL;
	String QueryString;
	String table;
	Connection connection;
	Statement statement;
	ResultSet rs;
	int updateQuery;

	public DatabaseConnect(String database, String tableName) throws SQLException {
		connectionURL = "jdbc:mysql://localhost:3306/"+database;
		connection = DriverManager.getConnection(connectionURL, "root", "");
		statement = connection.createStatement();
		table=tableName;
		updateQuery=0;
		QueryString="";
	}

	public Data[] getItems()
	{
		try {
			selectAll();
			int x=updateQuery;
			if(x==0) return null;
			Data s[];
			rs.beforeFirst();
			s=new Data[x];
			int i=0;
			while (rs.next()) {
//				System.out.print("Retrived :"+rs.getInt("id"));
				s[i++]=new Data( rs.getInt("id"),rs.getString("value"), rs.getString("location"));
			}
			return s;
		} catch (SQLException ex) {
			Logger.getLogger(DatabaseConnect.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	public Data insertItem(Data it)
	{
		try {
			Data itm=findItem(it.getId());
			if(itm!=null){
				updateItem(it);
			}
			else{
				QueryString = "INSERT INTO "+table+" (id,value,location) VALUES "+"('"+it.getId()+"','"+it.getValueToString()+"','"+it.getLocation()+"')";
				int x=statement.executeUpdate(QueryString);
				if(x==0) return null; 
			}
			return findItem(it.getId());
		} catch (SQLException ex) {
			Logger.getLogger(DatabaseConnect.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}


	public Data findItem(int id)
	{
		Data t[]=getItems();
		if(t==null) return null;
		for(int i=0; i<updateQuery; i++)
			if(t[i].getId()==id) return t[i];

		return null;

	}

	public int removeItem(Data it)
	{
		try {
			int id=it.getId();
			QueryString = "DELETE FROM "+table+" WHERE id='"+id+"'";
			int x=statement.executeUpdate(QueryString);
//			selectAll();
			return x;
		} catch (SQLException ex) {
			Logger.getLogger(DatabaseConnect.class.getName()).log(Level.SEVERE, null, ex);
			return 0;
		}
	}

	public int updateItem(Data it)
	{
		try {

			QueryString = "UPDATE "+table+" SET value='"+it.getValueToString()+"', location='"+it.getLocation()+"' WHERE id='"+it.getId()+"'";
			//            System.out.println(QueryString);
			int x=statement.executeUpdate(QueryString);
//			selectAll();
			return x;
		} catch (SQLException ex) {
			Logger.getLogger(DatabaseConnect.class.getName()).log(Level.SEVERE, null, ex);
			return 0;
		}
	}

	public int selectAll()
	{
		try {
			QueryString = "SELECT * from "+table;
			rs = statement.executeQuery(QueryString);
			rs.last();
			updateQuery=rs.getRow();
			return updateQuery;
		} catch (SQLException ex) {
			Logger.getLogger(DatabaseConnect.class.getName()).log(Level.SEVERE, null, ex);
			updateQuery=0;
			return 0;
		}
	}

	public void deleteAll()
	{
		try {
			QueryString = "TRUNCATE TABLE "+table;
			statement.execute(QueryString);
			updateQuery=0;
		} catch (SQLException ex) {
			Logger.getLogger(DatabaseConnect.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void close()
	{
		try {
			statement.close();
			rs.close();
		} catch (SQLException ex) {
			Logger.getLogger(DatabaseConnect.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
