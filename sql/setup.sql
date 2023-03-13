USE master;
GO
IF DB_ID (N'user_db') IS NOT NULL
DROP DATABASE [user_db];
GO
CREATE DATABASE [user_db];
GO

USE [user_db];
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE SCHEMA user_schema
GO
CREATE TABLE user_schema.test_referenced (
  pk1 int,
  pk2 int,
  CONSTRAINT test_ref_pk UNIQUE (pk1, pk2)
);
GO
CREATE TABLE user_schema.test (
  pk1 int,
  pk2 int check (pk2 = 3),
  fk1 int NOT NULL,
  fk2 int DEFAULT 1,
  CONSTRAINT test_pk PRIMARY KEY (pk1, pk2),
  CONSTRAINT test_fk_c FOREIGN KEY (fk1, fk2) REFERENCES user_schema.test_referenced(pk1, pk2),
  CONSTRAINT test_fk_s FOREIGN KEY (fk1) REFERENCES user_schema.international_company(c_fk),
  CONSTRAINT test_check CHECK (pk2 = fk1)
);
