<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" trimDirectiveWhitespaces="true" %>
<%
    request.setAttribute("pageTitle", "Temporarily unavailable");
    request.setAttribute("active", "");
    request.setAttribute("errCode", "503");
    request.setAttribute("errKey", "unavailable");
    request.setAttribute("errTitle", "Temporarily unavailable");
    request.setAttribute("errMsg", "SmartCity360 is busy or under maintenance. Please wait a moment and try again.");
    request.setAttribute("errTone", "off");
    request.setAttribute("errActions", "retry,home");
    request.setAttribute("errIco", "<circle cx=\"12\" cy=\"12\" r=\"9\"/><path d=\"M12 7v5l3 2\"/>");
%>
<%@ include file="/WEB-INF/jspf/error-page.jspf" %>
