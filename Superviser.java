package nil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Superviser {

public static void main(String[] args) throws IOException{
		
		//System.out.println(line.split("\t").length + " " + line.split("\t")[2]);
		String pos = "";
		int words = 0;
		
		for(int i = 1; i <= 40; i++){
			
			String inputFile = "pos (" + i + ").txt";
			FileReader inputReader = new FileReader(inputFile);
			BufferedReader bufferedReader = new BufferedReader(inputReader);
			String line;
			while((line = bufferedReader.readLine()) != null){
				if(line.charAt(0) != '#'){
					
					if(!pos.contains(line.split("\t")[2])){
						pos = pos.concat(line.split("\t")[2]);
						pos = pos.concat("#");
					}
					
					words++;
						
				}
			}
			bufferedReader.close();
			
		}
		
		System.out.println(pos.split("#").length);
		System.out.println(words);
		//2081 clusters by this count
			//However these clusters can get very specific
		//8 by conventional knowledge
		
	}
	
}
