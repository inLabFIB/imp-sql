USE master;
GO
IF DB_ID (N'cv2_db') IS NOT NULL
DROP DATABASE [cv2_db];
GO
CREATE DATABASE [cv2_db];
GO

USE [cv2_db];
GO
CREATE SCHEMA [user_schema]
GO

CREATE TABLE [user_schema].company (
  c_pk int primary key
);
GO
CREATE TABLE [user_schema].national_company (
  c_fk int primary key,
  constraint national_fk foreign key (c_fk) references [user_schema].company(c_pk)
);
GO
CREATE TABLE [user_schema].international_company (
  c_fk int primary key,
  constraint international_fk foreign key (c_fk) references [user_schema].company(c_pk)
);
GO
CREATE TABLE [user_schema].service (
  s_pk int primary key,
  c_fk int,
  constraint service_fk foreign key (c_fk) references [user_schema].company(c_pk)
);
GO
CREATE TABLE [user_schema].fast (
  s_fk int primary key,
  constraint fast_fk foreign key (s_fk) references [user_schema].service(s_pk)
);
GO
CREATE TABLE [user_schema].quality (
  s_fk int primary key,
  constraint quality_fk foreign key (s_fk) references [user_schema].service(s_pk)
);
GO
CREATE TABLE [user_schema].expensive (
  s_fk int primary key,
  constraint expensive_fk foreign key (s_fk) references [user_schema].service(s_pk)
);
GO
