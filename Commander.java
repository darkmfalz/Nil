package nil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Commander {

	public static void main(String[] args){
		
		Scanner scan = new Scanner(System.in);
		String input = scan.nextLine();
		while(!input.toLowerCase().equals("exit")){
			
			String[] inputTok = input.split(" ");
			
			switch(inputTok[0].toLowerCase()){
				case "select":
					//Select the dictionary you want to use
					selectLanguage(inputTok[1]);
					break;
				case "read":
					//Read from file and update dictionary
					if(!Dictionary.isConnected()){
						
						System.err.println("No dictionary is connected.");
						break;
						
					}
					String inputFile = inputTok[1];
					if(!inputFile.substring(inputFile.length()-4).equals(".txt"))
						inputFile = inputFile + ".txt";
					try{
						read(inputFile);
					}
					catch(IOException e){
						System.err.println("Could not read " + inputFile);
						e.printStackTrace();
					}
					break;
				case "parse":
					//Parse sentence from console
					if(!Dictionary.isConnected()){
						
						System.err.println("No dictionary is connected.");
						break;
						
					}
					break;
				default:
			}
			
			input = scan.nextLine();
			
		}
		
		scan.close();
		
	}

	public static void selectLanguage(String languageName){
		
		Dictionary.connect(languageName);
		
	}
	
	private static void read(String inputFile) throws IOException{
		
		FileReader inputReader = new FileReader(inputFile);
		BufferedReader bufferedReader = new BufferedReader(inputReader);
		String line;
		
		while ((line = bufferedReader.readLine()) != null){
			
			System.out.println(line);
			
		}
		
		bufferedReader.close();
		
	}
	
}
