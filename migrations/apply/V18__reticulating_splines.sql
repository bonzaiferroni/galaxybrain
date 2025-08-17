CREATE TABLE IF NOT EXISTS star_snippet (id uuid PRIMARY KEY, star_id uuid NOT NULL, comment_id uuid NULL, text_index INT NOT NULL, created_at TIMESTAMP NOT NULL);
ALTER TABLE star_snippet ADD CONSTRAINT fk_star_snippet_star_id__id FOREIGN KEY (star_id) REFERENCES star(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE star_snippet ADD CONSTRAINT fk_star_snippet_comment_id__id FOREIGN KEY (comment_id) REFERENCES "comment"(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE host ADD core TEXT NOT NULL;
ALTER TABLE star DROP COLUMN text_content;
ALTER TABLE host DROP COLUMN address;
