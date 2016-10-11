\connect sales_data;

select p.product_name, sum(oi.order_item_quantity) as orders
from
  products p
  join order_items oi on p.product_id = oi.order_item_product_id
  join orders o on o.order_id = oi.order_item_order_id
where
  o.order_status in ('COMPLETE', 'CLOSED')
group by
  p.product_name
order by orders desc
limit 10;
