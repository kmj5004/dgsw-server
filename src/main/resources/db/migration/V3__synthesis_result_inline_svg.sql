


ALTER TABLE synthesis_results
    DROP COLUMN svg_path,
    ADD COLUMN svg_content MEDIUMTEXT NOT NULL;
