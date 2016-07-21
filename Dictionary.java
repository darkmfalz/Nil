package nil;

import java.sql.*;
import java.util.Arrays;

public class Dictionary {

	private static String dictName;
	
	public static void connect(String dictName){
		
		try{
			
			Class.forName("org.sqlite.JDBC");
			Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
			Statement stmt = c.createStatement();
		    String sql = "create table if not exists Words (word text PRIMARY KEY, pos text, frequency real, sentences text, context text)";
		    stmt.executeUpdate(sql);
		    stmt.close();
		    stmt = c.createStatement();
		    sql = "create table if not exists Rank (rank real PRIMARY KEY, word text)";
		    stmt.executeUpdate(sql);
		    stmt.close();
		    c.close();
		    System.out.println("Connected database successfully!");
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
		
		//System.out.println(word  + ": " + sentence);
		
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
			stmt.setString(3, sentence + " . ");
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
			if(rset.next() && !word.matches("[\\.!?\\-;:,'\"\\(\\)]+|<[\\w]+>")){
				
				int frequency = rset.getInt("frequency");
				sentence = rset.getString("sentences") + sentence + " . ";
				stmt.close();
				frequency++;
				stmt = c.prepareStatement("UPDATE Words set frequency=? , sentences=? where word=?");
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
			PreparedStatement stmt = c.prepareStatement("SELECT word from Words");
			ResultSet rset = stmt.executeQuery();
			int count = 0;
			while(rset.next()){
				if(!rset.getString("word").matches("[\\.!?\\-;:,'\"\\(\\)]+|<[\\w]+>"))
					count++;
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
	
	static void updateWordContext(String word, double[] context){
		
		try{
			Class.forName("org.sqlite.JDBC");
			Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
			PreparedStatement stmt = c.prepareStatement("SELECT * from Words where word=?");
			stmt.setString(1, word);
			ResultSet rset = stmt.executeQuery();
			if(rset.next()){
				
				stmt.close();
				stmt = c.prepareStatement("UPDATE Words set context=? where word=?");
				stmt.setString(1, Arrays.toString(context));
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
	
	static String[] mostFreqWordsInit(int size) throws Exception{
		
		String[] mostFreq = new String[size];
		Class.forName("org.sqlite.JDBC");
		Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
		Statement stmt = c.createStatement();
		stmt.executeUpdate("drop table Rank");
		stmt.executeUpdate("create table if not exists Rank (rank real PRIMARY KEY, word text)");
		stmt.close();
		stmt = c.createStatement();
		ResultSet rset = stmt.executeQuery("select * from Words order by frequency desc");
		for(int i = 0; i < size; i++){
			
			if(!rset.next())
				break;
			mostFreq[i] = rset.getString("word");
			
		}
		stmt.close();
		//Add to table Rank
		for(int i = 0; i < size; i++){
			
			PreparedStatement pstmt = c.prepareStatement("INSERT INTO Rank VALUES (?,?)");
			pstmt.setInt(1, i+1);
			pstmt.setString(2, mostFreq[i]);
		    pstmt.executeUpdate();
		    pstmt.close();
			
		}
		c.close();
		return mostFreq;
		
	}
	
	static String[][] returnTaggableVocab() throws Exception{
		
		String[][] tagVocab = new String[vocabSize()][3];
		
		Class.forName("org.sqlite.JDBC");
		Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
		Statement stmt = c.createStatement();
		ResultSet rset = stmt.executeQuery("select * from Words");
		int i = 0;
		while(rset.next()){
			String word = rset.getString("word");
			if(!word.matches("[\\.!?\\-;:,'\"\\(\\)]+|<[\\w]+>")){
				tagVocab[i][0] = word;
				tagVocab[i][1] = rset.getString("pos");
				tagVocab[i][2] = rset.getString("sentences");
				i++;
			}
		}
		
		return tagVocab;
		
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
		stmt.executeUpdate("drop table Rank");
		stmt.close();
		c.close();
		
	}
	
}
