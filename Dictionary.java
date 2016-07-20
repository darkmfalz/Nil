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
		    String sql = "create table if not exists Words (word text PRIMARY KEY, pos text, frequency real, sentences text, context text)";
		    stmt.executeUpdate(sql);
		    stmt.close();
		    stmt = c.createStatement();
		    sql = "create table if not exists Rank (rank real PRIMARY KEY, word text)";
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
	public static void insertWord(String word, String sentence) throws SQLException{
		
		//MUST CLEAN UP TO ALLOW WORDS MULTIPLE PARTS OF SPEECH
		
		if(wordInDictionary(word))
			updateWordFrequency(word, sentence);
		else
			addWord(word, sentence);
		
	}
	
	private static void addWord(String word, String sentence){
		
		try{
			Class.forName("org.sqlite.JDBC");
			Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
			PreparedStatement stmt = c.prepareStatement("INSERT INTO Words VALUES (?,?,1,?,NULL)");
			stmt.setString(1, word);
			stmt.setString(2, word);
			stmt.setString(3, sentence);
		    stmt.executeUpdate();
		    stmt.close();
		    c.close();
		}
		catch(Exception e){
			System.err.println(word);
			e.printStackTrace();
		}
		
	}

	private static void updateWordFrequency(String word, String sentence){
		
		try{
			Class.forName("org.sqlite.JDBC");
			Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
			PreparedStatement stmt = c.prepareStatement("SELECT * from Words where word=?");
			stmt.setString(1, word);
			ResultSet rset = stmt.executeQuery();
			if(rset.next()){
				
				int frequency = rset.getInt("frequency");
				sentence = rset.getString("sentences") + " " + sentence;
				stmt.close();
				frequency++;
				stmt = c.prepareStatement("UPDATE Words set frequency=? and sentences=? where word=?");
				stmt.setInt(1, frequency);
				stmt.setString(2, sentence);
				stmt.setString(3, word);
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
	
	static boolean noWordPOS(String word){
		
		try{
			Class.forName("org.sqlite.JDBC");
			Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
			PreparedStatement stmt = c.prepareStatement("SELECT * from Words where word=? and pos is NULL");
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
	
	static void updateWordPOS(String word, String pos){
		
		try{
			Class.forName("org.sqlite.JDBC");
			Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
			PreparedStatement stmt = c.prepareStatement("SELECT * from Words where word=?");
			stmt.setString(1, word);
			ResultSet rset = stmt.executeQuery();
			if(rset.next()){
				
				stmt.close();
				stmt = c.prepareStatement("UPDATE Words set pos=? where word=?");
				stmt.setString(1, pos);
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
	
	static int vocabSize(){
		
		try{
			Class.forName("org.sqlite.JDBC");
			Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
			PreparedStatement stmt = c.prepareStatement("SELECT COUNT(*) from Words");
			ResultSet rset = stmt.executeQuery();
			int count = 0;
			if(rset.next()){
				count = rset.getInt(1);
			}
			stmt.close();
			c.close();
			return count;
		}
		catch(Exception e){
			e.printStackTrace();
			return 0;
		}
		
	}
	
	static String[] mostFreqWordsInit(int size) throws Exception{
		
		String[] mostFreq = new String[size];
		Class.forName("org.sqlite.JDBC");
		Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
		Statement stmt = c.createStatement();
		ResultSet rset = stmt.executeQuery("select * from Words order by frequency asc");
		for(int i = 0; i < size; i++){
			if(!rset.next())
				break;
			mostFreq[i] = rset.getString(1);
			
		}
		stmt.close();
		//Add to table Rank
		for(int i = 0; i < size; i++){
			
			PreparedStatement pstmt = c.prepareStatement("INSERT INTO Rank VALUES (?,?)");
			pstmt.setInt(1, i);
			pstmt.setString(2, mostFreq[i]);
		    pstmt.executeUpdate();
		    pstmt.close();
			
		}
		c.close();
		return mostFreq;
		
	}
	
	//Miscellaneous Utility
	public static void printTable() throws Exception{
		
		Class.forName("org.sqlite.JDBC");
		Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
		Statement stmt = c.createStatement();
		ResultSet rset = stmt.executeQuery("select * from Words order by frequency asc");
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
