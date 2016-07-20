package nil;

import java.util.Arrays;

public class Reader {
	
	public static void read(String corpus) throws Exception{
		
		corpus = preprocess(corpus);
		
		String[] sentences = corpus.split(" [.!?] ");
		for(int i = 0; i < sentences.length; i++){
			
			String[] words = sentences[i].split("[\\s]+");
			for(int j = 0; j < words.length; j++){
				
				Dictionary.insertWord(words[j].toLowerCase(), sentences[i].toLowerCase() + " . ");
				
			}
			
		}
		
		//Tagger.brownCluster(corpus);
		//Dictionary.printTable();
		//System.out.println(Dictionary.vocabSize());
		System.out.println(Arrays.toString(Dictionary.mostFreqWordsInit(500)));
		
	}
	
	private static String preprocess(String unprocessed){
		
		String header = "##[\\S]+";
		String processed = unprocessed.replaceAll(header, "");
		processed = processed.replaceAll("@ @ @ @ @ @ @ @ @ @", "<placeholder>");
		
		return processed;
		
	}

}
