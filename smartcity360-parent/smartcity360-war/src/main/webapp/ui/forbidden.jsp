<%@ page contentType="text/html;charset=UTF-8" language="java" trimDirectiveWhitespaces="true" %>
<%
    response.setStatus(403);                       // reached by redirect from PageGuardFilter, so say 403 explicitly
    request.setAttribute("pageTitle", "Access denied");
    request.setAttribute("active", "");
    request.setAttribute("errCode", "403");
    request.setAttribute("errKey", "forbidden");
    request.setAttribute("errTitle", "Access denied");
    request.setAttribute("errMsg", "Your role does not have permission to open this page. If you think this is a mistake, ask an administrator to review your access.");
    request.setAttribute("errTone", "crit");
    request.setAttribute("errActions", "home,back");
    request.setAttribute("errIco", "<rect x=\"4\" y=\"11\" width=\"16\" height=\"10\" rx=\"2\"/><path d=\"M8 11V7a4 4 0 0 1 8 0v4\"/><path d=\"M12 15v2\"/>");
%>
<%@ include file="/WEB-INF/jspf/error-page.jspf" %>
