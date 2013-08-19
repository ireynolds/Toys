# Starting in the top left corner of a 2x2 grid, and only being able 
# to move to the right and down, there are exactly 6 routes to 
# the bottom right corner.

# How many such routes are there through a 20x20 grid?

# Well ... 20 choose 10, but I think you're looking for a more 
# ... recursive solution

$len = 11
$wid = 11

def paths_to_corner
  paths = []
  paths_from_rec(0, 0, paths, [])
  return paths
end

def paths_from_rec(i, j, paths, path)
  # Have reached the end -- now return and explore a new path
  if i == $len and j == $wid
    paths << Array.new(path)
    return
  end
  
  # Have not reach the end -- keep exploring
  if i < $len
    paths_from_rec(i + 1, j, paths, path << :down)
    path.pop
  end
  
  if j < $wid
    paths_from_rec(i, j + 1, paths, path << :right)
    path.pop
  end
end

puts (paths_to_corner).length