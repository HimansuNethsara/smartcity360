<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" trimDirectiveWhitespaces="true" %>
<%
    request.setAttribute("pageTitle", "Sign in required");
    request.setAttribute("active", "");
    request.setAttribute("errCode", "401");
    request.setAttribute("errKey", "unauthenticated");
    request.setAttribute("errTitle", "Sign in required");
    request.setAttribute("errMsg", "You need to sign in to see this page. Your session may have expired.");
    request.setAttribute("errTone", "accent");
    request.setAttribute("errActions", "login,home");
    request.setAttribute("errIco", "<circle cx=\"8\" cy=\"15\" r=\"4\"/><path d=\"M10.8 12.2L20 3M16 7l3 3\"/>");
%>
<%@ include file="/WEB-INF/jspf/error-page.jspf" %>
