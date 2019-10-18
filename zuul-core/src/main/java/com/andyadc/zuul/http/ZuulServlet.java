package com.andyadc.zuul.http;

import com.andyadc.zuul.ZuulRunner;
import com.andyadc.zuul.context.RequestContext;
import com.andyadc.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Core Zuul servlet which intializes and orchestrates zuulFilter execution
 */
public class ZuulServlet extends HttpServlet {

	private static final Logger logger = LoggerFactory.getLogger(ZuulServlet.class);

	private static final long serialVersionUID = -3374242278843351500L;
	private ZuulRunner zuulRunner;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		String bufferReqsStr = config.getInitParameter("buffer-requests");
		boolean bufferReqs = bufferReqsStr != null && bufferReqsStr.equals("true");

		zuulRunner = new ZuulRunner(bufferReqs);
		logger.info("ZuulServlet inited.");
	}

	@Override
	public void service(javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse) throws ServletException, IOException {
		try {
			init((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse);

			// Marks this request as having passed through the "Zuul engine", as opposed to servlets
			// explicitly bound in web.xml, for which requests will not have the same data attached
			RequestContext context = RequestContext.getCurrentContext();
			context.setZuulEngineRan();

			try {
				preRoute();
			} catch (ZuulException e) {
				error(e);
				postRoute();
				return;
			}

			try {
				route();
			} catch (ZuulException e) {
				error(e);
				postRoute();
				return;
			}

			try {
				postRoute();
			} catch (ZuulException e) {
				error(e);
				return;
			}
		} catch (Throwable e) {
			error(new ZuulException(e, 500, "UNHANDLED_EXCEPTION_" + e.getClass().getName()));
		} finally {
			RequestContext.getCurrentContext().unset();
		}
	}

	/**
	 * executes "post" ZuulFilters
	 */
	void postRoute() throws ZuulException {
		zuulRunner.postRoute();
	}

	/**
	 * executes "route" filters
	 */
	void route() throws ZuulException {
		zuulRunner.route();
	}

	/**
	 * executes "pre" filters
	 */
	void preRoute() throws ZuulException {
		zuulRunner.preRoute();
	}

	/**
	 * initializes request
	 */
	void init(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		zuulRunner.init(servletRequest, servletResponse);
	}

	/**
	 * sets error context info and executes "error" filters
	 */
	void error(ZuulException e) {
		RequestContext.getCurrentContext().setThrowable(e);
		zuulRunner.error();
	}

}
