ALTER TABLE star ADD comment_count INT NOT NULL;
ALTER TABLE star ADD vote_count INT NOT NULL;
ALTER TABLE star_log ADD rise REAL NOT NULL;
ALTER TABLE star_log ADD comment_count INT NOT NULL;
ALTER TABLE star_log ADD vote_count INT NOT NULL;
ALTER TABLE galaxy ADD visibility REAL NOT NULL;
