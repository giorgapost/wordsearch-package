package wordsearch.structures.btree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import wordsearch.structures.Page;
import wordsearch.structures.Record;

/**
 * A realization of the abstract class {@link wordsearch.structures.Page}. It implements a page used by the B-tree of the algorithm.
 * @author Georgios Apostolakis
 *
 */
public class TreePage extends Page{
	private int[] child;
	private int father;
	private int size;
	
	/**
	 * The maximum number of children per page of the tree.
	 * @see "The theoretical report coming with the package, which explains the way this constant is computed."
	 */
	public final int CHILDREN_PER_PAGE;
	
	/**
	 * The maximum number of {@link wordsearch.structures.btree.TreeRecord} objects held by this page.
	 */
	public final int RECORDS_PER_PAGE;
	
	/**
	 * Constructs a new instance of this class, empty from {@link wordsearch.structures.btree.TreeRecord} objects. 
	 * Also, an array of {@link #RECORDS_PER_PAGE} size with {@code null} entries is initialized to store {@link wordsearch.structures.btree.TreeRecord} objects in the future with {@link wordsearch.structures.Page#setRecord(int, Record)} method.
	 * Moreover, an array of integers is initialized called {@code child[]}, which helps to link this instance with other {@link TreePage} objects.
	 * Finally, {@code father} integer parameter of this instance is set to -1 and {@code size} parameter is set to 0.
	 * @param SIZEOF_KEY The size of the {@link java.lang.String} {@code key} stored inside every {@link wordsearch.structures.btree.TreeRecord} instance. 
	 * @param SIZEOF_PAGE The size (in bytes) of the current page.
	 */
	public TreePage(int SIZEOF_KEY, int SIZEOF_PAGE){
		super(SIZEOF_KEY, SIZEOF_PAGE);
		
		this.CHILDREN_PER_PAGE = (SIZEOF_PAGE-SIZEOF_INT+SIZEOF_KEY)/(2*SIZEOF_INT+SIZEOF_KEY); //see report for detailed calculation
		this.RECORDS_PER_PAGE = CHILDREN_PER_PAGE - 1;
		setRecordList(new TreeRecord[RECORDS_PER_PAGE]);
		for(int i=0;i<RECORDS_PER_PAGE;i++)
			setRecord(i, null);
		
		child=new int[CHILDREN_PER_PAGE];
		for(int i=0;i<CHILDREN_PER_PAGE;i++)
			this.child[i]=0;
		
		this.father=-1;
		this.size=0;
	}
	
	/**
	 * Constructs a new instance of this class and initializes it with some {@link wordsearch.structures.btree.TreeRecord} objects. It also initializes the parameters {@code father}, {@code child[]} and {@code size} of this class.
	 * @param SIZEOF_KEY The size of the {@link java.lang.String} {@code key} stored inside every {@link wordsearch.structures.btree.TreeRecord} instance. 
	 * @param SIZEOF_PAGE The size (in bytes) of the current page.
	 * @param recordList An array of {@link wordsearch.structures.btree.TreeRecord} objects.
	 * @param child An array of integers which helps to link this instance with other {@link TreePage} objects.
	 * @param father An integer which helps to link this instance with another {@link TreePage object}.
	 * @param size This integer is used to keep track of the number of {@link wordsearch.structures.btree.TreeRecord} objects stored in this instance.
	 */
	public TreePage(int SIZEOF_KEY, int SIZEOF_PAGE, TreeRecord[] recordList, int[] child, int father, int size){
		super(SIZEOF_KEY, SIZEOF_PAGE);
		this.CHILDREN_PER_PAGE = (SIZEOF_PAGE-SIZEOF_INT+SIZEOF_KEY)/(2*SIZEOF_INT+SIZEOF_KEY);
		this.RECORDS_PER_PAGE = CHILDREN_PER_PAGE - 1;
		
		setRecordList(recordList);
		this.child=child;
		this.father=father;
		this.size=size;
	}
	
	/**
	 * Constructs a new instance of this class and initializes it with {@link wordsearch.structures.btree.TreeRecord} objects recovered by a byte array.
	 * @param SIZEOF_KEY The size of the {@link java.lang.String} {@code key} stored inside every {@link wordsearch.structures.btree.TreeRecord} instance. 
	 * @param SIZEOF_PAGE The size (in bytes) of the current page.
	 * @param byteArray A byte array that contains some {@link wordsearch.structures.btree.TreeRecord} objects. If {@code null}, then this method is equivalent with {@link #TreePage(int, int)}.
	 * @throws IOException in the case of a malformed byte array.
	 */
	public TreePage(int SIZEOF_KEY, int SIZEOF_PAGE, byte[] byteArray) throws IOException {
		super(SIZEOF_KEY, SIZEOF_PAGE);
		this.CHILDREN_PER_PAGE = (SIZEOF_PAGE-SIZEOF_INT+SIZEOF_KEY)/(2*SIZEOF_INT+SIZEOF_KEY);
		this.RECORDS_PER_PAGE = CHILDREN_PER_PAGE - 1;
		
		if(byteArray==null) {
			setRecordList(new TreeRecord[RECORDS_PER_PAGE]);
			for(int i=0;i<RECORDS_PER_PAGE;i++)
				setRecord(i, null);
			
			child=new int[CHILDREN_PER_PAGE];
			for(int i=0;i<CHILDREN_PER_PAGE;i++)
				this.child[i]=0;
			
			this.father=-1;
			this.size=0;
			return;
		}
		
		int[] infoArray = new int[RECORDS_PER_PAGE];
		String[] s = new String[RECORDS_PER_PAGE];
		
		ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
		DataInputStream dis = new DataInputStream(bis);		
		
		this.size = dis.readInt();     //read size
		this.father = dis.readInt();   //read father
		this.child = new int[CHILDREN_PER_PAGE];  //read child array
		for(int i=0;i<CHILDREN_PER_PAGE;i++)
			this.child[i] = dis.readInt();
				
		for(int i=0;i<RECORDS_PER_PAGE;i++)  //read infos
			infoArray[i] = dis.readInt();
		for(int i=0;i<RECORDS_PER_PAGE;i++){  //read each key char by char
			s[i] = "";
			for(int j=0;j<SIZEOF_KEY;j++){
				Byte bt = byteArray[(2+CHILDREN_PER_PAGE)*SIZEOF_INT+RECORDS_PER_PAGE*SIZEOF_INT+SIZEOF_KEY*i+j];
				s[i] += (char)(Integer.parseInt(bt.toString()));
			}
		}
		
		super.setRecordList(new TreeRecord[RECORDS_PER_PAGE]);
		for(int i=0;i<RECORDS_PER_PAGE;i++){ //for all the records of the page
			if(infoArray[i]<0)      //if there is a real record
				setRecord(i, null);
			else
				setRecord(i, new TreeRecord(s[i], infoArray[i], SIZEOF_KEY));
		}
		dis.close();
		bis.close();
	}
	
	/**
	 * Adds a new entry in the {@code child[]} array, if some free space exists (i.e. one or more of its last entries equal to 0).
	 * When the new entry is added at a specific position, all entries at this and greater positions are shifted (and one zero entry disappears from the end of the array).
	 * @param position The position of the {@code child[]} array where the new entry will be inserted.
	 * @param newChild The value (integer) of the new entry which will be inserted.
	 * @return True if the operation completed successfully, or false if an error was faced (invalid {@code position} argument or full {@code child[]} matrix).
	 */
	public boolean addChild(int position, int newChild){
		if(this.child[CHILDREN_PER_PAGE-1]!=0 || position<0 || position>CHILDREN_PER_PAGE-1)
			return false;
		
		for(int j=CHILDREN_PER_PAGE-1;j>position;j--)  //move the rest children one position front
			this.child[j] = this.child[j-1];
		
		this.child[position] = newChild;
		return true;
	}
	
	/**
	 * Adds a new {@link wordsearch.structures.btree.TreeRecord} object in the current instance. What is more, the new {@link wordsearch.structures.btree.TreeRecord} is placed to the correct position
	 * (an alphabetical sorting is followed according to the {@link wordsearch.structures.btree.TreeRecord#getKey() key} variable.
	 * @param r The new TreeRecord to be added
	 * @return The index in the list of {@link wordsearch.structures.btree.TreeRecord} objects where the new {@link wordsearch.structures.btree.TreeRecord} was added.
	 * If a {@link wordsearch.structures.btree.TreeRecord} with the same key already exists, then its location is returned and no new object is added.
	 * Finally, -1 is returned if the {@link wordsearch.structures.btree.TreeRecord} could not be added, due to an already full list of {@link wordsearch.structures.btree.TreeRecord} objects.
	 */
	public int addRecord(Record r){
		int location = -1;
		boolean spaceAvailableExists = false;
		
		for(int i=0;i<RECORDS_PER_PAGE;i++){
			if(getRecord(i)==null){ //if at least one null element exists in the list, free space is available
				spaceAvailableExists = true;
				break;
			}
			else if(((TreeRecord)r).getKey().equals(((TreeRecord)getRecord(i)).getKey())){ //a Record with the same key already exists
				location = i;
				break;
			}	
		}
		
		if((location>=0)||(spaceAvailableExists==false)) //a Record with the same key already exists or the list is full
			return location;
		
		for(int i=0;i<RECORDS_PER_PAGE;i++){
			if(getRecord(i)==null){  //if the end of the not-null Records in the list has been reached, insert the new Record here
				setRecord(i, r);
				location = i;
				break;
			}
			if(((TreeRecord)getRecord(i)).getKey().compareTo(((TreeRecord)r).getKey())>0){ //insert the new Record here, in order to be alphabetically sorted compared to the rest (with the criterion being its key)
				for(int j=CHILDREN_PER_PAGE-1;j>i;j--) //move the rest Records in order to make some space
					setRecord(j, getRecord(j-1));
				setRecord(i, r);
				location = i;
				break;
			}
		}
		size++;   //increase the number of stored Records in the page
		return location;
	}
	
	/**
	 * Finds (if exists) the {@link wordsearch.structures.btree.TreeRecord} which is stored in this instance and contains the key provided as an argument.
	 * @param key A {@link java.lang.String} to search for inside all {@link wordsearch.structures.btree.TreeRecord} objects stored in this instance.
	 * @return If exists, a {@link wordsearch.structures.btree.TreeRecord} object which contains the specific key, or else {@code null}.
	 */
	public TreeRecord findRecord(String key) {
		for(int i=0;i<RECORDS_PER_PAGE;i++)
			if(((TreeRecord)getRecord(i)).getKey().equals(key))
				return (TreeRecord)getRecord(i);
		return null;
	}
	
	/**
	 * Provides the variable {@code child[i]} which links this instance with another {@link TreePage} object.
	 * @param i An integer which is an index at the array {@code child[]}.
	 * @return An integer which is the value of {@code child[i]} variable. 
	 */
	public int getChild(int i) {
		return child[i];
	}
	
	/**
	 * Provides the whole {@code child[]} array, which links this instance with other {@link TreePage} objects.
	 * @return An integer array which has the same entries with the {@code child[]} variable of this instance. 
	 */
	public int[] getChildArray() {
		return this.child;
	}
	
	/**
	 * Provides the {@code father} integer variable which is a member of this class.
	 * @return An integer whose value is equal with the {@code father} member-variable of this class.
	 */
	public int getFather() {
		return father;
	}
	
	/**
	 * Provides the {@code size} integer variable which is a member of this class and indicates the number of {@link wordsearch.structures.btree.TreeRecord} objects stored in this instance.
	 * @return An integer with the value of the {@code size} member-variable of this class.
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * Updates an entry of the {@code child[]} variable to a specific (integer) value.
	 * @param i The index at the {@code child[]} array which will update its value.
	 * @param c The new integer value to store at {@code child[i]} variable. 
	 */
	public void setChild(int i, int c){
		this.child[i] = c;
	}
	
	/**
	 * Updates the value of the {@code father} member variable of this class.
	 * @param father The new integer value that will replace the old one on the {@code father} variable.
	 */
	public void setFather(int father) {
		this.father = father;
	}
	
	/**
	 * Splits the current instance into 2 new {@link TreePage} instances. The {@link wordsearch.structures.btree.TreeRecord} in the middle is omitted.
	 * Half of the {@link wordsearch.structures.btree.TreeRecord} objects are stored in the first new {@link wordsearch.structures.btree.TreePage} object and the other half are stored in the second new {@link TreePage} object.
	 * Alphabetical sorting according to the {@link wordsearch.structures.btree.TreeRecord#getKey() key} field is kept both inside and across the new objects.
	 * Finally, the {@code child[]} array is also split appropriately.
	 * @return A {@link TreePage TreePage[]} array that contains the 2 new {@link TreePage} objects.
	 */
	public TreePage[] split(){
		TreePage[] finPage = new TreePage[2];

		finPage[0] = new TreePage(SIZEOF_STRING, SIZEOF_PAGE, ((TreeRecord[])getRecordList()).clone(), child.clone(), this.father, this.size/2);
		for(int i=(int)RECORDS_PER_PAGE/2;i<RECORDS_PER_PAGE;i++)
			finPage[0].setRecord(i, null);
		for(int i=(int)RECORDS_PER_PAGE/2+1;i<CHILDREN_PER_PAGE;i++)
			finPage[0].setChild(i,0);
		
		finPage[1] = new TreePage(SIZEOF_STRING, SIZEOF_PAGE, ((TreeRecord[])getRecordList()).clone(), child.clone(), this.father, this.size/2);
		for(int i=((int)RECORDS_PER_PAGE/2+1);i<RECORDS_PER_PAGE;i++) //move the last elements to the start of the array
			finPage[1].setRecord(i-((int)RECORDS_PER_PAGE/2+1), finPage[1].getRecord(i));
		
		for(int i=(int)RECORDS_PER_PAGE/2;i<RECORDS_PER_PAGE;i++) //even
			finPage[1].setRecord(i, null);
		if(RECORDS_PER_PAGE%2 == 0) {
			finPage[1].setRecord((int)RECORDS_PER_PAGE/2-1, null);
		}

		for(int i=(int)RECORDS_PER_PAGE/2+1;i<CHILDREN_PER_PAGE;i++)  //move the last elements to the start of the array
			finPage[1].setChild(i-(int)RECORDS_PER_PAGE/2-1, finPage[1].getChild(i));
		
		
		for(int i=(int)RECORDS_PER_PAGE/2+1;i<CHILDREN_PER_PAGE;i++)
			finPage[1].setChild(i,0);
		if(RECORDS_PER_PAGE%2 == 0) {
			finPage[1].setChild((int)RECORDS_PER_PAGE/2, 0);
		}
		return finPage;
	}
	
	/**
	 * Converts the current instance into a byte array.
	 * @return A byte array with all information existing in this object.
	 */
	public byte[] toByteArray(){
		String nullRecString = "", keyRecString="", completePageStr="";
		byte [] array = null;
		
		
		for(int i=0;i<SIZEOF_STRING;i++)  // used in the position of null records
			nullRecString += ' ';
		for(int i=0;i<(SIZEOF_PAGE-2*SIZEOF_INT - CHILDREN_PER_PAGE*SIZEOF_INT - RECORDS_PER_PAGE*SIZEOF_RECORD);i++)
			completePageStr += ' ';   //used to complete the 128 bytes
		
		for(int i=0;i<RECORDS_PER_PAGE;i++) { //contains the keys from all records of this instance
			if(super.getRecord(i)!=null)
				keyRecString += ((TreeRecord)super.getRecord(i)).getKey();
			else
				keyRecString += nullRecString;
		}
		
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			
			dos.writeInt(this.size);  //write the size variable
			dos.writeInt(this.father);  //write the father variable
			for(int i=0;i<CHILDREN_PER_PAGE;i++)  //write the child[] variable
				dos.writeInt(child[i]);
			for(int i=0;i<RECORDS_PER_PAGE;i++){  //write the infos of the records
				if(super.getRecord(i)!=null)
					dos.writeInt(((TreeRecord)super.getRecord(i)).getInfo());
				else
					dos.writeInt(-1);
			}
			dos.writeBytes(keyRecString);   //write the keys of the records
			dos.writeBytes(completePageStr);  //to complete the size of the page
			
			array = bos.toByteArray();
			
			dos.close();
			bos.close();
		}
		catch(IOException e) {
			System.err.println("Error while converting a TreePage object to byte array.");
		}
		
		return array;
	}
}
