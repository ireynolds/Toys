-- These queries are run on an abridged IMDB dataset.
--
-- Actor(id, fname, lname, gender)
-- Movie(id, name, year)
-- Director(id, fname, lname)
-- Cast(pid, mid, role)
-- Movie_director(did, mid)
-- Genre(mid, genre)





-- Selects all the movies with no male actors.
WITH MoviesWithMen (mid) AS
(
  SELECT DISTINCT m.id
  FROM Movie m
  JOIN Casts c on m.id = c.mid
  JOIN Actor a on a.id = c.pid
  WHERE a.gender = 'M'
)

SELECT m.year, COUNT(m.id)
FROM Movie m
WHERE m.id NOT IN (SELECT mid 
                   FROM MoviesWithMen)
GROUP BY m.year;





-- Selects, for each year, the total number of movies 
-- made that year and the percentage of movies from 
-- that year with no male actors. 
WITH MoviesWithMen (mid) AS
(
  SELECT DISTINCT m.id
  FROM Movie m
  JOIN Casts c on m.id = c.mid
  JOIN Actor a on a.id = c.pid
  WHERE a.gender = 'M'
),

MoviesPerYear (year, count) AS
(
  SELECT m.year, COUNT(*)
  FROM Movie m
  GROUP BY m.year
)

SELECT m.year, 
       COUNT(*) * 100.0 / py.count,
       py.count
FROM Movie m
JOIN MoviesPerYear py ON py.year = m.year
WHERE m.id NOT IN (SELECT mid 
                   FROM MoviesWithMen)
GROUP BY m.year, py.count;





-- Selects the film(s) with the largest cast.
WITH CastSizes (mid, mname, count) AS
(
  SELECT m.id, m.name, COUNT(DISTINCT c.pid)
  FROM Movie m
  JOIN Casts c ON m.id = c.mid
  GROUP BY m.id, m.name
)

SELECT cs.mname, cs.count
FROM CastSizes cs
WHERE cs.count = (SELECT MAX(count)
                  FROM CastSizes);

                        
                        
           
-- Selects the decade during which the most films were made.
WITH DistinctYears (year) AS
(
  SELECT DISTINCT m.year
  FROM Movie m
),

MoviesPerDecade (year, count)  AS
(
  SELECT y.year, COUNT(DISTINCT m.id)
  FROM DistinctYears y 
  JOIN Movie m ON y.year <= m.year AND m.year < y.year + 10
  GROUP BY y.year
)
                 
SELECT *
FROM MoviesPerDecade mpd
WHERE mpd.count = (SELECT MAX(count)
                   FROM MoviesPerDecade);





-- Selects the actors with a 'Bacon Number' of 2.
WITH BaconOneActors (pid) AS
(
  SELECT DISTINCT c2.pid
  FROM Actor a
  JOIN Casts c ON c.pid = a.id
  JOIN Casts c2 ON c2.mid = c.mid
  WHERE a.fname = 'Kevin' AND a.lname = 'Bacon'
)

SELECT COUNT(DISTINCT c2.pid)
FROM BaconOneActors b1a
JOIN Casts c ON c.pid = b1a.pid
JOIN Casts c2 ON c2.mid = c.mid
AND c2.pid NOT IN (SELECT pid 
                   FROM BaconOneActors);