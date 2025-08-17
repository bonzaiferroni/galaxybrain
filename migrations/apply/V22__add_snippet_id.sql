ALTER TABLE star_link ADD snippet_id uuid NULL;
ALTER TABLE star_link ADD CONSTRAINT fk_star_link_snippet_id__id FOREIGN KEY (snippet_id) REFERENCES snippet(id) ON DELETE CASCADE ON UPDATE RESTRICT;
