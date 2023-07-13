## DataType conversions (SQLServer -> SQL standard)
- `money` --> `SQLNumeric(19,4)` (8 bytes -> 9 bytes, both store negative numbers)
- `small money` --> `SQLNumeric(9,4)` (4 bytes -> 5 bytes, both store negative numbers)
- `uniqueidentifier` --> `SQLChar(255)`
- `tinyint` --> `SQLSmallint()`

## DataType conversions (SQL standard -> SQLServer)
- `varbit(l)` -> Exception!!
