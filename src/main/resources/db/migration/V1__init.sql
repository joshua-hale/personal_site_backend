-- noinspection SqlNoDataSourceInspectionForFile

-- Initial schema for blog posts
CREATE TABLE posts (
                       id           BIGSERIAL PRIMARY KEY,
                       title        VARCHAR(200) NOT NULL,
                       slug         VARCHAR(200) NOT NULL UNIQUE,
                       content      TEXT NOT NULL,
                       hero_image   VARCHAR(500),
                       created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);