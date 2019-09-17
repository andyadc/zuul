package com.andyadc.zuul.filters

import com.netflix.config.DynamicBooleanProperty
import com.netflix.config.DynamicPropertyFactory
import com.netflix.config.DynamicStringProperty
import com.andyadc.zuul.ZuulFilter
import com.andyadc.zuul.constants.ZuulConstants
import com.andyadc.zuul.constants.ZuulHeaders
import com.andyadc.zuul.context.RequestContext
import com.andyadc.zuul.util.HTTPRequestUtils

/**
 * This is an abstract filter that will route requests that match the patternMatches() method to a debug Eureka "VIP" or
 * host specified by zuul.debug.vip or zuul.debug.host.
 * @author Mikey Cohen
 * Date: 6/27/12
 * Time: 12:54 PM
 */
abstract class SurgicalDebugFilter extends ZuulFilter {

    /**
     * Returning true by the pattern or logic implemented in this method will route the request to the specified origin
     * @return true if this request should be routed to the debug origin
     */
    abstract boolean patternMatches()

    @Override
    String filterType() {
        return "pre"
    }

    @Override
    int filterOrder() {
        return 99
    }

    boolean shouldFilter() {

        DynamicBooleanProperty debugFilterShutoff = DynamicPropertyFactory.getInstance().getBooleanProperty(ZuulConstants.ZUUL_DEBUGFILTERS_DISABLED, false)

        if (debugFilterShutoff.get()) return false

        if (isFilterDisabled()) return false

        String isSurgicalFilterRequest = RequestContext.currentContext.getRequest().getHeader(ZuulHeaders.X_ZUUL_SURGICAL_FILTER)
        if ("true".equals(isSurgicalFilterRequest)) return false // dont' apply filter if it was already applied
        return patternMatches()
    }

    @Override
    Object run() {
        DynamicStringProperty routeVip = DynamicPropertyFactory.getInstance().getStringProperty(ZuulConstants.ZUUL_DEBUG_VIP, null)
        DynamicStringProperty routeHost = DynamicPropertyFactory.getInstance().getStringProperty(ZuulConstants.ZUUL_DEBUG_HOST, null)

        if (routeVip.get() != null || routeHost.get() != null) {
            //RequestContext.currentContext.routeHost = routeHost.get();
            RequestContext.getCurrentContext().setRouteHost(new URL(routeHost.get()))
            //RequestContext.currentContext.routeVIP = routeVip.get();
            RequestContext.getCurrentContext().setRouteHost(new URL(routeVip.get()))
            RequestContext.currentContext.addZuulRequestHeader(ZuulHeaders.X_ZUUL_SURGICAL_FILTER, "true")
            if (HTTPRequestUtils.getInstance().getQueryParams() == null) {
                RequestContext.getCurrentContext().setRequestQueryParams(new HashMap<String, List<String>>())
            }
            HTTPRequestUtils.getInstance().getQueryParams().put("debugRequest", ["true"])
            RequestContext.currentContext.setDebugRequest(true)
            RequestContext.getCurrentContext().zuulToZuul = true
        }
    }
}
