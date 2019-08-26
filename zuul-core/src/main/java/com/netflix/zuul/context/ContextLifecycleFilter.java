package com.netflix.zuul.context;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Manages Zuul <code>RequestContext</code> lifecycle.
 */
public class ContextLifecycleFilter implements Filter {

	public void destroy() {
	}

	public void init(FilterConfig filterConfig) {
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
		throws IOException, ServletException {
		try {
			chain.doFilter(req, res);
		} finally {
			RequestContext.getCurrentContext().unset();
		}
	}
}
