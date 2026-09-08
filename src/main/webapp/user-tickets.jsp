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
<c:if test="${param.error eq 'unauthorized_ticket'}">
    <div class="alert alert-danger py-2" role="alert">
        <i class="bi bi-shield-lock-fill me-2"></i>Access Denied: You are not authorized to view other users' tickets.
    </div>
</c:if>
<c:if test="${param.msg eq 'ticket_deleted'}">
    <div class="alert alert-success py-2" role="alert">
        <i class="bi bi-check-circle-fill me-2"></i>Ticket was successfully deleted.
    </div>
</c:if>

<!-- Search and Filter Bar -->
<div class="card shadow-sm mb-4">
    <div class="card-body p-3">
        <form method="get" action="${pageContext.request.contextPath}/tickets" class="row g-2 align-items-center">
            <div class="col-md-4">
                <div class="input-group">
                    <span class="input-group-text bg-light"><i class="bi bi-search"></i></span>
                    <input type="text" class="form-control" name="search" placeholder="Search title or description..." value="${searchKeyword}">
                </div>
            </div>
            <div class="col-md-3">
                <div class="input-group">
                    <label class="input-group-text bg-light small fw-semibold">Status</label>
                    <select class="form-select" name="status">
                        <option value="ALL" ${empty selectedStatus || selectedStatus eq 'ALL' ? 'selected' : ''}>All Statuses</option>
                        <option value="OPEN" ${selectedStatus eq 'OPEN' ? 'selected' : ''}>Open</option>
                        <option value="IN_PROGRESS" ${selectedStatus eq 'IN_PROGRESS' ? 'selected' : ''}>In Progress</option>
                        <option value="RESOLVED" ${selectedStatus eq 'RESOLVED' ? 'selected' : ''}>Resolved</option>
                        <option value="CLOSED" ${selectedStatus eq 'CLOSED' ? 'selected' : ''}>Closed</option>
                    </select>
                </div>
            </div>
            <div class="col-md-2">
                <div class="input-group">
                    <label class="input-group-text bg-light small fw-semibold">Priority</label>
                    <select class="form-select" name="priority">
                        <option value="ALL" ${empty selectedPriority || selectedPriority eq 'ALL' ? 'selected' : ''}>All</option>
                        <option value="HIGH" ${selectedPriority eq 'HIGH' ? 'selected' : ''}>High</option>
                        <option value="MEDIUM" ${selectedPriority eq 'MEDIUM' ? 'selected' : ''}>Medium</option>
                        <option value="LOW" ${selectedPriority eq 'LOW' ? 'selected' : ''}>Low</option>
                    </select>
                </div>
            </div>
            <div class="col-md-3 d-flex gap-2 justify-content-md-end">
                <button type="submit" class="btn btn-primary px-3">
                    <i class="bi bi-funnel-fill me-1"></i>Filter
                </button>
                <a href="${pageContext.request.contextPath}/tickets" class="btn btn-outline-secondary" title="Reset Filters">
                    <i class="bi bi-arrow-counterclockwise me-1"></i>Reset
                </a>
            </div>
        </form>
    </div>
</div>

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
                        <th>SLA Status</th>
                        <c:if test="${sessionScope.user.technician}">
                            <th>Created By</th>
                        </c:if>
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
                                        <span class="${t.slaBadgeClass} px-2 py-1 small">
                                            <i class="bi bi-clock-history me-1"></i>${t.slaStatusText}
                                        </span>
                                    </td>
                                    <c:if test="${sessionScope.user.technician}">
                                        <td>
                                            <span class="small text-muted">${t.userName}</span>
                                        </td>
                                    </c:if>
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
                                <td colspan="${sessionScope.user.technician ? 9 : 8}" class="text-center py-5 text-muted">
                                    <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                                    <c:choose>
                                        <c:when test="${sessionScope.user.technician}">
                                            No tickets found matching your criteria.
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