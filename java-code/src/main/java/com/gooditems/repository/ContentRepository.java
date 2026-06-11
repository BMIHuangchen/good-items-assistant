package com.gooditems.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gooditems.common.PageResult;
import com.gooditems.dto.BannerRequest;
import com.gooditems.dto.CategoryRequest;
import com.gooditems.dto.GoodItemRequest;
import com.gooditems.dto.MiniProgramConfigRequest;
import com.gooditems.exception.ApiException;
import com.gooditems.model.Banner;
import com.gooditems.model.Category;
import com.gooditems.model.DashboardStats;
import com.gooditems.model.GoodItem;
import com.gooditems.model.MiniProgramConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class ContentRepository {
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public ContentRepository(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    public List<Banner> publicBanners() {
        return jdbc.query("select * from content_banners where enabled = 1 order by sort_order desc, id desc", bannerMapper());
    }

    public List<Category> publicCategories() {
        return jdbc.query("select * from content_categories where enabled = 1 order by sort_order desc, id desc", categoryMapper());
    }

    public MiniProgramConfig miniProgramConfig() {
        return jdbc.query("select * from mini_program_config where id = 1", miniProgramConfigMapper())
                .stream()
                .findFirst()
                .orElseGet(this::createDefaultMiniProgramConfig);
    }

    public MiniProgramConfig updateMiniProgramConfig(MiniProgramConfigRequest request) {
        jdbc.update("""
                insert into mini_program_config(id, hero_eyebrow, hero_title, hero_subtitle, featured_title, search_placeholder,
                hot_words_json, me_title, me_description)
                values(1,?,?,?,?,?,?,?,?)
                on duplicate key update hero_eyebrow=values(hero_eyebrow), hero_title=values(hero_title),
                hero_subtitle=values(hero_subtitle), featured_title=values(featured_title),
                search_placeholder=values(search_placeholder), hot_words_json=values(hot_words_json),
                me_title=values(me_title), me_description=values(me_description)
                """, request.heroEyebrow(), request.heroTitle(), request.heroSubtitle(), request.featuredTitle(),
                request.searchPlaceholder(), json(request.hotWords()), request.meTitle(), request.meDescription());
        return miniProgramConfig();
    }

    public PageResult<GoodItem> publicItems(Long categoryId, String keyword, int pageNum, int pageSize) {
        StringBuilder where = new StringBuilder(" where i.status = 'PUBLISHED' and c.enabled = 1");
        if (categoryId != null) {
            where.append(" and i.category_id = ").append(categoryId);
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" and (i.title like ? or i.summary like ? or i.tags_json like ?)");
            String like = "%" + keyword.trim() + "%";
            long total = jdbc.queryForObject(baseItemCountSql() + where, Long.class, like, like, like);
            List<GoodItem> list = jdbc.query(baseItemSql() + where + " order by i.sort_order desc, i.id desc limit ? offset ?",
                    itemMapper(), like, like, like, pageSize, (pageNum - 1) * pageSize);
            return new PageResult<>(list, total, pageNum, pageSize);
        }
        long total = jdbc.queryForObject(baseItemCountSql() + where, Long.class);
        List<GoodItem> list = jdbc.query(baseItemSql() + where + " order by i.sort_order desc, i.id desc limit ? offset ?",
                itemMapper(), pageSize, (pageNum - 1) * pageSize);
        return new PageResult<>(list, total, pageNum, pageSize);
    }

    public GoodItem publicItem(Long id) {
        GoodItem item = jdbc.query(baseItemSql() + " where i.id = ? and i.status = 'PUBLISHED' and c.enabled = 1", itemMapper(), id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(404, "内容不存在或未发布"));
        jdbc.update("update content_items set view_count = view_count + 1 where id = ?", id);
        return item;
    }

    public PageResult<GoodItem> adminItems(String status, int pageNum, int pageSize) {
        String where = status == null || status.isBlank() ? "" : " where i.status = ?";
        long total = where.isEmpty()
                ? jdbc.queryForObject("select count(*) from content_items i", Long.class)
                : jdbc.queryForObject("select count(*) from content_items i" + where, Long.class, status);
        List<GoodItem> list = where.isEmpty()
                ? jdbc.query(baseItemSql() + " order by i.updated_at desc limit ? offset ?", itemMapper(), pageSize, (pageNum - 1) * pageSize)
                : jdbc.query(baseItemSql() + where + " order by i.updated_at desc limit ? offset ?", itemMapper(), status, pageSize, (pageNum - 1) * pageSize);
        return new PageResult<>(list, total, pageNum, pageSize);
    }

    public GoodItem findItem(Long id) {
        return jdbc.query(baseItemSql() + " where i.id = ?", itemMapper(), id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(404, "内容不存在"));
    }

    public GoodItem createItem(GoodItemRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into content_items(category_id,title,summary,experience,tags_json,cover_image,gallery_json,status,sort_order,published_at)
                    values(?,?,?,?,?,?,?,?,?,case when ? = 'PUBLISHED' then now() else null end)
                    """, Statement.RETURN_GENERATED_KEYS);
            bindItem(ps, request);
            ps.setString(10, status(request.status()));
            return ps;
        }, keyHolder);
        return findItem(keyHolder.getKey().longValue());
    }

    public GoodItem updateItem(Long id, GoodItemRequest request) {
        jdbc.update("""
                update content_items set category_id=?, title=?, summary=?, experience=?, tags_json=?, cover_image=?,
                gallery_json=?, status=?, sort_order=?, published_at=case when ? = 'PUBLISHED' and published_at is null then now() else published_at end
                where id=?
                """, request.categoryId(), request.title(), request.summary(), request.experience(), json(request.tags()),
                request.coverImage(), json(request.gallery()), status(request.status()), value(request.sortOrder(), 0),
                status(request.status()), id);
        return findItem(id);
    }

    public List<Category> adminCategories() {
        return jdbc.query("select * from content_categories order by sort_order desc, id desc", categoryMapper());
    }

    public Category createCategory(CategoryRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into content_categories(name,slug,description,cover_image,sort_order,enabled) values(?,?,?,?,?,?)
                    """, Statement.RETURN_GENERATED_KEYS);
            bindCategory(ps, request);
            return ps;
        }, keyHolder);
        return findCategory(keyHolder.getKey().longValue());
    }

    public Category updateCategory(Long id, CategoryRequest request) {
        jdbc.update("update content_categories set name=?, slug=?, description=?, cover_image=?, sort_order=?, enabled=? where id=?",
                request.name(), request.slug(), request.description(), request.coverImage(), value(request.sortOrder(), 0), bool(request.enabled()), id);
        return findCategory(id);
    }

    public Category findCategory(Long id) {
        return jdbc.query("select * from content_categories where id=?", categoryMapper(), id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(404, "分类不存在"));
    }

    public List<Banner> adminBanners() {
        return jdbc.query("select * from content_banners order by sort_order desc, id desc", bannerMapper());
    }

    public Banner createBanner(BannerRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into content_banners(title,image_url,target_type,target_value,sort_order,enabled) values(?,?,?,?,?,?)
                    """, Statement.RETURN_GENERATED_KEYS);
            bindBanner(ps, request);
            return ps;
        }, keyHolder);
        return findBanner(keyHolder.getKey().longValue());
    }

    public Banner updateBanner(Long id, BannerRequest request) {
        jdbc.update("update content_banners set title=?, image_url=?, target_type=?, target_value=?, sort_order=?, enabled=? where id=?",
                request.title(), request.imageUrl(), request.targetType(), request.targetValue(), value(request.sortOrder(), 0), bool(request.enabled()), id);
        return findBanner(id);
    }

    public Banner findBanner(Long id) {
        return jdbc.query("select * from content_banners where id=?", bannerMapper(), id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(404, "轮播图不存在"));
    }

    public DashboardStats dashboardStats() {
        Map<String, Object> row = jdbc.queryForMap("""
                select
                sum(case when status='PUBLISHED' then 1 else 0 end) published_items,
                sum(case when status='DRAFT' then 1 else 0 end) draft_items,
                (select count(*) from content_categories) categories,
                (select count(*) from content_banners) banners,
                coalesce(sum(view_count),0) total_views,
                coalesce(sum(favorite_count),0) total_favorites
                from content_items
                """);
        return new DashboardStats(num(row, "published_items"), num(row, "draft_items"), num(row, "categories"),
                num(row, "banners"), num(row, "total_views"), num(row, "total_favorites"));
    }

    private String baseItemSql() {
        return """
                select i.*, c.name category_name from content_items i
                left join content_categories c on c.id = i.category_id
                """;
    }

    private String baseItemCountSql() {
        return """
                select count(*) from content_items i
                left join content_categories c on c.id = i.category_id
                """;
    }

    private RowMapper<Category> categoryMapper() {
        return (rs, rowNum) -> new Category(rs.getLong("id"), rs.getString("name"), rs.getString("slug"),
                rs.getString("description"), rs.getString("cover_image"), rs.getInt("sort_order"),
                rs.getBoolean("enabled"), time(rs.getObject("created_at")), time(rs.getObject("updated_at")));
    }

    private RowMapper<Banner> bannerMapper() {
        return (rs, rowNum) -> new Banner(rs.getLong("id"), rs.getString("title"), rs.getString("image_url"),
                rs.getString("target_type"), rs.getString("target_value"), rs.getInt("sort_order"),
                rs.getBoolean("enabled"), time(rs.getObject("created_at")), time(rs.getObject("updated_at")));
    }

    private RowMapper<GoodItem> itemMapper() {
        return (rs, rowNum) -> new GoodItem(rs.getLong("id"), rs.getLong("category_id"), rs.getString("category_name"),
                rs.getString("title"), rs.getString("summary"), rs.getString("experience"),
                list(rs.getString("tags_json")), rs.getString("cover_image"), list(rs.getString("gallery_json")),
                rs.getString("status"), rs.getInt("sort_order"), rs.getInt("view_count"), rs.getInt("favorite_count"),
                time(rs.getObject("published_at")), time(rs.getObject("created_at")), time(rs.getObject("updated_at")));
    }

    private RowMapper<MiniProgramConfig> miniProgramConfigMapper() {
        return (rs, rowNum) -> new MiniProgramConfig(rs.getLong("id"), rs.getString("hero_eyebrow"),
                rs.getString("hero_title"), rs.getString("hero_subtitle"), rs.getString("featured_title"),
                rs.getString("search_placeholder"), list(rs.getString("hot_words_json")),
                rs.getString("me_title"), rs.getString("me_description"), time(rs.getObject("updated_at")));
    }

    private MiniProgramConfig createDefaultMiniProgramConfig() {
        MiniProgramConfigRequest request = new MiniProgramConfigRequest(
                "Good Finds",
                "好物展示小助手",
                "记录日常生活里真正顺手的小物件",
                "精选好物",
                "搜索好物、标签或体验关键词",
                List.of("收纳", "日用", "清洁", "数码"),
                "好物展示小助手",
                "这里用于浏览个人生活好物记录，是非经营性的内容展示工具。"
        );
        return updateMiniProgramConfig(request);
    }

    private void bindItem(PreparedStatement ps, GoodItemRequest request) throws java.sql.SQLException {
        ps.setLong(1, request.categoryId());
        ps.setString(2, request.title());
        ps.setString(3, request.summary());
        ps.setString(4, request.experience());
        ps.setString(5, json(request.tags()));
        ps.setString(6, request.coverImage());
        ps.setString(7, json(request.gallery()));
        ps.setString(8, status(request.status()));
        ps.setInt(9, value(request.sortOrder(), 0));
    }

    private void bindCategory(PreparedStatement ps, CategoryRequest request) throws java.sql.SQLException {
        ps.setString(1, request.name());
        ps.setString(2, request.slug());
        ps.setString(3, request.description());
        ps.setString(4, request.coverImage());
        ps.setInt(5, value(request.sortOrder(), 0));
        ps.setBoolean(6, bool(request.enabled()));
    }

    private void bindBanner(PreparedStatement ps, BannerRequest request) throws java.sql.SQLException {
        ps.setString(1, request.title());
        ps.setString(2, request.imageUrl());
        ps.setString(3, value(request.targetType(), "NONE"));
        ps.setString(4, request.targetValue());
        ps.setInt(5, value(request.sortOrder(), 0));
        ps.setBoolean(6, bool(request.enabled()));
    }

    private String json(List<String> value) {
        try {
            return mapper.writeValueAsString(value == null ? List.of() : value);
        } catch (Exception e) {
            throw new ApiException(500, "内容序列化失败");
        }
    }

    private List<String> list(String value) {
        try {
            if (value == null || value.isBlank()) {
                return List.of();
            }
            return mapper.readValue(value, STRING_LIST);
        } catch (Exception e) {
            return List.of();
        }
    }

    private LocalDateTime time(Object value) {
        return value instanceof LocalDateTime localDateTime ? localDateTime : null;
    }

    private String status(String value) {
        return value == null || value.isBlank() ? "DRAFT" : value;
    }

    private String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private int value(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private boolean bool(Boolean value) {
        return value == null || value;
    }

    private long num(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value instanceof Number number ? number.longValue() : 0;
    }
}
