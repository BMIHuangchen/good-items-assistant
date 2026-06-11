package com.gooditems.common;

import java.util.List;

public record PageResult<T>(List<T> list, long total, int pageNum, int pageSize) {
}
