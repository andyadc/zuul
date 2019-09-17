package com.andyadc.zuul;

import com.andyadc.zuul.context.Debug;
import com.andyadc.zuul.context.RequestContext;
import com.andyadc.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This the the core class to execute filters.
 */
public class FilterProcessor {

	private static final Logger logger = LoggerFactory.getLogger(FilterProcessor.class);
	private static FilterProcessor INSTANCE = new FilterProcessor();

	private FilterUsageNotifier usageNotifier;

	public FilterProcessor() {
		usageNotifier = new BasicFilterUsageNotifier();
	}

	/**
	 * @return the singleton FilterProcessor
	 */
	public static FilterProcessor getInstance() {
		return INSTANCE;
	}

	/**
	 * sets a singleton processor in case of a need to override default behavior
	 *
	 * @param processor
	 */
	public static void setProcessor(FilterProcessor processor) {
		INSTANCE = processor;
	}

	/**
	 * Override the default filter usage notification impl.
	 */
	public void setFilterUsageNotifier(FilterUsageNotifier notifier) {
		this.usageNotifier = notifier;
	}

	/**
	 * runs "post" filters which are called after "route" filters. ZuulExceptions from ZuulFilters are thrown.
	 * Any other Throwables are caught and a ZuulException is thrown out with a 500 status code
	 */
	public void postRoute() throws ZuulException {
		try {
			runFilters("post");
		} catch (ZuulException e) {
			throw e;
		} catch (Throwable e) {
			throw new ZuulException(e, 500, "UNCAUGHT_EXCEPTION_IN_POST_FILTER_" + e.getClass().getName());
		}
	}

	/**
	 * runs all "error" filters. These are called only if an exception occurs. Exceptions from this are swallowed and logged so as not to bubble up.
	 */
	public void error() {
		try {
			runFilters("error");
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Runs all "route" filters. These filters route calls to an origin.
	 */
	public void route() throws ZuulException {
		try {
			runFilters("route");
		} catch (ZuulException e) {
			throw e;
		} catch (Throwable e) {
			throw new ZuulException(e, 500, "UNCAUGHT_EXCEPTION_IN_ROUTE_FILTER_" + e.getClass().getName());
		}
	}

	/**
	 * runs all "pre" filters. These filters are run before routing to the orgin.
	 */
	public void preRoute() throws ZuulException {
		try {
			runFilters("pre");
		} catch (ZuulException e) {
			throw e;
		} catch (Throwable e) {
			throw new ZuulException(e, 500, "UNCAUGHT_EXCEPTION_IN_PRE_FILTER_" + e.getClass().getName());
		}
	}

	/**
	 * runs all filters of the filterType sType/ Use this method within filters to run custom filters by type
	 *
	 * @param sType the filterType.
	 */
	public Object runFilters(String sType) throws Throwable {
		if (RequestContext.getCurrentContext().debugRouting()) {
			Debug.addRoutingDebug("Invoking {" + sType + "} type filters");
		}
		boolean bResult = false;
		List<ZuulFilter> list = FilterLoader.getInstance().getFiltersByType(sType);
		if (list != null) {
			for (ZuulFilter zuulFilter : list) {
				Object result = processZuulFilter(zuulFilter);
				if (result instanceof Boolean) {
					bResult |= ((Boolean) result);
				}
			}
		}
		return bResult;
	}

	/**
	 * Processes an individual ZuulFilter. This method adds Debug information. Any uncaught Thowables are caught by this method and converted to a ZuulException with a 500 status code.
	 *
	 * @return the return value for that filter
	 */
	public Object processZuulFilter(ZuulFilter filter) throws ZuulException {

		RequestContext ctx = RequestContext.getCurrentContext();
		boolean bDebug = ctx.debugRouting();
		final String metricPrefix = "zuul.filter-";
		long execTime = 0;
		String filterName = "";
		try {
			long ltime = System.currentTimeMillis();
			filterName = filter.getClass().getSimpleName();

			RequestContext copy = null;
			Object o = null;
			Throwable t = null;

			if (bDebug) {
				Debug.addRoutingDebug("Filter " + filter.filterType() + " " + filter.filterOrder() + " " + filterName);
				copy = ctx.copy();
			}

			ZuulFilterResult result = filter.runFilter();
			ExecutionStatus s = result.getStatus();
			execTime = System.currentTimeMillis() - ltime;

			switch (s) {
				case FAILED:
					t = result.getException();
					ctx.addFilterExecutionSummary(filterName, ExecutionStatus.FAILED.name(), execTime);
					break;
				case SUCCESS:
					o = result.getResult();
					ctx.addFilterExecutionSummary(filterName, ExecutionStatus.SUCCESS.name(), execTime);
					if (bDebug) {
						Debug.addRoutingDebug("Filter {" + filterName + " TYPE:" + filter.filterType() + " ORDER:" + filter.filterOrder() + "} Execution time = " + execTime + "ms");
						Debug.compareContextState(filterName, copy);
					}
					break;
				default:
					break;
			}

			if (t != null) throw t;

			usageNotifier.notify(filter, s);
			return o;
		} catch (Throwable e) {
			if (bDebug) {
				Debug.addRoutingDebug("Running Filter failed " + filterName + " type:" + filter.filterType() + " order:" + filter.filterOrder() + " " + e.getMessage());
			}
			usageNotifier.notify(filter, ExecutionStatus.FAILED);
			if (e instanceof ZuulException) {
				throw (ZuulException) e;
			} else {
				ZuulException ex = new ZuulException(e, "Filter threw Exception", 500, filter.filterType() + ":" + filterName);
				ctx.addFilterExecutionSummary(filterName, ExecutionStatus.FAILED.name(), execTime);
				throw ex;
			}
		}
	}

	/**
	 * Publishes a counter metric for each filter on each use.
	 */
	public static class BasicFilterUsageNotifier implements FilterUsageNotifier {
		private static final String METRIC_PREFIX = "zuul.filter-";

		@Override
		public void notify(ZuulFilter filter, ExecutionStatus status) {
//            DynamicCounter.increment(METRIC_PREFIX + filter.getClass().getSimpleName(), "status", status.name(), "filtertype", filter.filterType());
		}
	}
}
