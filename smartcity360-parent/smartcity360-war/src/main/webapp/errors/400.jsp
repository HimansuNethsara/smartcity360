<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" trimDirectiveWhitespaces="true" %>
<%
    request.setAttribute("pageTitle", "Bad request");
    request.setAttribute("active", "");
    request.setAttribute("errCode", "400");
    request.setAttribute("errKey", "bad_request");
    request.setAttribute("errTitle", "Bad request");
    request.setAttribute("errMsg", "The server could not understand that request. Check the address or the form you submitted and try again.");
    request.setAttribute("errTone", "warn");
    request.setAttribute("errActions", "home,back");
    request.setAttribute("errIco", "<circle cx=\"12\" cy=\"12\" r=\"9\"/><path d=\"M15 9l-6 6M9 9l6 6\"/>");
%>
<%@ include file="/WEB-INF/jspf/error-page.jspf" %>
