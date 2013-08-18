// An efficient way to iterate over a set's subsets of a given size.

// An Optimization of EnumerateSubsets

// The goal of this extension is to improve the efficiency of enumerating subsets, which will allow 
// the optimizer to quickly compute optimized physical plans for queries with several joins. There 
// are 2^N subsets of a set of size N, so enumerating them will always have exponential running time. 
// This extension does not improve the 2^N running time, but it does improve the coefficient by which
// the 2^N is multiplied.

// The first optimization is to stream the subsets rather than instantiating a set of all subsets. 
// This means that enumerateSubsets returns an Iterable<Set> rather than a Set<Set>. This new 
// implementation is more space-efficient than the existing implementation, which does not stream 
// subsets.

// The second optimization is to instantiate only the subsets that will be returned. This negates 
// the cost of instantiating sets that will only be garbage collected later. This requires creating 
// a more efficient representation of a subset. 

// The more efficient representation of a set is in the SubsetIter class. This arranges the elements 
// of the source set in sequence and creates a bit string that contains one bit for each element of
// the source. The bit at index i in the string is 1 if and only if the element at index i in the 
// source is included in the subset, and 0 otherwise. 

// The bit string is the 32-bit int primitive type. The primary limitation of using the primitive 
// type is that it prevents the optimizer from considering more than 32 joins. However, not only is
// a query with 32 joins unlikely, but to compute an optimized plan, even with the optimized design,
// would be computationally infeasible in user time. Additionally, using the primitive has the 
// following advantages over using, for example, a BitSet:
  
//   1. Bitwise operations on primitives are simple instructions implemented directly in hardware 
//      and machine code. This will be much more efficient than making calls to virtual functions in 
//      a reference type such as BitSet.
//   2. A BitSet is a reference type, which means that every access to a BitSet requires a memory 
//      access. In a 2N algorithm, there are likely to be many accesses to BitSets.
//   3. A BitSet object is much more expensive to instantiate than a primitive. This is both because 
//      instantiating a primitive does not require a call to a constructor, and also because a primitive
//      is stack-allocated, so it does not require running a dynamic memory-allocation call (a BitSet is 
//      heap-allocated, so it will require an expensive memory-allocation call).
//   4. Primitives are all stack-allocated, not heap-allocated, so they do not need to be garbage 
//      collected (and, because the new design is streamed, there is no worry of running out of stack 
//      space).

// The naïve way to generate all the bit sequences of the correct size is to iterate over all the 
// bit sequences (by incrementing the integer) and return only the ones with the correct cardinality. 
// This, however, requires iterating over many integers that do not have the correct cardinality. 
// There are several main optimizations to this algorithm.

// First, for cardinality n, begin iterating from 2**n-1. This is the first integer with a cardinality
// of n. This eliminates 2**n-1 iterations that are guaranteed to not produce an integer with the 
// correct cardinality.

// Second, calculate the total number of subsets of size n using the binomial combinations formula,
// which is a cheap computation. This optimization allows the algorithm to report that there are
// no more subsets as soon as possible.

// Third, use a more complex iteration algorithm than incrementing to go straight from one integer
// with the proper cardinality to the next one. This algorithm computes no unnecessary integers. 
// This algorithm is described in detail in the IntIter class. Below is an example. Given a source
// of size four, computing subsets of size two, the IntIter class will produce the following 
// integers in the following order:

//   0011, 0101, 1001, 0110, 1010, 1100

// The performance tests in the next section confirm that this implementation is several orders of
// magnitude more efficient than the old implementation.

// (Please contact me to see the results of the performance tests.)
 
 
import java.util.NoSuchElementException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
 
public class SubsetIter<T> implements Iterator<Set<T>> {
  Object[] sourceEls;
  int subsetSize;
  IntIter iter;
  
  public SubsetIter(Collection<T> source, int subsetSize) {
    sourceEls = new Object[source.size()];
    int i = 0;
    for (Object el : source) {
      sourceEls[i] = el;
      i += 1;
    }
    
    this.subsetSize = subsetSize;
    iter = new IntIter(source.size(), subsetSize);
  }
  
  public boolean hasNext() {
    return iter.hasNext();
  }
  
  @SuppressWarnings("unchecked")
  public Set<T> next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    
    int members = iter.next();
    Set<T> subset = new HashSet<T>();
    
    for (int i = 0; i < sourceEls.length; i++) {
      boolean contains = ((members & 0x1) == 1) ? true : false;
      if (contains) {
        subset.add((T)sourceEls[i]);
      }
      members = members >>> 1;
    }
    
    return subset;
  }
 
  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
 
public class IntIter {
  private final int length;
  private final int ones;
  private final long numTotal;
  private int numReturned;
  private int lastReturned;
  
  // For length = 6, ones = 3, outputs the following integers in the 
  // following order (integers shown in binary representation):
  //
  // 000111
  // 001011
  // 010011
  // 100011
  // 
  // 001101
  // 010101
  // 100101
  // 
  // 011001
  // 101001
  // 
  // 110001
  //
  // 001110
  // 010110
  // 100110
  // ...
  // 
  // ...
  // 111000
  //
  
  public IntIter(int length, int ones) {
    if (length > 32) {
      throw new IllegalArgumentException("Desired length exceeded 32.");
    }
    
    this.lastReturned = (int)Math.round(Math.pow(2, ones)) - 1;
    this.ones = ones;
    this.length = length;
    this.numTotal = IntUtils.choose(length, ones);
    this.numReturned = 0;
  }
  
  public boolean hasNext() {
    return numReturned < numTotal;
  }
  
  public int next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    
    // Short circuit in this case (because lastReturned is not 
    // necessarily a valid bit sequence right now).
    if (numReturned == 0) {
      numReturned += 1;
      return lastReturned;
    }
    
    // Will become the next bit sequence with this.ones ones.
    int bitSeq = lastReturned;
    
    int bitNum = 0;
    int prevLeftBound = length;
    for (; bitNum < ones; bitNum++) {
      // Get the index of the ith one from the left.
      int leftBound = IntUtils.leftmostBit(bitSeq, prevLeftBound);
      
      // If you can move this bit to the left, do so.
      if (leftBound < prevLeftBound - 1) {
        bitSeq = shiftOneLeft(bitSeq, leftBound);
        prevLeftBound = leftBound;
        break;
      } 
      
      // Set this bit to zero.
      bitSeq = IntUtils.setBitAt(bitSeq, leftBound, 0);
      prevLeftBound = leftBound;
    }
    
    // Set this to the index of current leftmost one.
    prevLeftBound += 1; 
    
    // For each one set to zero in the loop above, insert a new one.
    // The new ones will sit immediately on the left of the "current
    // leftmost one", which is the bit that was shifted one to the left
    // in the loop above.
    for (int i = 0; i < bitNum; i++) {
      bitSeq = IntUtils.setBitAt(bitSeq, prevLeftBound + i + 1, 1);
    }
    
    // Update internal structures.
    lastReturned = bitSeq;
    numReturned += 1;
    return bitSeq;
    
  }
  
  private static int shiftOneLeft(int val, int index) {
    val = IntUtils.setBitAt(val, index, 0);
    val = IntUtils.setBitAt(val, index + 1, 1);
    return val;
  }
}
 
 
public class IntUtils {
 
  public static int getBitAt(int val, int index) {
    return (val >> index) & 0x1;
  }
  
  public static int setBitAt(int val, int index, int bit) {
    int mask = 0x1 << index;
    if (bit == 0) {
      return val & ~mask;
    } else {
      return val | mask;
    } 
  }
  
  public static int cardinality(int x) {
    // Hamming weight. Essentially, break the 32-bit integer into 
    // 16 2-byte buckets, each of which stores the number of ones
    // in that bucket. Accomplish this by masking against 010101...
    // Then stick it into 8 4-byte buckets, 4 8-byte buckets, 2 16-byte
    // buckets, and finally 1 32-byte bucket (that's what you return).
    int mask = 0x55555555;
    x = (x & mask) + ((x >> 1) & mask);
      
    mask = 0x33333333;
    x = (x & mask) + ((x >> 2) & mask);
      
    mask = 0x0f0f0f0f;
    x = (x & mask) + ((x >> 4) & mask);
      
    mask = 0x00ff00ff;
    x = (x & mask) + ((x >> 8) & mask);
    
    mask = 0x0000ffff;
    x = (x & mask) + ((x >> 16) & mask);
      
    return x;
  }
  
  public static int leftmostBit(int val, int leftExtent) {
    for (int i = leftExtent - 1; i >= 0; i--) {
      if (IntUtils.getBitAt(val, i) == 1) {
        return i;
      }
    }
    return -1;
  }
 
  public static long choose(int n, int k) {
    // Optimization
      if (k > (n - k)) {
        k = n - k;
      }
      
      // Short-circuit here
      if (n == 0) {
        return (k == 0) ? 1 : 0;
      } else if (k == 1) {
        return n;
      } else if (k == 0) {
        return 1;
      }
 
      // Calculate it
      long res = 1;
      for (int i = 1; i <= k; i++) {
        res *= (n - (k - i));
        res /= i;
          
          // Check for overflow
          if (res < 0) {
            throw new RuntimeException("Overflow in choose(int, int)");
          }
      }
      
      return res;
  }
}