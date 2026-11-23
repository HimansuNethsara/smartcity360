<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" trimDirectiveWhitespaces="true" %>
<%
    request.setAttribute("pageTitle", "Action not allowed");
    request.setAttribute("active", "");
    request.setAttribute("errCode", "405");
    request.setAttribute("errKey", "method_not_allowed");
    request.setAttribute("errTitle", "Action not allowed");
    request.setAttribute("errMsg", "That page does not accept this kind of request. Go back and use the buttons on the page instead.");
    request.setAttribute("errTone", "warn");
    request.setAttribute("errActions", "home,back");
    request.setAttribute("errIco", "<circle cx=\"12\" cy=\"12\" r=\"9\"/><path d=\"M5.6 5.6l12.8 12.8\"/>");
%>
<%@ include file="/WEB-INF/jspf/error-page.jspf" %>
