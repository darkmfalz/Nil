package nil;

import java.sql.*;

public class Dictionary {

	private static Connection c;
	private static Statement stmt;
	private static String dictName;
	private static boolean connected;
	
	public static void connect(String dictName){
		
		Dictionary.dictName = dictName;
		c = null;
		stmt = null;
		try{
			
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
			connected = true;
			System.out.println("Connected successfully!");
			
			stmt = c.createStatement();
		    String sql = "create table if not exists Words (word text PRIMARY KEY, frequency real)";
		    stmt.executeUpdate(sql);
		    stmt.close();
		    c.close();
		    System.out.println("Connected table successfully!");
			
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
	
	//Adding words
	public static void insertWord(String word) throws SQLException{
		
		if(wordInDictionary(word))
			updateWordFrequency(word);
		else
			addWord(word);
		
	}
	
	private static void addWord(String word){
		
		try{
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
			PreparedStatement pstmt = c.prepareStatement("INSERT INTO Words VALUES (?,1)");
			pstmt.setString(1, word);
		    pstmt.executeUpdate();
		    pstmt.close();
		    c.close();
		}
		catch(Exception e){
			System.err.println(word);
			e.printStackTrace();
		}
		
	}

	private static void updateWordFrequency(String word){
		
		try{
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
			PreparedStatement pstmt = c.prepareStatement("SELECT * from Words where word=?");
			pstmt.setString(1, word);
			ResultSet rset = pstmt.executeQuery();
			if(rset.next()){
				
				int frequency = rset.getInt("frequency");
				pstmt.close();
				frequency++;
				pstmt = c.prepareStatement("UPDATE Words set frequency=? where word=?");
				pstmt.setInt(1, frequency);
				pstmt.setString(2, word);
				pstmt.executeUpdate();
				
			}
			pstmt.close();
			c.close();
		}
		catch(Exception e){
			System.err.println(word);
			e.printStackTrace();
		}
		
	}
	
	private static boolean wordInDictionary(String word){
		
		try{
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
			PreparedStatement pstmt = c.prepareStatement("SELECT * from Words where word=?");
			pstmt.setString(1, word);
			ResultSet rset = pstmt.executeQuery();
			boolean inDict = rset.next();
			pstmt.close();
			c.close();
			return inDict;
		}
		catch(Exception e){
			System.err.println(word);
			e.printStackTrace();
			return false;
		}
		
	}
	
	//Miscellaneous Utility
	public static void printTable() throws Exception{
		
		Class.forName("org.sqlite.JDBC");
		c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
		stmt = c.createStatement();
		ResultSet rset = stmt.executeQuery("select * from Words");
		ResultSetMetaData rsmd = rset.getMetaData();
		
		int columnsNumber = rsmd.getColumnCount();
		while (rset.next()) {
			//Print one row          
			for(int i = 1 ; i <= columnsNumber; i++){

			      System.out.print(rset.getString(i) + " "); //Print one element of a row

			}

			System.out.println();//Move to the next line to print the next row.           

		}
		stmt.close();
		c.close();
		
	}
	
	public static void dropTable() throws Exception{
		
		Class.forName("org.sqlite.JDBC");
		c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
		stmt = c.createStatement();
		stmt.executeUpdate("drop table Words");
		stmt.close();
		c.close();
		
	}
	
}
