package com.netflix.zuul;

import com.netflix.zuul.filters.FilterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is one of the core classes in Zuul. It compiles, loads from a File, and checks if source code changed.
 * It also holds ZuulFilters by filterType.
 */
public class FilterLoader {
	private final static FilterLoader INSTANCE = new FilterLoader();

	private static final Logger LOG = LoggerFactory.getLogger(FilterLoader.class);
	static DynamicCodeCompiler COMPILER;
	static FilterFactory FILTER_FACTORY = new DefaultFilterFactory();
	private final Map<String, Long> filterClassLastModified = new ConcurrentHashMap<>();
	private final Map<String, String> filterClassCode = new ConcurrentHashMap<>();
	private final Map<String, String> filterCheck = new ConcurrentHashMap<>();
	private final Map<String, List<ZuulFilter>> hashFiltersByType = new ConcurrentHashMap<>();
	private FilterRegistry filterRegistry = FilterRegistry.instance();

	/**
	 * @return Singleton FilterLoader
	 */
	public static FilterLoader getInstance() {
		return INSTANCE;
	}

	/**
	 * Sets a Dynamic Code Compiler
	 *
	 * @param compiler
	 */
	public void setCompiler(DynamicCodeCompiler compiler) {
		COMPILER = compiler;
	}

	// overidden by tests
	public void setFilterRegistry(FilterRegistry r) {
		this.filterRegistry = r;
	}

	/**
	 * Sets a FilterFactory
	 *
	 * @param factory
	 */
	public void setFilterFactory(FilterFactory factory) {
		FILTER_FACTORY = factory;
	}

	/**
	 * Given source and name will compile and store the filter if it detects that the filter code has changed or
	 * the filter doesn't exist. Otherwise it will return an instance of the requested ZuulFilter
	 *
	 * @param sCode source code
	 * @param sName name of the filter
	 * @return the ZuulFilter
	 */
	public ZuulFilter getFilter(String sCode, String sName) throws Exception {

		if (filterCheck.get(sName) == null) {
			filterCheck.putIfAbsent(sName, sName);
			if (!sCode.equals(filterClassCode.get(sName))) {
				LOG.info("reloading code " + sName);
				filterRegistry.remove(sName);
			}
		}
		ZuulFilter filter = filterRegistry.get(sName);
		if (filter == null) {
			Class clazz = COMPILER.compile(sCode, sName);
			if (!Modifier.isAbstract(clazz.getModifiers())) {
				filter = FILTER_FACTORY.newInstance(clazz);
			}
		}
		return filter;
	}

	/**
	 * @return the total number of Zuul filters
	 */
	public int filterInstanceMapSize() {
		return filterRegistry.size();
	}

	/**
	 * From a file this will read the ZuulFilter source code, compile it, and add it to the list of current filters
	 * a true response means that it was successful.
	 *
	 * @param file
	 * @return true if the filter in file successfully read, compiled, verified and added to Zuul
	 */
	public boolean putFilter(File file) throws Exception {
		String sName = file.getAbsolutePath() + file.getName();
		if (filterClassLastModified.get(sName) != null && (file.lastModified() != filterClassLastModified.get(sName))) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("reloading filter " + sName);
			}
			filterRegistry.remove(sName);
		}
		ZuulFilter filter = filterRegistry.get(sName);
		if (filter == null) {
			Class clazz = COMPILER.compile(file);
			if (!Modifier.isAbstract(clazz.getModifiers())) {
				filter = FILTER_FACTORY.newInstance(clazz);
				List<ZuulFilter> list = hashFiltersByType.get(filter.filterType());
				if (list != null) {
					hashFiltersByType.remove(filter.filterType()); //rebuild this list
				}
				filterRegistry.put(file.getAbsolutePath() + file.getName(), filter);
				filterClassLastModified.put(sName, file.lastModified());
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns a list of filters by the filterType specified
	 */
	public List<ZuulFilter> getFiltersByType(String filterType) {

		List<ZuulFilter> list = hashFiltersByType.get(filterType);
		if (list != null) return list;

		list = new ArrayList<>();

		Collection<ZuulFilter> filters = filterRegistry.getAllFilters();
		for (Iterator<ZuulFilter> iterator = filters.iterator(); iterator.hasNext(); ) {
			ZuulFilter filter = iterator.next();
			if (filter.filterType().equals(filterType)) {
				list.add(filter);
			}
		}
		Collections.sort(list); // sort by priority

		hashFiltersByType.putIfAbsent(filterType, list);
		return list;
	}

	public static class TestZuulFilter extends ZuulFilter {

		public TestZuulFilter() {
			super();
		}

		@Override
		public String filterType() {
			return "test";
		}

		@Override
		public int filterOrder() {
			return 0;
		}

		public boolean shouldFilter() {
			return false;
		}

		public Object run() {
			return null;
		}
	}
}
