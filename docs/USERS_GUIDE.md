# User Guide

IMP SQL is the implementation of a standard SQL metamodel which aims to model basic SQL objects like Table, View or
Assertion which is assumed for the user to be familiar with.

All the different SQL metamodel entities are divided in the following groups which are appropriately mapped to code 
packages:

- **Main Entities** 
  - SQLObjectSchema
  - Assertion
  - Table
  - Attribute
  - View
  - Schema Reference
- **Relational Expressions**:
  - **JOIN Operation** 
    - Cross Join
    - On Join
  - **Aliasable Relational Expression**
    - Table Reference
    - **Query**
      - Set Operation (NOT IMPLEMENTED YET)
      - Table Expression
- **Boolean Expressions**:
  - Not Operation
  - Is Operation (NOT IMPLEMENTED YET)
  - Predicate Operation
  - **Predicate**
    - Comparison Predicate
    - Exist Predicate
    - (...) (NOT IMPLEMENTED YET)
- **Value Expressions**:
  - Column Reference
  - Unary Operation (NOT IMPLEMENTED YET)
  - Arithmetic Operation (NOT IMPLEMENTED YET)
  - _Query_
  - **Primitive Expressions**
    - Null (NOT IMPLEMENTED YET)
    - **Primitive Constants**
      - SQL Binary (NOT IMPLEMENTED YET)
      - SQL Float
      - SQL Integer
      - SQL String
- **Selection Expressions**:
  - Asterisk
  - Aliasable Select Item
- **SQL Data Types**:
  - SQL Char
  - SQL Varchar
  - SQL Bit
  - SQL Int
  - SQL Smallint
  - SQL Float
  - SQL Real
  - SQL Data
  - (...) (NOT IMPLEMENTED YET)
- **Constraints**:
  - _Assertion_
  - **Table Constraint**
    - Check
    - Foreign Key
    - Primary Key
    - Unique
    
By contract, almost all IMP SQL entities are immutable.


[comment]: <> (TODO: FINISH THIS)

## Supported SQL expressions

All that can be deduced from the previous objects.

[comment]: <> (TODO: FINISH THIS)

## Work in progress

- **Relational Expressions**
  - UNIONS
  - SET OPERATIONS
- **Value Expressions**
  - VALUE OPERATIONS (unary & binary)
  - NULL
- **Boolean Expressions**
  - IS OPERATION
  - MORE PREDICATES
- **Other**
  - MORE DATA TYPES

[comment]: <> (TODO: FINISH THIS)

## Instantiating a SQL object schema

[comment]: <> (TODO: FINISH THIS)

### How to use the TableBuilder and TableSetBuilder

[comment]: <> (TODO: FINISH THIS)
