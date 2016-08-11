package nil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Librarian {

	private static String dictName;
	
	public static void connect(String dictName){
		
		try{
			
			long startTime = System.nanoTime();
			Class.forName("org.sqlite.JDBC");
			Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
			System.out.println("Connected database in " + Commander.convertTime(System.nanoTime() - startTime) + ".");
			
			startTime = System.nanoTime();
			
			Statement stmt = c.createStatement();
		    String sql = "create table if not exists Words (word text PRIMARY KEY, pos text, frequency real, sentences text)";
		    stmt.executeUpdate(sql);
		    //stmt.close();
		    
		    //stmt = c.createStatement();
		    sql = "create table if not exists Rank (rank real PRIMARY KEY, word text)";
		    stmt.executeUpdate(sql);
		    //stmt.close();
		    
		    //stmt = c.createStatement();
		    sql = "create table if not exists Corpus (dex real PRIMARY KEY, sentence text)";
		    stmt.executeUpdate(sql);
		    stmt.close();
		    
		    c.close();
		    System.out.println("Queried database in " + Commander.convertTime(System.nanoTime() - startTime) + ".");
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
		System.out.println("Preprocessed corpus in " + Commander.convertTime(System.nanoTime() - startTime) + ".");
		
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
			String[] sentences = corpus.split(" [\\.!?] |<p>");
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
					word = words[j];
					stmt = c.prepareStatement("SELECT * from Words where word=?");
					stmt.setString(1, word);
					rset = stmt.executeQuery();
					
					if(rset.next()  && !word.equals("")){
						
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
					else if(!word.equals("")){
						
						//Close the last statement
						stmt.close();
						//Insert the word into the table
						stmt = c.prepareStatement("INSERT INTO Words VALUES (?,?,1,?)");
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
			System.out.println("Inserted words into database in " + Commander.convertTime(System.nanoTime() - startTime) + ".");
			
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
	
	public static HashMap<String, HashMap<String, Double>> returnProbabilityMap() throws Exception{
		
		//final int V = vocabSize();
		HashMap<String, HashMap<String, Double>> probabilityMap = new HashMap<String, HashMap<String, Double>>();
		Class.forName("org.sqlite.JDBC");
		Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
		Statement stmt = c.createStatement();
		ResultSet rset = stmt.executeQuery("select word from Words order by word asc");
		//String compareTo() reveals the same order as this method
		HashMap<String, Integer> indices = new HashMap<String, Integer>();
		int i = 0;
		while(rset.next()){
			String word = rset.getString("word");
			if(!word.matches("[\\.!?\\-;:,'\"\\(\\)]+|<[\\w]+>") && !word.equals("")){
				indices.put(word, i);
				i++;
			}
		}
		//actually update probabilityMap
		stmt = c.createStatement();
		rset = stmt.executeQuery("select sentence from Corpus");
		//double[] frequency = new double[V];
		while(rset.next()){
			String sentence = rset.getString("sentence");
			
			//Split the sentence into string array
			String[] wordsFirst = sentence.split("[\\s]+");
			ArrayList<String> wordsTemp = new ArrayList<String>();
			for(i = 0; i < wordsFirst.length; i++)
				if(!wordsFirst[i].matches("[\\.!?\\-;:,'\"\\(\\)]+|<[\\w]+>") && !wordsFirst[i].equals(""))
					wordsTemp.add(wordsFirst[i]);
			String[] words = wordsTemp.toArray(new String[0]);
			
			for(i = 0; i < words.length - 1; i++){
				
				//int indexI = indices.get(words[i]);
				//double n = ++frequency[indexI];
				
				if(!probabilityMap.containsKey(words[i])){
					probabilityMap.put(words[i], new HashMap<String, Double>());
				}
				if(!probabilityMap.get(words[i]).containsKey(words[i+1])){
					probabilityMap.get(words[i]).put(words[i+1], new Double(0.0));
				}
				double currFreq = probabilityMap.get(words[i]).get(words[i+1]);
				probabilityMap.get(words[i]).put(words[i+1], currFreq + 1.0);
				/*String[] currentMap = probabilityMap.get(words[i]).keySet().toArray(new String[0]);
				
				for(int j = 0; j < currentMap.length; j++){
					
					if(!currentMap[j].equals(words[i+1]))
						probabilityMap.get(words[i]).put(currentMap[j], probabilityMap.get(words[i]).get(currentMap[j]) * (n - 1)/n);
					else
						probabilityMap.get(words[i]).put(currentMap[j], probabilityMap.get(words[i]).get(currentMap[j]) * (n - 1)/n + 1.0/n);
					
				}*/
				
			}
		}
		
		//System.out.println(probabilityMap.toString());
		
		stmt.close();
		c.close();
		return probabilityMap;
		
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
				if(!rset.getString("word").matches("[\\.!?\\-;:,'\"\\(\\)]+|<[\\w]+>") && !rset.getString("word").equals(""))
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
	
	static HashMap<String, String> returnTaggableVocab() throws Exception{
		
		HashMap<String, String> tagVocab = new HashMap<String, String>();
		
		Class.forName("org.sqlite.JDBC");
		Connection c = DriverManager.getConnection("jdbc:sqlite:" + dictName + ".db");
		Statement stmt = c.createStatement();
		ResultSet rset = stmt.executeQuery("select word from Words");
		while(rset.next()){
			String word = rset.getString("word");
			if(!word.matches("[\\.!?\\-;:,'\"\\(\\)]+|<[\\w]+>") && !word.equals("")){
				tagVocab.put(word, word);
			}
		}
		stmt.close();
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
		stmt.executeUpdate("drop table Corpus");
		stmt.close();
		c.close();
		
		connect(dictName);
		
	}
	
}
