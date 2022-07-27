# Java package to search for word occurrences in ASCII files
This package implements a system which can search for some given words into a large number of text files and return all the locations where they are found. As 'word' is defined every sequence of characters separated with one or more spaces or the following punctuation symbols:
{@code \t, \n, ',', '.', '!', '-', '(', ')', '"', ''', ':', ';', '?'}.

File Examples.java provides examples for the 2 alternative ways in which the package may be utilized. The simplest one is calling the answerQuestions() method, which provides a basic user interface (through console) and allows the user to type the word he needs to search for.
The other alternative passes the words as argument to the method, and the list with their occurrences is provided as the return value of the method. While the first one helps to get acquainted with the software, the latter one may be more useful in most applications.

Note: Analytical documentation for the package can be created with the javadoc tool (see below).

## Implementation details
The system consists of a BTree(https://en.wikipedia.org/wiki/B-tree) structure, as well as of a paged index. Every record on the tree consists of a key, which is the word we search for,
as well as of an integer which points to a page of the index. Every page of the index contains a specific number of records, with each one of them consisting of
one filename and one integer which corresponds to a location on that file. What is more, an index page contains an integer which may point to another one.
With this technique the construction of "chains" of pages is possible.

After all, when the system is asked to search for a word, it crosses the BTree (from root to leaves) and finds (if exists) the record which contains that word. Afterwards, it reads the index page
pointed by the tree record and returns all its contents (which are the filenames and locations of that word in the input ASCII files). If the occurrences are too much,
then a "chain" of index pages is created, as described above, and the system returns the contents of the whole chain.

This package implements both the BTree and the index in a file on the disk. That means that every operation is written immediately, and when a page is required
it has to be read from the disk again. The purpose of this technique is to minimize the memory usage as much as possible, even with the cost of a slower execution. Of course,
a hybrid or exclusively-in-RAM implementation is also possible.

Notice that the construction of such structures may take enough time, especially for large inputs. However, once the construction has been completed, the search for
a word is a very efficient procedure. Rebuilding of the structures has to take place only in the case where the input changes, and every time it builds the data structures
of the system from scratch.

## Compilation
The package is provided with the 'Examples.java' class which contains 2 examples on its utilization, and it can be easily integrated into any system.
To compile and execute the provided example code run:

javac -d classes Examples.java #To compile the .java files into .class binaries.
java -classpath "classes" Examples input/ #Provide as argument the location of the input ASCII files.

To compile the documentation through the javadoc tool run:

javadoc -d doc wordsearch wordsearch.structures wordsearch.file wordsearch.structures.btree wordsearch.structures.index #To generate documentation

Finally, to compile package wordsearch alone (which contains the implementation of the system), use the following command:

javac -d classes wordsearch/WordSearch.java
