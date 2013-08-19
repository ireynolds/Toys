# The prime factors of 13195 are 5, 7, 13 and 29.
# What is the largest prime factor of the number 600851475143?

require 'prime'
require 'benchmark'

# Monkey-patch the Integer class to add (not override) 
# a method
class Integer
  def divides?(n)
    n % self == 0
  end
end

num = 600_851_475_143

#=======================================

# Starting low, count up by ones until you find the first 
# number that is a factor and whose 'opposite factor' is prime.

def alg_one(num)
  (1).step(num / 2) do |div|
    if div.divides? num && Prime.prime? num / div
      return num / div
    end
  end
end

puts Benchmark.measure { alg_one num }

#=======================================

# Starting low, count up by ones and fill an Array with
# all of the factors. Then start at the end and look
# for the first prime factor.

def alg_two(num)
  factors = []
  (1).step(num / 2) do |div|
    factors << div if div.divides? num
  end
  factors << num

  factors.reverse_each do |factor|
    if Prime.prime? factor
      return factor
    end
  end
end

puts Benchmark.measure { alg_two num }

#=======================================

# Starting high, count down by ones until you find the
# first factor that is prime.

def alg_three(num)
  (num / 2).step(1, -1) do |divisor|
    if divisor.divides? num && Prime.prime? divisor
      return divisor
    end
  end
end

puts Benchmark.measure { alg_three num }