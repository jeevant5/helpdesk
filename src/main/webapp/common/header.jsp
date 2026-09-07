<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Helpdesk Ticket Resolution System</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <style>
        :root {
            --primary-color: #0d6efd;
            --tech-color: #6f42c1;
        }
        body {
            background-color: #f8f9fa;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }
        .navbar-brand {
            font-weight: 700;
            letter-spacing: 0.5px;
        }
        .card {
            border: none;
            box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
            border-radius: 0.75rem;
        }
        .stat-card {
            border-radius: 0.75rem;
            transition: transform 0.2s ease;
        }
        .stat-card:hover {
            transform: translateY(-3px);
        }
        .badge-priority-high { background-color: #dc3545; color: white; }
        .badge-priority-medium { background-color: #fd7e14; color: white; }
        .badge-priority-low { background-color: #20c997; color: white; }
        .badge-status-open { background-color: #0d6efd; color: white; }
        .badge-status-in_progress { background-color: #ffc107; color: #212529; }
        .badge-status-resolved { background-color: #198754; color: white; }
        .badge-status-closed { background-color: #6c757d; color: white; }
        .thread-bubble {
            border-radius: 0.75rem;
            padding: 1rem 1.25rem;
            margin-bottom: 1rem;
        }
        .thread-bubble-user {
            background-color: #f1f3f5;
            border-left: 4px solid #0d6efd;
        }
        .thread-bubble-tech {
            background-color: #f3f0ff;
            border-left: 4px solid #6f42c1;
        }
        footer {
            margin-top: auto;
        }
    </style>
</head>
<body>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark sticky-top shadow-sm">
    <div class="container">
        <a class="navbar-brand d-flex align-items-center gap-2" href="${pageContext.request.contextPath}/">
            <i class="bi bi-headset text-primary fs-4"></i>
            <span>Helpdesk Support</span>
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navMenu">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navMenu">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <c:if test="${not empty sessionScope.user}">
                    <c:choose>
                        <c:when test="${sessionScope.user.technician}">
                            <li class="nav-item">
                                <a class="nav-link active text-warning" href="${pageContext.request.contextPath}/tech-dashboard">
                                    <i class="bi bi-speedometer2 me-1"></i>Technician Dashboard
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/tickets">
                                    <i class="bi bi-card-list me-1"></i>All Tickets
                                </a>
                            </li>
                        </c:when>
                        <c:otherwise>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/tickets">
                                    <i class="bi bi-ticket-detailed me-1"></i>My Tickets
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/submit-ticket">
                                    <i class="bi bi-plus-circle me-1"></i>New Ticket
                                </a>
                            </li>
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </ul>
            <div class="d-flex align-items-center gap-3">
                <c:choose>
                    <c:when test="${not empty sessionScope.user}">
                        <div class="text-white d-flex align-items-center gap-2">
                            <i class="bi bi-person-circle fs-5"></i>
                            <div>
                                <span class="fw-semibold">${sessionScope.user.name}</span>
                                <span class="badge ${sessionScope.user.technician ? 'bg-warning text-dark' : 'bg-primary'} ms-1">
                                    ${sessionScope.user.role}
                                </span>
                            </div>
                        </div>
                        <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-light btn-sm">
                            <i class="bi bi-box-arrow-right me-1"></i>Logout
                        </a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/login" class="btn btn-outline-light btn-sm">
                            <i class="bi bi-box-arrow-in-right me-1"></i>Login
                        </a>
                        <a href="${pageContext.request.contextPath}/register" class="btn btn-primary btn-sm">
                            <i class="bi bi-person-plus-fill me-1"></i>Sign Up
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</nav>
<main class="container my-4">