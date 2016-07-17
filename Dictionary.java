package nil;

import java.sql.*;

public class Dictionary {

	private static Connection c;
	private static String dictName;
	private static boolean connected;
	
	public static void connect(String dictName){
		
		Dictionary.dictName = dictName;
		c = null;
		try{
			
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
			connected = true;
			
		}
		catch(Exception e){
			
			e.printStackTrace();
			System.err.println(c);
			connected = false;
			System.exit(0);
			
		}
		
	}
	
	public static boolean isConnected(){
		
		return connected;
		
	}
	
	public static String getDictName(){
		
		return dictName;
		
	}

}
