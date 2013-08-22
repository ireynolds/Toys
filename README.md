# Toys

These are snippets of code that I've written. Some are complete, but others are modules of a larger project. Most came from school (hence the number of different languages and types of problems). 

Feel free to peruse! Here are some highlights:

* Great **testing** and programming by contract: _Graph.java_.
* Well-optimized, readable code with great **long-form documentation**: _SubsetIter.java_.
* **Interesting, ready-to-run one-offs** with great potential for decomposition and optimization: _Euler Problems_.
* Careful thought and deep understanding of **databases and their implementations**: _LockManager.java_ and _Queries.sql_ (and also _Join.java_ and _LogFile.java_).

You may not get much out of these, but each one taught me a little something (except perhaps BitPuzzles--those are up because of the blood, sweat, and tears I poured into them--and the Euler Problems--those are up because they were fun and interesting!). 

#### Join.java

Implements a inner join operator on a relational DBMS. Defaults to page nested loop join, but uses hash equijoin when possible. Does not consider indices or hash joins on involving tables that do not fit in memory.

#### LockManager.java

A lock manager. Implements efficiently-blocking (using Java Semaphores) calls to gain shared and exclusive locks. Also implements a waits-for dependency graph to efficiently detect and resolve deadlocks. This particular lock manager is used to implement page-level locking on a relational DBMS.

#### LogFile.java

Implements a undo-redo log for a relational DBMS, including logging and recovery algorithms.

#### SentenceBuilder.cc

Analyzes a text and constructs a graph of n-grams and their contexts in the text. Then generates a sentence by following a path through the graph.

#### SubsetIter.java

An efficient way to iterate over a set's subsets of a given size. This toy and the optimizations it uses are described in great detail in the file.

#### BitPuzzles.c

Algorithms on integers in C using a limited set (and limited number) of bitwise operators. Some interesting and subtle algorithms!

#### Graph.java

An implementation of a directed multigraph with data on nodes and edges. Supports all basic collection operations--add, remove, and update both nodes and edges. Also includes comprehensive documentation and unit tests.

#### Queries.sql

Several complex and interesting SQL queries that can be run on an abridged IMDB dataset (including tables for Actors, Movies, Roles/Casts, Directors, and Genres).

#### Euler Problems

Solutions to several problems from [Project Euler](http://projecteuler.net/). Each problem is solved and described in its own file. Several contain multiple implementations and benchmarking code to compare performance quantitatively.
