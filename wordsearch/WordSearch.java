package wordsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import wordsearch.file.AsciiFileProcessor;
import wordsearch.structures.btree.BTree;
import wordsearch.structures.btree.TreeRecord;
import wordsearch.structures.index.Index;
import wordsearch.structures.index.IndexRecord;

/**
 * This class implements a system that searches for one or more words into some ASCII files.
 * All occurrences of every word are returned each one with its exact location.
 * The implementation is based on a BTree, which is used to search for the desired word.
 * If it is found, it is linked with a location at an index, where all of its occurrences have been saved.
 * Both the BTree and the index are implemented on the disk, in order to minimize the memory usage as much as possible.
 * @author Georgios Apostolakis
 */
public class WordSearch {
	private BTree dictionary;
	private Index index;
	private String[] fileNames;
	
	/**
	 * The exact size of every word, as stored in the dictionary of the system.
	 * Smaller words are padded with spaces at their endings, while the last characters from larger words are deleted.
	 * This constant does not affect the output of the system, which will contain the full keys. 
	 */
	public final int SIZEOF_KEY;
	
	/**
	 * The exact size of every filename, as stored in the system.
	 * Smaller filenames are padded with spaces at their endings, while the last characters from larger words are deleted.
	 * This constant does not affect the output of the system, which will contain the full filenames.
	 */
	public final int SIZEOF_FILENAME;
	
	/**
	 * The exact size of every disk page.
	 * Both the BTree and the index are implemented on the disk, thus all their data are saved
	 * in pages of a size defined by this constant.
	 */
	public final int SIZEOF_DISK_PAGE;
	
	/**
	 * The name of the binary file where the BTree will be saved.
	 */
	public final String BTREE_FILENAME = "wordsearch-btree.dat";
	
	/**
	 * The name of the binary file where the index will be saved.
	 */
	public final String INDEX_FILENAME = "wordsearch-index.dat";
	
	/**
	 * Constructs a new instance of this class.
	 * @param SIZEOF_KEY The integer value for the {@link #SIZEOF_KEY} constant of this class.
	 * @param SIZEOF_FILENAME The integer value for the {@link #SIZEOF_FILENAME} constant of this class.
	 * @param SIZEOF_DISK_PAGE The integer value for the {@link #SIZEOF_DISK_PAGE} constant of this class.
	 * @param fileNames A {@link java.lang.String String[]} array with the name of the input ASCII files.
	 */
	public WordSearch(int SIZEOF_KEY, int SIZEOF_FILENAME, int SIZEOF_DISK_PAGE, String[] fileNames){
		this.SIZEOF_KEY = SIZEOF_KEY;
		this.SIZEOF_FILENAME = SIZEOF_FILENAME;
		this.SIZEOF_DISK_PAGE = SIZEOF_DISK_PAGE;
		
		dictionary = new BTree(SIZEOF_DISK_PAGE, SIZEOF_KEY, BTREE_FILENAME);
		index = new Index(SIZEOF_FILENAME, SIZEOF_DISK_PAGE, INDEX_FILENAME);
		this.fileNames = fileNames;
	}	
	
	/**
	 * It provides a basic user interface through console, in which the user can type words
	 * and the system to return their occurrences into all the input ASCII files.
	 */
	public void answerQuestions() {
		String question, formattedQuestion;
		Scanner sc = new Scanner(System.in);		
		ArrayList<String[]> results;
		
		System.out.print("Enter a word to be found (Q to quit): ");
		question = sc.next();

		while(!question.equals("Q") && !question.equals("q")) {
			formattedQuestion = question.toLowerCase();
			for(int i=question.length();i<SIZEOF_KEY;i++) //pad with spaces to contain SIZEOF_KEY characters
				formattedQuestion += ' ';
			if(formattedQuestion.length()>SIZEOF_KEY)  //delete the last characters in order to contain exactly SIZEOF_KEY of them.
				formattedQuestion = formattedQuestion.substring(0, SIZEOF_KEY);
			try {
				int foundPos = dictionary.searchTree(formattedQuestion);
				if(foundPos<0)
					System.out.println("Word '" + question + "' was not found. :(");
				else {
					int info = dictionary.getPage(foundPos).findRecord(formattedQuestion).getInfo();
					results = index.findData(info, this.fileNames);
					for(int i=0; i<results.size(); i++)
						System.out.println("Word '" + question + "' was found in file " + results.get(i)[0] + " after " + results.get(i)[1] + " characters from the start.");
				}
			}catch(IOException e) {
				System.err.println("Error. The binary files " + BTREE_FILENAME + ", " + INDEX_FILENAME + " are not accessible.");
			}
			
			System.out.print("Enter a word to be found (Q to quit): ");
			question = sc.next();
		}
		sc.close();
	}
	
	/**
	 * It provides the occurrences of the given words into all the input ASCII files.
	 * @param questions A {@link java.lang.String String[]} array. Each entry is a word to be searched for into the input ASCII files.
	 * @return An {@link java.util.ArrayList} object, each entry of which contains the list
	 * of occurrences for the respective question provided as an argument. That list is 
	 * also an {@link java.util.ArrayList} object, which contains {@link java.lang.String String[]}
	 * arrays. Every array corresponds to a different occurrence of a given word, with its
	 * first element being the filename where it was found and its second element being the
	 * location (number of bytes from the start of the file) where the occurrence takes place.
	 */
	public ArrayList<ArrayList<String[]>> answerQuestions(String[] questions) {
		String formattedQuestion;
		ArrayList<ArrayList<String[]>> results = new ArrayList<ArrayList<String[]>>();

		for(int i=0;i<questions.length;i++) {
			formattedQuestion = questions[i].toLowerCase();
			for(int j=questions[i].length();j<SIZEOF_KEY;j++) //pad with spaces to contain SIZEOF_KEY characters
				formattedQuestion += ' ';
			if(formattedQuestion.length()>SIZEOF_KEY)  //delete the last characters in order to contain exactly SIZEOF_KEY of them.
				formattedQuestion = formattedQuestion.substring(0, SIZEOF_KEY);
			
			try {
				int foundPos = dictionary.searchTree(formattedQuestion);
				
				if(foundPos<0)
					results.add(new ArrayList<String[]>());
				else {
					int info = dictionary.getPage(foundPos).findRecord(formattedQuestion).getInfo();
					results.add(index.findData(info, this.fileNames));
				}
			}catch(IOException e) {
				System.err.println("Error. The binary files " + BTREE_FILENAME + ", " + INDEX_FILENAME + " are not accessible.");
			}
		}
		return results;
	}
	
	/**
	 * Builds the BTree and the index into the respective binary files in the disk.
	 * It doesn't have to be called before every execution of the system, but only when
	 * the input ASCII files change.
	 * @return An integer with the total number of words (not essentially unique) that were read from the input files.
	 */
	public int buildDataFiles(){
		String key;
		int currInputFilePos, foundPos, info=0, wordCounter=0;
		AsciiFileProcessor reader;
		
		try {
			dictionary.clearDataOfTreeFile();
			index.clearDataOfIndexFile();

			for(int f=0;f<this.fileNames.length;f++) {
				reader = new AsciiFileProcessor(this.fileNames[f]);
				
				currInputFilePos = (int)reader.getFilePosition();
				key = reader.readNext();
				
				while(key != null){   //while EOF hasn't been found
					wordCounter++;
					key = key.toLowerCase();
					for(int i=key.length();i<SIZEOF_KEY;i++)  //pad with spaces to contain SIZEOF_KEY characters
						key += ' ';
					if(key.length()>SIZEOF_KEY)  //delete the last characters in order to contain exactly SIZEOF_KEY of them.
						key = key.substring(0, SIZEOF_KEY);

					foundPos = dictionary.searchTree(key);
					if(foundPos>=0){  //if the key already exists in the dictionary
						info = dictionary.getPage(foundPos).findRecord(key).getInfo();
						index.addRecord(info, new IndexRecord(fileNames[f], currInputFilePos, SIZEOF_FILENAME));  //auto format to the fileName by the constructor IndexRecord()
					}
					else {  //enter the new record first in a new page in the index and then in the tree using the appropriate info
						info = index.addRecord(-1, new IndexRecord(fileNames[f], currInputFilePos, SIZEOF_FILENAME)); 
						dictionary.insert(new TreeRecord(key,info, SIZEOF_KEY));
					}
					
					currInputFilePos = (int)reader.getFilePosition();
					key = reader.readNext();
				} 
				reader.close();
		    }
		}catch(IOException e) {
			System.err.println("Error. Some input or data files are inaccessible.");
			System.exit(-1);
		}
		
		return wordCounter;
	}
	
	/**
	 * Provides the number of disk accesses (since last reset) into the index binary file.
	 * @return An integer with the number of disk accesses.
	 */
	public int getNumOfBtreeDiskAccesses() {
		return dictionary.getNumOfDiskAccesses();
	}
	
	/**
	 * Provides the number of disk accesses (since last reset) into the BTree binary file.
	 * @return An integer with the number of disk accesses.
	 */
	public int getNumOfIndexDiskAccesses() {
		return index.getNumOfDiskAccesses();
	}
	
	/**
	 * Resets the counter of disk accesses for both the BTree's and the index's binary file.
	 */
	public void resetNumOfDiskAccesses() {
		dictionary.resetNumOfDiskAccesses();
		index.resetNumOfDiskAccesses();
	}
	
	/**
	 * Terminates the operation of the system after closing any open file streams.
	 */
	public void terminate() {
		try {
			dictionary.close();
			index.close();
			System.exit(0);
		}catch(IOException e) {
			System.err.println("Error. Some data files are inaccessible.");
			System.exit(-1);
		}
	}
}
