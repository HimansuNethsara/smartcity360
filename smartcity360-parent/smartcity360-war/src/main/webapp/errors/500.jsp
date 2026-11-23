<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" trimDirectiveWhitespaces="true" %>
<%
    request.setAttribute("pageTitle", "Something went wrong on our side");
    request.setAttribute("active", "");
    request.setAttribute("errCode", "500");
    request.setAttribute("errKey", "server_error");
    request.setAttribute("errTitle", "Something went wrong on our side");
    request.setAttribute("errMsg", "An unexpected error stopped this request. It is not your fault, and nothing you entered was lost on the server.");
    request.setAttribute("errTone", "crit");
    request.setAttribute("errActions", "retry,home");
    request.setAttribute("errIco", "<path d=\"M10.3 3.9L2 18a2 2 0 0 0 1.7 3h16.6A2 2 0 0 0 22 18L13.7 3.9a2 2 0 0 0-3.4 0z\"/><path d=\"M12 9v4M12 17h.01\"/>");
%>
<%@ include file="/WEB-INF/jspf/error-page.jspf" %>
