ALTER TABLE star_link ADD comment_id uuid NULL;
ALTER TABLE star_link ADD CONSTRAINT fk_star_link_comment_id__id FOREIGN KEY (comment_id) REFERENCES "comment"(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE "comment" DROP COLUMN "text";
