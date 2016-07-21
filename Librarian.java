package nil;

import java.sql.*;
import java.util.Arrays;

public class Librarian {

	private static String dictName;
	
	public static void connect(String dictName){
		
		try{
			
			long startTime = System.nanoTime();
			Class.forName("org.sqlite.JDBC");
			Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
			System.out.println("Connected database in " + (System.nanoTime() - startTime) + " ns.");
			
			startTime = System.nanoTime();
			
			Statement stmt = c.createStatement();
		    String sql = "create table if not exists Words (word text PRIMARY KEY, pos text, frequency real, sentences text, context text)";
		    stmt.executeUpdate(sql);
		    stmt.close();
		    
		    stmt = c.createStatement();
		    sql = "create table if not exists Rank (rank real PRIMARY KEY, word text)";
		    stmt.executeUpdate(sql);
		    stmt.close();
		    
		    stmt = c.createStatement();
		    sql = "create table if not exists Corpus (index real PRIMARY KEY, sentence text)";
		    stmt.executeUpdate(sql);
		    stmt.close();
		    
		    c.close();
		    System.out.println("Queried database in " + (System.nanoTime() - startTime) + " ns.");
		    Librarian.dictName = dictName;
			
		}
		catch(Exception e){
			
			e.printStackTrace();
			System.exit(0);
			
		}
		
	}
	
	public static String getDictName(){
		
		return dictName;
		
	}
	
	//Read
	public static void read(String corpus) throws SQLException{
		
		//MUST CLEAN UP TO ALLOW WORDS MULTIPLE PARTS OF SPEECH
		String word = "";
		
		long startTime = System.nanoTime();
		corpus = preprocess(corpus.toLowerCase());
		System.out.println("Preprocessed corpus in " + (System.nanoTime() - startTime) + " ns.");
		
		try{
			
			startTime = System.nanoTime();
			//Connect into database
			Class.forName("org.sqlite.JDBC");
			Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
			
			long sentence;
			PreparedStatement stmt = c.prepareStatement("SELECT COUNT(*) from Corpus");
			ResultSet rset = stmt.executeQuery();
			if(rset.next())
				sentence = rset.getLong(1);
			else
				sentence = 0;
			stmt.close();
			String[] sentences = corpus.split(" [.!?] ");
			for(int i = 0; i < sentences.length; i++){
				
				//Insert the sentence into the Corpus table
				stmt = c.prepareStatement("INSERT INTO Corpus VALUES (?,?)");
				stmt.setLong(1, sentence);
				stmt.setString(2, sentences[i]);
			    stmt.executeUpdate();
			    stmt.close();
			    
			    //Insert the words into the Words table
			    String[] words = sentences[i].split("[\\s]+");
				for(int j = 0; j < words.length; j++){
					
					//Check if word is in database
					word = words[i];
					stmt = c.prepareStatement("SELECT * from Words where word=?");
					stmt.setString(1, word);
					rset = stmt.executeQuery();
					
					if(rset.next()){
						
						//Retrieve current frequency
						int frequency = rset.getInt("frequency");
						String sentenceIn = rset.getString("sentences") + Long.toString(sentence) + ";";
						stmt.close();
						if(!word.matches("[\\.!?\\-;:,'\"\\(\\)]+|<[\\w]+>"))
							frequency++;
						//Update frequency
						stmt = c.prepareStatement("UPDATE Words set frequency=? , sentences=? where word=?");
						stmt.setInt(1, frequency);
						stmt.setString(2, sentenceIn);
						stmt.setString(3, word);
						stmt.executeUpdate();
						stmt.close();
						
					}
					else{
						
						//Close the last statement
						stmt.close();
						//Insert the word into the table
						stmt = c.prepareStatement("INSERT INTO Words VALUES (?,?,1,?,NULL)");
						stmt.setString(1, word);
						stmt.setString(2, word);
						stmt.setString(3, Long.toString(sentence) + ";");
					    stmt.executeUpdate();
					    stmt.close();
					}
				}
				
				sentence++;
				
			}
			c.close();
			System.out.println("Inserted words into database in " + (System.nanoTime() - startTime) + " ns.");
			
		}
		catch(Exception e){
			System.err.println(word);
			e.printStackTrace();
		}
		
	}
	
	private static String preprocess(String unprocessed){
		
		String header = "##[\\S]+";
		String processed = unprocessed.replaceAll(header, "");
		processed = processed.replaceAll("@ @ @ @ @ @ @ @ @ @", "<placeholder>");
		
		return processed;
		
	}
	
	//Other Update
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
				//String[] sentenceIndices = rset.getString("sentences").split(";");
				i++;
			}
		}
		stmt.close();
		for(i = 0; i < tagVocab.length; i++){
			
			String[] sentenceIndices = tagVocab[i][2].split(";");
			String sentences = "";
			for(int j = 0; j < sentenceIndices.length; j++){
				
				PreparedStatement pstmt = c.prepareStatement("SELECT * from Corpus where index=?");
				pstmt.setString(1, sentenceIndices[j]);
				rset = pstmt.executeQuery();
				if(rset.next())
					sentences = sentences + rset.getString("sentence") + " . ";
				pstmt.close();
				
			}
			tagVocab[i][2] = sentences;
			
		}
		c.close();
		
		return tagVocab;
		
	}
	
	//Miscellaneous Utility
	public static void printTableFull() throws Exception{
		
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
	
	public static void printTable() throws Exception{
		
		Class.forName("org.sqlite.JDBC");
		Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
		Statement stmt = c.createStatement();
		ResultSet rset = stmt.executeQuery("select * from Words order by frequency asc");
		while (rset.next()) {
			//Print one row          
			System.out.print(rset.getString("word") + " "); //Print one element of a row
			System.out.print(rset.getString("pos") + " "); //Print one element of a row

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
		
		connect(dictName);
		
	}
	
}
