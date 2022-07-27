package wordsearch.structures.btree;

import java.io.FileNotFoundException;
import java.io.IOException;

import wordsearch.file.BinFileProcessor;

/**
 * This class implements a BTree on the disk, which serves as a dictionary for searching words.
 * More in detail, this BTree contains all the words found in the input text files, as well as their respective page in the Index.
 * Finding the location of a word is the process of finding that word in the tree and going to the correct location
 * at the index from where a list with locations can be retrieved.
 * @author Georgios Apostolakis
 *
 */
public class BTree {
	private BinFileProcessor binaryFileEditor;
	
	/**
	 * The size (in bytes) of every {@link wordsearch.structures.btree.TreePage page} of the BTree.
	 */
	public final int SIZEOF_PAGE;
	
	/**
	 * The size (in bytes) of every key stored inside the {@link wordsearch.structures.btree.TreeRecord} objects of the BTree.
	 */
	public final int SIZEOF_KEY;
	
	/**
	 * The maximum number of children per {@link wordsearch.structures.btree.TreePage page} of the tree.
	 */
	public final int CHILDREN_PER_TREE_PAGE;
	
	/**
	 * The maximum number of {@link wordsearch.structures.btree.TreeRecord} objects held by a {@link wordsearch.structures.btree.TreePage}.
	 */
	public final int RECORDS_PER_TREE_PAGE;
	
	/**
	 * Constructs a new instance of this class which builds a BTree into a binary file.
	 * @param SIZEOF_PAGE The size (in bytes) of every {@link wordsearch.structures.btree.TreePage page} of the BTree.
	 * @param SIZEOF_KEY The size (in bytes) of every key stored inside the {@link wordsearch.structures.btree.TreeRecord} objects of the BTree.
	 * @param filename The name of the binary file into which the BTree will be stored.
	 */
	public BTree(int SIZEOF_PAGE, int SIZEOF_KEY, String filename){
		this.SIZEOF_PAGE = SIZEOF_PAGE;
		this.SIZEOF_KEY = SIZEOF_KEY;
		this.CHILDREN_PER_TREE_PAGE = (SIZEOF_PAGE-(Integer.SIZE/8)+SIZEOF_KEY)/(2*(Integer.SIZE/8)+SIZEOF_KEY);
		this.RECORDS_PER_TREE_PAGE = CHILDREN_PER_TREE_PAGE - 1;
		try{
			binaryFileEditor = new BinFileProcessor(SIZEOF_PAGE, filename);
		} catch(FileNotFoundException e){
			System.err.println("Error. Unable to create btree file.");
			System.exit(-1);
		}
	}
	
	/**
	 * Deletes the contents (if any) of the binary file where the btree is stored.
	 * @throws IOException In case of an error while accessing the file.
	 */
	public void clearDataOfTreeFile() throws IOException {
		this.binaryFileEditor.clearFile();
	}
	
	/**
	 * Releases the binary file associated with this instance.
	 * @throws IOException In case of an error while closing the stream to the file.
	 */
	public void close() throws IOException {
		binaryFileEditor.close();
	}
	
	/**
	 * Returns the size of the binary file that contains the btree, in number of {@link wordsearch.structures.btree.TreePage} objects (and not in bytes).
	 * @return An integer number with the size of the binary file.
	 * @throws IOException In case of an error while reading the file.
	 */
	public int getSize() throws IOException{
		return (int)this.binaryFileEditor.fileSize();
	}
	
	/**
	 * Returns the number of disk accesses from the last time they were reset.
	 * @return An integer number with the number of disk accesses for the purposes of this instance.
	 */
	public int getNumOfDiskAccesses() {
		return this.binaryFileEditor.getNumOfAccesses();
	}
	
	/**
	 * Provides a specific page of the tree.
	 * @param i The index (at the binary file of the btree) of the {@link wordsearch.structures.btree.TreePage} to be returned.
	 * @return The {@link wordsearch.structures.btree.TreePage} object found at the specified index of the btree file.
	 * @throws IOException In case of an error while reading the file, or a malformed binary file.
	 */
	public TreePage getPage(int i) throws IOException{
		byte[] tmp = binaryFileEditor.readPage(i);
		if(tmp != null)
			return new TreePage(SIZEOF_KEY, SIZEOF_PAGE, tmp);
		return null;
	}
	
	/**
	 * Inserts a new {@link wordsearch.structures.btree.TreeRecord} into the BTree, at the appropriate location.
	 * @param r The new {@link wordsearch.structures.btree.TreeRecord} to be added.
	 * @throws IOException In case of an error while reading/writing the file with the btree.
	 */
	public void insert(TreeRecord r) throws IOException{
		int positionInFile;
		TreePage pageRead;
		
		positionInFile = searchTree(r.getKey());
		if(positionInFile>=0)    //A TreeRecord with the same key already exists in the file
			return;

		//If the TreeRecord doesn't exist, search returns a negative number to indicate where it should be inserted (see searchTree() method).
		positionInFile = - positionInFile -1; 
		pageRead = new TreePage(SIZEOF_KEY, SIZEOF_PAGE, binaryFileEditor.readPage(positionInFile));
		
		pageRead.addRecord(r);  //Just add the record, all children remain to 0 since the TreePage is a leaf of the tree
		
		binaryFileEditor.writePage(positionInFile, pageRead); //Write the updated TreePage back to the file
		splitNodes(positionInFile);	//Split this page and its ancestors, if needed	
	}
	
	/**
	 * Resets to zero the variable that counts the disk accesses.
	 */
	public void resetNumOfDiskAccesses() {
		this.binaryFileEditor.resetNumOfAccesses();
	}
	
	/**
	 * Searches the tree for a {@link wordsearch.structures.btree.TreeRecord} with a specific {@link wordsearch.structures.btree.TreeRecord#getKey() key}.
	 * @param key A {@link java.lang.String} object to search for inside the {@link wordsearch.structures.btree.TreeRecord} instances of the tree.
	 * @return If the key was found, the index of the respective {@link wordsearch.structures.btree.TreePage} in the btree file.
	 * If the key was not found, the index {@code idx} of the {@link wordsearch.structures.btree.TreePage} in the btree file where it should be inserted, in the form {@code (-idx-1)}
	 * @throws IOException In case of an error while reading/writing the file with the btree.
	 */
	public int searchTree(String key) throws IOException{
		int foundPosition = -1;
		int currNode = 0; //the root of the tree
		int nextNode=-1;
		TreePage node;
		
		if(this.getSize()==0)  //Empty file - the new page must be inserted in position -0-1 = -1
			return -1;
		node = new TreePage(SIZEOF_KEY, SIZEOF_PAGE, binaryFileEditor.readPage(0));  //page 0 always is the root of the tree
		  		
		do{
			if(((TreeRecord)node.getRecord(0)).getKey().compareTo(key)>0)  //if key < every key in this node
				nextNode = node.getChild(0);  
			else if(((TreeRecord)node.getRecord(node.getSize()-1)).getKey().compareTo(key)<0)  // if key > every key in this node
				nextNode = node.getChild(node.getSize());  
			else {
				for(int i=0;i<node.getSize();i++)  //if key equals to one of the node's keys
					if(((TreeRecord)node.getRecord(i)).getKey().equals(key)){
						foundPosition=currNode;
						break;
					}
				if(foundPosition<0)  //if key different than every key in this node but it should be somewhere between them
					for(int i=0;i<node.getSize()-1;i++)
						if(((TreeRecord)node.getRecord(i)).getKey().compareTo(key)<0 && ((TreeRecord)node.getRecord(i+1)).getKey().compareTo(key)>0) {  //if record[i].key < key < record[i+1].key
							nextNode = node.getChild(i+1);
							break;
						}
			}
			
			if(foundPosition<0 && nextNode>0) {  //not found and not reached a leaf
				node = new TreePage(SIZEOF_KEY, SIZEOF_PAGE, binaryFileEditor.readPage(nextNode));
				currNode = nextNode;
			}
		}while(foundPosition<0 && nextNode>0);  //next node to be read will never be node 0, since that's the tree's root, so nextNode will equal to 0 only when a leaf is reached
		
		if(foundPosition<0)  //if not found
			return -currNode-1;
		return foundPosition;  //if found
	}
	
	/**
	 * Splits a {@link wordsearch.structures.btree.TreePage page} of the tree into 2 new {@link wordsearch.structures.btree.TreePage pages}, if the initial is full of {@link wordsearch.structures.btree.TreeRecord} objects.
	 * Otherwise, it performs no action.
	 * @param position The index in the btree file of the {@link wordsearch.structures.btree.TreePage page} to be split (if it is full).
	 * @throws IOException In case of an error while reading/writing the file with the btree.
	 */
	public void splitNodes(int position) throws IOException{
		int node1Pos = position;  //nodePos1 = the position of the initial node, as well as for the 1st of the 2 final nodes
		int node2Pos; 			  //nodePos2 = the position of the 2nd of the final nodes
		TreePage initial = new TreePage(SIZEOF_KEY, SIZEOF_PAGE, binaryFileEditor.readPage(position));  //the node to be split (if needed)
		int father;
		TreePage fatherNode = null;
		TreePage[] finNode;
		int posAdded;   //The position where the middle record was added into the father node
		
		while(initial.getSize()==RECORDS_PER_TREE_PAGE){
			
			father = initial.getFather();
			finNode = initial.split();
			node2Pos = this.getSize();  //the position of the 2nd node will be at the end of the file

			if(father<0) {  //if initial is the root of the tree
				finNode[0].setFather(0);  //because now the old father (-1) is NOT equal to the new one (0)
				finNode[1].setFather(0);
				binaryFileEditor.writePage(node2Pos, finNode[0]);  //write the 1st and 2nd node at the end of the file
				binaryFileEditor.writePage(node2Pos+1, finNode[1]);

				fatherNode = new TreePage(SIZEOF_KEY, SIZEOF_PAGE);
				fatherNode.addRecord(initial.getRecord((int)RECORDS_PER_TREE_PAGE/2)); //the middle record of the split node will be stored in the father of the 2 final nodes
				for(int i=0;i<2;i++)  //add the two final nodes as children to the father.
					fatherNode.setChild(i, node2Pos+i);
				binaryFileEditor.writePage(node1Pos, fatherNode);  //replace the root node in the file

				updateFatherOfChildren(finNode[0], node2Pos);  //for every split node, update its children's father variable, since its position now changed
				updateFatherOfChildren(finNode[1], node2Pos+1);
			}
			else { //if initial is not the root of the tree
				binaryFileEditor.writePage(node1Pos, finNode[0]);  //the position here doesn't change, so no update of node's 1 children's father variable is needed
				binaryFileEditor.writePage(node2Pos, finNode[1]);
				updateFatherOfChildren(finNode[1], node2Pos); //for every split node, update its children's father variable, since its position now changed
				
				fatherNode = new TreePage(SIZEOF_KEY, SIZEOF_PAGE, binaryFileEditor.readPage(father));
				posAdded = fatherNode.addRecord(initial.getRecord((int)RECORDS_PER_TREE_PAGE/2));
				fatherNode.setChild(posAdded, node1Pos);  //add the two final nodes as children to the father to the 'posAdded' position in the children's array
				fatherNode.addChild(posAdded+1, node2Pos); //change the posAdded child and put the extra one in posAdded+1 position by shifting the others right
				binaryFileEditor.writePage(father, fatherNode);  //replace the father node in the file
			}
			
			if(father<0)  //if we just split the root of the tree it's no use looking for a new father
	            break;
	        initial=new TreePage(SIZEOF_KEY, SIZEOF_PAGE, binaryFileEditor.readPage(father));  //read the new father so as to loop again if needed
			node1Pos=father;  //the new position for the 1st of the final nodes, if the loop is repeated (or for the new root, if the new initial node is the tree's root)
		}
	}
	
	/**
	 * Updates the value of the father member-variable, for all the children-{@link wordsearch.structures.btree.TreePage pages} of a given {@link wordsearch.structures.btree.TreePage}.
	 * @param pageToBeUpdated A {@link wordsearch.structures.btree.TreePage} whose children will have their father member-variable updated.
	 * @param newFather The (integer) index of the new father in the btree file.
	 * @throws IOException In case of an error while reading/writing the file with the btree.
	 */
	public void updateFatherOfChildren(TreePage pageToBeUpdated, int newFather) throws IOException{
		TreePage tmpNode;
		int child;
		
		for(int i=0;i<CHILDREN_PER_TREE_PAGE;i++){
			child = pageToBeUpdated.getChild(i);
			if(child==0)
				break;
			
			tmpNode = new TreePage(SIZEOF_KEY, SIZEOF_PAGE, binaryFileEditor.readPage(child));
			tmpNode.setFather(newFather);
			binaryFileEditor.writePage(child, tmpNode);
		}
	}	
}
