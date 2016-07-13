package nil;

import java.sql.*;

public class Dictionary {

	private static Connection c;
	private static boolean connected;
	
	public static void connect(String dictName){
		
		c = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
	}
	
}
