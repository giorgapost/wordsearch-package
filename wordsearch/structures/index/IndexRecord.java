package wordsearch.structures.index;

import wordsearch.structures.Record;

/**
 * This class implements a record which can be stored in a page of the index.
 * @author Georgios Apostolakis
 */
public class IndexRecord extends Record{
	/**
	 * Constructs a new instance of {@link IndexRecord} that contains the {@link java.lang.String} {@code filename} and integer {@code offset} provided as arguments.
	 * The length of the {@code filename} is made to be exactly SIZEOF_KEY characters, by either padding with spaces at the ending, or by deleting its last characters.
	 * @param filename The {@link java.lang.String} to be stored in the new instance.
	 * @param offset The integer to be stored in the new instance.
	 * @param SIZEOF_FILENAME The value for the {@link wordsearch.structures.Record#SIZEOF_STRING} constant which determines the size of {@code filename}.
	 */
	public IndexRecord(String filename, int offset, int SIZEOF_FILENAME) {
		super(SIZEOF_FILENAME);		
		setFilename(filename);
		setOffset(offset);
	}
	
	/**
	 * Provides the filename stored in this instance.
	 * @return A {@link java.lang.String} containing the filename stored in this object.
	 */
	public String getFilename() {
		return super.getString();
	}
	
	/**
	 * Provides the offset of this instance.
	 * @return An integer with the offset variable of this instance.
	 */
	public int getOffset() {
		return super.getIntValue();
	}
	
	/**
	 * Sets the filename of this instance.
	 * @param filename The new filename, of type {@link java.lang.String}. If its length is less than SIZEOF_STRING, it is padded with spaces at its ending. If its length is greater than SIZEOF_STRING, its last characters are deleted.
	 */
	public void setFilename(String filename) {
		for(int i=filename.length();i<SIZEOF_STRING;i++) //pad with spaces at the end of the filename, so its length will be exactly SIZEOF_STRING
			filename += ' ';
		if(filename.length()>SIZEOF_STRING) //cut the last characters of the key, so its length will be exactly SIZEOF_STRING
			filename = filename.substring(0, SIZEOF_STRING);
		super.setString(filename);
	}
	
	/**
	 * Sets the offset of this instance.
	 * @param offset An integer containing the offset that will be stored in this object.
	 */
	public void setOffset(int offset) {
		super.setIntValue(offset);
	}
}