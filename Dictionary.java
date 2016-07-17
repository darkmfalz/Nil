package nil;

import java.sql.*;

public class Dictionary {

	private static String dictName;
	
	public static void connect(String dictName){
		
		try{
			
			Class.forName("org.sqlite.JDBC");
			Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
			System.out.println("Connected successfully!");
			
			Statement stmt = c.createStatement();
		    String sql = "create table if not exists Words (word text PRIMARY KEY, frequency real)";
		    stmt.executeUpdate(sql);
		    stmt.close();
		    c.close();
		    System.out.println("Connected table successfully!");
		    Dictionary.dictName = dictName;
			
		}
		catch(Exception e){
			
			e.printStackTrace();
			System.exit(0);
			
		}
		
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
			Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
			PreparedStatement stmt = c.prepareStatement("INSERT INTO Words VALUES (?,1)");
			stmt.setString(1, word);
		    stmt.executeUpdate();
		    stmt.close();
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
			Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
			PreparedStatement stmt = c.prepareStatement("SELECT * from Words where word=?");
			stmt.setString(1, word);
			ResultSet rset = stmt.executeQuery();
			if(rset.next()){
				
				int frequency = rset.getInt("frequency");
				stmt.close();
				frequency++;
				stmt = c.prepareStatement("UPDATE Words set frequency=? where word=?");
				stmt.setInt(1, frequency);
				stmt.setString(2, word);
				stmt.executeUpdate();
				
			}
			stmt.close();
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
			Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
			PreparedStatement stmt = c.prepareStatement("SELECT * from Words where word=?");
			stmt.setString(1, word);
			ResultSet rset = stmt.executeQuery();
			boolean inDict = rset.next();
			stmt.close();
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
		Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
		Statement stmt = c.createStatement();
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
		Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
		Statement stmt = c.createStatement();
		stmt.executeUpdate("drop table Words");
		stmt.close();
		c.close();
		
	}
	
}
