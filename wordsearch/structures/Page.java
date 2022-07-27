package wordsearch.structures;

import java.io.IOException;

/**
 * An abstract class which contains a list of {@link wordsearch.structures.Record} objects.
 * @author Georgios Apostolakis
 */
public abstract class Page {
	private Record[] recordList;
	
	/**
	 * The size (in bytes) of the {@link java.lang.String} stored in every record of this page.
	 */
	public final int SIZEOF_STRING;
	
	/**
	 * The size (in bytes) of the integer stored in every record of this page.
	 */
	public final int SIZEOF_INT;
	
	/**
	 * The size (in bytes) of the data from every record of this page.
	 */
	public final int SIZEOF_RECORD;
	
	/**
	 * The size (in bytes) of the current page.
	 */
	public final int SIZEOF_PAGE;
	
	/**
	 * Constructs a new instance of this class, empty from {@link wordsearch.structures.Record} objects. Method {@link Page#setRecord(int, Record)} will fail until {@link Page#setRecordList(Record[])} is called first.
	 * @param SIZEOF_STRING The size (in bytes) of the {@link java.lang.String} stored in every record of this page.
	 * @param SIZEOF_PAGE The size (in bytes) of the current page.
	 */
	public Page(int SIZEOF_STRING, int SIZEOF_PAGE) {
		this.SIZEOF_STRING = SIZEOF_STRING;
		this.SIZEOF_PAGE = SIZEOF_PAGE;
		this.SIZEOF_INT = Integer.SIZE/8;
		this.SIZEOF_RECORD = SIZEOF_STRING + SIZEOF_INT;
		this.recordList = null;
	}
	
	/**
	 * Constructs a new object of this class, which stores the {@link wordsearch.structures.Record} objects given by the argument, in the same order.
	 * @param SIZEOF_STRING The size (in bytes) of the {@link java.lang.String} stored in every record of this page.
	 * @param SIZEOF_PAGE The size (in bytes) of the current page.
	 * @param recordList An array of records with which the new object will be initialized.
	 */
	public Page(int SIZEOF_STRING, int SIZEOF_PAGE, Record[] recordList) {
		this.SIZEOF_STRING = SIZEOF_STRING;
		this.SIZEOF_PAGE = SIZEOF_PAGE;
		this.SIZEOF_INT = Integer.SIZE/8;
		this.SIZEOF_RECORD = SIZEOF_STRING + SIZEOF_INT;
		this.recordList = recordList;
	}
	
	/**
	 * Adds a new {@link wordsearch.structures.Record} object to the current class, if there exists some empty position. 
	 * @param r The new {@link wordsearch.structures.Record} object to be added.
	 * @return The position into which the new {@link wordsearch.structures.Record} object was stored, or -1 in the case of no available space.
	 */
	public abstract int addRecord(Record r);
	
	/**
	 * Returns the {@link wordsearch.structures.Record} object stored at a specific position.
	 * @param i The position at which the {@link wordsearch.structures.Record} object is stored. It can get values from 0 to {@link #getRecordListMaxLength()}- 1.
	 * @return A {@link wordsearch.structures.Record} object or {@code null}, depending on the contents of the specified position.
	 */
	public Record getRecord(int i){
		if(recordList!=null && i>=0 && i<recordList.length)
			return this.recordList[i];
		return null;
	}
	
	/**
	 * Returns all the {@link wordsearch.structures.Record} objects stored in this class, in the form of an array. Some cells may be null, if no {@link wordsearch.structures.Record} has been previously assigned to them.
	 * @return An array with all the {@link wordsearch.structures.Record} objects of this class, or {@code null} if no entries exist.
	 */
	public Record[] getRecordList(){
		return this.recordList;
	}
	
	/**
	 * Provides the maximum number of {@link wordsearch.structures.Record} objects that can be stored by this class. This size can change by appropriately calling {@link #setRecordList(Record[])}.
	 * @return The length of the array that stores the {@link wordsearch.structures.Record} objects.
	 */
	public int getRecordListMaxLength() {
		if (this.recordList == null)
				return 0;
		else
			return this.recordList.length;
	}
	
	/**
	 * Change the value of a specific {@link wordsearch.structures.Record} object stored in this class.
	 * @param i The position of the {@link wordsearch.structures.Record} whose value will be updated. It can get values from 0 to {@link #getRecordListMaxLength()}- 1.
	 * @param r The new value of the {@link wordsearch.structures.Record} at position provided by the previous argument.
	 * @return True if the operation is completed successfully and false otherwise.
	 */
	public boolean setRecord(int i, Record r){
		if(recordList!=null && i>=0 && i<recordList.length) {
			recordList[i] = r;
			return true;
		}
		return false;
	}
	
	/**
	 * Initializes the list of {@link wordsearch.structures.Record} objects stored in the current class, and preserves their order.
	 * @param recordList An array containing {@link wordsearch.structures.Record} objects.
	 */
	public void setRecordList(Record[] recordList) {
		this.recordList = recordList;
	}
	
	/**
	 * Converts the current class into a byte array.
	 * @return A byte array with the contents of this class.
	 * @throws IOException Children methods may have to throw IOException during their implementation.
	 */
	public abstract byte[] toByteArray() throws IOException;
}
