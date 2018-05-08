package org.pricelessfestival.crossoff.server;

import lombok.extern.log4j.Log4j2;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.EnumSet;

/**
 * Created by ivan on 4/26/18.
 */
@Log4j2
class CrossoffWebServer {

    private int port;
    private Server server;

    public CrossoffWebServer(int port) {
        this.port = port;
        this.server = null;
    }

    CrossoffWebServer start() throws Exception {
        // static html
        URI resourceUri = CrossoffWebServer.class.getClassLoader().getResource("html/").toURI();
        ServletContextHandler htmlHandler = new ServletContextHandler();
        htmlHandler.setBaseResource(Resource.newResource(resourceUri));
        ServletHolder htmlHolder = new ServletHolder("default", DefaultServlet.class);
        htmlHandler.addServlet(htmlHolder, "/");

        // Jersey integration
        ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletHandler.addFilter(CrossoffWebServer.LogFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        ServletHolder jerseyServlet = servletHandler.addServlet(ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);
        // use Jackson's JAX-RS JSON support (instead of Jersey's own), and our own provider to inject ObjectMapper
        jerseyServlet.setInitParameter("jersey.config.server.provider.packages",
                "com.fasterxml.jackson.jaxrs.json," + GlobalObjectMapper.class.getPackage().getName());
        // register our one Jersey resource class
        servletHandler.setContextPath("/tickets");
        jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", TicketsResource.class.getCanonicalName());

        // start server
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { servletHandler, htmlHandler });
        server = new Server(port);
        server.setHandler(handlers);
        server.start();
        log.info("Crossoff server UI and API online at http://localhost:{}/ + listening on all interfaces", port);
        return this;
    }

    public static class LogFilter implements Filter {

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            try {
                chain.doFilter(request, response);
            } finally {
                if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
                    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                    log.info("{} {} {} : HTTP {}",
                            httpServletRequest.getRemoteAddr(),
                            httpServletRequest.getMethod(),
                            httpServletRequest.getContextPath()
                                    + httpServletRequest.getPathInfo()
                                    + (httpServletRequest.getQueryString() == null ? "" : "?" + httpServletRequest.getQueryString()),
                            httpServletResponse.getStatus());
                }
            }
        }

        @Override
        public void destroy() {
        }
    }
}
