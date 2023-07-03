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
      - Set Operation
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
  - SQLFunction
  - _Query_ (should be scalar)
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

  - SQL Bit
  - SQL Varbit (NOT IMPLEMENTED YET)

  - SQL Char
  - SQL Varchar

  - SQL Date
  - SQL Time (NOT IMPLEMENTED YET)
  - SQL DateTime*
  - SQL Timestamp (NOT IMPLEMENTED YET)

  - SQL Decimal (NOT IMPLEMENTED YET)
  - SQL DoublePrecision
  - SQL Float
  - SQL Integer
  - SQL Smallint
  - SQL Numeric
  - SQL Real

  - SQL Interval (NOT IMPLEMENTED YET)

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

### Supported SQL expressions
All SLQ92 supported data types are considered and defined below:
- SQL Bit: 
- SQL Varbit

- SQL Char
- SQL Varchar

- SQL Date
- SQL Time
- SQL DateTime*
- SQL Timestamp

- SQL Decimal
- SQL DoublePrecision
- SQL Float
- SQL Integer
- SQL Smallint
- SQL Numeric
- SQL Real

- SQL Interval (NOT IMPLEMENTED YET)

*DateTime is not from the SQL92 standard but is also added!

[comment]: <> (TODO: FINISH THIS)

## Work in progress / Future work

- **Value Expressions**
  - VALUE OPERATIONS (unary & arithmetic)
  - NULL
  - MORE PRIMITIVE CONSTANTS
- **Boolean Expressions**
  - IS OPERATION
  - MORE PREDICATES
- **Other**
  - MORE DATA TYPES

  

[comment]: <> (TODO: FINISH THIS)

## Instantiating a SQL object schema

There are two main ways the general user is expected to instantiate an SQLObjectSchema:
- Using the `SQLObjectSchemaParser` to parse an SQL string into SQL object schema entities. It accepts the statements:
  - `CREATE TABLE`
  - `CREATE VIEW`
  - `CREATE ASSERTION`
- Using the `SQLObjectSchemaFetcher` to connect to a database and retrieve the tables of the specified schemas.
  - Future work: Retrieve also the views.

Both ways use, under the hood, the `TableSetBuilder` class, which helps create `Table` instances with the correct
immutable attributes and constraints.

[comment]: <> (TODO: FINISH THIS)

### How to use the TableBuilder and TableSetBuilder

These classes were added because, since the schema entities are, by contract, immutable, cyclic foreign key
dependencies could not be implemented.

A user can initialize a set of `Table` instances via the `TableSetBuilder` by adding new tables, and adding new
attributes and constraints into them calling the functions with name:
- `addTable`
- `addAttribute`
  - `setAttributeNullable`
  - `setAttributeType`
  - `setAttributeDefaultExpression`
- `addCheckConstraint`
- `addUniqueConstraint`
- `addPrimaryKeyConstraint`
- `addForeignKeyConstraint`

Internally, the class uses 'provisional' instances that hold the configuration of the tables to be built.
A subsequent call to `build` traverses these provisional instances and returns the set of new `Table` instances.

## Validating the correctness of an SQL object schema

While building the SQL object schema via the parser, some 'easy' checks are done automatically, like
ensuring that table references found in an Assertion's definition actually reference `Table` instances of
the schema (that is why order matters, the necessary `CREATE TABLE` statements should be given before the 
`CREATE ASSERTION` statement).

A more difficult task is to validate that the aliases used in an Assertion's definition are all correct,
meaning that they reference context-available relational expressions and that there are no collisions or
ambiguities. An additional service is provided, which validates these scenarios: `SQLObjectSchemaValidator`.

For now, it only validates aliases, although future validations can be included.

