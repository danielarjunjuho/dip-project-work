* Start PostgreSQL CLI

`% psql`

* First we need to use `\c sales_data` to choose our database.

`\connect sales_data`

* The `\dt` command is useful for listing relations (and more importantly; **tables**)

* Testing:
- `sales_data-# SELECT * FROM orders`
- `sales_data-# select * from sales_data.orders`
- `sales_data-# select * from public."orders"`
- `sales_data-# select * from sales_data."orders"`
* Nothing works
#And this is where I give up!
