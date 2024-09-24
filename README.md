# csvql

yet another cli tool to query a csv w/ sql

## Example queries

### Select all columns from a csv file

```sh
./bin/csvql csvql 'select * from file.csv'
```

### Select single / multiple specific columns

```sh
./bin/csvql csvql 'select col1, col2 from file.csv'
```

### Conditions

```sh
./bin/csvql csvql 'select * from file.csv where col1 + col2 = 2'
```
