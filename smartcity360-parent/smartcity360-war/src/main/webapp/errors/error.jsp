<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" trimDirectiveWhitespaces="true" %>
<%
    Object sc = request.getAttribute("jakarta.servlet.error.status_code");
    request.setAttribute("pageTitle", "Unexpected error");
    request.setAttribute("active", "");
    request.setAttribute("errCode", sc == null ? "Error" : String.valueOf(sc));
    request.setAttribute("errKey", "error");
    request.setAttribute("errTitle", "Unexpected error");
    request.setAttribute("errMsg", "Something unexpected happened. Please go back or return to the home page.");
    request.setAttribute("errTone", "off");
    request.setAttribute("errActions", "home,back");
    request.setAttribute("errIco", "<circle cx=\"12\" cy=\"12\" r=\"9\"/><path d=\"M12 8h.01M11 12h1v5h1\"/>");
%>
<%@ include file="/WEB-INF/jspf/error-page.jspf" %>
