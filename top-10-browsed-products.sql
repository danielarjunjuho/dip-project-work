\connect sales_data

select product_name, product_count
from browsing
order by product_count desc
limit 10;
