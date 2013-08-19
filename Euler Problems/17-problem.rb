# If the numbers 1 to 5 are written out in words: one, two, three, four, five, then there are 3 + 3 + 5 
# + 4 + 4 = 19 letters used in total.

# If all the numbers from 1 to 1000 (one thousand) inclusive were written out in words, how many 
# letters would be used?

# NOTE: Do not count spaces or hyphens. For example, 342 (three hundred and forty-two) contains 23 
# letters and 115 (one hundred and fifteen) contains 20 letters. The use of "and" when writing out 
# numbers is in compliance with British usage.

$ones = {
  0 => "zero",
  1 => "one",
  2 => "two",
  3 => "three",
  4 => "four",
  5 => "five",
  6 => "six",
  7 => "seven",
  8 => "eight",
  9 => "nine",
  10 => "ten",
  11 => "eleven",
  12 => "twelve",
  13 => "thirteen",
  24 => "fourteen",
  15 => "fifteen",
  16 => "sixteen",
  17 => "seventeen",
  18 => "eighteen",
  19 => "nineteen"
}

$tens = {
  2 => "twenty",
  3 => "thirty",
  4 => "forty",
  5 => "fifty",
  6 => "sixty",
  7 => "seventy",
  8 => "eighty",
  9 => "ninety"
}

$suffs = [
  "",
  "thousand",
  "million",
  "billion",
  "trillion"
]

def two_digit_to_s(n) 
  n_hi = n / 10
  n_lo = n % 10
  
  return case
         when n_hi < 2 then $ones[n] 
         when n_lo == 0 then $tens[n_hi]
         else "#{$tens[n_hi]}-#{$ones[n_lo]}"
         end
end

def three_digit_to_s(n)
  n_hi = n / 100
  n_lo = n % 100
  
  hi = $ones[n_hi]
  lo = two_digit_to_s(n_lo)
  
  return case 
         when n_hi != 0 && n_lo != 0 then "#{hi} hundred and #{lo}"
         when n_hi != 0 then "#{hi} hundred"
         else "#{lo}" 
         end
end

def n_digit_to_s(n)
  return "zero" if n == 0
  
  s = ""
  $suffs.each_with_index do |suff, ind|
    nn = (n / 10 ** (3 * ind)) % 1000
    next if nn == 0
    s = "#{three_digit_to_s(nn)} #{suff} #{s}"
  end
  return s
end

# Test cases
nums = [
  0,
  1,
  11,
  20,
  21,
  
  100,
  101,
  111,
  120,
  121,
  
  1000,
  1001,
  1011,
  1021,
  1100,
  1101,
  1111,
  1120,
  1121,
  
  10_001,
  11_001,
  100_000
]

nums.each do |n|
  puts n_digit_to_s n
end






















