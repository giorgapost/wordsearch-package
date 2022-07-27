package wordsearch.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import wordsearch.structures.Page;

/**
 * A simple class to read and write pages in binary files. It also counts the number of accesses in the disk.
 * @author Georgios Apostolakis
 */
public class BinFileProcessor {
	private RandomAccessFile processor;
	private int diskAccessesCounter;
	
	/**
	 * The size (in bytes) of every {@link wordsearch.structures.Page} object in the binary file.
	 */
	public final int SIZEOF_PAGE;
	
	/**
	 * Constructs an instance of this class which reads/writes to a specific file on the disk.
	 * @param SIZEOF_PAGE The value for the {@link #SIZEOF_PAGE} constant of this class.
	 * @param filename The name of the binary file where the read/write operations will take place.
	 * @throws FileNotFoundException In case where  the given filename does not denote an existing, writable file and a new file of that name cannot be created.
	 */
	public BinFileProcessor(int SIZEOF_PAGE, String filename) throws FileNotFoundException{
		this.SIZEOF_PAGE = SIZEOF_PAGE;
		this.processor = new RandomAccessFile(filename, "rw");
		this.diskAccessesCounter = 0;
	}
	
	/**
	 * Deletes any contents existing in the binary file.
	 * @throws IOException In case of an error while accessing the file.
	 */
	public void clearFile() throws IOException{
		this.processor.setLength(0);
	}
	
	/**
	 * Releases the binary file associated with this instance.
	 * @throws IOException In case of an error while closing the stream to the file.
	 */
	public void close() throws IOException {
		this.processor.close();
	}
	
	/**
	 * Returns the size of the binary file, in number of {@link wordsearch.structures.Page} objects (and not in bytes).
	 * @return A long number with the size of the binary file.
	 * @throws IOException In case of an error while reading the file.
	 */
	public long fileSize() throws IOException {
		long tmp = processor.length();
		return tmp/SIZEOF_PAGE;
	}
	
	/**
	 * Provides the current number of disk accesses, since the last time the counter was reset.
	 * @return An integer with the number of disk accesses.
	 */
	public int getNumOfAccesses() {
		return this.diskAccessesCounter;
	}
	
	/**
	 * Provides a byte array with the contents of a given page from the binary file.
	 * @param pageNum The number of the page to be read.
	 * @return A byte array with the read page from the file, or {@code null} when the page number is invalid.
	 * @throws IOException In case of an error while reading from the file.
	 */
	public byte[] readPage(long pageNum) throws IOException{
		byte[] buffer = new byte[SIZEOF_PAGE];
		
		if(fileSize()<=pageNum||pageNum<0)
			return null;
		
		processor.seek(pageNum*SIZEOF_PAGE);
		processor.read(buffer);
		this.diskAccessesCounter++;
		
		return buffer;
	}
	
	/**
	 * Resets the counter of disk accesses to zero.
	 */
	public void resetNumOfAccesses() {
		this.diskAccessesCounter = 0;
	}
	
	/**
	 * Converts a given {@link wordsearch.structures.Page} object into a byte array and writes it in the binary file at a specified position.
	 * @param position An integer with the position where the {@link wordsearch.structures.Page} will be written. Gets values greater or equal to zero.
	 * Notice that the i-th position corresponds to the i-th page of the file, and not to its i-th byte.
	 * @param page A {@link wordsearch.structures.Page} object to be written into the file.
	 * @throws IOException In case of an error while writing into the file.
	 */
	public void writePage(long position, Page page) throws IOException {
		byte[] buffer;
		
		buffer = page.toByteArray();
		processor.seek(position*SIZEOF_PAGE);
		processor.write(buffer);
		this.diskAccessesCounter++;
	}
}