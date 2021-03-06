package com.andyadc.zuul.filters;

import com.andyadc.zuul.ZuulFilter;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class FilterRegistry {

    private static final FilterRegistry INSTANCE = new FilterRegistry();

	private final ConcurrentHashMap<String, ZuulFilter> filters = new ConcurrentHashMap<>();

	public static FilterRegistry instance() {
        return INSTANCE;
    }

    private FilterRegistry() {
    }

    public ZuulFilter remove(String key) {
        return this.filters.remove(key);
    }

    public ZuulFilter get(String key) {
        return this.filters.get(key);
    }

    public void put(String key, ZuulFilter filter) {
        this.filters.putIfAbsent(key, filter);
    }

    public int size() {
        return this.filters.size();
    }

    public Collection<ZuulFilter> getAllFilters() {
        return this.filters.values();
    }

}
