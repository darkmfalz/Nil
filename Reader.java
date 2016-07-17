package nil;

public class Reader {
	
	public static void read(String corpus) throws Exception{
		
		corpus = preprocess(corpus);
		
		String[] sentences = corpus.split(" [.!?] ");
		for(int i = 0; i < sentences.length; i++){
			
			String[] words = sentences[i].split("[\\s]+");
			for(int j = 0; j < words.length; j++)
				Dictionary.insertWord(words[j].toLowerCase());
			
		}
		
		Dictionary.printTable();
		
	}
	
	private static String preprocess(String unprocessed){
		
		String header = "##[\\S]+";
		String processed = unprocessed.replaceAll(header, "");
		processed = processed.replaceAll("@ @ @ @ @ @ @ @ @ @", "<placeholder>");
		
		return processed;
		
	}

}
