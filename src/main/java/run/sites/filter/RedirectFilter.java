package main.java.run.sites.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import main.java.run.sites.domain.Site;

import static main.java.run.sites.service.OfyService.ofy;

/**
 * Redirect to the site url.
 */
public final class RedirectFilter implements Filter {

  private FilterConfig filterConfig;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    String servletPath = ((HttpServletRequest)request).getRequestURI().toString();
    String name = servletPath.substring(1);
    System.out.println(name);
    if (name.length() > 0 && name.charAt(0) != '_') {
      Site site = ofy().load().type(Site.class).id(name).now();
      if (site != null) {
        ((HttpServletResponse)response).sendRedirect(site.getSource());
        return;
      }
    }
    filterChain.doFilter(request, response);
  }

  public FilterConfig getFilterConfig() {
    return filterConfig;
  }

  @Override
  public void init(FilterConfig filterConfig) {
    this.filterConfig = filterConfig;
  }

  @Override
  public void destroy() {
  }
}
