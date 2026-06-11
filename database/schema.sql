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
