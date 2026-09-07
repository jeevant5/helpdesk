<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<jsp:include page="/common/header.jsp" />

<div class="d-flex justify-content-between align-items-center mb-4">
    <div>
        <h2 class="fw-bold mb-1"><i class="bi bi-speedometer2 text-warning me-2"></i>Technician Command Center</h2>
        <p class="text-muted small mb-0">Real-time aggregate ticket metrics, unassigned queue, and technician work log.</p>
    </div>
    <div class="d-flex gap-2">
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
        <i class="bi bi-check-circle-fill me-2"></i>Ticket successfully claimed and assigned to you!
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<!-- Real-time Summary Cards using Aggregate Queries (COUNT(*) GROUP BY status) -->
<div class="row g-3 mb-4">
    <div class="col-6 col-md-3">
        <div class="card stat-card bg-primary text-white p-3">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <span class="text-uppercase small fw-semibold opacity-75">Open Tickets</span>
                    <h2 class="fw-bold mb-0 mt-1">${openCount}</h2>
                </div>
                <div class="bg-white bg-opacity-25 rounded-circle p-3">
                    <i class="bi bi-envelope-open fs-3"></i>
                </div>
            </div>
        </div>
    </div>

    <div class="col-6 col-md-3">
        <div class="card stat-card bg-warning text-dark p-3">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <span class="text-uppercase small fw-semibold opacity-75">In Progress</span>
                    <h2 class="fw-bold mb-0 mt-1">${inProgressCount}</h2>
                </div>
                <div class="bg-dark bg-opacity-10 rounded-circle p-3">
                    <i class="bi bi-gear-wide-connected fs-3"></i>
                </div>
            </div>
        </div>
    </div>

    <div class="col-6 col-md-3">
        <div class="card stat-card bg-success text-white p-3">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <span class="text-uppercase small fw-semibold opacity-75">Resolved</span>
                    <h2 class="fw-bold mb-0 mt-1">${resolvedCount}</h2>
                </div>
                <div class="bg-white bg-opacity-25 rounded-circle p-3">
                    <i class="bi bi-check2-circle fs-3"></i>
                </div>
            </div>
        </div>
    </div>

    <div class="col-6 col-md-3">
        <div class="card stat-card bg-secondary text-white p-3">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <span class="text-uppercase small fw-semibold opacity-75">Total Volume</span>
                    <h2 class="fw-bold mb-0 mt-1">${totalTickets}</h2>
                </div>
                <div class="bg-white bg-opacity-25 rounded-circle p-3">
                    <i class="bi bi-stack fs-3"></i>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Unassigned Queue (Technicians assign tickets to themselves) -->
<div class="card shadow-sm mb-4">
    <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
        <div class="d-flex align-items-center gap-2">
            <span class="badge bg-danger rounded-pill">${unassignedTickets.size()}</span>
            <h5 class="fw-bold mb-0">Unassigned Ticket Queue (Ready to Claim)</h5>
        </div>
        <span class="small text-muted">Claim tickets to begin resolution</span>
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
                        <th>Reported By</th>
                        <th>Logged At</th>
                        <th class="text-end pe-4">Action</th>
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
                                        <div>${t.userName}</div>
                                        <div class="small text-muted">${t.userEmail}</div>
                                    </td>
                                    <td class="small text-muted">
                                        <fmt:formatDate value="${t.createdAt}" pattern="yyyy-MM-dd HH:mm" />
                                    </td>
                                    <td class="text-end pe-4">
                                        <form action="${pageContext.request.contextPath}/assign-ticket" method="post" class="d-inline">
                                            <input type="hidden" name="ticketId" value="${t.ticketId}">
                                            <button type="submit" class="btn btn-warning btn-sm fw-semibold">
                                                <i class="bi bi-person-plus-fill me-1"></i>Assign to Me
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="7" class="text-center py-4 text-muted">
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

<!-- My Assigned Tickets Section -->
<div class="card shadow-sm">
    <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
        <h5 class="fw-bold mb-0"><i class="bi bi-person-badge text-primary me-2"></i>My Active Assignments (${myAssignedTickets.size()})</h5>
        <span class="small text-muted">Tickets assigned to ${sessionScope.user.name}</span>
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
                        <th>User</th>
                        <th>Logged At</th>
                        <th class="text-end pe-4">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty myAssignedTickets}">
                            <c:forEach var="t" items="${myAssignedTickets}">
                                <tr>
                                    <td class="ps-4 fw-bold">#${t.ticketId}</td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/ticket-detail?id=${t.ticketId}" class="fw-semibold text-dark text-decoration-none">
                                            ${t.title}
                                        </a>
                                    </td>
                                    <td>
                                        <span class="badge ${t.priority eq 'HIGH' ? 'badge-priority-high' : (t.priority eq 'MEDIUM' ? 'badge-priority-medium' : 'badge-priority-low')}">
                                            ${t.priority}
                                        </span>
                                    </td>
                                    <td>
                                        <span class="badge ${t.status eq 'OPEN' ? 'badge-status-open' : (t.status eq 'IN_PROGRESS' ? 'badge-status-in_progress' : 'badge-status-resolved')}">
                                            ${t.status}
                                        </span>
                                    </td>
                                    <td>${t.userName}</td>
                                    <td class="small text-muted">
                                        <fmt:formatDate value="${t.createdAt}" pattern="yyyy-MM-dd HH:mm" />
                                    </td>
                                    <td class="text-end pe-4">
                                        <a href="${pageContext.request.contextPath}/ticket-detail?id=${t.ticketId}" class="btn btn-outline-primary btn-sm">
                                            Manage & Resolve <i class="bi bi-arrow-right ms-1"></i>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="7" class="text-center py-4 text-muted">
                                    You currently have no tickets assigned to you. Claim an unassigned ticket from the queue above.
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