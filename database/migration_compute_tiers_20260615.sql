set names utf8mb4;
use good_items_assistant;

-- 上线执行前必须先备份 good_items_assistant。本迁移为幂等迁移，用于新增三级会员算力额度。
create table if not exists compute_tiers (
  tier_code varchar(20) primary key,
  tier_name varchar(40) not null,
  daily_token_limit bigint not null default 0,
  monthly_token_limit bigint not null default 0,
  daily_call_limit int not null default 0,
  enabled tinyint(1) not null default 1,
  sort_order int not null default 0,
  updated_at datetime not null default current_timestamp on update current_timestamp
);

insert ignore into compute_tiers(tier_code, tier_name, daily_token_limit, monthly_token_limit, daily_call_limit, enabled, sort_order)
values
('LEVEL_1','一级会员',50000,1000000,20,1,10),
('LEVEL_2','二级会员',200000,5000000,80,1,20),
('LEVEL_3','三级会员',1000000,20000000,300,1,30);

set @tier_column_exists = (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'mini_users'
    and column_name = 'tier_code'
);
set @tier_column_sql = if(@tier_column_exists = 0,
  'alter table mini_users add column tier_code varchar(20) not null default ''LEVEL_1'' after status',
  'select "mini_users.tier_code already exists"');
prepare stmt from @tier_column_sql;
execute stmt;
deallocate prepare stmt;

set @daily_token_column_exists = (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'mini_users'
    and column_name = 'custom_daily_token_limit'
);
set @daily_token_column_sql = if(@daily_token_column_exists = 0,
  'alter table mini_users add column custom_daily_token_limit bigint null after tier_code',
  'select "mini_users.custom_daily_token_limit already exists"');
prepare stmt from @daily_token_column_sql;
execute stmt;
deallocate prepare stmt;

set @monthly_token_column_exists = (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'mini_users'
    and column_name = 'custom_monthly_token_limit'
);
set @monthly_token_column_sql = if(@monthly_token_column_exists = 0,
  'alter table mini_users add column custom_monthly_token_limit bigint null after custom_daily_token_limit',
  'select "mini_users.custom_monthly_token_limit already exists"');
prepare stmt from @monthly_token_column_sql;
execute stmt;
deallocate prepare stmt;

set @daily_call_column_exists = (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'mini_users'
    and column_name = 'custom_daily_call_limit'
);
set @daily_call_column_sql = if(@daily_call_column_exists = 0,
  'alter table mini_users add column custom_daily_call_limit int null after custom_monthly_token_limit',
  'select "mini_users.custom_daily_call_limit already exists"');
prepare stmt from @daily_call_column_sql;
execute stmt;
deallocate prepare stmt;

update mini_users set tier_code = 'LEVEL_1' where tier_code is null or tier_code = '';

set @tier_index_exists = (
  select count(*) from information_schema.statistics
  where table_schema = database()
    and table_name = 'mini_users'
    and index_name = 'idx_mini_users_tier'
);
set @tier_index_sql = if(@tier_index_exists = 0,
  'alter table mini_users add index idx_mini_users_tier (tier_code)',
  'select "idx_mini_users_tier already exists"');
prepare stmt from @tier_index_sql;
execute stmt;
deallocate prepare stmt;
