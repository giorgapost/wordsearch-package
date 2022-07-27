package wordsearch.structures.btree;

import wordsearch.structures.Record;

/**
 * This class implements a record which can be stored in a page of the B-tree.
 * @author Georgios Apostolakis
 */
public class TreeRecord extends Record{
	/**
	 * Constructs a new instance of {@link TreeRecord} that contains the {@link java.lang.String} {@code key} and integer {@code info} provided as arguments.
	 * The length of the {@code key} is made to be exactly SIZEOF_KEY characters, by either padding with spaces at the ending, or by deleting its last characters.
	 * Moreover, all characters of the {@code key} are turned into lowercase letters.
	 * @param key The {@link java.lang.String} to be stored in the new instance.
	 * @param info The integer to be stored in the new instance.
	 * @param SIZEOF_KEY The value for the {@link wordsearch.structures.Record#SIZEOF_STRING} constant which determines the size of {@code key}.
	 */
	public TreeRecord(String key, int info, int SIZEOF_KEY){
		super(SIZEOF_KEY);
		setKey(key);
		setInfo(info);
	}
	
	/**
	 * Provides the info of this instance.
	 * @return An integer with the info variable of this instance.
	 */
	public int getInfo() {
		return super.getIntValue();
	}
	
	/**
	 * Provides the key of this instance.
	 * @return A {@link java.lang.String} containing the key of this object.
	 */
	public String getKey() {
		return super.getString();
	}
	
	/**
	 * Sets the info of this instance.
	 * @param info An integer containing the info that will be stored in this object.
	 */
	public void setInfo(int info) {
		super.setIntValue(info);
	}
	
	/**
	 * Sets the key of this instance.
	 * @param key The new key, of type {@link java.lang.String}. If its length is less than SIZEOF_STRING, it is padded with spaces at its ending. If its length is greater than SIZEOF_STRING, its last characters are deleted.
	 */
	public void setKey(String key) {
		key = key.toLowerCase();
		while(key.length()<SIZEOF_STRING) //pad with spaces at the end of the key, so its length will be exactly SIZEOF_STRING
			key += ' ';
		if(key.length()>SIZEOF_STRING)  //cut the last characters of the key, so its length will be exactly SIZEOF_STRING
			key = key.substring(0, SIZEOF_STRING);
		super.setString(key);
	}
}
