drop table if exists player_account CASCADE;
drop table if exists transaction CASCADE;
create table player_account (account_id varchar(255) not null, created_at timestamp, version integer, balance numeric(19,2), updated_at timestamp, primary key (account_id));
create table transaction (id varchar(255) not null, created_at timestamp, version integer, amount numeric(19,2), transaction_type varchar(255), user_transaction_time timestamp, player_account_id varchar(255), primary key (id));
alter table transaction add constraint transaction_player_FK foreign key (player_account_id) references player_account;

