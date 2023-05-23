USE master;
GO
IF DB_ID (N'test_db') IS NOT NULL
DROP DATABASE [test_db];
GO
CREATE DATABASE [test_db];
GO
BEGIN TRANSACTION

USE [test_db];
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE SCHEMA ref_test_schema
GO
CREATE TABLE ref_test_schema.test_referenced_table (
  pk1 BIT,
  pk2 CHAR(255),
  CONSTRAINT test_ref_pk PRIMARY KEY (pk1, pk2)
);
GO
CREATE SCHEMA test_schema
GO
CREATE TABLE test_schema.test_referenced_table (
  pk1 INT,
  pk2 VARCHAR(64),
  CONSTRAINT test_ref_pk PRIMARY KEY (pk1),
  CONSTRAINT test_ref_u UNIQUE (pk2)
);
GO
CREATE TABLE test_schema.test (
  btAttr BIT,
  chAttr CHAR(255) DEFAULT NEWID(),
  dtAttr DATETIME2(7),
  dpAttr DOUBLE PRECISION,
  flAttr FLOAT(16),
  itAttr INT DEFAULT 1,
  rlAttr REAL NOT NULL,
  siAttr SMALLINT CHECK (siAttr = 5),
  vcAttr VARCHAR(64),
  selfRl REAL,
  CONSTRAINT test_pk PRIMARY KEY (chAttr, itAttr),
  CONSTRAINT test_fk_1 FOREIGN KEY (btAttr, chAttr) REFERENCES ref_test_schema.test_referenced_table(pk1, pk2),
  CONSTRAINT test_fk_2 FOREIGN KEY (vcAttr) REFERENCES test_schema.test_referenced_table(pk2),
  CONSTRAINT test_fk_self FOREIGN KEY (selfRl) REFERENCES test_schema.test(rlAttr),
  CONSTRAINT test_ck CHECK (dpAttr = flAttr),
  CONSTRAINT test_u UNIQUE (rlAttr)
);
GO
COMMIT
