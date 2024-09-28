package com.lab.backend.report.dto.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * A generic class for paginated responses.
 *
 * @param <T> the type of objects contained in the paginated response
 * @author Ömer Asaf BALIKÇI
 */

@Getter
@Setter
public class PagedResponse<T> {
    private List<T> content;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
    private boolean isFirst;
    private boolean isLast;
    private boolean hasNext;
    private boolean hasPrevious;

    public PagedResponse() {
    }

    public PagedResponse(List<T> content, int currentPage, int totalPages, long totalItems, int pageSize, boolean isFirst,
                         boolean isLast, boolean hasNext, boolean hasPrevious) {
        this.content = content;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
        this.pageSize = pageSize;
        this.isFirst = isFirst;
        this.isLast = isLast;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }
}
