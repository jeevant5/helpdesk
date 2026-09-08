<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<jsp:include page="/common/header.jsp" />

<div class="row justify-content-center mt-4 mb-5">
    <div class="col-md-6 col-lg-5">
        <div class="card p-4 shadow-sm">
            <div class="text-center mb-3">
                <div class="bg-success text-white d-inline-flex p-3 rounded-circle mb-2">
                    <i class="bi bi-lock-fill fs-2"></i>
                </div>
                <h3 class="fw-bold">Create New Password</h3>
                <p class="text-muted small">Choose a secure password for your account</p>
            </div>

            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger py-2 small" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-1"></i>${errorMessage}
                </div>
            </c:if>

            <c:choose>
                <c:when test="${validToken}">
                    <form action="${pageContext.request.contextPath}/reset-password" method="post">
                        <input type="hidden" name="token" value="${token}">

                        <div class="mb-3">
                            <label for="password" class="form-label fw-semibold">New Password</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-key"></i></span>
                                <input type="password" class="form-control" id="password" name="password" required 
                                       placeholder="New password (minimum 4 characters)">
                            </div>
                        </div>

                        <div class="mb-3">
                            <label for="confirmPassword" class="form-label fw-semibold">Confirm New Password</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-check-circle"></i></span>
                                <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required 
                                       placeholder="Repeat new password">
                            </div>
                        </div>

                        <button type="submit" class="btn btn-success w-100 py-2 fw-semibold">
                            <i class="bi bi-check-lg me-1"></i>Save New Password
                        </button>
                    </form>
                </c:when>
                <c:otherwise>
                    <div class="text-center my-3">
                        <a href="${pageContext.request.contextPath}/forgot-password" class="btn btn-outline-primary btn-sm">
                            <i class="bi bi-arrow-repeat me-1"></i>Request New Reset Link
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>

            <hr class="my-4">

            <div class="text-center">
                <a href="${pageContext.request.contextPath}/login" class="text-decoration-none small text-muted">
                    <i class="bi bi-arrow-left me-1"></i>Back to Sign In
                </a>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/common/footer.jsp" />