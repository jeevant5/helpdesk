<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<jsp:include page="/common/header.jsp" />

<div class="d-flex justify-content-between align-items-center mb-4">
    <div>
        <c:choose>
            <c:when test="${sessionScope.user.admin}">
                <h2 class="fw-bold mb-1"><i class="bi bi-shield-lock-fill text-primary me-2"></i>Administrator Command Center</h2>
                <p class="text-muted small mb-0">System oversight: Real-time ticket volume, resolution statistics, technician assignments, and system management.</p>
            </c:when>
            <c:otherwise>
                <h2 class="fw-bold mb-1"><i class="bi bi-speedometer2 text-warning me-2"></i>Technician Command Center</h2>
                <p class="text-muted small mb-0">Real-time aggregate ticket metrics, unassigned queue, and technician work log.</p>
            </c:otherwise>
        </c:choose>
    </div>
    <div class="d-flex gap-2">
        <c:if test="${sessionScope.user.admin}">
            <button type="button" class="btn btn-outline-danger btn-sm" data-bs-toggle="modal" data-bs-target="#purgeAllModal">
                <i class="bi bi-trash3 me-1"></i>Purge All Tickets
            </button>
        </c:if>
        <a href="${pageContext.request.contextPath}/export-tickets" class="btn btn-outline-success btn-sm">
            <i class="bi bi-filetype-csv me-1"></i>Export All (CSV)
        </a>
        <a href="${pageContext.request.contextPath}/tech-dashboard" class="btn btn-outline-secondary btn-sm">
            <i class="bi bi-arrow-clockwise me-1"></i>Refresh Data
        </a>
    </div>
</div>

<c:if test="${param.error eq 'unauthorized_create'}">
    <div class="alert alert-warning alert-dismissible fade show py-2" role="alert">
        <i class="bi bi-shield-lock-fill me-2"></i>Technicians resolve support tickets and cannot open new tickets. Please use an End-User account to submit requests.
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<c:if test="${param.msg eq 'assigned'}">
    <div class="alert alert-success alert-dismissible fade show py-2" role="alert">
        <i class="bi bi-check-circle-fill me-2"></i>Ticket successfully assigned to technician!
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<c:if test="${param.msg eq 'all_purged'}">
    <div class="alert alert-success alert-dismissible fade show py-2" role="alert">
        <i class="bi bi-check-circle-fill me-2"></i>All tickets have been successfully purged from the database (${not empty param.count ? param.count : 'all'} tickets removed).
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<c:if test="${param.msg eq 'ticket_deleted'}">
    <div class="alert alert-success alert-dismissible fade show py-2" role="alert">
        <i class="bi bi-check-circle-fill me-2"></i>Ticket was successfully deleted.
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<c:if test="${param.error eq 'delete_failed' or param.error eq 'purge_failed'}">
    <div class="alert alert-danger alert-dismissible fade show py-2" role="alert">
        <i class="bi bi-exclamation-triangle-fill me-2"></i>Failed to complete the ticket deletion operation. Please try again.
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<!-- Real-time Summary Cards using Aggregate Queries (COUNT(*) GROUP BY status) -->
<div class="row row-cols-2 row-cols-md-5 g-3 mb-4">
    <div class="col">
        <div class="card stat-card bg-primary text-white p-3 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <span class="text-uppercase small fw-semibold opacity-75">Open Tickets</span>
                    <h2 class="fw-bold mb-0 mt-1">${openCount}</h2>
                </div>
                <div class="bg-white bg-opacity-25 rounded-circle p-2 p-md-3">
                    <i class="bi bi-envelope-open fs-4"></i>
                </div>
            </div>
        </div>
    </div>

    <div class="col">
        <div class="card stat-card bg-warning text-dark p-3 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <span class="text-uppercase small fw-semibold opacity-75">In Progress</span>
                    <h2 class="fw-bold mb-0 mt-1">${inProgressCount}</h2>
                </div>
                <div class="bg-dark bg-opacity-10 rounded-circle p-2 p-md-3">
                    <i class="bi bi-gear-wide-connected fs-4"></i>
                </div>
            </div>
        </div>
    </div>

    <div class="col">
        <div class="card stat-card bg-success text-white p-3 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <span class="text-uppercase small fw-semibold opacity-75">Resolved</span>
                    <h2 class="fw-bold mb-0 mt-1">${resolvedCount}</h2>
                </div>
                <div class="bg-white bg-opacity-25 rounded-circle p-2 p-md-3">
                    <i class="bi bi-check2-circle fs-4"></i>
                </div>
            </div>
        </div>
    </div>

    <div class="col">
        <div class="card stat-card bg-secondary text-white p-3 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <span class="text-uppercase small fw-semibold opacity-75">Closed</span>
                    <h2 class="fw-bold mb-0 mt-1">${closedCount}</h2>
                </div>
                <div class="bg-white bg-opacity-25 rounded-circle p-2 p-md-3">
                    <i class="bi bi-archive-fill fs-4"></i>
                </div>
            </div>
        </div>
    </div>

    <div class="col">
        <div class="card stat-card bg-dark text-white p-3 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <span class="text-uppercase small fw-semibold opacity-75">Total Volume</span>
                    <h2 class="fw-bold mb-0 mt-1">${totalTickets}</h2>
                </div>
                <div class="bg-white bg-opacity-25 rounded-circle p-2 p-md-3">
                    <i class="bi bi-stack fs-4"></i>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Unassigned Queue (Admin assigns to techs, Techs claim tickets) -->
<div class="card shadow-sm mb-4">
    <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
        <div class="d-flex align-items-center gap-2">
            <span class="badge bg-danger rounded-pill">${unassignedTickets.size()}</span>
            <h5 class="fw-bold mb-0">Unassigned Ticket Queue</h5>
        </div>
        <span class="small text-muted">
            ${sessionScope.user.admin ? 'Assign tickets to available technicians' : 'Claim tickets to begin resolution'}
        </span>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th class="ps-4">ID</th>
                        <th>Subject</th>
                        <th>Priority</th>
                        <th>Status</th>
                        <th>SLA Status</th>
                        <th>Reported By</th>
                        <th>Logged At</th>
                        <th class="text-end pe-4">${sessionScope.user.admin ? 'Assign Technician' : 'Action'}</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty unassignedTickets}">
                            <c:forEach var="t" items="${unassignedTickets}">
                                <tr>
                                    <td class="ps-4 fw-bold">#${t.ticketId}</td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/ticket-detail?id=${t.ticketId}" class="fw-semibold text-dark text-decoration-none">
                                            ${t.title}
                                        </a>
                                        <c:if test="${t.hasAttachment}">
                                            <i class="bi bi-paperclip text-muted ms-1" title="Attachment included"></i>
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
                                    <td><span class="badge badge-status-open">${t.status}</span></td>
                                    <td>
                                        <span class="${t.slaBadgeClass} px-2 py-1 small">
                                            <i class="bi bi-clock-history me-1"></i>${t.slaStatusText}
                                        </span>
                                    </td>
                                    <td>
                                        <div>${t.userName}</div>
                                        <div class="small text-muted">${t.userEmail}</div>
                                    </td>
                                    <td class="small text-muted">
                                        <fmt:formatDate value="${t.createdAt}" pattern="yyyy-MM-dd HH:mm" />
                                    </td>
                                    <td class="text-end pe-4">
                                        <div class="d-inline-flex gap-1 align-items-center">
                                            <c:choose>
                                                <c:when test="${sessionScope.user.admin}">
                                                    <form action="${pageContext.request.contextPath}/assign-ticket" method="post" class="d-inline-flex gap-1 align-items-center">
                                                        <input type="hidden" name="ticketId" value="${t.ticketId}">
                                                        <select name="techId" class="form-select form-select-sm" required style="min-width: 140px;">
                                                            <option value="" disabled selected>Assign Tech...</option>
                                                            <c:forEach var="tech" items="${technicians}">
                                                                <option value="${tech.userId}">${tech.name}</option>
                                                            </c:forEach>
                                                        </select>
                                                        <button type="submit" class="btn btn-primary btn-sm fw-semibold" title="Assign to Technician">
                                                            <i class="bi bi-person-check-fill me-1"></i>Assign
                                                        </button>
                                                    </form>
                                                    <form action="${pageContext.request.contextPath}/ticket-delete" method="post" class="d-inline" onsubmit="return confirm('Permanently delete ticket #${t.ticketId}?');">
                                                        <input type="hidden" name="action" value="deleteSingle">
                                                        <input type="hidden" name="ticketId" value="${t.ticketId}">
                                                        <button type="submit" class="btn btn-outline-danger btn-sm" title="Delete Ticket">
                                                            <i class="bi bi-trash"></i>
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <form action="${pageContext.request.contextPath}/assign-ticket" method="post" class="d-inline">
                                                        <input type="hidden" name="ticketId" value="${t.ticketId}">
                                                        <button type="submit" class="btn btn-warning btn-sm fw-semibold">
                                                            <i class="bi bi-person-plus-fill me-1"></i>Assign to Me
                                                        </button>
                                                    </form>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="8" class="text-center py-4 text-muted">
                                    <i class="bi bi-check-all text-success fs-3 d-block mb-1"></i>
                                    All tickets have been claimed! No pending unassigned tickets.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Assigned Tickets Section -->
<div class="card shadow-sm">
    <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
        <c:choose>
            <c:when test="${sessionScope.user.admin}">
                <h5 class="fw-bold mb-0"><i class="bi bi-people-fill text-primary me-2"></i>All Assigned Tickets by Technician (${assignedTickets.size()})</h5>
                <span class="small text-muted">Real-time technician assignments and resolution status</span>
            </c:when>
            <c:otherwise>
                <h5 class="fw-bold mb-0"><i class="bi bi-person-badge text-primary me-2"></i>My Active Assignments (${myAssignedTickets.size()})</h5>
                <span class="small text-muted">Tickets assigned to ${sessionScope.user.name}</span>
            </c:otherwise>
        </c:choose>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th class="ps-4">ID</th>
                        <th>Subject</th>
                        <c:if test="${sessionScope.user.admin}">
                            <th>Assigned Technician</th>
                        </c:if>
                        <th>Priority</th>
                        <th>Status</th>
                        <th>SLA Status</th>
                        <th>User</th>
                        <th>Logged At</th>
                        <th class="text-end pe-4">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty assignedTickets}">
                            <c:forEach var="t" items="${assignedTickets}">
                                <tr>
                                    <td class="ps-4 fw-bold">#${t.ticketId}</td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/ticket-detail?id=${t.ticketId}" class="fw-semibold text-dark text-decoration-none">
                                            ${t.title}
                                        </a>
                                    </td>
                                    <c:if test="${sessionScope.user.admin}">
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty t.techName}">
                                                    <span class="badge bg-light text-dark border px-2 py-1">
                                                        <i class="bi bi-person-badge-fill text-primary me-1"></i>${t.techName}
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-light text-muted border">Unassigned</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </c:if>
                                    <td>
                                        <span class="badge ${t.priority eq 'HIGH' ? 'badge-priority-high' : (t.priority eq 'MEDIUM' ? 'badge-priority-medium' : 'badge-priority-low')}">
                                            ${t.priority}
                                        </span>
                                    </td>
                                    <td>
                                        <span class="badge ${t.status eq 'OPEN' ? 'badge-status-open' : (t.status eq 'IN_PROGRESS' ? 'badge-status-in_progress' : (t.status eq 'RESOLVED' ? 'badge-status-resolved' : 'badge-status-closed'))}">
                                            ${t.status}
                                        </span>
                                    </td>
                                    <td>
                                        <span class="${t.slaBadgeClass} px-2 py-1 small">
                                            <i class="bi bi-clock-history me-1"></i>${t.slaStatusText}
                                        </span>
                                    </td>
                                    <td>${t.userName}</td>
                                    <td class="small text-muted">
                                        <fmt:formatDate value="${t.createdAt}" pattern="yyyy-MM-dd HH:mm" />
                                    </td>
                                    <td class="text-end pe-4">
                                        <div class="d-inline-flex gap-1">
                                            <c:choose>
                                                <c:when test="${sessionScope.user.admin || t.status eq 'RESOLVED' || t.status eq 'CLOSED'}">
                                                    <a href="${pageContext.request.contextPath}/ticket-detail?id=${t.ticketId}" class="btn btn-outline-secondary btn-sm">
                                                        View Details <i class="bi bi-arrow-right ms-1"></i>
                                                    </a>
                                                </c:when>
                                                <c:otherwise>
                                                    <a href="${pageContext.request.contextPath}/ticket-detail?id=${t.ticketId}" class="btn btn-outline-primary btn-sm">
                                                        Manage & Resolve <i class="bi bi-arrow-right ms-1"></i>
                                                    </a>
                                                </c:otherwise>
                                            </c:choose>
                                            <c:if test="${sessionScope.user.admin}">
                                                <form action="${pageContext.request.contextPath}/ticket-delete" method="post" class="d-inline" onsubmit="return confirm('Permanently delete ticket #${t.ticketId}?');">
                                                    <input type="hidden" name="action" value="deleteSingle">
                                                    <input type="hidden" name="ticketId" value="${t.ticketId}">
                                                    <button type="submit" class="btn btn-outline-danger btn-sm" title="Delete Ticket">
                                                        <i class="bi bi-trash"></i>
                                                    </button>
                                                </form>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="${sessionScope.user.admin ? 9 : 8}" class="text-center py-4 text-muted">
                                    <c:choose>
                                        <c:when test="${sessionScope.user.admin}">
                                            No assigned tickets found. Use the unassigned queue above to assign tickets to technicians.
                                        </c:when>
                                        <c:otherwise>
                                            You currently have no tickets assigned to you. Claim an unassigned ticket from the queue above.
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

<!-- Purge All Tickets Modal (Admin Only) -->
<c:if test="${sessionScope.user.admin}">
    <div class="modal fade" id="purgeAllModal" tabindex="-1" aria-labelledby="purgeAllModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <form action="${pageContext.request.contextPath}/ticket-delete" method="post">
                    <input type="hidden" name="action" value="purgeAll">
                    <div class="modal-header bg-danger text-white">
                        <h5 class="modal-title" id="purgeAllModalLabel">
                            <i class="bi bi-exclamation-triangle-fill me-2"></i>Purge All Tickets
                        </h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <p class="mb-2 fw-semibold text-danger">Are you sure you want to permanently delete ALL tickets?</p>
                        <p class="text-muted small mb-0">
                            This operation will permanently remove all tickets, attachment files, and discussion comment history from the database. 
                            This action <strong>cannot</strong> be undone.
                        </p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-danger">
                            <i class="bi bi-trash3 me-1"></i>Yes, Purge All Tickets
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</c:if>

<jsp:include page="/common/footer.jsp" />