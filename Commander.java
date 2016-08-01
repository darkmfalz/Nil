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
					TaggerDeprecated.hierachicalCluster();
					break;
				case "parse":
					//Parse sentence from console
					break;
				case "print":
					Librarian.printTable();
					break;
				case "drop":
					Librarian.dropTable();
					break;
				default:
			}
			
			System.out.print("input: ");
			input = scan.nextLine();
			
		}
		
		scan.close();
		
	}

	public static void selectLanguage(String languageName){
		
		Librarian.connect(languageName);
		
	}
	
	private static void read(String inputFile) throws Exception{
		
		FileReader inputReader = new FileReader(inputFile);
		BufferedReader bufferedReader = new BufferedReader(inputReader);
		String file = "";
		String line;
		
		while((line = bufferedReader.readLine()) != null)
			file = file.concat(line);
		Librarian.read(file);
		
		bufferedReader.close();
		
	}
	
	static String convertTime(long time){
		
		String strTime = "";
		//If it's more than an hour
		if(time/1000000000 >= 3600){
			long hr = (time/1000000000) / 3600;
			long min = ((time/1000000000) % 3600) / 60;
			long sec = (((time/1000000000) % 3600) % 60);
			strTime = Long.toString(hr) + "h " + Long.toString(min) + "m " + Long.toString(sec) + "s";
		}
		//If it's less than an hour and more than a minute
		else if(time/1000000000 >= 60){
			long min = (time/1000000000) / 60;
			long sec = (time/1000000000) % 60;
			strTime = Long.toString(min) + "m " + Long.toString(sec) + "s";
		}
		else{
			long sec = time/1000000000;
			long time2 = time/10000000 - sec*100;
			if(sec != 0 || time2 != 0)
				strTime = Long.toString(sec) + "." + Long.toString(time2) + "s";
			else{
				long msec = time/1000000;
				long time3 = time/10000 - msec*100;
				strTime = Long.toString(msec) + "." + Long.toString(time3) + "ms";
			}
		}
		return strTime;
		
	}
	
}
