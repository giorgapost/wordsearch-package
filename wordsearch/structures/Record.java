package wordsearch.structures;

/**
 * A simple class which stores a {@link java.lang.String} object, an integer number and an integer constant.
 * @author Georgios Apostolakis
 */
public class Record {
	private String str;
	private int intValue;
	
	/**
	 * This constant indicates the correct length of the {@link java.lang.String} member-variable of this instance, when it has been set properly.
	 */
	public final int SIZEOF_STRING;
	
	/**
	 * The default constructor, which stores an empty string and sets the integer equal to -1.
	 * @param SIZEOF_STRING An integer that will be the value of the constant SIZEOF_STRING.
	 */
	public Record(int SIZEOF_STRING){
		this.str = null;
		this.intValue = -1;
		this.SIZEOF_STRING = SIZEOF_STRING;
	}
	
	/**
	 * Constructs a new instance containing the string and integer which are provided as arguments.
	 * @param str The string to be stored at this instance.
	 * @param intValue The integer to be stored at this instance.
	 * @param SIZEOF_STRING An integer that will be the value of the constant SIZEOF_STRING.
	 */
	public Record(String str, int intValue, int SIZEOF_STRING){
		this.str = str;
		this.intValue = intValue;
		this.SIZEOF_STRING = SIZEOF_STRING;
	}
	
	/**
	 * Creates and returns a copy of this object. For any {@link Record} r, the following expression is true:
	 * <blockquote>
	 * r.clone() != r
 	 * </blockquote>
	 * @return A clone of this instance.
	 */
	public Record clone(){
		return new Record(this.str, this.intValue, this.SIZEOF_STRING);
	}
	
	/**
	 * Provides the integer member-variable of this object.
	 * @return The integer which is stored inside this object.
	 */
	public int getIntValue() {
		return this.intValue;
	}
	
	/**
	 * Provides the string member-variable of this instance.
	 * @return A {@link java.lang.String} which is the string that was stored inside this instance.
	 */
	public String getString() {
		return this.str;
	}
	
	/**
	 * Sets the integer variable of this instance equal to its argument
	 * @param intValue An integer that will be stored inside this instance.
	 */
	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}
	
	/**
	 * Sets the string variable of this object equal to its argument.
	 * @param str A string that will be stored inside this object.
	 */
	public void setString(String str) {
		this.str = str;
	}
}
