package com.netflix.zuul;

/**
 * Interface to implement for registering a callback for each time a filter
 * is used.
 */
public interface FilterUsageNotifier {
    void notify(ZuulFilter filter, ExecutionStatus status);
}
