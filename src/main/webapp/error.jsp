<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<jsp:include page="/common/header.jsp" />

<div class="row justify-content-center my-5">
    <div class="col-md-8 col-lg-6">
        <div class="card shadow border-0 rounded-4 overflow-hidden">
            <div class="card-header bg-danger text-white py-4 text-center">
                <i class="bi bi-shield-slash-fill fs-1 d-block mb-2"></i>
                <h4 class="fw-bold mb-0">Error Processing Request</h4>
                <p class="small mb-0 text-white-50">An unexpected system condition was encountered</p>
            </div>
            <div class="card-body p-4 text-center">
                <div class="alert alert-danger bg-danger-subtle border-danger-subtle text-danger py-3 mb-4 rounded-3 text-start">
                    <div class="d-flex align-items-center">
                        <i class="bi bi-exclamation-octagon-fill fs-4 me-3"></i>
                        <div>
                            <div class="fw-semibold">
                                <c:choose>
                                    <c:when test="${not empty userFriendlyMessage}">
                                        ${userFriendlyMessage}
                                    </c:when>
                                    <c:otherwise>
                                        Error processing request. Please try again later.
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="small text-muted mt-1">Our technical team has been automatically alerted of this incident.</div>
                        </div>
                    </div>
                </div>

                <!-- Incident Tracking Reference -->
                <div class="bg-light p-3 rounded-3 mb-4 text-start small">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-muted fw-semibold">Tracking Reference:</span>
                        <span class="badge bg-secondary font-monospace">
                            <c:out value="${incidentId != null ? incidentId : 'ERR-SYSTEM-FAILOVER'}" />
                        </span>
                    </div>
                    <c:if test="${not empty incidentTimestamp}">
                        <div class="d-flex justify-content-between align-items-center">
                            <span class="text-muted fw-semibold">Logged Timestamp:</span>
                            <span class="text-muted">${incidentTimestamp}</span>
                        </div>
                    </c:if>
                </div>

                <div class="d-flex gap-2 justify-content-center">
                    <c:choose>
                        <c:when test="${sessionScope.user.technician}">
                            <a href="${pageContext.request.contextPath}/tech-dashboard" class="btn btn-primary px-4">
                                <i class="bi bi-speedometer2 me-1"></i>Return to Dashboard
                            </a>
                        </c:when>
                        <c:when test="${not empty sessionScope.user}">
                            <a href="${pageContext.request.contextPath}/tickets" class="btn btn-primary px-4">
                                <i class="bi bi-card-checklist me-1"></i>My Tickets
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/login" class="btn btn-primary px-4">
                                <i class="bi bi-box-arrow-in-right me-1"></i>Sign In
                            </a>
                        </c:otherwise>
                    </c:choose>
                    <a href="javascript:history.back()" class="btn btn-outline-secondary px-3">
                        <i class="bi bi-arrow-left me-1"></i>Go Back
                    </a>
                </div>
            </div>
            <div class="card-footer bg-light text-center py-2 text-muted small">
                Helpdesk Ticket Resolution System &bull; Enterprise Reliability Module
            </div>
        </div>
    </div>
</div>

<jsp:include page="/common/footer.jsp" />