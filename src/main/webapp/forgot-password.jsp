<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<jsp:include page="/common/header.jsp" />

<div class="row justify-content-center mt-4 mb-5">
    <div class="col-md-6 col-lg-5">
        <div class="card p-4 shadow-sm">
            <c:choose>
                <c:when test="${step eq 2}">
                    <div class="text-center mb-3">
                        <div class="bg-primary text-white d-inline-flex p-3 rounded-circle mb-2">
                            <i class="bi bi-shield-lock-fill fs-2"></i>
                        </div>
                        <h3 class="fw-bold">Security Verification</h3>
                        <p class="text-muted small">Answer your confidential security question to reset your password.</p>
                    </div>

                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger py-2 small" role="alert">
                            <i class="bi bi-exclamation-triangle-fill me-1"></i>${errorMessage}
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/forgot-password" method="post">
                        <input type="hidden" name="action" value="reset_with_question">
                        <input type="hidden" name="email" value="<c:out value='${email}'/>">
                        <input type="hidden" name="securityQuestion" value="<c:out value='${securityQuestion}'/>">

                        <div class="mb-3">
                            <label class="form-label text-muted small fw-semibold mb-1">Recovering Account:</label>
                            <div class="input-group input-group-sm">
                                <span class="input-group-text"><i class="bi bi-person-check-fill text-success"></i></span>
                                <input type="text" class="form-control bg-light" value="<c:out value='${email}'/>" readonly>
                            </div>
                        </div>

                        <div class="alert alert-primary py-2 px-3 mb-3">
                            <small class="fw-bold text-uppercase" style="font-size: 0.72rem; letter-spacing: 0.5px;">Registered Security Question:</small>
                            <div class="fw-bold text-primary mt-1">
                                <i class="bi bi-patch-question-fill me-1"></i><c:out value="${securityQuestion}"/>
                            </div>
                        </div>

                        <div class="mb-3">
                            <label for="securityAnswer" class="form-label fw-semibold">Your Security Answer <span class="text-danger">*</span></label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-key"></i></span>
                                <input type="text" class="form-control" id="securityAnswer" name="securityAnswer" required 
                                       placeholder="Enter your security answer" autocomplete="off" autofocus>
                            </div>
                        </div>

                        <div class="mb-3">
                            <label for="newPassword" class="form-label fw-semibold">New Password <span class="text-danger">*</span></label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-lock"></i></span>
                                <input type="password" class="form-control" id="newPassword" name="newPassword" required minlength="4" 
                                       placeholder="At least 4 characters">
                            </div>
                        </div>

                        <div class="mb-4">
                            <label for="confirmPassword" class="form-label fw-semibold">Confirm New Password <span class="text-danger">*</span></label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-lock-fill"></i></span>
                                <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required minlength="4" 
                                       placeholder="Re-type new password">
                            </div>
                        </div>

                        <button type="submit" class="btn btn-success w-100 py-2 fw-semibold mb-2">
                            <i class="bi bi-check-circle-fill me-1"></i>Reset Password & Sign In
                        </button>
                    </form>

                    <div class="text-center mt-3">
                        <a href="${pageContext.request.contextPath}/forgot-password" class="text-decoration-none small text-muted">
                            <i class="bi bi-arrow-left me-1"></i>Use a different email address
                        </a>
                    </div>
                </c:when>

                <c:otherwise>
                    <div class="text-center mb-3">
                        <div class="bg-primary text-white d-inline-flex p-3 rounded-circle mb-2">
                            <i class="bi bi-key-fill fs-2"></i>
                        </div>
                        <h3 class="fw-bold">Forgot Password</h3>
                        <p class="text-muted small">Enter your registered email address to verify your security question.</p>
                    </div>

                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger py-2 small" role="alert">
                            <i class="bi bi-exclamation-triangle-fill me-1"></i>${errorMessage}
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/forgot-password" method="post">
                        <div class="mb-3">
                            <label for="email" class="form-label fw-semibold">Email Address</label>
                            <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-envelope"></i></span>
                                <input type="email" class="form-control" id="email" name="email" required 
                                       placeholder="name@company.com" value="${email}">
                            </div>
                        </div>

                        <!-- Security CAPTCHA -->
                        <div class="mb-3">
                            <label class="form-label fw-semibold">Security Verification (CAPTCHA)</label>
                            <div class="d-flex align-items-center gap-2 mb-2">
                                <img id="captchaImg" src="${pageContext.request.contextPath}/captcha?t=<%= System.currentTimeMillis() %>" 
                                     alt="Security CAPTCHA" class="border rounded shadow-sm" style="height: 48px; width: 160px; object-fit: cover;">
                                <button type="button" class="btn btn-outline-secondary btn-sm" onclick="refreshCaptcha()" title="Refresh Code">
                                    <i class="bi bi-arrow-clockwise fs-5"></i>
                                </button>
                            </div>
                            <input type="text" class="form-control" name="captcha" required maxlength="6" 
                                   placeholder="Enter 5 characters shown above" autocomplete="off">
                        </div>

                        <button type="submit" class="btn btn-primary w-100 py-2 fw-semibold">
                            <i class="bi bi-shield-check me-1"></i>Continue to Security Question
                        </button>
                    </form>

                    <hr class="my-4">

                    <div class="text-center">
                        <a href="${pageContext.request.contextPath}/login" class="text-decoration-none small text-muted">
                            <i class="bi bi-arrow-left me-1"></i>Back to Sign In
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<script>
function refreshCaptcha() {
    var img = document.getElementById('captchaImg');
    img.src = '${pageContext.request.contextPath}/captcha?t=' + new Date().getTime();
}
</script>

<jsp:include page="/common/footer.jsp" />