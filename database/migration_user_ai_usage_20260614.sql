set names utf8mb4;
use good_items_assistant;

-- 线上执行前必须先备份 good_items_assistant。本迁移面向 MariaDB 10.11，只应在确认未存在同名外键时执行一次。

create table if not exists mini_users (
  id bigint primary key auto_increment,
  openid varchar(120) not null unique,
  unionid varchar(120),
  nickname varchar(120),
  avatar_url varchar(800),
  status varchar(30) not null default 'ACTIVE',
  login_count int not null default 0,
  first_login_at datetime,
  last_login_at datetime,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  index idx_mini_users_last_login (last_login_at),
  index idx_mini_users_status (status)
);

create table if not exists user_login_events (
  id bigint primary key auto_increment,
  user_id bigint not null,
  openid varchar(120) not null,
  request_id varchar(80),
  created_at datetime not null default current_timestamp,
  constraint fk_login_events_user foreign key (user_id) references mini_users(id),
  index idx_login_events_user_created (user_id, created_at),
  index idx_login_events_created (created_at)
);

create table if not exists user_behavior_events (
  id bigint primary key auto_increment,
  user_id bigint not null,
  event_type varchar(80) not null,
  target_type varchar(80),
  target_id varchar(120),
  page_path varchar(255),
  detail varchar(500),
  request_id varchar(80),
  created_at datetime not null default current_timestamp,
  constraint fk_behavior_events_user foreign key (user_id) references mini_users(id),
  index idx_behavior_events_user_created (user_id, created_at),
  index idx_behavior_events_type_created (event_type, created_at),
  index idx_behavior_events_created (created_at)
);

create table if not exists user_favorites (
  id bigint primary key auto_increment,
  user_id bigint not null,
  item_id bigint not null,
  created_at datetime not null default current_timestamp,
  constraint fk_user_favorites_user foreign key (user_id) references mini_users(id),
  constraint fk_user_favorites_item foreign key (item_id) references content_items(id),
  unique key uk_user_favorites_user_item (user_id, item_id),
  index idx_user_favorites_created (created_at)
);

set @ai_tasks_user_column_exists = (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'ai_image_analysis_tasks'
    and column_name = 'user_id'
);
set @ai_tasks_user_column_sql = if(@ai_tasks_user_column_exists = 0,
  'alter table ai_image_analysis_tasks add column user_id bigint null after request_id',
  'select "ai_image_analysis_tasks.user_id already exists"');
prepare stmt from @ai_tasks_user_column_sql;
execute stmt;
deallocate prepare stmt;

set @ai_tasks_user_index_exists = (
  select count(*) from information_schema.statistics
  where table_schema = database()
    and table_name = 'ai_image_analysis_tasks'
    and index_name = 'idx_ai_image_tasks_user_created'
);
set @ai_tasks_user_index_sql = if(@ai_tasks_user_index_exists = 0,
  'alter table ai_image_analysis_tasks add index idx_ai_image_tasks_user_created (user_id, created_at)',
  'select "idx_ai_image_tasks_user_created already exists"');
prepare stmt from @ai_tasks_user_index_sql;
execute stmt;
deallocate prepare stmt;
set @fk_ai_tasks_user_exists = (
  select count(*) from information_schema.table_constraints
  where table_schema = database()
    and table_name = 'ai_image_analysis_tasks'
    and constraint_name = 'fk_ai_image_tasks_user'
);
set @fk_ai_tasks_user_sql = if(@fk_ai_tasks_user_exists = 0,
  'alter table ai_image_analysis_tasks add constraint fk_ai_image_tasks_user foreign key (user_id) references mini_users(id)',
  'select "fk_ai_image_tasks_user already exists"');
prepare stmt from @fk_ai_tasks_user_sql;
execute stmt;
deallocate prepare stmt;

set @ai_logs_user_column_exists = (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'ai_call_logs'
    and column_name = 'user_id'
);
set @ai_logs_user_column_sql = if(@ai_logs_user_column_exists = 0,
  'alter table ai_call_logs add column user_id bigint null after request_id',
  'select "ai_call_logs.user_id already exists"');
prepare stmt from @ai_logs_user_column_sql;
execute stmt;
deallocate prepare stmt;

set @ai_logs_user_index_exists = (
  select count(*) from information_schema.statistics
  where table_schema = database()
    and table_name = 'ai_call_logs'
    and index_name = 'idx_ai_call_logs_user_created'
);
set @ai_logs_user_index_sql = if(@ai_logs_user_index_exists = 0,
  'alter table ai_call_logs add index idx_ai_call_logs_user_created (user_id, created_at)',
  'select "idx_ai_call_logs_user_created already exists"');
prepare stmt from @ai_logs_user_index_sql;
execute stmt;
deallocate prepare stmt;
set @fk_ai_logs_user_exists = (
  select count(*) from information_schema.table_constraints
  where table_schema = database()
    and table_name = 'ai_call_logs'
    and constraint_name = 'fk_ai_call_logs_user'
);
set @fk_ai_logs_user_sql = if(@fk_ai_logs_user_exists = 0,
  'alter table ai_call_logs add constraint fk_ai_call_logs_user foreign key (user_id) references mini_users(id)',
  'select "fk_ai_call_logs_user already exists"');
prepare stmt from @fk_ai_logs_user_sql;
execute stmt;
deallocate prepare stmt;
