-- MySQL DDL for Helpdesk Ticket Resolution System
-- Compatible with MySQL 5.7, 8.0+

DROP TABLE IF EXISTS ticket_comments;
DROP TABLE IF EXISTS tickets;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER'
);

CREATE TABLE tickets (
    ticket_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    tech_id INT,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(10) DEFAULT 'MEDIUM',
    status VARCHAR(20) DEFAULT 'OPEN',
    attachment LONGBLOB,
    attachment_name VARCHAR(255),
    attachment_type VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (tech_id) REFERENCES users(user_id)
);

CREATE TABLE ticket_comments (
    comment_id INT PRIMARY KEY AUTO_INCREMENT,
    ticket_id INT NOT NULL,
    author_id INT NOT NULL,
    comment_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES tickets(ticket_id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(user_id)
);

-- Seed Users
INSERT INTO users (name, email, password, role) VALUES ('John Doe', 'john@example.com', 'user123', 'USER');
INSERT INTO users (name, email, password, role) VALUES ('Jane Smith', 'jane@example.com', 'user123', 'USER');
INSERT INTO users (name, email, password, role) VALUES ('Alex Tech', 'alex.tech@company.com', 'tech123', 'TECHNICIAN');
INSERT INTO users (name, email, password, role) VALUES ('Sarah Tech', 'sarah.tech@company.com', 'tech123', 'TECHNICIAN');
INSERT INTO users (name, email, password, role) VALUES ('IT Admin', 'admin@company.com', 'admin123', 'ADMIN');