ALTER TABLE idea ADD comment_id uuid NULL;
ALTER TABLE idea ADD CONSTRAINT fk_idea_comment_id__id FOREIGN KEY (comment_id) REFERENCES "comment"(id) ON DELETE CASCADE ON UPDATE RESTRICT;
