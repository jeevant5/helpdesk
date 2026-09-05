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
                        <input type="text" class="form-control" id="name" name="name" required placeholder="John Doe">
                    </div>
                </div>

                <div class="mb-3">
                    <label for="email" class="form-label fw-semibold">Email Address <span class="text-danger">*</span></label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-envelope"></i></span>
                        <input type="email" class="form-control" id="email" name="email" required placeholder="name@company.com">
                    </div>
                </div>

                <div class="mb-3">
                    <label for="password" class="form-label fw-semibold">Password <span class="text-danger">*</span></label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-key"></i></span>
                        <input type="password" class="form-control" id="password" name="password" required placeholder="Choose a password">
                    </div>
                </div>

                <div class="mb-4">
                    <label class="form-label fw-semibold d-block">Account Type <span class="text-danger">*</span></label>
                    <div class="row g-2">
                        <div class="col-6">
                            <input type="radio" class="btn-check" name="role" id="roleUser" value="USER" checked>
                            <label class="btn btn-outline-primary w-100 py-2 d-flex flex-column align-items-center" for="roleUser">
                                <i class="bi bi-person fs-4 mb-1"></i>
                                <span class="fw-bold">End-User</span>
                                <small class="text-muted" style="font-size: 0.75rem;">Submit & Track</small>
                            </label>
                        </div>
                        <div class="col-6">
                            <input type="radio" class="btn-check" name="role" id="roleTech" value="TECHNICIAN">
                            <label class="btn btn-outline-warning w-100 py-2 d-flex flex-column align-items-center" for="roleTech">
                                <i class="bi bi-tools fs-4 mb-1"></i>
                                <span class="fw-bold">Technician</span>
                                <small class="text-muted" style="font-size: 0.75rem;">Resolve & Manage</small>
                            </label>
                        </div>
                    </div>
                </div>

                <button type="submit" class="btn btn-success w-100 py-2 fw-semibold">
                    <i class="bi bi-check-circle-fill me-1"></i>Complete Registration
                </button>
            </form>

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