package nil;

public class Reader {
	
	public static void read(String corpus){
		
		corpus = preprocess(corpus);
		System.out.println(corpus);
		
	}
	
	private static String preprocess(String unprocessed){
		
		String header = "##[\\S]+";
		String processed = unprocessed.replaceAll(header, "");
		processed = unprocessed.replaceAll("@ @ @ @ @ @ @ @ @ @", "<placeholder>");
		
		return processed;
		
	}

}
