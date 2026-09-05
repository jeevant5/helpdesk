<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:choose>
    <c:when test="${not empty sessionScope.user}">
        <c:choose>
            <c:when test="${sessionScope.user.technician}">
                <c:redirect url="/tech-dashboard" />
            </c:when>
            <c:otherwise>
                <c:redirect url="/tickets" />
            </c:otherwise>
        </c:choose>
    </c:when>
    <c:otherwise>
        <c:redirect url="/login" />
    </c:otherwise>
</c:choose>