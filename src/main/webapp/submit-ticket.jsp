<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<jsp:include page="/common/header.jsp" />

<div class="row justify-content-center">
    <div class="col-lg-8">
        <div class="d-flex align-items-center justify-content-between mb-3">
            <div>
                <h3 class="fw-bold mb-1"><i class="bi bi-pencil-square text-primary me-2"></i>Create New Support Ticket</h3>
                <p class="text-muted small mb-0">Describe the issue encountered and attach logs or screenshots for our technical support team.</p>
            </div>
            <a href="${pageContext.request.contextPath}/tickets" class="btn btn-outline-secondary btn-sm">
                <i class="bi bi-arrow-left me-1"></i>Back to Tickets
            </a>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger py-2" role="alert">
                <i class="bi bi-exclamation-octagon-fill me-2"></i>${errorMessage}
            </div>
        </c:if>

        <div class="card p-4 shadow-sm">
            <form action="${pageContext.request.contextPath}/submit-ticket" method="post" enctype="multipart/form-data">
                
                <div class="mb-3">
                    <label for="title" class="form-label fw-semibold">Ticket Subject / Title <span class="text-danger">*</span></label>
                    <input type="text" class="form-control" id="title" name="title" required maxlength="150" 
                           placeholder="E.g., VPN connection fails with error code 800">
                </div>

                <div class="row mb-3">
                    <div class="col-md-6">
                        <label for="priority" class="form-label fw-semibold">Priority Level <span class="text-danger">*</span></label>
                        <select class="form-select" id="priority" name="priority" required>
                            <option value="LOW">🟢 Low (Minor inquiry, general feedback)</option>
                            <option value="MEDIUM" selected>🟠 Medium (Service degraded, workaround available)</option>
                            <option value="HIGH">🔴 High (Critical outage, work blocked)</option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label for="attachment" class="form-label fw-semibold">
                            Attachment <span class="text-muted small">(Log files or screenshots, saved to DB as BLOB)</span>
                        </label>
                        <input class="form-control" type="file" id="attachment" name="attachment" accept="image/*,.txt,.log,.pdf,.zip">
                    </div>
                </div>

                <div class="mb-4">
                    <label for="description" class="form-label fw-semibold">Detailed Description <span class="text-danger">*</span></label>
                    <textarea class="form-control" id="description" name="description" rows="6" required 
                              placeholder="Provide step-by-step reproduction details, error logs, or environment context..."></textarea>
                </div>

                <div class="d-flex justify-content-end gap-2">
                    <a href="${pageContext.request.contextPath}/tickets" class="btn btn-light px-4">Cancel</a>
                    <button type="submit" class="btn btn-primary px-4 fw-semibold">
                        <i class="bi bi-send-fill me-1"></i>Submit Ticket
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="/common/footer.jsp" />