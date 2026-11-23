package lk.jiat.smartcity360.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Locale;

@WebFilter(urlPatterns = "/*")
public class CleanUrlFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());
        String lower = path.toLowerCase(Locale.ROOT);
        boolean read = "GET".equals(req.getMethod()) || "HEAD".equals(req.getMethod());

        // Judge the address the browser asked for, not the file the server mapped it to: opening "/" is mapped to
        // /index.jsp internally (welcome file) and must not be redirected back to "/" again.
        String asked = req.getRequestURI().toLowerCase(Locale.ROOT);
        if (read && asked.endsWith(".jsp") && lower.endsWith(".jsp") && !lower.startsWith("/web-inf/")) {
            String clean = lower.equals("/index.jsp") ? "/" : path.substring(0, path.length() - 4);
            String query = req.getQueryString();
            resp.sendRedirect(req.getContextPath() + clean + (query == null ? "" : "?" + query));
            return;
        }

        if (read && isCleanPage(lower)) {
            String target = path + ".jsp";
            if (exists(req.getServletContext(), target)) {
                req.getRequestDispatcher(target).forward(request, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    static boolean isCleanPage(String lowerPath) {
        if (lowerPath == null || lowerPath.length() < 2 || lowerPath.endsWith("/")) {
            return false;
        }
        if (lowerPath.startsWith("/api/") || lowerPath.startsWith("/css/") || lowerPath.startsWith("/js/")
                || lowerPath.startsWith("/images/") || lowerPath.startsWith("/web-inf") || lowerPath.startsWith("/meta-inf")) {
            return false;
        }
        String last = lowerPath.substring(lowerPath.lastIndexOf('/') + 1);
        return !last.isEmpty() && last.indexOf('.') < 0;
    }

    private static boolean exists(ServletContext context, String path) {
        try {
            return context.getResource(path) != null;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}