CREATE TABLE IF NOT EXISTS "comment" (id uuid PRIMARY KEY, parent_id uuid NULL, star_id uuid NOT NULL, identifier TEXT NOT NULL, author TEXT NOT NULL, "text" TEXT NOT NULL, "depth" INT NULL, visibility REAL NOT NULL, visibility_ratio REAL NOT NULL, vote_count INT NOT NULL, reply_count INT NOT NULL, permalink TEXT NOT NULL, created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP NOT NULL, accessed_at TIMESTAMP NOT NULL);
ALTER TABLE "comment" ADD CONSTRAINT fk_comment_parent_id__id FOREIGN KEY (parent_id) REFERENCES "comment"(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE "comment" ADD CONSTRAINT fk_comment_star_id__id FOREIGN KEY (star_id) REFERENCES star(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE star ADD accessed_at TIMESTAMP NOT NULL;
ALTER TABLE star DROP COLUMN discovered_at;
