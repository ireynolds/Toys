# A palindromic number reads the same both ways. The largest 
# palindrome made from the product of two 2-digit numbers is 
# 9009 = 91 99.

# Find the largest palindrome made from the product of two 
# 3-digit numbers.

require 'benchmark'

def palindrome?(n)
  return true if n == nil || n.length < 2 
  return false if n[0] != n[-1]
  return palindrome? n[1...-1]
end

#=======================================

# Multiply every distinct ordered pair, and keep track of the max.

def alg_one
  cap = 1000
  max = 0

  cap.times do |i|
    cap.times do |j|
      prod = i * j
      max = prod if prod > max && palindrome? prod.to_s
    end
  end
  
  return max
end

puts Benchmark.measure { alg_one }

#=======================================

# Start multiplying distinct unordered pairs, starting with the one 
# with the largest product and working back. Keep track of the max
# palindromic product, and short-circuit the inner loop if you find
# any product that is less than the current max because prod is
# monotonically decreasing for each iteration of the outer loop.

def alg_two
  cap = 1000
  max = 0
  
  cap.step(1, -1) do |i|
    cap.step(i, -1) do |j|
      prod = i * j
      break if prod < max
      max = prod if palindrome? prod.to_s
    end
  end
  
  return max
end

puts Benchmark.measure { alg_two }