package nil;

public class Reader {
	
	public static void read(String corpus) throws Exception{
		
		long startTime = System.nanoTime();
		corpus = preprocess(corpus.toLowerCase());
		System.out.println("Preprocessed corpus in " + (System.nanoTime() - startTime)/1000000000 + " s.");
		
		startTime = System.nanoTime();
		String[] sentences = corpus.split(" [.!?] ");
		for(int i = 0; i < sentences.length; i++){
			
			String[] words = sentences[i].split("[\\s]+");
			for(int j = 0; j < words.length; j++){
				
				Dictionary.insertWord(words[j].toLowerCase(), sentences[i] + " . ");
				
			}
			
		}
		System.out.println("Inserted words into database in " + (System.nanoTime() - startTime)/1000000000 + " s.");
		
		//Tagger.brownCluster(corpus);
		//Dictionary.printTable();
		//System.out.println(Dictionary.vocabSize());
		
	}
	
	private static String preprocess(String unprocessed){
		
		String header = "##[\\S]+";
		String processed = unprocessed.replaceAll(header, "");
		processed = processed.replaceAll("@ @ @ @ @ @ @ @ @ @", "<placeholder>");
		
		return processed;
		
	}

}
