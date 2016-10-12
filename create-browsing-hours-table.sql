\connect sales_data;

drop table if exists browsinghours;

create table browsinghours (
	hour integer primary key,
	amount integer not null
);
