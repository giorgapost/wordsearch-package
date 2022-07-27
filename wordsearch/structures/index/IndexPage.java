package wordsearch.structures.index;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import wordsearch.structures.Page;
import wordsearch.structures.Record;

/**
 * A realization of the abstract class {@link wordsearch.structures.Page}. It implements a page used by the index mechanism of the algorithm.
 * @author Georgios Apostolakis
 *
 */
public class IndexPage extends Page{
	private int nextPage;
	
	/**
	 * The number of records stored in this page.
	 */
	public final int RECORDS_PER_PAGE;
	
	/**
	 * Constructs a new instance of this class, empty from {@link wordsearch.structures.index.IndexRecord} objects. 
	 * Also, an array of {@link IndexPage#RECORDS_PER_PAGE} size with {@code null} entries is initialized to store {@link wordsearch.structures.index.IndexRecord} objects in the future with {@link wordsearch.structures.Page#setRecord(int, Record)} method.
	 * Finally, {@code nextPage} integer parameter of this instance is set to -1.
	 * @param SIZEOF_FILENAME The size of the {@link java.lang.String} {@code filename} stored inside every {@link wordsearch.structures.index.IndexRecord} instance. 
	 * @param SIZEOF_PAGE The size (in bytes) of the current page.
	 */
	public IndexPage(int SIZEOF_FILENAME, int SIZEOF_PAGE) {
		super(SIZEOF_FILENAME, SIZEOF_PAGE);
		this.RECORDS_PER_PAGE = SIZEOF_PAGE / (SIZEOF_FILENAME + SIZEOF_INT);
		
		setRecordList(new IndexRecord[RECORDS_PER_PAGE]);
		for(int i=0;i<RECORDS_PER_PAGE;i++)
			setRecord(i, null);
		
		this.nextPage = -1;
	}
	
	/**
	 * Constructs a new instance of this class and initializes it with some {@link wordsearch.structures.index.IndexRecord} objects.
	 * @param SIZEOF_FILENAME The size of the {@link java.lang.String} {@code filename} stored inside every {@link wordsearch.structures.index.IndexRecord} instance. 
	 * @param SIZEOF_PAGE The size (in bytes) of the current page.
	 * @param recordList An array of {@link wordsearch.structures.index.IndexRecord} objects.
	 * @param nextPage An integer which links this instance with another {@link IndexPage} object.
	 */
	public IndexPage(int SIZEOF_FILENAME, int SIZEOF_PAGE, IndexRecord[] recordList, int nextPage) {
		super(SIZEOF_FILENAME, SIZEOF_PAGE);
		this.RECORDS_PER_PAGE = SIZEOF_PAGE / (SIZEOF_FILENAME + SIZEOF_INT);
		setRecordList(recordList);
		this.nextPage = nextPage;
	}
	
	/**
	 * Constructs a new instance of this class and initializes it with {@link wordsearch.structures.index.IndexRecord} objects recovered by a byte array.
	 * @param SIZEOF_FILENAME The size of the {@link java.lang.String} {@code filename} stored inside every {@link wordsearch.structures.btree.TreeRecord} instance. 
	 * @param SIZEOF_PAGE The size (in bytes) of the current page.
	 * @param byteArray A byte array that contains some {@link wordsearch.structures.index.IndexRecord} objects. If {@code null}, then this method is equivalent with {@link #IndexPage(int, int)}.
	 * @throws IOException in the case of a malformed byte array.
	 */
	public IndexPage(int SIZEOF_FILENAME, int SIZEOF_PAGE, byte[] byteArray) throws IOException {
		super(SIZEOF_FILENAME, SIZEOF_PAGE);
		this.RECORDS_PER_PAGE = SIZEOF_PAGE / (SIZEOF_FILENAME + SIZEOF_INT);
		if(byteArray==null) {
			setRecordList(new IndexRecord[RECORDS_PER_PAGE]);
			for(int i=0;i<RECORDS_PER_PAGE;i++)
				setRecord(i, null);
			this.nextPage = -1;
			return;
		}
		
		int[] offsetArray = new int[RECORDS_PER_PAGE];
		String[] s = new String[RECORDS_PER_PAGE];
		
		ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
		DataInputStream dis = new DataInputStream(bis);	
		
		this.nextPage = dis.readInt(); //read nextPage
		
		for(int i=0;i<RECORDS_PER_PAGE;i++)  //read offset list
			offsetArray[i] = dis.readInt();
		
		for(int i=0;i<RECORDS_PER_PAGE;i++){  //read each filename char by char
			s[i] = "";
			for(int j=0;j<SIZEOF_FILENAME;j++){
				Byte bt = byteArray[SIZEOF_INT+RECORDS_PER_PAGE*SIZEOF_INT+SIZEOF_FILENAME*i+j];
				s[i] += (char)(Integer.parseInt(bt.toString()));
			}
		}
			
		super.setRecordList(new IndexRecord[10]); //Construct the array of Record objects
		for(int i=0;i<RECORDS_PER_PAGE;i++){ //for all the records of the page
			if(offsetArray[i]<0)      
				setRecord(i, null); //if there is not a real record
			else
				setRecord(i, new IndexRecord(s[i], offsetArray[i], SIZEOF_FILENAME));
		}
	}
	
	/**
	 * Checks if there is any empty position in the list of {@link wordsearch.structures.index.IndexRecord} objects of this instance
	 * and, if yes, stores its argument at that position and returns its index.
	 * @param r The new {@link wordsearch.structures.index.IndexRecord} object to be added in the list.
	 * @return -1 if there is no free space and {@code r} could not be added, or the index of the list where {@code r} was placed.
	 */
	public int addRecord(Record r) {
		if(getRecord(RECORDS_PER_PAGE-1)!=null) //full page - no records can be added.
			return -1;

		int i=0;
		while(getRecord(i)!=null)
			i++; //Find the first empty position.
		
		if(setRecord(i, r))
			return i;     //Successfully added.
		else
			return -1;      //Failed to add the record.
	}
	
	/**
	 * Searches for an {@link wordsearch.structures.index.IndexRecord} object inside the list kept in this instance and returns its index.
	 * @param r An {@link wordsearch.structures.index.IndexRecord} to search for.
	 * @return An integer value which is the position of {@code r} in the list kept by the current {@link IndexPage} instance, or -1 if it was not found.
	 */
	public int find(IndexRecord r) {
		int foundPos = -1;
		boolean flag1, flag2;
		for(int i=0;i<RECORDS_PER_PAGE;i++) {
			if(getRecord(i)==null) //null record at this point of the list
				continue;
			
			flag1 = (((IndexRecord)getRecord(i)).getOffset()==r.getOffset()); //the offsets must be the same
			flag2 = (((IndexRecord)getRecord(i)).getFilename().equals(r.getFilename())); //the filenames must be the same
			if(flag1 && flag2) {
				foundPos = i;
				break;
			}
		}
		return foundPos;
	}
	
	/**
	 * Provides the variable {@code nextPage} which links this instance with another {@link IndexPage} object.
	 * @return An integer which is the value of {@code nextPage} variable. 
	 */
	public int getNextPage() {
		return nextPage;
	}
	
	/**
	 * Sets a value for {@code nextPage} variable of this instance.
	 * @param nextPage The new value for the {@code nextPage} variable.
	 */
	public void setNextPage(int nextPage) {
		this.nextPage = nextPage;
	}
	
	/**
	 * Converts the current instance into a byte array.
	 * @return A byte array with all information existing in this object.
	 */
	public byte[] toByteArray(){
		String nullRecString = "", completePageStr="", filenameRecString="";
		byte[] array = null;
		
		for(int i=0;i<SIZEOF_STRING;i++)  // used in the position of null records
			nullRecString += ' ';
		for(int i=0;i<(SIZEOF_PAGE-RECORDS_PER_PAGE*SIZEOF_RECORD-SIZEOF_INT);i++)  //used to complete the page size
			completePageStr += ' ';
		for(int i=0;i<RECORDS_PER_PAGE;i++) { //Contains the filenames from all records of this instance
			if(getRecord(i)!=null)
				filenameRecString += ((IndexRecord)getRecord(i)).getFilename();
			else
				filenameRecString += nullRecString;
		}
		
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			
			dos.writeInt(this.nextPage);  //first, write the nextPage variable			
			for(int i=0;i<RECORDS_PER_PAGE;i++){  //write the offsets of the records
				if(getRecord(i)!=null)
					dos.writeInt(((IndexRecord)getRecord(i)).getOffset());
				else
					dos.writeInt(-1);
			}
			dos.writeBytes(filenameRecString);   //write the filenames of the records
			dos.writeBytes(completePageStr);  //finally, complete the size of the page
			
			array = bos.toByteArray();  //convert the stream to byte array
			
			dos.close();  //close the streams
			bos.close();
		}
		catch(IOException e) {
			System.err.println("Error while converting an IndexPage object to byte array.");
		}
		
		return array;
	}
}
