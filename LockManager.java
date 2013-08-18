// A lock manager. Implements efficiently-blocking (using Java Semaphores) calls to 
// gain shared and exclusive locks. Also implements a waits-for dependency graph to 
// efficiently detect and resolve deadlocks. This particular lock manager is used to 
// implement page-level locking on a relational DBMS.

private class LockManager {
      
    /*
     * This class grants and releases locks on pages on behalf of 
     * transactions. The acquireLock method blocks efficiently until the 
     * lock is available. 
     * 
     * If one transaction is executed by multiple threads simultaneously,
     * then there can be race conditions that result in one transaction 
     * acquiring the same lock twice and then being unable to release its 
     * lock. A one-to-one relationship between a thread and a transaction
     * prevents these problems.
     */
    
    private static final int READ_TOKENS = 1;
    private static final int WRITE_TOKENS = Integer.MAX_VALUE;
    private static final int TOTAL_TOKENS = WRITE_TOKENS;
    private static final boolean FAIR = true;
    
    /*
     * Maintains a distinct semaphore for each PageId pid.
     */
    private final Map<PageId, Semaphore> semaphores;
    
    /*
     * If tid has a READ_ONLY lock on pid, then pid is in readLocks.get(tid).
     * For all such pid, tid has acquired exactly READ_TOKENS tokens from
     * semaphores.get(pid).
     */
    private final Map<TransactionId, Set<PageId>> readLocks;
    
    /*
     * If tid has a READ_WRITE lock on pid, then pid is in writeLocks.get(tid).
     * For all such pid, tid has acquired exactly WRITE_TOKENS tokens from
     * semaphores.get(pid).
     */
    private final Map<TransactionId, Set<PageId>> writeLocks;
    
    private final WaitsForGraph waitsForGraph;
    
    public LockManager() {
        semaphores = new ConcurrentHashMap<PageId, Semaphore>();
        readLocks = new ConcurrentHashMap<TransactionId, Set<PageId>>();
        writeLocks = new ConcurrentHashMap<TransactionId, Set<PageId>>();
        waitsForGraph = new WaitsForGraph();
    }
    
  ////////////////////////////////////////////////////////////////////////
  ////////// Methods that help maintain internal state
  ////////////////////////////////////////////////////////////////////////
    
    /**
     * Returns the semaphore associated with the given page.
     */
    private synchronized Semaphore getSemaphore(PageId pid) {
        Semaphore semaphore = semaphores.get(pid);
        if (semaphore == null) {
            semaphore = new Semaphore(TOTAL_TOKENS, FAIR);
            semaphores.put(pid, semaphore);
        }
        return semaphore;
    }
    
    /**
     * Returns a set containing each page on which tid has a read lock.
     */
    public Set<PageId> getReadLocked(TransactionId tid) {
       return getLockedPagesFromMap(tid, readLocks);
    }
    
    /**
     * Returns a set containing each page on which tid has a write lock.
     */
    public Set<PageId> getWriteLocked(TransactionId tid) {
       return getLockedPagesFromMap(tid, writeLocks);
    }
    
    /**
     * Helper -- do not use.
     */
    private synchronized Set<PageId> getLockedPagesFromMap(TransactionId tid, Map<TransactionId, Set<PageId>> locks) {
        Set<PageId> pages = locks.get(tid);
        if (pages == null) {
            pages = Collections.newSetFromMap(new ConcurrentHashMap<PageId, Boolean>());
            locks.put(tid, pages);
        }
        return pages;
    }
    
  ////////////////////////////////////////////////////////////////////////
  ////////// Acquiring locks
  ////////////////////////////////////////////////////////////////////////
    
    /**
     * Grants tid a lock on pid with the given permissions. Blocks until the
     * given permissions become available.
     */
    public void acquireLock(PageId pid, TransactionId tid, Permissions permissions) throws TransactionAbortedException, DbException {
        if (tid == null) {
            return;
        }
        
        if (permissions == Permissions.READ_ONLY) {
            acquireReadLock(pid, tid);
        } else if (permissions == Permissions.READ_WRITE) {
            acquireWriteLock(pid, tid);
        }
    }
    
    /**
     * Guarantees that tid has acquired exactly (no more than) READ_TOKEN 
     * tokens from pid's semaphore.
     */
    private void acquireReadLock(PageId pid, TransactionId tid) throws TransactionAbortedException, DbException {
        if (hasReadLock(pid, tid)) {
            return;
        }
        
        printAcquiringLock(pid, tid, "read");
      
        Semaphore sem = getSemaphore(pid);
        if (sem.availablePermits() < READ_TOKENS) {
            waitsForGraph.request(tid, pid);
        }
        sem.acquireUninterruptibly(READ_TOKENS);
        waitsForGraph.grant(tid, pid);

        printAcquiredLock(pid, tid, "read");

        // Update internal state
        synchronized (tid) {
            getReadLocked(tid).add(pid);
        }
    }

    /**
     * Guarantees that tid has acquired exactly (no more than) WRITE_TOKEN
     * tokens from pid's semaphore.
     */
    private void acquireWriteLock(PageId pid, TransactionId tid) throws TransactionAbortedException, DbException {
        if (hasWriteLock(pid, tid)) {
            return;
        }
        
        printAcquiringLock(pid, tid, "write");
      
        // Ensure that this transaction has exactly READ_TOKEN tokens,
        // the acquire the rest of them.
        Semaphore sem = getSemaphore(pid);
        acquireReadLock(pid, tid);
        if (sem.availablePermits() != WRITE_TOKENS - READ_TOKENS) {
            waitsForGraph.request(tid, pid);
        }
        sem.acquireUninterruptibly(WRITE_TOKENS - READ_TOKENS);
        waitsForGraph.grant(tid, pid);
          
        printAcquiredLock(pid, tid, "write");
        
        // Update internal state
        synchronized (tid) {
            getReadLocked(tid).remove(pid);
            getWriteLocked(tid).add(pid);
        }
    }
    
  ////////////////////////////////////////////////////////////////////////
  ////////// Determining if a transaction has a lock
  ////////////////////////////////////////////////////////////////////////
    
    /**
     * Returns true iff tid has some lock (read or write) on pid.
     */
    public boolean hasLock(PageId pid, TransactionId tid) {
        return hasReadLock(pid, tid) || hasWriteLock(pid, tid);
    }
    
    /**
     * Returns true iff tid has a read lock on pid.
     */
    public boolean hasReadLock(PageId pid, TransactionId tid) {
        return hasLockIn(tid, pid, readLocks) || hasWriteLock(pid, tid);
    }
    
    /**
     * Returns true iff tid has a write lock on pid.
     */
    public boolean hasWriteLock(PageId pid, TransactionId tid) {
        return hasLockIn(tid, pid, writeLocks);
    }
    
    /**
     * Helper -- do not use.
     */
    private boolean hasLockIn(TransactionId tid, PageId pid, Map<TransactionId, Set<PageId>> locks) {
        synchronized (tid) {
            Set<PageId> pages = locks.get(tid);
            return pages != null && pages.contains(pid);
        }
    }
    
  ////////////////////////////////////////////////////////////////////////
  ////////// Releasing locks
  ////////////////////////////////////////////////////////////////////////
    
    /**
     * Releases all the locks associated with tid.
     */
    public void releaseLocks(TransactionId tid) {
        synchronized (tid) {
            releaseLockedIn(getReadLocked(tid), tid, READ_TOKENS, "read");
            releaseLockedIn(getWriteLocked(tid), tid, WRITE_TOKENS, "write");
        }
    }
    
    /**
     * Helper -- do not use.
     */
    private void releaseLockedIn(Set<PageId> locked, TransactionId tid, int numTokens, String type) {
        if (locked != null) {
            for (PageId pid : locked ) {
                getSemaphore(pid).release(numTokens);
                waitsForGraph.release(tid, pid);
                
                printReleasedLock(pid, tid, type);
            }
            locked .clear();
        }
    }
    
    /**
     * Releases the lock that tid has on pid.
     */
    public void releaseLock(PageId pid, TransactionId tid) {
        synchronized (tid) {
            Semaphore sem = getSemaphore(pid);
            if (hasWriteLock(pid, tid)) {
                sem.release(WRITE_TOKENS);
                waitsForGraph.release(tid, pid);
                getWriteLocked(tid).remove(pid);
                
                printReleasedLock(pid, tid, "write");
            } else if (hasReadLock(pid, tid)) {
                sem.release(READ_TOKENS);
                waitsForGraph.release(tid, pid);
                getReadLocked(tid).remove(pid);
                
                printReleasedLock(pid, tid, "read");
            } 
        }
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////////// Print methods for debugging
    ////////////////////////////////////////////////////////////////////////
    
    private static final boolean DEBUG_ON = false;
    
    private void printAcquiringLock(PageId pid, TransactionId tid, String type) {
        if (!DEBUG_ON) return;
        System.out.println("Transaction " + tid.getId() + " is acquiring a " + type + " lock on page " + pid);
    }
    
    private void printAcquiredLock(PageId pid, TransactionId tid, String type) {
        if (!DEBUG_ON) return;
        System.out.println("Transaction " + tid.getId() + " acquired a " + type + " lock on page " + pid);
    }
    
    private void printReleasedLock(PageId pid, TransactionId tid, String type) {
        if (!DEBUG_ON) return;
        System.out.println("Transaction " + tid.getId() + " released a " + type + " lock on page " + pid);
    }
    
  ////////////////////////////////////////////////////////////////////////
  ////////// The waits-for graph
  ////////////////////////////////////////////////////////////////////////
    
    private class WaitsForGraph {
      
        private final Map<PageId, Set<TransactionId>> waitingFor;
        private final Map<PageId, Set<TransactionId>> has;
        
        public WaitsForGraph() {
            waitingFor = new HashMap<PageId, Set<TransactionId>>();
            has = new HashMap<PageId, Set<TransactionId>>();
        }
        
        /** It possible that a request might both not block and produce a 
         * cycle. Only call this method if the request is going to block.
         */
        public synchronized void request(TransactionId tid, PageId pid) throws TransactionAbortedException {
            getWaiting(pid).add(tid);
            
            if (createsDeadlock(tid)) {
                throw new TransactionAbortedException();
            }
        }
        
        public synchronized void grant(TransactionId tid, PageId pid) {
            getWaiting(pid).remove(tid);
            getHas(pid).add(tid);
        }
        
        public synchronized void release(TransactionId tid, PageId pid) {
            getHas(pid).remove(tid);
        }
        
        private Set<TransactionId> getWaiting(PageId pid) {
            return getFrom(pid, waitingFor);
        }
        
        private Set<TransactionId> getHas(PageId pid) {
            return getFrom(pid, has);
        }
        
        private Set<TransactionId> getFrom(PageId pid, Map<PageId, Set<TransactionId>> map) {
            Set<TransactionId> txns = map.get(pid);
            if (txns == null) {
                txns = new HashSet<TransactionId>();
                map.put(pid, txns);
            }
            return txns;
        }
        
        private boolean createsDeadlock(TransactionId start) {
            Map<TransactionId, Set<TransactionId>> graph = build();
            
            Stack<TransactionId> path = new Stack<TransactionId>();
            Set<TransactionId> visited = new HashSet<TransactionId>();
            
            path.add(start);
            visited.add(start);
            while (!path.isEmpty()) {
                TransactionId curr = path.pop();
                visited.add(curr);
                
                if (graph.get(curr) == null) {
                    continue;
                }
                
                for (TransactionId child : graph.get(curr)) {
                  if (visited.contains(child)) {
                      return true;
                  }
                  path.push(child);
                }
            }
            
            return false;
        }
        
        private Map<TransactionId, Set<TransactionId>> build() {
            Map<TransactionId, Set<TransactionId>> graph = new HashMap<TransactionId, Set<TransactionId>>();
            
            Set<PageId> pages = new HashSet<PageId>();
            pages.addAll(has.keySet());
            pages.addAll(waitingFor.keySet());
            
            for (PageId pid : pages) {
                for (TransactionId has : getHas(pid)) {
                    for (TransactionId waiting : getWaiting(pid)) {
                        if (!waiting.equals(has)) {
                            if (!graph.containsKey(waiting)) {
                                graph.put(waiting, new HashSet<TransactionId>());
                            }
                            graph.get(waiting).add(has);
                        }
                    }
                }
            }
            
            return graph;
        }
    }
}

 
