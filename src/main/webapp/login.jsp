<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<jsp:include page="/common/header.jsp" />

<div class="row justify-content-center mt-4">
    <div class="col-md-6 col-lg-5">
        <div class="card p-4 shadow-sm">
            <div class="text-center mb-4">
                <div class="bg-primary text-white d-inline-flex p-3 rounded-circle mb-2">
                    <i class="bi bi-shield-lock fs-2"></i>
                </div>
                <h3 class="fw-bold">Helpdesk Portal</h3>
                <p class="text-muted">Sign in to manage and resolve support tickets</p>
            </div>

            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger py-2" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>${errorMessage}
                </div>
            </c:if>

            <c:if test="${param.msg eq 'logged_out'}">
                <div class="alert alert-success py-2" role="alert">
                    <i class="bi bi-check-circle-fill me-2"></i>You have been logged out successfully.
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/login" method="post" id="loginForm">
                <div class="mb-3">
                    <label for="email" class="form-label fw-semibold">Email address</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-envelope"></i></span>
                        <input type="email" class="form-control" id="email" name="email" required placeholder="name@company.com" value="alex.tech@company.com">
                    </div>
                </div>

                <div class="mb-3">
                    <label for="password" class="form-label fw-semibold">Password</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-key"></i></span>
                        <input type="password" class="form-control" id="password" name="password" required placeholder="Password" value="tech123">
                    </div>
                </div>

                <button type="submit" class="btn btn-primary w-100 py-2 fw-semibold">
                    <i class="bi bi-box-arrow-in-right me-1"></i>Sign In
                </button>
            </form>

            <hr class="my-4">

            <div class="text-center">
                <p class="small text-muted mb-2">Quick Demo Accounts (Click to Autofill):</p>
                <div class="d-flex justify-content-center gap-2">
                    <button type="button" class="btn btn-outline-warning btn-sm" onclick="setDemo('alex.tech@company.com', 'tech123')">
                        <i class="bi bi-wrench-adjustable me-1"></i>Technician Demo
                    </button>
                    <button type="button" class="btn btn-outline-secondary btn-sm" onclick="setDemo('john@example.com', 'user123')">
                        <i class="bi bi-person me-1"></i>End-User Demo
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
function setDemo(email, pass) {
    document.getElementById('email').value = email;
    document.getElementById('password').value = pass;
}
</script>

<jsp:include page="/common/footer.jsp" />