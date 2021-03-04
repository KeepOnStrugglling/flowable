package com.javatest.flowable.common.page;

/**
 * 条件查询父类
 */
public class PageParams {

    private Long curPage;

    private Long limit;

    private String sidx;

    private String order;

    public PageParams(Long curPage, Long limit, String sidx, String order) {
        this.curPage = curPage;
        this.limit = limit;
        this.sidx = sidx;
        this.order = order;
    }

    public Long getCurPage() {
        return curPage;
    }

    public void setCurPage(Long curPage) {
        this.curPage = curPage;
    }

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public String getSidx() {
        return sidx;
    }

    public void setSidx(String sidx) {
        this.sidx = sidx;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }
}
