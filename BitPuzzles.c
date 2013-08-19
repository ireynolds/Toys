
// Implement the following functions. Each function is decorated with the allowed 
// bitwise operators you may use and the total number of operators you may use. 
// You must adhere to these rules.

// Do not use:

//   1. Do, if, for, while, switch, etc.
//   2. Macros.
//   3. Any functions.
//   6. Casting.
//   7. A type other than int. 
//   8. Any integer constant other than 0-255 (0x0 to 0xFF), inclusive.

/* 
 * Compute x & y.
 * 
 *   Operators allowed: | ~
 *   Maximum number of operators allowed: 8
 */
int and(int x, int y) {
  // Use de Morgan's laws
  return ~(~x | ~y);
}

/* 
 * Compute x | y.
 * 
 *   Operators allowed: & ~
 *   Maximum number of operators allowed: 8
 */
int or(int x, int y) {
  // Use de Morgan's laws
  return ~(~x & ~y);
}

/*
 * Return 1 if x is TMax, and 0 otherwise.
 * 
 *   Operators allowed: ~ ! ^ & + | >> <<
 *   Maximum number of operators allowed: 10
 */
int recognizeTmax(int x) {
  // If you have TMax, adding a one at the MSB makes it -1. Then adding 1 makes it
  // zero.
  return !(x + (1 << 31) + 1);
}

/*
 * Return 0 if x != 0, and 1 if x == 0.
 * 
 *   Operators allowed: ~ ! ^ & + | >> <<
 *   Maximum number of operators allowed: 2
 */
int recognizeZero(int x) {
  // By definition of the ! operator.
  return !x;
}

/* 
 * Return 1 if x can be stored as an n-bit number in two's complement 
 * representation. (Assume 32 >= n >= 1.)
 * 
 *   Operators allowed: ~ ! ^ & + | >> <<
 *   Maximum number of operators allowed: 15
 */
int fitsIn(int x, int n) {
  // In order to fit in (n-1) bits, then either every bit to the left of the 
  // (n-1)th bit is a 1 or every bit to the left of the (n-1)th bit is a 0.
  x = x >> (n + (~0x1 + 1));
  return !(x ^ ~0x0) | !(x ^ 0x0);
}

/* 
 * Return 1 if the operation x + y will neither underflow nor underflow, 
 * and 0 otherwise. 
 * 
 *   Operators allowed: ~ ! ^ & + | >> <<
 *   Maximum number of operators allowed: 20
 */
int canAdd(int x, int y) {
  // If the operands have the same sign and the sum has a different sign, then
  // an overflow occurred.
  int mask = 0x1 << 31;
  int xm = x & mask;
  int ym = y & mask;
  int sameSign = !(xm ^ ym) << 31;
  
  int sum = x + y;
  int summ = sum & mask;
  
  return !(sameSign & (xm ^ summ));
}

/* 
 * Return 1 if x > y , and return 0 otherwise. 
 * 
 *   Operators allowed: ~ ! ^ & + | >> <<
 *   Maximum number of operators allowed: 24
 */
int greaterThan(int x, int y) {
  // Check three cases--different sign, same sign, and equal
  int signMask = 0x1 << 31;
   
  // greater1 is true if the two have different signs and x is positive
  int xSign = x & signMask;
  int ySign = y & signMask;
  int diffSign = xSign ^ ySign;
  int greater1 = diffSign & ySign;

  // greater2 is true if the two have the same sign and the difference x - y is positive
  int diff = x + (~y + 1);
  int sign2 = diff & signMask;
  int greater2 = (~diffSign & ~sign2) & signMask;
  
  // notEqual is true if the two are not exactly equal
  int notEqual = !!(x ^ y);
  
  // combines the three cases
  return !!(greater1 | greater2) & notEqual;
}

/* 
 * Replace the index^th byte in x with newByte. The least significant 
 * byte is the 0^th, and the most significant is the 3^rd. Assume that 
 * 3 >= index >= 0 and that 255 >= newByte >= 0.
 * 
 *   Operators allowed: ~ ! ^ & + | >> <<
 *   Maximum number of operators allowed: 10
 */
int writeByte(int x, int index, int newByte) {
  // Zero out the bits of interest
  int mask = 0xff << (index << 3);
  mask = ~mask;
  x = x & mask;

  // Insert the bits to add
  mask = newByte << (index << 3);
  x = x | mask;
  return x;
}

/* 
 * Rotate x n bits to the left. (Assume that 31 >= n >= 0.)
 * 
 *   Operators allowed: ~ ! ^ & + | >> <<
 *   Maximum number of operators allowed: 25
 */
int rotateToTheLeft(int x, int n) {
  // Extract the most significant n bits, shift them right.
  // Then shift x left n bits and insert the most significant bits.
  int msbMask = ~(0x8 << 28);
  int xLeft = (x >> 1) & msbMask;
  xLeft = xLeft >> (31 + (~n + 1));

  return ((x << n) | xLeft);
}

/*
 * Return the number of bits in the given integer equal to 1.
 * 
 *   Operators allowed: ~ ! ^ & + | >> <<
 *   Maximum number of operators allowed: 40
 */
int weight(int x) {
  // Hamming weight. Essentially, break the 32-bit integer into 
  // 16 2-byte buckets, each of which stores the number of ones
  // in that bucket. Accomplish this by masking against 010101...
  // Then stick it into 8 4-bite buckets, 4 8-byte buckets, 2 16-byte
  // buckets, and finally 1 32-byte bucket (that's what you return).
  int mask = 0x55;
  mask = (mask << 8) | mask;
  mask = (mask << 16) | mask;
  x = (x & mask) + ((x >> 1) & mask);
  
  mask = 0x33;
  mask = (mask << 8) | mask;
  mask = (mask << 16) | mask;
  x = (x & mask) + ((x >> 2) & mask);
  
  mask = 0x0f;
  mask = (mask << 8) | mask;
  mask = (mask << 16) | mask;
  x = (x & mask) + ((x >> 4) & mask);
  
  mask = 0xff;
  mask = (mask << 16) | mask;
  x = (x & mask) + ((x >> 8) & mask);

  mask = 0xff;
  mask = (mask << 8) | mask;
  x = (x & mask) + ((x >> 16) & mask);
  
  return x;
}
