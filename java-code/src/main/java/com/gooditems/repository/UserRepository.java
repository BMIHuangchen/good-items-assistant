package com.gooditems.repository;

import com.gooditems.dto.AnalyticsOverviewResponse;
import com.gooditems.dto.BehaviorEventRequest;
import com.gooditems.dto.MiniUsageResponse;
import com.gooditems.exception.ApiException;
import com.gooditems.model.GoodItem;
import com.gooditems.model.MiniUser;
import com.gooditems.model.UserAiUsage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;
    private final ContentRepository contentRepository;

    public UserRepository(JdbcTemplate jdbc, ContentRepository contentRepository) {
        this.jdbc = jdbc;
        this.contentRepository = contentRepository;
    }

    public MiniUser upsertLogin(String openid, String unionid, String nickname, String avatarUrl, String requestId) {
        jdbc.update("""
                insert into mini_users(openid, unionid, nickname, avatar_url, status, login_count, first_login_at, last_login_at)
                values(?,?,?,?, 'ACTIVE', 1, now(), now())
                on duplicate key update unionid=coalesce(values(unionid), unionid),
                nickname=coalesce(nullif(values(nickname), ''), nickname),
                avatar_url=coalesce(nullif(values(avatar_url), ''), avatar_url),
                login_count=login_count+1, last_login_at=now(), updated_at=now()
                """, openid, unionid, nickname, avatarUrl);
        MiniUser user = findByOpenid(openid);
        jdbc.update("""
                insert into user_login_events(user_id, openid, request_id, created_at)
                values(?,?,?,now())
                """, user.id(), user.openid(), requestId);
        return user;
    }

    public MiniUser requireActive(Long userId) {
        MiniUser user = findById(userId);
        if (!"ACTIVE".equals(user.status())) {
            throw new ApiException(403, "当前用户不可使用该功能");
        }
        return user;
    }

    public MiniUser findById(Long id) {
        return jdbc.query("select * from mini_users where id = ?", userMapper(), id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(401, "请先登录后再使用"));
    }

    public MiniUser findByOpenid(String openid) {
        return jdbc.query("select * from mini_users where openid = ?", userMapper(), openid)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(404, "用户不存在"));
    }

    public List<MiniUser> users(int pageSize) {
        return jdbc.query("select * from mini_users order by last_login_at desc, id desc limit ?", userMapper(), pageSize);
    }

    public void recordEvent(Long userId, BehaviorEventRequest request, String requestId) {
        jdbc.update("""
                insert into user_behavior_events(user_id, event_type, target_type, target_id, page_path, detail, request_id)
                values(?,?,?,?,?,?,?)
                """, userId, value(request.eventType(), "UNKNOWN"), request.targetType(), request.targetId(),
                request.pagePath(), request.detail(), requestId);
    }

    public void addFavorite(Long userId, Long itemId) {
        contentRepository.publicItemWithoutViewIncrement(itemId);
        int rows = jdbc.update("""
                insert ignore into user_favorites(user_id, item_id, created_at)
                values(?,?,now())
                """, userId, itemId);
        if (rows > 0) {
            jdbc.update("update content_items set favorite_count = favorite_count + 1 where id = ?", itemId);
        }
    }

    public void removeFavorite(Long userId, Long itemId) {
        int rows = jdbc.update("delete from user_favorites where user_id = ? and item_id = ?", userId, itemId);
        if (rows > 0) {
            jdbc.update("update content_items set favorite_count = greatest(favorite_count - 1, 0) where id = ?", itemId);
        }
    }

    public List<GoodItem> favorites(Long userId) {
        return jdbc.query("""
                select i.*, c.name category_name
                from user_favorites f
                join content_items i on i.id = f.item_id
                left join content_categories c on c.id = i.category_id
                where f.user_id = ? and i.status = 'PUBLISHED'
                order by f.created_at desc
                """, contentRepository.itemRowMapper(), userId);
    }

    public MiniUsageResponse miniUsage(Long userId) {
        return new MiniUsageResponse(
                usageSummary("and created_at >= curdate()", userId),
                usageSummary("and created_at >= date_format(curdate(), '%Y-%m-01')", userId),
                jdbc.query("""
                        select provider_code, model_name, count(*) call_count, coalesce(sum(total_tokens),0) total_tokens,
                        coalesce(sum(estimated_cost),0) estimated_cost
                        from ai_call_logs where user_id = ?
                        group by provider_code, model_name
                        order by call_count desc
                        """, (rs, rowNum) -> new MiniUsageResponse.ModelUsage(rs.getString("provider_code"),
                        rs.getString("model_name"), rs.getLong("call_count"), rs.getLong("total_tokens"),
                        rs.getBigDecimal("estimated_cost")), userId),
                jdbc.query("""
                        select t.id, t.provider_code, t.status, t.item_title, m.public_url media_url, t.created_at
                        from ai_image_analysis_tasks t
                        left join media_assets m on m.id = t.media_asset_id
                        where t.user_id = ?
                        order by t.created_at desc limit 10
                        """, (rs, rowNum) -> new MiniUsageResponse.RecentAiTask(rs.getLong("id"),
                        rs.getString("provider_code"), rs.getString("status"), rs.getString("item_title"),
                        rs.getString("media_url"), String.valueOf(rs.getObject("created_at"))), userId)
        );
    }

    public List<UserAiUsage> userAiUsage(int pageSize) {
        return jdbc.query("""
                select u.id user_id, u.openid, u.nickname,
                count(l.id) call_count,
                sum(case when l.status='SUCCESS' then 1 else 0 end) success_count,
                sum(case when l.status<>'SUCCESS' then 1 else 0 end) failed_count,
                coalesce(sum(l.prompt_tokens),0) prompt_tokens,
                coalesce(sum(l.completion_tokens),0) completion_tokens,
                coalesce(sum(l.total_tokens),0) total_tokens,
                coalesce(sum(l.estimated_cost),0) estimated_cost,
                coalesce(avg(l.duration_ms),0) avg_duration_ms
                from mini_users u
                left join ai_call_logs l on l.user_id = u.id
                group by u.id, u.openid, u.nickname
                order by total_tokens desc, call_count desc, u.last_login_at desc
                limit ?
                """, userAiUsageMapper(), pageSize);
    }

    public AnalyticsOverviewResponse analyticsOverview() {
        Map<String, Object> row = jdbc.queryForMap("""
                select
                (select count(*) from mini_users) total_users,
                (select count(*) from user_login_events where created_at >= curdate()) today_logins,
                (select count(distinct user_id) from user_behavior_events where created_at >= curdate()) today_active_users,
                (select count(*) from user_behavior_events) total_behavior_events,
                (select count(*) from ai_call_logs) total_ai_calls,
                (select coalesce(sum(total_tokens),0) from ai_call_logs) total_ai_tokens,
                (select coalesce(sum(estimated_cost),0) from ai_call_logs) total_ai_estimated_cost
                """);
        return new AnalyticsOverviewResponse(
                num(row, "total_users"),
                num(row, "today_logins"),
                num(row, "today_active_users"),
                num(row, "total_behavior_events"),
                num(row, "total_ai_calls"),
                num(row, "total_ai_tokens"),
                decimal(row, "total_ai_estimated_cost"),
                trend("user_login_events", "created_at"),
                trend("ai_call_logs", "created_at"),
                nameValues("select event_type name, count(*) value from user_behavior_events group by event_type order by value desc limit 8"),
                nameValues("select provider_code name, count(*) value from ai_call_logs group by provider_code order by value desc limit 8"),
                nameValues("""
                        select i.title name, count(*) value
                        from user_behavior_events e
                        join content_items i on i.id = cast(e.target_id as unsigned)
                        where e.target_type='ITEM' and e.target_id regexp '^[0-9]+$'
                        group by i.title order by value desc limit 8
                        """)
        );
    }

    private MiniUsageResponse.Summary usageSummary(String timeCondition, Long userId) {
        Map<String, Object> row = jdbc.queryForMap("""
                select count(*) call_count,
                sum(case when status='SUCCESS' then 1 else 0 end) success_count,
                coalesce(sum(total_tokens),0) total_tokens,
                coalesce(sum(estimated_cost),0) estimated_cost
                from ai_call_logs where user_id = ?
                """ + " " + timeCondition, userId);
        return new MiniUsageResponse.Summary(num(row, "call_count"), num(row, "success_count"),
                num(row, "total_tokens"), decimal(row, "estimated_cost"));
    }

    private List<AnalyticsOverviewResponse.TrendPoint> trend(String table, String column) {
        String sql = """
                select date(%s) date_value, count(*) value
                from %s
                where %s >= date_sub(curdate(), interval 13 day)
                group by date(%s)
                order by date_value
                """.formatted(column, table, column, column);
        return jdbc.query(sql, (rs, rowNum) -> new AnalyticsOverviewResponse.TrendPoint(
                String.valueOf(rs.getObject("date_value")), rs.getLong("value")));
    }

    private List<AnalyticsOverviewResponse.NameValue> nameValues(String sql) {
        return jdbc.query(sql, (rs, rowNum) -> new AnalyticsOverviewResponse.NameValue(rs.getString("name"), rs.getLong("value")));
    }

    private RowMapper<MiniUser> userMapper() {
        return (rs, rowNum) -> new MiniUser(rs.getLong("id"), rs.getString("openid"), rs.getString("unionid"),
                rs.getString("nickname"), rs.getString("avatar_url"), rs.getString("status"), rs.getInt("login_count"),
                time(rs.getObject("first_login_at")), time(rs.getObject("last_login_at")),
                time(rs.getObject("created_at")), time(rs.getObject("updated_at")));
    }

    private RowMapper<UserAiUsage> userAiUsageMapper() {
        return (rs, rowNum) -> new UserAiUsage(rs.getLong("user_id"), mask(rs.getString("openid")),
                rs.getString("nickname"), rs.getLong("call_count"), rs.getLong("success_count"),
                rs.getLong("failed_count"), rs.getLong("prompt_tokens"), rs.getLong("completion_tokens"),
                rs.getLong("total_tokens"), rs.getBigDecimal("estimated_cost"), rs.getLong("avg_duration_ms"));
    }

    private LocalDateTime time(Object value) {
        return value instanceof LocalDateTime localDateTime ? localDateTime : null;
    }

    private String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private long num(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value instanceof Number number ? number.longValue() : 0;
    }

    private BigDecimal decimal(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.ZERO;
    }

    private String mask(String openid) {
        if (openid == null || openid.length() <= 8) {
            return "****";
        }
        return openid.substring(0, 4) + "****" + openid.substring(openid.length() - 4);
    }
}
