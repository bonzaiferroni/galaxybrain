ALTER TABLE star_log ADD star_id uuid NOT NULL;
ALTER TABLE star_log ADD CONSTRAINT fk_star_log_star_id__id FOREIGN KEY (star_id) REFERENCES star(id) ON DELETE CASCADE ON UPDATE RESTRICT;
