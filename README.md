# Toys

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
