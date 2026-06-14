set names utf8mb4;

create database if not exists good_items_assistant
  default character set utf8mb4
  collate utf8mb4_unicode_ci;

create user if not exists 'good_items'@'localhost' identified by 'change-me';
grant all privileges on good_items_assistant.* to 'good_items'@'localhost';
create user if not exists 'good_items'@'127.0.0.1' identified by 'change-me';
grant all privileges on good_items_assistant.* to 'good_items'@'127.0.0.1';

use good_items_assistant;

create table if not exists content_categories (
  id bigint primary key auto_increment,
  name varchar(80) not null,
  slug varchar(80) unique,
  description varchar(255),
  cover_image varchar(500),
  sort_order int not null default 0,
  enabled tinyint(1) not null default 1,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp
);

create table if not exists content_items (
  id bigint primary key auto_increment,
  category_id bigint not null,
  title varchar(120) not null,
  summary varchar(300),
  experience text,
  tags_json json,
  cover_image varchar(500) not null,
  gallery_json json,
  status varchar(20) not null default 'DRAFT',
  sort_order int not null default 0,
  view_count int not null default 0,
  favorite_count int not null default 0,
  published_at datetime null,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  constraint fk_content_items_category foreign key (category_id) references content_categories(id),
  index idx_content_items_status_sort (status, sort_order, id),
  index idx_content_items_category_status (category_id, status)
);

create table if not exists content_banners (
  id bigint primary key auto_increment,
  title varchar(120) not null,
  image_url varchar(500) not null,
  target_type varchar(40) not null default 'NONE',
  target_value varchar(120),
  sort_order int not null default 0,
  enabled tinyint(1) not null default 1,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  index idx_content_banners_enabled_sort (enabled, sort_order, id)
);

create table if not exists mini_program_config (
  id bigint primary key,
  hero_eyebrow varchar(80) not null,
  hero_title varchar(120) not null,
  hero_subtitle varchar(255) not null,
  featured_title varchar(80) not null,
  search_placeholder varchar(120) not null,
  hot_words_json json,
  me_title varchar(120) not null,
  me_description varchar(500) not null,
  updated_at datetime not null default current_timestamp on update current_timestamp
);

create table if not exists operation_audit_logs (
  id bigint primary key auto_increment,
  request_id varchar(80) not null,
  operator varchar(80),
  action varchar(120) not null,
  target_type varchar(80),
  target_id varchar(80),
  detail_json json,
  created_at datetime not null default current_timestamp,
  index idx_audit_request_id (request_id),
  index idx_audit_created_at (created_at)
);

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

create table if not exists mini_users (
  id bigint primary key auto_increment,
  openid varchar(120) not null unique,
  unionid varchar(120),
  nickname varchar(120),
  avatar_url varchar(800),
  status varchar(30) not null default 'ACTIVE',
  tier_code varchar(20) not null default 'LEVEL_1',
  custom_daily_token_limit bigint null,
  custom_monthly_token_limit bigint null,
  custom_daily_call_limit int null,
  login_count int not null default 0,
  first_login_at datetime,
  last_login_at datetime,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  index idx_mini_users_last_login (last_login_at),
  index idx_mini_users_status (status),
  index idx_mini_users_tier (tier_code)
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

create table if not exists media_assets (
  id bigint primary key auto_increment,
  source varchar(40) not null default 'MINI_AI_UPLOAD',
  original_filename varchar(255),
  mime_type varchar(120) not null,
  file_size bigint not null default 0,
  sha256 varchar(80),
  object_key varchar(500) not null,
  public_url varchar(800) not null,
  created_at datetime not null default current_timestamp,
  index idx_media_assets_source_created (source, created_at),
  index idx_media_assets_sha256 (sha256)
);

create table if not exists ai_feature_settings (
  id bigint primary key,
  ai_enabled tinyint(1) not null default 0,
  auto_ingest_enabled tinyint(1) not null default 0,
  auto_publish_enabled tinyint(1) not null default 0,
  low_confidence_review_enabled tinyint(1) not null default 1,
  confidence_threshold decimal(5,4) not null default 0.7500,
  daily_call_limit int not null default 50,
  max_image_size_mb int not null default 5,
  updated_at datetime not null default current_timestamp on update current_timestamp
);

create table if not exists ai_model_configs (
  id bigint primary key auto_increment,
  provider_code varchar(40) not null unique,
  display_name varchar(80) not null,
  model_name varchar(120) not null,
  base_url varchar(500) not null,
  api_key_env varchar(120) not null,
  enabled tinyint(1) not null default 0,
  prompt_price_per_1k decimal(12,6) not null default 0,
  completion_price_per_1k decimal(12,6) not null default 0,
  sort_order int not null default 0,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  index idx_ai_model_enabled_sort (enabled, sort_order, id)
);

create table if not exists ai_prompt_templates (
  id bigint primary key auto_increment,
  scenario varchar(80) not null unique,
  title varchar(120) not null,
  prompt_text text not null,
  enabled tinyint(1) not null default 1,
  updated_at datetime not null default current_timestamp on update current_timestamp
);

create table if not exists ai_image_analysis_tasks (
  id bigint primary key auto_increment,
  request_id varchar(80) not null,
  user_id bigint,
  media_asset_id bigint not null,
  provider_code varchar(40) not null,
  model_name varchar(120) not null,
  status varchar(40) not null default 'PENDING_REVIEW',
  ingest_mode varchar(20) not null default 'MANUAL',
  item_title varchar(120),
  summary varchar(300),
  experience text,
  tags_json json,
  decision varchar(40),
  matched_category_id bigint,
  new_category_name varchar(80),
  new_category_slug varchar(80),
  new_category_description varchar(255),
  confidence decimal(5,4),
  reason varchar(500),
  review_reason varchar(500),
  raw_result_json json,
  created_category_id bigint,
  created_item_id bigint,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  constraint fk_ai_image_tasks_user foreign key (user_id) references mini_users(id),
  constraint fk_ai_image_tasks_media foreign key (media_asset_id) references media_assets(id),
  index idx_ai_image_tasks_user_created (user_id, created_at),
  index idx_ai_image_tasks_status_created (status, created_at),
  index idx_ai_image_tasks_request_id (request_id)
);

create table if not exists ai_call_logs (
  id bigint primary key auto_increment,
  request_id varchar(80) not null,
  user_id bigint,
  provider_code varchar(40) not null,
  model_name varchar(120) not null,
  scenario varchar(80) not null,
  status varchar(30) not null,
  prompt_tokens int not null default 0,
  completion_tokens int not null default 0,
  total_tokens int not null default 0,
  estimated_cost decimal(14,6) not null default 0,
  duration_ms int not null default 0,
  error_message varchar(500),
  task_id bigint,
  created_at datetime not null default current_timestamp,
  constraint fk_ai_call_logs_user foreign key (user_id) references mini_users(id),
  index idx_ai_call_logs_user_created (user_id, created_at),
  index idx_ai_call_logs_created (created_at),
  index idx_ai_call_logs_provider_created (provider_code, created_at),
  index idx_ai_call_logs_request_id (request_id)
);
