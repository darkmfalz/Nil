package nil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;

public class Commander {

	public static void main(String[] args) throws Exception{
		
		Scanner scan = new Scanner(System.in);
		System.out.print("input: ");
		String input = scan.nextLine();
		while(!input.toLowerCase().equals("exit")){
			
			String[] inputTok = input.split("[\\s]+");
			
			switch(inputTok[0].toLowerCase()){
				case "select":
					//Select the dictionary you want to use
					selectLanguage(inputTok[1]);
					break;
				case "read":
					//Read from file and update dictionary
					String inputFile = inputTok[1];
					if(!inputFile.substring(inputFile.length()-4).equals(".txt"))
						inputFile = inputFile + ".txt";
					try{
						read(inputFile);
					}
					catch(Exception e){
						System.err.println("Could not read " + inputFile);
						e.printStackTrace();
					}
					break;
				case "tag":
					Tagger.brownCluster();
					break;
				case "parse":
					//Parse sentence from console
					break;
				case "print":
					Dictionary.printTable();
					break;
				case "drop":
					Dictionary.dropTable();
					break;
				default:
			}
			
			System.out.print("input: ");
			input = scan.nextLine();
			
		}
		
		scan.close();
		
	}

	public static void selectLanguage(String languageName){
		
		Dictionary.connect(languageName);
		
	}
	
	private static void read(String inputFile) throws Exception{
		
		FileReader inputReader = new FileReader(inputFile);
		BufferedReader bufferedReader = new BufferedReader(inputReader);
		String file = "";
		String line;
		
		while((line = bufferedReader.readLine()) != null)
			file = file.concat(line);
		Reader.read(file);
		
		bufferedReader.close();
		
	}
	
}
