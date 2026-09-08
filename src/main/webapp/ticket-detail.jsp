<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<jsp:include page="/common/header.jsp" />

<div class="row g-4">
    <!-- Left Column: Ticket Overview & Discussion Thread -->
    <div class="col-lg-8">
        <c:if test="${not empty param.feedbackSuccess}">
            <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i>Thank you! Your feedback has been recorded successfully.
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <div class="card p-4 shadow-sm mb-4">
            <div class="d-flex justify-content-between align-items-start mb-3">
                <div>
                    <span class="badge bg-secondary mb-2">Ticket #${ticket.ticketId}</span>
                    <h3 class="fw-bold mb-1">${ticket.title}</h3>
                    <p class="text-muted small mb-0">
                        Submitted by <span class="fw-semibold text-dark">${ticket.userName}</span> (${ticket.userEmail}) 
                        on <fmt:formatDate value="${ticket.createdAt}" pattern="MMM dd, yyyy HH:mm" />
                    </p>
                </div>
                <div class="text-end">
                    <c:choose>
                        <c:when test="${ticket.status eq 'OPEN'}">
                            <span class="badge badge-status-open fs-6 px-3 py-2">OPEN</span>
                        </c:when>
                        <c:when test="${ticket.status eq 'IN_PROGRESS'}">
                            <span class="badge badge-status-in_progress fs-6 px-3 py-2">IN PROGRESS</span>
                        </c:when>
                        <c:when test="${ticket.status eq 'RESOLVED'}">
                            <span class="badge badge-status-resolved fs-6 px-3 py-2">RESOLVED</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge badge-status-closed fs-6 px-3 py-2">CLOSED</span>
                        </c:otherwise>
                    </c:choose>
                    <div class="mt-2">
                        <span class="${ticket.slaBadgeClass} px-2 py-1 small">
                            <i class="bi bi-clock-history me-1"></i>${ticket.slaStatusText}
                        </span>
                    </div>
                </div>
            </div>

            <hr>

            <div class="mb-3">
                <h6 class="text-uppercase text-muted fw-bold small">Issue Description</h6>
                <div class="bg-light p-3 rounded-3 mt-2" style="white-space: pre-wrap;">${ticket.description}</div>
            </div>

            <!-- BLOB Attachment Display / Download -->
            <c:if test="${ticket.hasAttachment}">
                <div class="card border bg-light mt-3 p-3">
                    <div class="d-flex justify-content-between align-items-center">
                        <div class="d-flex align-items-center gap-2">
                            <i class="bi bi-paperclip fs-4 text-primary"></i>
                            <div>
                                <div class="fw-semibold">${ticket.attachmentName}</div>
                                <div class="small text-muted">Stored directly in database as BLOB (${ticket.attachmentType})</div>
                            </div>
                        </div>
                        <a href="${pageContext.request.contextPath}/attachment?id=${ticket.ticketId}" 
                           target="_blank" class="btn btn-outline-primary btn-sm">
                            <i class="bi bi-box-arrow-up-right me-1"></i>View / Download Attachment
                        </a>
                    </div>
                </div>
            </c:if>
        </div>

        <!-- Ticket Discussion Thread -->
        <div class="card p-4 shadow-sm">
            <h4 class="fw-bold mb-3"><i class="bi bi-chat-left-text text-primary me-2"></i>Discussion Thread</h4>

            <c:if test="${param.msg eq 'updated'}">
                <div class="alert alert-success alert-dismissible fade show py-2" role="alert">
                    <i class="bi bi-check-circle-fill me-2"></i>Discussion update posted successfully.
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>
            <c:if test="${param.error eq 'ticket_closed'}">
                <div class="alert alert-warning alert-dismissible fade show py-2" role="alert">
                    <i class="bi bi-lock-fill me-2"></i>This ticket has been closed. Discussion thread is locked and replies are disabled.
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>

            <div class="thread-container mb-4">
                <c:choose>
                    <c:when test="${not empty comments}">
                        <c:forEach var="c" items="${comments}">
                            <div class="thread-bubble ${c.authorRole eq 'TECHNICIAN' || c.authorRole eq 'ADMIN' ? 'thread-bubble-tech' : 'thread-bubble-user'}">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <div>
                                        <span class="fw-bold">${c.authorName}</span>
                                        <span class="badge ${c.authorRole eq 'TECHNICIAN' || c.authorRole eq 'ADMIN' ? 'bg-warning text-dark' : 'bg-primary'} ms-1 small">
                                            ${c.authorRole}
                                        </span>
                                    </div>
                                    <span class="small text-muted">
                                        <fmt:formatDate value="${c.createdAt}" pattern="yyyy-MM-dd HH:mm" />
                                    </span>
                                </div>
                                <div style="white-space: pre-wrap;" class="text-secondary-emphasis">${c.commentText}</div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="text-center py-4 text-muted bg-light rounded-3">
                            <i class="bi bi-chat-dots fs-3 d-block mb-1"></i>
                            No updates posted yet. Add a message below to join the discussion.
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- Post New Comment & Dynamically Update Status -->
            <c:choose>
                <c:when test="${ticket.status eq 'CLOSED' and not sessionScope.user.technician}">
                    <div class="alert alert-secondary py-3 text-center mb-0" role="alert">
                        <i class="bi bi-lock-fill me-2 fs-5 text-secondary"></i>
                        <span class="fw-semibold">This ticket is CLOSED. The discussion thread is locked and replies are disabled.</span>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="card border-0 bg-light p-3 rounded-3">
                        <h6 class="fw-bold mb-2">Add Reply / Resolution Update</h6>
                        <form action="${pageContext.request.contextPath}/ticket-comment" method="post">
                            <input type="hidden" name="ticketId" value="${ticket.ticketId}">

                            <div class="mb-3">
                                <textarea class="form-control" name="commentText" rows="4" required 
                                          placeholder="Type your response, troubleshooting questions, or resolution notes..."></textarea>
                            </div>

                            <div class="row align-items-center g-2">
                                <div class="col-md-7">
                                    <div class="input-group">
                                        <label class="input-group-text bg-white small fw-semibold" for="newStatus">
                                            <i class="bi bi-arrow-repeat me-1"></i>Change Status:
                                        </label>
                                        <select class="form-select" id="newStatus" name="newStatus">
                                            <option value="NO_CHANGE" selected>-- Keep Current Status (${ticket.status}) --</option>
                                            <c:if test="${ticket.status ne 'OPEN'}">
                                                <option value="OPEN">Mark as OPEN</option>
                                            </c:if>
                                            <c:if test="${ticket.status ne 'IN_PROGRESS'}">
                                                <option value="IN_PROGRESS">Mark as IN_PROGRESS</option>
                                            </c:if>
                                            <c:if test="${ticket.status ne 'RESOLVED' and ticket.status ne 'CLOSED'}">
                                                <option value="RESOLVED">Mark as RESOLVED</option>
                                            </c:if>
                                            <c:if test="${ticket.status ne 'CLOSED'}">
                                                <option value="CLOSED">Mark as CLOSED</option>
                                            </c:if>
                                        </select>
                                    </div>
                                </div>
                                <div class="col-md-5 text-md-end">
                                    <button type="submit" class="btn btn-primary px-4 fw-semibold w-100 w-md-auto">
                                        <i class="bi bi-send-check me-1"></i>Post Update
                                    </button>
                                </div>
                            </div>
                        </form>
                    </div>
                </c:otherwise>
            </c:choose>

            <!-- Customer Satisfaction (CSAT) Section -->
            <c:if test="${ticket.status eq 'RESOLVED' || ticket.status eq 'CLOSED'}">
                <c:choose>
                    <c:when test="${not empty feedback}">
                        <div class="card border border-success-subtle bg-success-subtle bg-opacity-10 p-4 rounded-3 mt-4">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <h6 class="fw-bold text-success mb-0">
                                    <i class="bi bi-star-fill text-warning me-2"></i>Customer Satisfaction Feedback
                                </h6>
                                <span class="small text-muted">
                                    Submitted on <fmt:formatDate value="${feedback.createdAt}" pattern="yyyy-MM-dd HH:mm" />
                                </span>
                            </div>
                            <div class="my-2">
                                <span class="fw-semibold me-2">Rating:</span>
                                <c:forEach begin="1" end="${feedback.rating}">
                                    <i class="bi bi-star-fill text-warning fs-5"></i>
                                </c:forEach>
                                <c:forEach begin="${feedback.rating + 1}" end="5">
                                    <i class="bi bi-star text-secondary fs-5"></i>
                                </c:forEach>
                                <span class="badge bg-secondary ms-2">${feedback.rating} / 5</span>
                            </div>
                            <c:if test="${not empty feedback.notes}">
                                <div class="mt-2 p-2 bg-white rounded border border-light-subtle">
                                    <span class="small text-muted d-block fw-semibold mb-1">User Feedback:</span>
                                    <div class="text-secondary" style="white-space: pre-wrap;">${feedback.notes}</div>
                                </div>
                            </c:if>
                        </div>
                    </c:when>
                    <c:when test="${sessionScope.user.userId eq ticket.userId}">
                        <div class="card border border-warning-subtle bg-warning-subtle bg-opacity-10 p-4 rounded-3 mt-4">
                            <h6 class="fw-bold text-dark mb-1">
                                <i class="bi bi-patch-check-fill text-warning me-2"></i>Rate Your Support Experience
                            </h6>
                            <p class="small text-muted mb-3">This ticket is marked as ${ticket.status}. Please let us know how satisfied you were with the resolution.</p>
                            
                            <form action="${pageContext.request.contextPath}/submit-feedback" method="post">
                                <input type="hidden" name="ticketId" value="${ticket.ticketId}">
                                <div class="mb-3">
                                    <label class="form-label small fw-semibold">Satisfaction Rating (1 = Poor, 5 = Excellent):</label>
                                    <div class="d-flex gap-3 align-items-center">
                                        <c:forEach begin="1" end="5" var="score">
                                            <div class="form-check form-check-inline">
                                                <input class="form-check-input" type="radio" name="rating" id="rating${score}" value="${score}" ${score eq 5 ? 'checked' : ''} required>
                                                <label class="form-check-label fw-bold" for="rating${score}">${score} <i class="bi bi-star-fill text-warning small"></i></label>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </div>
                                <div class="mb-3">
                                    <label for="feedbackNotes" class="form-label small fw-semibold">Additional Comments / Review (Optional):</label>
                                    <textarea class="form-control bg-white" id="feedbackNotes" name="notes" rows="2" placeholder="Tell us how the technician did..."></textarea>
                                </div>
                                <button type="submit" class="btn btn-warning fw-semibold px-4">
                                    <i class="bi bi-check-circle me-1"></i>Submit Feedback
                                </button>
                            </form>
                        </div>
                    </c:when>
                </c:choose>
            </c:if>
        </div>
    </div>

    <!-- Right Column: Meta Information & Assignment Controls -->
    <div class="col-lg-4">
        <!-- Ticket Attributes Card -->
        <div class="card p-3 shadow-sm mb-4">
            <h5 class="fw-bold mb-3 border-bottom pb-2">Ticket Properties</h5>
            
            <div class="mb-3">
                <span class="text-muted small d-block">Priority Level:</span>
                <span class="badge ${ticket.priority eq 'HIGH' ? 'badge-priority-high' : (ticket.priority eq 'MEDIUM' ? 'badge-priority-medium' : 'badge-priority-low')} fs-6 mt-1">
                    ${ticket.priority} PRIORITY
                </span>
            </div>

            <div class="mb-3">
                <span class="text-muted small d-block">SLA Target Resolution:</span>
                <div class="mt-1">
                    <span class="${ticket.slaBadgeClass} px-2 py-1 small">
                        <i class="bi bi-clock-history me-1"></i>${ticket.slaStatusText}
                    </span>
                </div>
                <div class="small text-muted mt-1">
                    Window: ${ticket.slaHours} hours from creation
                </div>
            </div>

            <div class="mb-3">
                <span class="text-muted small d-block">Current Status:</span>
                <span class="fw-bold mt-1 d-inline-block">${ticket.status}</span>
            </div>

            <div class="mb-3">
                <span class="text-muted small d-block">Assigned Technician:</span>
                <c:choose>
                    <c:when test="${not empty ticket.techName}">
                        <div class="d-flex align-items-center gap-2 mt-1">
                            <i class="bi bi-person-check-fill text-success fs-5"></i>
                            <div>
                                <span class="fw-semibold">${ticket.techName}</span>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <span class="badge bg-danger-subtle text-danger border border-danger-subtle mt-1">Unassigned</span>
                    </c:otherwise>
                </c:choose>
            </div>

            <c:if test="${sessionScope.user.technician}">
                <hr>
                <div class="d-grid gap-2">
                    <form action="${pageContext.request.contextPath}/assign-ticket" method="post">
                        <input type="hidden" name="ticketId" value="${ticket.ticketId}">
                        <input type="hidden" name="redirect" value="detail">
                        <button type="submit" class="btn btn-outline-warning w-100 fw-semibold">
                            <i class="bi bi-person-plus-fill me-1"></i>Assign to Myself
                        </button>
                    </form>
                </div>
            </c:if>
        </div>

        <!-- Navigation Card -->
        <div class="card p-3 shadow-sm">
            <h6 class="fw-bold mb-2">Quick Navigation</h6>
            <div class="d-grid gap-2">
                <a href="${pageContext.request.contextPath}/tickets" class="btn btn-outline-secondary btn-sm text-start">
                    <i class="bi bi-arrow-left me-2"></i>Back to Tickets List
                </a>
                <c:if test="${sessionScope.user.technician}">
                    <a href="${pageContext.request.contextPath}/tech-dashboard" class="btn btn-outline-dark btn-sm text-start">
                        <i class="bi bi-speedometer2 me-2"></i>Technician Dashboard
                    </a>
                </c:if>
            </div>
        </div>

        <!-- Danger Zone: Delete Ticket (Admin Only) -->
        <c:if test="${sessionScope.user.admin}">
            <div class="card p-3 shadow-sm border-danger-subtle mt-4">
                <h6 class="fw-bold text-danger mb-2"><i class="bi bi-exclamation-triangle-fill me-1"></i>Danger Zone</h6>
                <p class="small text-muted mb-3">Permanently delete this ticket and all associated discussion comment threads.</p>
                <form action="${pageContext.request.contextPath}/ticket-delete" method="post" onsubmit="return confirm('Are you sure you want to permanently delete Ticket #${ticket.ticketId}? This cannot be undone.');">
                    <input type="hidden" name="action" value="deleteSingle">
                    <input type="hidden" name="ticketId" value="${ticket.ticketId}">
                    <button type="submit" class="btn btn-outline-danger w-100 btn-sm fw-semibold">
                        <i class="bi bi-trash3 me-1"></i>Delete Ticket #${ticket.ticketId}
                    </button>
                </form>
            </div>
        </c:if>
    </div>
</div>

<jsp:include page="/common/footer.jsp" />