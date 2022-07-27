package wordsearch.file;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * This class can read words from an ASCII file.
 * Words may be separated by spaces or any from the following punctuation symbols: 
 * {@code \t, \n, ',', '.', '!', '-', '(', ')', '"', ''', ':', ';', '?'}
 * @author Georgios Apostolakis
 */
public class AsciiFileProcessor {
	private RandomAccessFile processor;
	private char c;
	private boolean eofFound;
	
	/**
	 * Constructs a new instance of this class, which reads an existing file with a given filename.
	 * @param filename A {@link java.lang.String} with the name of the file to be read.
	 * @throws IOException In case of an error while accessing the file.
	 */
	public AsciiFileProcessor(String filename) throws IOException {
		this.processor = new RandomAccessFile(filename, "r");
		this.eofFound = false;
		
		try {
			Byte b = processor.readByte();  //find the beginning of the first word
			this.c =(char)(Integer.parseInt(b.toString()));
			while((c=='\t') || (c=='\n') || (c==' ') || (c==',') || (c=='.') || (c=='!') || (c=='-') || (c=='(') || (c==')') || (c=='"') || (c=='\'') || (c==':') || (c==';') || (c=='?')) {
				b = processor.readByte();
				this.c =(char)(Integer.parseInt(b.toString()));
			}	
		}catch (EOFException e){
			this.eofFound = true;
		}
	}
	
	/**
	 * Provides the position at the file where the next character is going to be read from.
	 * @return A long number with the current position at the file.
	 * @throws IOException In case of an error while accessing the file.
	 */
	public long getFilePosition() throws IOException {
		return processor.getFilePointer();
	}
	
	/**
	 * Releases the ASCII file associated with this instance.
	 * @throws IOException In case of an error while closing the stream to the file.
	 */
	public void close() throws IOException{
		processor.close();
	}
	
	/**
	 * Reads the next word from the file. Words are separated with spaces or any of the following symbols:
	 * {@code \t, \n, ',', '.', '!', '-', '(', ')', '"', ''', ':', ';'}
	 * @return A {@link java.lang.String} with the next word that was read from the file, or {@code null} if the end of the file has already been reached.
	 * @throws IOException In case of an error while closing the stream to the file.
	 */
	public String readNext() throws IOException {
		if(eofFound)  //if the end of the file has been reached, return null
			return null;
		
		String str = "" + this.c; //the character read at the end of the previous time
		try {  
			Byte b = processor.readByte();
			this.c =(char)(Integer.parseInt(b.toString()));  
			
			//read char by char until end of the word or until EOF is found
			while((c!='\t') && (c!='\n') && (c!=' ') && (c!=',') && (c!='.') && (c!='!') && (c!='-') && (c!='(') && (c!=')') && (c!='"') && (c!='\'') && (c!=':') && (c!=';') && (c!='?')) {
				str += c;
				b = processor.readByte();
				this.c = (char)(Integer.parseInt(b.toString()));		
			}
			
			//read until the beginning of a new word or until EOF is found
			while((c=='\t') || (c=='\n') || (c==' ') || (c==',') || (c=='.') || (c=='!') || (c=='-') || (c=='(') || (c==')') || (c=='"') || (c=='\'') || (c==':') || (c==';') || (c=='?')) {
				b = processor.readByte();
				this.c = (char)(Integer.parseInt(b.toString()));
			}
		} catch(EOFException e) {
			eofFound = true;  //from the next time, EOF will have been found and the method will return null
		}
		return str;		
	}
}
