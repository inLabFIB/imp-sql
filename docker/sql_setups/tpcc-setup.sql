USE master;
GO
IF DB_ID (N'tpcc_db') IS NOT NULL
DROP DATABASE [tpcc_db];
GO
CREATE DATABASE [tpcc_db];
GO

USE [tpcc_db];
GO
CREATE SCHEMA [user_schema]
  GO

-- [user_schema].customer definition

-- Drop table

-- DROP TABLE [user_schema].customer;

CREATE TABLE [user_schema].customer (
  c_id int NOT NULL,
  c_d_id smallint NOT NULL,
  c_w_id int NOT NULL,
  c_discount smallmoney NULL,
  c_credit_lim money NULL,
  c_last char(16) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  c_first char(16) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  c_credit char(2) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  c_balance money NULL,
  c_ytd_payment money NULL,
  c_payment_cnt smallint NULL,
  c_delivery_cnt smallint NULL,
  c_street_1 char(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  c_street_2 char(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  c_city char(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  c_state char(2) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  c_zip char(9) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  c_phone char(16) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  c_since datetime NULL,
  c_middle char(2) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  c_data char(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  CONSTRAINT PK_CUSTOMER PRIMARY KEY (c_w_id,c_d_id,c_id)
);
CREATE NONCLUSTERED INDEX customer_c_last ON [user_schema].customer (  c_w_id ASC  , c_d_id ASC  , c_last ASC  , c_first ASC  , c_id ASC  )
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = OFF  )
	 ON [PRIMARY ] ;


-- [user_schema].district definition

-- Drop table

-- DROP TABLE [user_schema].district;

CREATE TABLE [user_schema].district (
  d_id smallint NOT NULL,
  d_w_id int NOT NULL,
  d_ytd money NOT NULL,
  d_next_o_id int NULL,
  d_tax smallmoney NULL,
  d_name char(10) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  d_street_1 char(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  d_street_2 char(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  d_city char(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  d_state char(2) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  d_zip char(9) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  padding char(6000) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT replicate('X',(6000)) NOT NULL,
  CONSTRAINT PK_DISTRICT PRIMARY KEY (d_w_id,d_id)
);
CREATE NONCLUSTERED INDEX district_d_id ON [user_schema].district (  d_id ASC  )
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = OFF  )
	 ON [PRIMARY ] ;


-- [user_schema].history definition

-- Drop table

-- DROP TABLE [user_schema].history;

CREATE TABLE [user_schema].history (
  h_id uniqueidentifier DEFAULT newid() NOT NULL,
  h_c_id int NULL,
  h_c_d_id smallint NULL,
  h_c_w_id int NULL,
  h_d_id smallint NULL,
  h_w_id int NULL,
  h_date datetime NULL,
  h_amount smallmoney NULL,
  h_data char(24) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  CONSTRAINT PK__history__430F8EDB1EEC4947 PRIMARY KEY (h_id)
);


-- [user_schema].item definition

-- Drop table

-- DROP TABLE [user_schema].item;

CREATE TABLE [user_schema].item (
  i_id int NOT NULL,
  i_name char(24) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  i_price smallmoney NULL,
  i_data char(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  i_im_id int NULL,
  CONSTRAINT PK_ITEM PRIMARY KEY (i_id)
);


-- [user_schema].new_order definition

-- Drop table

-- DROP TABLE [user_schema].new_order;

CREATE TABLE [user_schema].new_order (
  no_o_id int NOT NULL,
  no_d_id smallint NOT NULL,
  no_w_id int NOT NULL,
  CONSTRAINT PK_NEW_ORDER PRIMARY KEY (no_o_id,no_d_id,no_w_id)
);


-- [user_schema].order_line definition

-- Drop table

-- DROP TABLE [user_schema].order_line;

CREATE TABLE [user_schema].order_line (
  ol_o_id int NOT NULL,
  ol_d_id smallint NOT NULL,
  ol_w_id int NOT NULL,
  ol_number smallint NOT NULL,
  ol_i_id int NULL,
  ol_delivery_d datetime NULL,
  ol_amount smallmoney NULL,
  ol_supply_w_id int NULL,
  ol_quantity smallint NULL,
  ol_dist_info char(24) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  CONSTRAINT PK_ORDER_LINE PRIMARY KEY (ol_w_id,ol_d_id,ol_o_id,ol_number)
);


-- [user_schema].orders definition

-- Drop table

-- DROP TABLE [user_schema].orders;

CREATE TABLE [user_schema].orders (
  o_id int NOT NULL,
  o_d_id smallint NOT NULL,
  o_w_id int NOT NULL,
  o_c_id int NOT NULL,
  o_carrier_id smallint NULL,
  o_ol_cnt smallint NULL,
  o_all_local smallint NULL,
  o_entry_d datetime NULL,
  CONSTRAINT PK_ORDER PRIMARY KEY (o_w_id,o_d_id,o_id)
);
CREATE NONCLUSTERED INDEX orders_i2 ON [user_schema].orders (  o_w_id ASC  , o_d_id ASC  , o_c_id ASC  , o_id ASC  )
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = OFF , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;


-- [user_schema].stock definition

-- Drop table

-- DROP TABLE [user_schema].stock;

CREATE TABLE [user_schema].stock (
  s_i_id int NOT NULL,
  s_w_id int NOT NULL,
  s_quantity smallint NOT NULL,
  s_ytd int NOT NULL,
  s_order_cnt smallint NULL,
  s_remote_cnt smallint NULL,
  s_data char(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  s_dist_01 char(24) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  s_dist_02 char(24) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  s_dist_03 char(24) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  s_dist_04 char(24) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  s_dist_05 char(24) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  s_dist_06 char(24) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  s_dist_07 char(24) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  s_dist_08 char(24) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  s_dist_09 char(24) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  s_dist_10 char(24) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  CONSTRAINT PK_STOCK PRIMARY KEY (s_w_id,s_i_id)
);


-- [user_schema].warehouse definition

-- Drop table

-- DROP TABLE [user_schema].warehouse;

CREATE TABLE [user_schema].warehouse (
  w_id int NOT NULL,
  w_ytd money NOT NULL,
  w_tax smallmoney NOT NULL,
  w_name char(10) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  w_street_1 char(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  w_street_2 char(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  w_city char(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  w_state char(2) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  w_zip char(9) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
  padding char(4000) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT replicate('x',(4000)) NOT NULL,
  CONSTRAINT PK_WAREHOUSE PRIMARY KEY (w_id)
);
