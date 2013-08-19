# A Pythagorean triplet is a set of three natural numbers {a, b, c} 
# for which a**2 + b**2 = c**2

# For example, 3**2 + 4**2 = 9 + 16 = 25 = 5**2.

# There exists exactly one Pythagorean triplet for which 
# a + b + c = 1000. Find the product abc.

require 'benchmark'

def print_bench
  a = b = c = nil
  puts Benchmark.measure { a, b, c = yield }
  puts "a: #{a}, b: #{b}, c: #{c}"
end

#=======================================

# Try every unordered triple. Dear god, this is slow.

def alg_one
  (1000).times do |a|
    (1000).times do |b|
      (1000).times do |c|
        return a, b, c if a + b + c == 1000 && 
                          a**2 + b**2 == c**2 &&
                          a != b && b != c && a != c
      end
    end
  end
end

# Don't run this -- way too slow
# print_bench { alg_one } 

#=======================================

# Try every ordered pair, and use the pair and the sum
# to determine the third.

def alg_two
  (1000).times do |a|
    (1).upto(1000 - a) do |b|
      c = 1000 - (a + b)
      return a, b, c if a + b + c == 1000 && 
                     a**2 + b**2 == c**2 &&
                     a != b && b != c && a != c
    end
  end
end

print_bench { alg_two }

#=======================================

# Try every ordered pair, and use the pair and the sum
# to determine the third. Try a different arrangement of
# branching conditions to optimize performance.

def alg_three
  (1000).times do |a|
    (1).upto(1000 - a) do |b|
      c = 1000 - (a + b)
      next if a == b || b == c || a == c
      return a, b, c if a**2 + b**2 == c**2
    end
  end
end

print_bench { alg_three } 