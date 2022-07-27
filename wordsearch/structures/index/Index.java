package wordsearch.structures.index;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import wordsearch.file.BinFileProcessor;

/**
 * This class implements an index on the disk, where every word from the input files is matched with its location on those files.
 * Every page of the index contains the filenames and the location inside the respective files where a specific word can be found.
 * More in detail, the location is determined as the number of bytes from the start of the file until the word occurs.
 * If a word appears multiple times, then more than one page may be required to store all the locations where it can be found.
 * Those pages (which concern the same word) are linked via the {@link wordsearch.structures.index.IndexPage#getNextPage() nextPage} integer.
 * @author Georgios Apostolakis
 */
public class Index {
	private BinFileProcessor binaryFileEditor;
	
	/**
	 * This constant is returned when the wanted page does not exist in the index. It has to be less than zero.
	 */
	public final int NO_SUCH_PAGE_EXISTS = -3;
	
	/**
	 * This constant is returned when an already existing {@link wordsearch.structures.index.IndexRecord} is attempted to be inserted. It has to be less than zero.
	 */
	public final int RECORD_ALREADY_EXISTS_IN_FILE = -2;
	
	/**
	 * This constant is returned when a {@link wordsearch.structures.index.IndexRecord} is inserted in the index successfully. It has to be less than zero.
	 */
	public final int NEW_RECORD_SUCCESSFULLY_INSERTED = -1;
	
	/**
	 * The value for the {@link wordsearch.structures.Record#SIZEOF_STRING} constant inside every {@link wordsearch.structures.index.IndexRecord} object, which determines the size of the {@code filename} that is stored there.
	 */
	public final int SIZEOF_FILENAME;
	
	/**
	 * The size (in bytes) of the integer stored in every {@link wordsearch.structures.index.IndexRecord} object.
	 */
	public final int SIZEOF_INT;
	
	/**
	 * The size (in bytes) of every {@link wordsearch.structures.index.IndexPage} object of this index.
	 */
	public final int SIZEOF_PAGE;
	
	/**
	 * The number of {@link wordsearch.structures.index.IndexRecord} instances stored in every {@link wordsearch.structures.index.IndexPage} object of this index.
	 */
	public final int RECORDS_PER_INDEX_PAGE;
	
	/**
	 * Constructs a new instance of this class which builds an index into a file with a specific filename.
	 * @param SIZEOF_FILENAME The value for the {@link Index#SIZEOF_FILENAME} constant of this class.
	 * @param SIZEOF_PAGE The value for the {@link Index#SIZEOF_PAGE} constant of this class.
	 * @param indexFilename The name of the file into which the index will be stored.
	 */
	public Index(int SIZEOF_FILENAME, int SIZEOF_PAGE, String indexFilename){
		this.SIZEOF_FILENAME = SIZEOF_FILENAME;
		this.SIZEOF_INT = Integer.SIZE/8;
		this.SIZEOF_PAGE = SIZEOF_PAGE;
		this.RECORDS_PER_INDEX_PAGE = SIZEOF_PAGE / (SIZEOF_FILENAME + SIZEOF_INT);
		
		try{
			binaryFileEditor = new BinFileProcessor(SIZEOF_PAGE, indexFilename);
		} catch(FileNotFoundException e){
			System.err.println("Error. Unable to create index file.");
			System.exit(-1);
		}	
	}
	
	/**
	 * Adds a new {@link wordsearch.structures.index.IndexRecord} object at the end of a specific page of the index.
	 * If that page is full, a new page is constructed and the previous one is linked with it. 
	 * @param r The new {@link wordsearch.structures.index.IndexRecord} object to be added.
	 * @param page An integer greater or equal to 0 to add the {@link wordsearch.structures.index.IndexRecord} into the respective existing page,
	 * or any integer less than 0 to create a new page for the {@link wordsearch.structures.index.IndexRecord}.
	 * @return The number of the new page (if so was required by the {@code page} argument)
	 * or {@link #NEW_RECORD_SUCCESSFULLY_INSERTED} {@code <0} for successful addition to an existing page (or creation of a new due to lack of space).
	 * Moreover, it may return or {@link #NO_SUCH_PAGE_EXISTS} {@code <0} or {@link #RECORD_ALREADY_EXISTS_IN_FILE} {@code <0} in case of a failure.
	 * @throws IOException In case of a problem while reading/writing data from/to the index at the disk.
	 */
	public int addRecord(int page, IndexRecord r) throws IOException {
		IndexPage tmpPage;
		boolean lackOfSpace = true;  //is true when a new page is added due to the lack of space in the existing one
		int currPage=page, nextPage=page;
				
		if(page>=binaryFileEditor.fileSize())
			return NO_SUCH_PAGE_EXISTS;   //fail - the given page does not exist
		
		if(page>=0) {  //the new Record will be added to an existing page
			while(lackOfSpace){
				tmpPage = new IndexPage(SIZEOF_FILENAME, SIZEOF_PAGE, binaryFileEditor.readPage(currPage));
				
				if(tmpPage.find(r)>=0)  //if exactly the same record already exists, probably from previous creation of the file, it's useless to save it for a second time
					return RECORD_ALREADY_EXISTS_IN_FILE;
				
				lackOfSpace = (tmpPage.addRecord(r)<0); //becomes true when tmpPage is full
				if(lackOfSpace){ //if the current page is full
					if(tmpPage.getNextPage()>=0)  //if a next (linked) page already exists
						nextPage = tmpPage.getNextPage();
					else {              //create a new page and link it with the old one
						nextPage = (int)binaryFileEditor.fileSize();
						tmpPage.setNextPage(nextPage);
						binaryFileEditor.writePage(currPage, tmpPage); //write back the existing after linking it to the new
						
						tmpPage = new IndexPage(SIZEOF_FILENAME, SIZEOF_PAGE);  //create a new page
						tmpPage.addRecord(r);      //write the new record to it
						binaryFileEditor.writePage(nextPage, tmpPage);  //write the new page at the end of the file
						
						lackOfSpace=false;  //the Record was added to a new page, so we set this variable false to exit the loop.
					}
				}
				else {//if the current page is not full
					binaryFileEditor.writePage(currPage, tmpPage);
				}
				currPage = nextPage; //the loop is repeated until we reach to the last page of the chain, which may not be full.
			}
		}
		else{   //page<0, a new page has to be created and the Record will be added to it.
			tmpPage = new IndexPage(SIZEOF_FILENAME, SIZEOF_PAGE);
			tmpPage.addRecord(r);
			binaryFileEditor.writePage(binaryFileEditor.fileSize(), tmpPage);
		}
		
		if(page>=0)
			return NEW_RECORD_SUCCESSFULLY_INSERTED;  // if page>=0, was added to an existing page
		return (int)binaryFileEditor.fileSize()-1;   //if page<0, new page was created	
	}
	
	/**
	 * Releases the binary file associated with this instance.
	 * @throws IOException In case of an error while closing the stream to the file.
	 */
	public void close() throws IOException {
		binaryFileEditor.close();
	}
	
	/**
	 * Searches a given chain of linked pages for entries corresponding to specific filenames. Then, it returns all those entries.
	 * @param pageNumber The position of the {@link wordsearch.structures.index.IndexPage} into which a search will take place. The linked (to it) pages will be searched too.
	 * @param fileNames The filenames for which we are interested to find entries inside the index.
	 * @return An {@link java.util.ArrayList} with {@link java.lang.String String[]} arrays. Each array contains the data from one entry.
	 * @throws IOException In case of a problem while reading data from the index at the disk.
	 */
	public ArrayList<String[]> findData(int pageNumber, String[] fileNames) throws IOException{
		IndexPage tmpPage;
		int next = pageNumber;
		String fName = "";
		String str1, str2;
		ArrayList<String[]> results = new ArrayList<String[]>();
		
		if(pageNumber<0 || pageNumber>=binaryFileEditor.fileSize())  //invalid number of page
			return results; //we return an empty list
		
		while(next>=0){
			tmpPage = new IndexPage(SIZEOF_FILENAME, SIZEOF_PAGE, binaryFileEditor.readPage(next));
			for(int i=0;i<RECORDS_PER_INDEX_PAGE;i++){
				if(tmpPage.getRecord(i)==null) //the end of page was reached
					break;
				
				//Every IndeRecord may not contain the whole filename (due to restrictions regarding its size)
				for(int f=0;f<fileNames.length;f++) { //Find the whole filename corresponding to the i-th IndexRecord, from filenames array
					str1 = fileNames[f];
					str2 = ((IndexRecord)tmpPage.getRecord(i)).getFilename();
					if(str1.regionMatches(0, str2, 0, java.lang.Math.min(str1.length(), str2.length()))) {
						fName = fileNames[f];
						break;
					}
				}
				results.add(new String[] {fName, ((IndexRecord)tmpPage.getRecord(i)).getOffset()+""});
			}
			next = tmpPage.getNextPage();
		}
		return results;
	}
	
	/**
	 * Deletes the contents (if any) of the binary file where the index is stored.
	 * @throws IOException In case of an error while accessing the file.
	 */
	public void clearDataOfIndexFile() throws IOException {
		this.binaryFileEditor.clearFile();
	}
	
	/**
	 * Returns the number of disk accesses from the last time they were reset.
	 * @return An integer number with the number of disk accesses for the purposes of this instance.
	 */
	public int getNumOfDiskAccesses() {
		return this.binaryFileEditor.getNumOfAccesses();
	}
	
	/**
	 * Resets to zero the variable that counts the disk accesses.
	 */
	public void resetNumOfDiskAccesses() {
		this.binaryFileEditor.resetNumOfAccesses();
	}
}
