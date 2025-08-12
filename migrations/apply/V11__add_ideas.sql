CREATE TABLE IF NOT EXISTS idea (id uuid PRIMARY KEY, audio_url TEXT NULL, "text" TEXT NULL, image_url TEXT NULL, thumb_url TEXT NULL);
CREATE TABLE IF NOT EXISTS star_idea (star_id uuid, idea_id uuid, CONSTRAINT pk_star_idea PRIMARY KEY (star_id, idea_id), CONSTRAINT fk_star_idea_star_id__id FOREIGN KEY (star_id) REFERENCES star(id) ON DELETE CASCADE ON UPDATE RESTRICT, CONSTRAINT fk_star_idea_idea_id__id FOREIGN KEY (idea_id) REFERENCES idea(id) ON DELETE CASCADE ON UPDATE RESTRICT);
CREATE INDEX star_idea_star_id ON star_idea (star_id);
CREATE INDEX star_idea_idea_id ON star_idea (idea_id);
