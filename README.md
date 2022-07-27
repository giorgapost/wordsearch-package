# Java package to search for words in ASCII files
This package implements a system which searches for some given words into a large number of text files, and returns all the locations where they were found. 

> **Note**  
> As 'word' is defined every sequence of characters separated with one or more spaces or the following punctuation symbols:
> ```
> \t,  \n,  ',',  '.',  '!',  '-',  '(',  ')',  '"',  ''',  ':',  ';',  '?'
> ```

> **Note**  
> Analytical documentation for the source code of the package can be generated with the [javadoc](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/javadoc.html) tool (see below).

## Implementation details
The system consists of a [B-Tree](https://en.wikipedia.org/wiki/B-tree) structure, as well as of a paged index. 
- Every record on the tree consists of a key, which is the word we search for, as well as of an integer which points to a page of the index. 
- Every page of the index contains a specific number of records, with each one of them consisting of one String (filename) and one integer which corresponds to a location (bytes from the beginning of a file). What is more, an index page contains an integer which may point to another page. With this technique, the construction of "chains" of pages is possible for that structure.

After all, when the system is asked to search for a word, it crosses the BTree (from root to leaves) and finds (if exists) the record which contains that word. That records points to a page of the index, and the system reads it and returns all of its contents (which are the filenames and locations where occurrences are observed in the input ASCII files). If the occurrences are too much, then a "chain" of index pages has been created as described above, and the system returns the contents of the whole chain.

> **Warning**  
> Both the B-Tree and the index structures have to be built before any search operation takes place.

This Java package implements both the B-Tree and the index in some binary files on the disk. Every operation on these structures is written immediately, and every required piece of data has to be read from the disk again. What is more, the data of the binary files are separated into pages of predetermined size, and every read/write operation takes place on exactly one page. The purpose of this technique is to minimize the memory usage as much as possible, even at the cost of a slower execution. Of course a hybrid or exclusively-in-RAM implementation is also possible.

Notice that the construction of such structures may take enough time, especially when the input is large, due to the large number of disk operations. However, once the construction has been completed the search for a word is a very quick process, since very few pages have to be read from the B-Tree and the index structures (compared to their total size).

> **Warning**  
> Every time when the contents of the input text files change, both the B-Tree and the index have to be rebuilt through the [buildDataFiles()](https://github.com/giorgapost/wordsearch-package/blob/d6124c653c18e11111da905ff3d5022bbbfe89b0/wordsearch/WordSearch.java#L155) method. While there are special cases where they may be adapted to the new input without a rebuild from scratch, this is not the case of the current implementation. In order to guarantee valid results, the user has to recreate both structures after any change of the input.

> **Note**  
> When the input remains unchanged, the B-Tree and the index do not have to be generated more than once. Questions to search for can be given to the system right after its execution.

File [Examples.java](Examples.java) provides examples of 2 alternative ways in which the package may be utilized.
- The simplest one calls the [answerQuestions()](https://github.com/giorgapost/wordsearch-package/blob/d6124c653c18e11111da905ff3d5022bbbfe89b0/wordsearch/WordSearch.java#L78) method, which provides a basic user interface (through console) and allows the user to type the word he needs to search for. 
- The other way passes the words as argument to the [method](https://github.com/giorgapost/wordsearch-package/blob/d6124c653c18e11111da905ff3d5022bbbfe89b0/wordsearch/WordSearch.java#L122), and the list with their occurrences is provided as the return value. 

While the first one helps to get acquainted with the software, the latter one may be more useful in most applications.

> **Warning**  
> When integrating the package to larger applications do not forget to call [terminate()](https://github.com/giorgapost/wordsearch-package/blob/d6124c653c18e11111da905ff3d5022bbbfe89b0/wordsearch/WordSearch.java#L228) before exiting the program.

## Compilation
The package is provided with the 'Examples.java' class which contains 2 examples on its utilization, and it can be easily integrated into any system.
To compile and execute the provided example code run:

javac -d classes Examples.java #To compile the .java files into .class binaries.
java -classpath "classes" Examples input/ #Provide as argument the location of the input ASCII files.

To compile the documentation through the javadoc tool run:

javadoc -d doc wordsearch wordsearch.structures wordsearch.file wordsearch.structures.btree wordsearch.structures.index #To generate documentation

Finally, to compile package wordsearch alone (which contains the implementation of the system), use the following command:

javac -d classes wordsearch/WordSearch.java
