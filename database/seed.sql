set names utf8mb4;

use good_items_assistant;

insert into mini_program_config(id, hero_eyebrow, hero_title, hero_subtitle, featured_title, search_placeholder, hot_words_json, me_title, me_description) values
(1, 'Good Finds', '好物展示小助手', '记录日常生活里真正顺手的小物件', '精选好物', '搜索好物、标签或体验关键词', '["收纳","日用","清洁","数码"]', '好物展示小助手', '这里用于浏览个人生活好物记录，是非经营性的内容展示工具。')
on duplicate key update
  hero_eyebrow = values(hero_eyebrow),
  hero_title = values(hero_title),
  hero_subtitle = values(hero_subtitle),
  featured_title = values(featured_title),
  search_placeholder = values(search_placeholder),
  hot_words_json = values(hot_words_json),
  me_title = values(me_title),
  me_description = values(me_description);

update content_categories
set enabled = 0
where slug in ('home-storage', 'kitchen-tools', 'daily-carry');

insert into content_categories(name, slug, description, cover_image, sort_order, enabled) values
('家居', 'home', '适合家里日常使用的小物件', 'https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com/good-items/categories/home.jpg', 100, 1),
('收纳', 'storage', '让空间更整齐的收纳用品', 'https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com/good-items/categories/storage.jpg', 95, 1),
('出行', 'travel', '随身携带和外出使用的小物', 'https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com/good-items/categories/travel.jpg', 90, 1),
('数码', 'digital', '日常使用的数码周边和小工具', 'https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com/good-items/categories/digital.jpg', 85, 1),
('清洁', 'cleaning', '提升清洁效率的小用品', 'https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com/good-items/categories/cleaning.jpg', 80, 1),
('日用', 'daily', '每天都能顺手用上的生活小物', 'https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com/good-items/categories/daily.jpg', 75, 1)
on duplicate key update
  name = values(name),
  description = values(description),
  cover_image = values(cover_image),
  sort_order = values(sort_order),
  enabled = values(enabled);

update content_items
set status = 'DRAFT'
where title in ('桌面分格收纳盒', '磁吸量勺套装', '薄款随身卡包');

update content_items
set category_id = (select id from content_categories where slug='storage'),
    summary = '适合放数据线、便签和零碎小工具。',
    experience = '实际使用下来，最大的好处是桌面不会再被小物件打散。透明分格方便一眼找到东西，适合放在书桌角落。',
    tags_json = '["收纳","桌面","图文心得"]',
    status = 'PUBLISHED',
    sort_order = 90,
    published_at = coalesce(published_at, now())
where title = '桌面分格收纳盒';

insert into content_banners(title, image_url, target_type, target_value, sort_order, enabled)
select '日用好物灵感', 'https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com/good-items/banners/daily-finds.jpg', 'CATEGORY', 'daily', 100, 1
where not exists (select 1 from content_banners where title = '日用好物灵感');
insert into content_banners(title, image_url, target_type, target_value, sort_order, enabled)
select '小空间整理灵感', 'https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com/good-items/banners/storage-ideas.jpg', 'CATEGORY', 'storage', 90, 1
where not exists (select 1 from content_banners where title = '小空间整理灵感');

insert into content_items(category_id, title, summary, experience, tags_json, cover_image, gallery_json, status, sort_order, published_at)
select (select id from content_categories where slug='daily'), '便携纸巾盒', '适合随身放纸巾和小包装湿巾。', '它适合放在包里、车里或办公桌边。取用顺手，外观看起来也比散放纸巾更整洁。', '["日用","图文心得","随身"]', 'https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com/good-items/items/tissue-box-cover.jpg', '["https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com/good-items/items/tissue-box-1.jpg"]', 'PUBLISHED', 100, now()
where not exists (select 1 from content_items where title = '便携纸巾盒');
insert into content_items(category_id, title, summary, experience, tags_json, cover_image, gallery_json, status, sort_order, published_at)
select (select id from content_categories where slug='daily'), '随手记事本', '适合记录临时想法、清单和待办。', '纸笔记录比手机更直接，适合放在书桌、包里或床头。平时写购物清单、灵感和提醒都方便。', '["日用","图文心得","记录"]', 'https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com/good-items/items/notebook-cover.jpg', '["https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com/good-items/items/notebook-1.jpg"]', 'PUBLISHED', 95, now()
where not exists (select 1 from content_items where title = '随手记事本');
insert into content_items(category_id, title, summary, experience, tags_json, cover_image, gallery_json, status, sort_order, published_at)
select (select id from content_categories where slug='storage'), '桌面分格收纳盒', '适合放数据线、便签和零碎小工具。', '实际使用下来，最大的好处是桌面不会再被小物件打散。透明分格方便一眼找到东西，适合放在书桌角落。', '["收纳","桌面","图文心得"]', 'https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com/good-items/items/desk-organizer-cover.jpg', '["https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com/good-items/items/desk-organizer-1.jpg"]', 'PUBLISHED', 90, now()
where not exists (select 1 from content_items where title = '桌面分格收纳盒');
insert into content_items(category_id, title, summary, experience, tags_json, cover_image, gallery_json, status, sort_order, published_at)
select (select id from content_categories where slug='cleaning'), '迷你清洁喷瓶', '适合随手清洁桌面、镜片和小物件。', '小容量更适合放在包里或办公桌边，不占地方。需要擦拭的时候不用到处找清洁用品。', '["清洁","日用","图文心得"]', 'https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com/good-items/items/cleaning-spray-cover.jpg', '["https://ai-file-1409230880.cos.ap-guangzhou.myqcloud.com/good-items/items/cleaning-spray-1.jpg"]', 'PUBLISHED', 85, now()
where not exists (select 1 from content_items where title = '迷你清洁喷瓶');

insert into ai_feature_settings(
  id, ai_enabled, auto_ingest_enabled, auto_publish_enabled, low_confidence_review_enabled,
  confidence_threshold, daily_call_limit, max_image_size_mb
) values (1, 0, 0, 0, 1, 0.7500, 50, 5)
on duplicate key update
  ai_enabled = values(ai_enabled),
  auto_ingest_enabled = values(auto_ingest_enabled),
  auto_publish_enabled = values(auto_publish_enabled),
  low_confidence_review_enabled = values(low_confidence_review_enabled),
  confidence_threshold = values(confidence_threshold),
  daily_call_limit = values(daily_call_limit),
  max_image_size_mb = values(max_image_size_mb);

insert into ai_model_configs(
  provider_code, display_name, model_name, base_url, api_key_env, enabled,
  prompt_price_per_1k, completion_price_per_1k, sort_order
) values
('kimi', 'Kimi 视觉模型', 'moonshot-v1-8k-vision-preview', 'https://api.moonshot.cn/v1', 'KIMI_API_KEY', 0, 0, 0, 100),
('doubao', '豆包视觉模型', 'doubao-1-5-vision-pro-32k', 'https://ark.cn-beijing.volces.com/api/v3', 'DOUBAO_API_KEY', 0, 0, 0, 90)
on duplicate key update
  display_name = values(display_name),
  model_name = values(model_name),
  base_url = values(base_url),
  api_key_env = values(api_key_env),
  enabled = values(enabled),
  prompt_price_per_1k = values(prompt_price_per_1k),
  completion_price_per_1k = values(completion_price_per_1k),
  sort_order = values(sort_order);

insert into ai_prompt_templates(scenario, title, prompt_text, enabled) values
('IMAGE_CLASSIFY', '图片好物分类分析', '你是“好物展示小助手”的内容整理员。请只围绕图片中的日用好物、生活小用品、家居、收纳、出行、数码、清洁等内容进行分析。根据已有分类判断应放入哪个分类，或建议新增一个分类。必须返回严格 JSON，不要输出 Markdown。', 1)
on duplicate key update
  title = values(title),
  prompt_text = values(prompt_text),
  enabled = values(enabled);
