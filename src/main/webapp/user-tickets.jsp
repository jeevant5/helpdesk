<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<jsp:include page="/common/header.jsp" />

<div class="d-flex justify-content-between align-items-center mb-4">
    <div>
        <h3 class="fw-bold mb-1">
            <c:choose>
                <c:when test="${sessionScope.user.technician}">
                    <i class="bi bi-card-checklist text-primary me-2"></i>All Support Tickets
                </c:when>
                <c:otherwise>
                    <i class="bi bi-ticket-perforated text-primary me-2"></i>My Support Tickets
                </c:otherwise>
            </c:choose>
        </h3>
        <p class="text-muted small mb-0">Track and monitor real-time resolution status of helpdesk inquiries.</p>
    </div>
    <c:if test="${not sessionScope.user.technician}">
        <a href="${pageContext.request.contextPath}/submit-ticket" class="btn btn-primary">
            <i class="bi bi-plus-lg me-1"></i>New Ticket
        </a>
    </c:if>
</div>

<c:if test="${param.error eq 'unauthorized'}">
    <div class="alert alert-warning py-2" role="alert">
        <i class="bi bi-shield-exclamation me-2"></i>Technician privileges are required to access that dashboard.
    </div>
</c:if>

<div class="card shadow-sm">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th class="ps-4">Ticket ID</th>
                        <th>Title</th>
                        <th>Priority</th>
                        <th>Status</th>
                        <th>Created By</th>
                        <th>Assigned Technician</th>
                        <th>Created Date</th>
                        <th class="text-end pe-4">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty tickets}">
                            <c:forEach var="t" items="${tickets}">
                                <tr>
                                    <td class="ps-4 fw-semibold">#${t.ticketId}</td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/ticket-detail?id=${t.ticketId}" class="text-decoration-none fw-bold text-dark">
                                            ${t.title}
                                        </a>
                                        <c:if test="${t.hasAttachment}">
                                            <span class="badge bg-secondary ms-1" title="Contains BLOB attachment">
                                                <i class="bi bi-paperclip"></i>
                                            </span>
                                        </c:if>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${t.priority eq 'HIGH'}">
                                                <span class="badge badge-priority-high">HIGH</span>
                                            </c:when>
                                            <c:when test="${t.priority eq 'MEDIUM'}">
                                                <span class="badge badge-priority-medium">MEDIUM</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-priority-low">LOW</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${t.status eq 'OPEN'}">
                                                <span class="badge badge-status-open">OPEN</span>
                                            </c:when>
                                            <c:when test="${t.status eq 'IN_PROGRESS'}">
                                                <span class="badge badge-status-in_progress">IN PROGRESS</span>
                                            </c:when>
                                            <c:when test="${t.status eq 'RESOLVED'}">
                                                <span class="badge badge-status-resolved">RESOLVED</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-status-closed">CLOSED</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <span class="small text-muted">${t.userName}</span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty t.techName}">
                                                <span class="badge bg-light text-dark border">
                                                    <i class="bi bi-person-check me-1 text-success"></i>${t.techName}
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-light text-muted border">Unassigned</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="small text-muted">
                                        <fmt:formatDate value="${t.createdAt}" pattern="yyyy-MM-dd HH:mm" />
                                    </td>
                                    <td class="text-end pe-4">
                                        <a href="${pageContext.request.contextPath}/ticket-detail?id=${t.ticketId}" class="btn btn-sm btn-outline-primary">
                                            View Thread <i class="bi bi-chevron-right ms-1"></i>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="8" class="text-center py-5 text-muted">
                                    <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                                    <c:choose>
                                        <c:when test="${sessionScope.user.technician}">
                                            No tickets submitted in the system yet. Incoming user requests will appear here.
                                        </c:when>
                                        <c:otherwise>
                                            No tickets found. <a href="${pageContext.request.contextPath}/submit-ticket">Create a new ticket</a> to get started.
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="/common/footer.jsp" />