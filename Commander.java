package nil;

import java.util.Scanner;

public class Commander {

	public static void main(String[] args){
		
		Scanner scan = new Scanner(System.in);
		String input = scan.next();
		while(!input.toLowerCase().equals("exit")){
			
			String[] inputTok = input.split(" ");
			
			switch(inputTok[0].toLowerCase()){
				case "select":
					selectLanguage(inputTok[1]);
					break;
				default:
			}
			
			input = scan.next();
			
		}
		
		scan.close();
		
	}
	
	public static void selectLanguage(String languageName){
		
		Dictionary.connect(languageName);
		
	}
	
}
