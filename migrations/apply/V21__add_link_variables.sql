ALTER TABLE star_link ADD "text" TEXT NULL;
ALTER TABLE star_link ADD start_index INT NULL;
ALTER TABLE star_snippet ADD snippet_id uuid NOT NULL;
ALTER TABLE star_snippet ADD CONSTRAINT fk_star_snippet_snippet_id__id FOREIGN KEY (snippet_id) REFERENCES snippet(id) ON DELETE CASCADE ON UPDATE RESTRICT;
