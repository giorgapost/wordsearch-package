# A Java package to search for words in ASCII files

The `wordsearch` package implements a system which searches for given words into multiple text files,
and returns all the locations where they were found.

> **Note**  
> As 'word' is defined every sequence of characters separated with one or more spaces or the following punctuation symbols:
> ```
> \t,  \n,  ',',  '.',  '!',  '-',  '(',  ')',  '"',  ''',  ':',  ';',  '?'
> ```

## Table of Contents

- [Description](#description)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#compilation-instructions)
- [Usage](#usage)
- [Status](#status)
- [License](#license)
- [Authors](#authors)

## Description

The system consists of 2 major subsystems:
- A [B-Tree](https://en.wikipedia.org/wiki/B-tree) structure. Every record on the tree consists of a key,
which is the word we search for, as well as of an integer which points to a page of another structure.
That structure contains all the locations where this word can be found in the input text files.
- A paged Index structure. Every page of the Index contains a predetermined number of records. Each record consists
of one String (filename) and one integer (bytes from the file's beginning) and corresponds to a specific point
of the input text files. Consequently, to retrieve the locations where a given word can be found in the input text files,
we have to read all entries from the appropriate page of the index. What is more, every page contains an integer which
may point to another page. With this technique, the construction of "chains" of pages is possible.

After all, when the system is asked to search for a word, it crosses the B-Tree (from root to leaves) and finds (if exists)
the record which contains that word. That record points to a page of the index, and the system reads it and returns all of
its contents (which are the filenames and locations where occurrences are observed in the input ASCII files).
If the occurrences are too much, then a "chain" of index pages has been created as described above,
and the system returns the contents of the whole chain.

> **Warning**  
> Both the B-Tree and the Index structures have to be built before any search operation takes place.
Moreover, they have to be rebuilt every time when the contents of the input ASCII files change.

This Java package implements both the B-Tree and the index in some binary files on the disk.
Every operation on these structures is written immediately on the disk, and every required piece of data
has to be read from the disk again (nothing is stored in memory).
What is more, the data of the binary files are separated into pages of predetermined size, and every read/write
operation takes place on exactly one page. The purpose of this technique is to minimize memory usage as much as
possible, even at the cost of increased disk operations.

Notice that the construction of such structures may take enough time, especially when the input is large, due
to the high number of disk operations. However, once the construction has been completed, the search for a word
is a very quick process, since very few pages have to be read from the B-Tree and the Index structures (compared
to their total sizes).

> **Note**  
> When the input remains unchanged, the B-Tree and the Index do not have to be generated more than once.
The system may be restarted and questions can be given right after its initialization.

## Getting Started

### Prerequisites

The [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/) is required in order to compile and execute the code.

### Compilation instructions

The package can be downloaded (and the current directory be changed to its location) with the following commands:

```bash
git clone https://github.com/giorgapost/wordsearch-package
cd wordsearch-package
```

Then, to compile it and get the `.class` files into a subdirectory named `\classes\`, simply run:
```bash
javac -d classes wordsearch/WordSearch.java
```

Moreover, to compile the file [Examples.java](Examples.java) which contains some examples on how to use the package, run:
```bash
javac -d classes Examples.java
```

Finally, to generate detailed documentation with the [javadoc](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/javadoc.html) tool run:
```
javadoc -d doc wordsearch wordsearch.structures wordsearch.file wordsearch.structures.btree wordsearch.structures.index
```
Then, go to a new subdirectory called `doc/` and open a file named `index.html` with any browser.

## Usage

File [Examples.java](Examples.java) provides examples of 2 alternative ways in which the package may be utilized. 

In both examples the
[buildDataFiles()](https://github.com/giorgapost/wordsearch-package/blob/d6124c653c18e11111da905ff3d5022bbbfe89b0/wordsearch/WordSearch.java#L155)
method constructs the binary files on disk which contain the B-Tree and the Index structures required by the package.
Thus, it has to be called *only* when the input ASCII files change (and not at every execution of the algorithm). 

> **Warning**  
> In order to guarantee valid results, the user has to rebuild the structures after the slightest change of the input ASCII files.

- The first example is the simplest and calls the
[answerQuestions()](https://github.com/giorgapost/wordsearch-package/blob/d6124c653c18e11111da905ff3d5022bbbfe89b0/wordsearch/WordSearch.java#L78)
method, which provides a basic user interface (through console) and allows the user to type the word he needs to search for. 
- The second example passes the words as argument to the overloaded
[answerQuestions()](https://github.com/giorgapost/wordsearch-package/blob/d6124c653c18e11111da905ff3d5022bbbfe89b0/wordsearch/WordSearch.java#L122)
method, and the list with their occurrences is provided as its return value. 

While the first example helps to get acquainted with the software, the latter may be more useful when the provided
package has to be integrated as a part of another application. Finally, do not forget to call 
[terminate()](https://github.com/giorgapost/wordsearch-package/blob/d6124c653c18e11111da905ff3d5022bbbfe89b0/wordsearch/WordSearch.java#L228)
before the execution of the program completes.

## Status

Under maintenance.

## License

Distributed under the GPL-3.0 License. See [`LICENSE`](LICENSE) for more information.

## Authors

[Georgios Apostolakis](https://www.linkedin.com/in/giorgapost)
