<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" trimDirectiveWhitespaces="true" %>
<%
    request.setAttribute("pageTitle", "Page not found");
    request.setAttribute("active", "");
    request.setAttribute("errCode", "404");
    request.setAttribute("errKey", "not_found");
    request.setAttribute("errTitle", "Page not found");
    request.setAttribute("errMsg", "We could not find that page. The address may be mistyped, or the page may have moved.");
    request.setAttribute("errTone", "warn");
    request.setAttribute("errActions", "home,back");
    request.setAttribute("errIco", "<circle cx=\"11\" cy=\"11\" r=\"7\"/><path d=\"M21 21l-4.3-4.3\"/><path d=\"M8.5 11h5\"/>");
%>
<%@ include file="/WEB-INF/jspf/error-page.jspf" %>
