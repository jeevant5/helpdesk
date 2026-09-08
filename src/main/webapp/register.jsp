<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<jsp:include page="/common/header.jsp" />

<div class="row justify-content-center mt-3 mb-5">
    <div class="col-md-7 col-lg-6">
        <div class="card p-4 shadow-sm">
            <div class="text-center mb-4">
                <div class="bg-success text-white d-inline-flex p-3 rounded-circle mb-2">
                    <i class="bi bi-person-plus-fill fs-2"></i>
                </div>
                <h3 class="fw-bold">Create an Account</h3>
                <p class="text-muted">Register as an End-User or Support Technician</p>
            </div>

            <c:if test="${not empty sessionScope.user}">
                <div class="alert alert-info py-2 d-flex align-items-center justify-content-between mb-3" role="alert">
                    <div>
                        <i class="bi bi-info-circle-fill me-2"></i>
                        Signed in as <strong><c:out value="${sessionScope.user.name}"/></strong> (<c:out value="${sessionScope.user.email}"/>).
                    </div>
                    <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-danger btn-sm">Sign Out</a>
                </div>
            </c:if>

            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger py-2" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>${errorMessage}
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/register" method="post" id="registerForm">
                <div class="mb-3">
                    <label for="name" class="form-label fw-semibold">Full Name <span class="text-danger">*</span></label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-person"></i></span>
                        <input type="text" class="form-control" id="name" name="name" required placeholder="John Doe" value="<c:out value='${name}'/>">
                    </div>
                </div>

                <div class="mb-3">
                    <label for="email" class="form-label fw-semibold">Email Address <span class="text-danger">*</span></label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-envelope"></i></span>
                        <input type="email" class="form-control" id="email" name="email" required placeholder="name@company.com" value="<c:out value='${email}'/>">
                    </div>
                </div>

                <div class="mb-3">
                    <label for="password" class="form-label fw-semibold">Password <span class="text-danger">*</span></label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-key"></i></span>
                        <input type="password" class="form-control" id="password" name="password" required minlength="4" placeholder="Choose a password (min. 4 characters)">
                    </div>
                </div>

                <div class="mb-4">
                    <label class="form-label fw-semibold d-block">Account Type <span class="text-danger">*</span></label>
                    <div class="row g-2">
                        <div class="col-6">
                            <input type="radio" class="btn-check" name="role" id="roleUser" value="USER" ${empty selectedRole or selectedRole eq 'USER' ? 'checked' : ''}>
                            <label class="btn btn-outline-primary w-100 py-2 d-flex flex-column align-items-center" for="roleUser">
                                <i class="bi bi-person fs-4 mb-1"></i>
                                <span class="fw-bold">End-User</span>
                                <small class="text-muted" style="font-size: 0.75rem;">Submit & Track</small>
                            </label>
                        </div>
                        <div class="col-6">
                            <input type="radio" class="btn-check" name="role" id="roleTech" value="TECHNICIAN" ${selectedRole eq 'TECHNICIAN' ? 'checked' : ''}>
                            <label class="btn btn-outline-warning w-100 py-2 d-flex flex-column align-items-center" for="roleTech">
                                <i class="bi bi-tools fs-4 mb-1"></i>
                                <span class="fw-bold">Technician</span>
                                <small class="text-muted" style="font-size: 0.75rem;">Resolve & Manage</small>
                            </label>
                        </div>
                    </div>
                </div>

                <!-- Password Recovery Security Question -->
                <div class="card p-3 mb-4 bg-light border">
                    <h6 class="fw-bold mb-2 text-primary">
                        <i class="bi bi-shield-lock-fill me-1"></i>Account Recovery Setup
                    </h6>
                    <p class="text-muted small mb-3">
                        Choose a security question to easily reset your password if you ever forget it.
                    </p>

                    <div class="mb-3">
                        <label for="securityQuestion" class="form-label fw-semibold">Security Question <span class="text-danger">*</span></label>
                        <select class="form-select" id="securityQuestion" name="securityQuestion" required>
                            <option value="What was the name of your first pet?" ${selectedQuestion eq 'What was the name of your first pet?' ? 'selected' : ''}>What was the name of your first pet?</option>
                            <option value="What is your mother's maiden name?" ${selectedQuestion eq "What is your mother's maiden name?" ? 'selected' : ''}>What is your mother's maiden name?</option>
                            <option value="What was the name of your first school?" ${selectedQuestion eq 'What was the name of your first school?' ? 'selected' : ''}>What was the name of your first school?</option>
                            <option value="What city were you born in?" ${selectedQuestion eq 'What city were you born in?' ? 'selected' : ''}>What city were you born in?</option>
                            <option value="What is your favorite movie or book?" ${selectedQuestion eq 'What is your favorite movie or book?' ? 'selected' : ''}>What is your favorite movie or book?</option>
                        </select>
                    </div>

                    <div class="mb-1">
                        <label for="securityAnswer" class="form-label fw-semibold">Security Answer <span class="text-danger">*</span></label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="bi bi-patch-question"></i></span>
                            <input type="text" class="form-control" id="securityAnswer" name="securityAnswer" required 
                                   placeholder="Enter your confidential answer" value="<c:out value='${securityAnswer}'/>">
                        </div>
                        <small class="text-muted" style="font-size: 0.75rem;">Your answer is case-insensitive and used only for password recovery.</small>
                    </div>
                </div>

                <!-- Security CAPTCHA -->
                <div class="mb-4">
                    <label class="form-label fw-semibold">Security Verification (CAPTCHA) <span class="text-danger">*</span></label>
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

                <button type="submit" class="btn btn-success w-100 py-2 fw-semibold">
                    <i class="bi bi-check-circle-fill me-1"></i>Complete Registration
                </button>
            </form>

            <script>
            function refreshCaptcha() {
                var img = document.getElementById('captchaImg');
                img.src = '${pageContext.request.contextPath}/captcha?t=' + new Date().getTime();
            }
            </script>

            <hr class="my-4">

            <div class="text-center">
                <p class="small text-muted mb-0">
                    Already have an account? 
                    <a href="${pageContext.request.contextPath}/login" class="fw-bold text-primary text-decoration-none">Sign In here</a>
                </p>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/common/footer.jsp" />