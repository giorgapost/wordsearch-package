import java.util.ArrayList;

import wordsearch.WordSearch;

public class Examples {
	
	public static final int SIZEOF_PAGE = 128;
	public static final int SIZEOF_KEY = 12;
	public static final int SIZEOF_FILENAME = 8;
	
	public static final String asciiFiles[] = { "Kennedy.txt",
												"MartinLutherKing.txt",
												"Obama.txt"};
	
	public static void main(String[] args){
		if(args.length>0)
			for(int i=0;i<asciiFiles.length;i++)
				asciiFiles[i] = args[0] + asciiFiles[i];
		
		example1();
//		example2();
	}
	
	public static void example1() {
		WordSearch system = new WordSearch(SIZEOF_KEY, SIZEOF_FILENAME, SIZEOF_PAGE, asciiFiles);
		
		//Has to be called every time the ASCII input files change. Otherwise it can be omitted for a quicker execution.
		system.resetNumOfDiskAccesses();
		int totalWords = system.buildDataFiles();
		System.out.println(totalWords + " words were found in total in the ASCII files that were given as input.");
		int totalAccesses = system.getNumOfBtreeDiskAccesses() + system.getNumOfIndexDiskAccesses();
		System.out.println(totalAccesses + " disk accesses were required to build the BTree and index on the disk.");
		
		
		//Gets words from the user (through console) and prints their occurrences.
		system.resetNumOfDiskAccesses();
		system.answerQuestions();
		totalAccesses = system.getNumOfBtreeDiskAccesses() + system.getNumOfIndexDiskAccesses();
		System.out.println(totalAccesses + " disk accesses were required to answer the questions.");
		
		//Has to be called always at the end of the program.
		system.terminate();
	}

	public static void example2() {
		String questions[] =  { "President",	"victory",		"wishes",	"states",
								"only",			"not",		"Isaiah",		"rainbow"};
		WordSearch system = new WordSearch(SIZEOF_KEY, SIZEOF_FILENAME, SIZEOF_PAGE, asciiFiles);
		
		
		//Has to be called every time the ASCII input files change. Otherwise it can be omitted for a quicker execution.
		system.resetNumOfDiskAccesses();
		int totalWords = system.buildDataFiles();
		System.out.println(totalWords + " words were found in total in the ASCII files that were given as input.");
		int totalAccesses = system.getNumOfBtreeDiskAccesses() + system.getNumOfIndexDiskAccesses();
		System.out.println(totalAccesses + " disk accesses were required to build the BTree and index on the disk.");
		
		
		//Gets words from the user (through console) and prints their occurrences.
		system.resetNumOfDiskAccesses();
		ArrayList<ArrayList<String[]>>results = system.answerQuestions(questions);
		for(int w=0;w<results.size();w++) {
			for(int e=0;e<results.get(w).size();e++) {
				System.out.println("Word '" + questions[w] + "' was found in file " + results.get(w).get(e)[0] + " after " + results.get(w).get(e)[1] + " characters from the start.");
			}
			if(results.get(w).size()==0)
				System.out.println("Word '" + questions[w] + "' was not found. :(");
		}		
		int totalAccesses2 = system.getNumOfBtreeDiskAccesses() + system.getNumOfIndexDiskAccesses();
		System.out.println(totalAccesses2 + " disk accesses were required to answer the questions.");
		
		//Has to be called always at the end of the program.
		system.terminate();
	}
}
