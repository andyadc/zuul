package com.netflix.zuul;

import com.netflix.zuul.exception.ZuulException;

/**
 * BAse interface for ZuulFilters
 */
public interface IZuulFilter {
    
    /**
     * a "true" return from this method means that the run() method should be invoked
     *
     * @return true if the run() method should be invoked. false will not invoke the run() method
     */
    boolean shouldFilter();

    /**
     * if shouldFilter() is true, this method will be invoked. this method is the core method of a ZuulFilter
     *
     * @return Some arbitrary artifact may be returned. Current implementation ignores it.
     * @throws ZuulException if an error occurs during execution.
     */
    Object run() throws ZuulException;
}
