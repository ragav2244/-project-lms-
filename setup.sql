-- ============================================================
--  LibMS2 — Run this ONCE in MySQL Workbench before starting
--  Step 1: Run this entire file
-- ============================================================

CREATE DATABASE lms_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE lms_db;

CREATE TABLE tbl_catalog (
    catalog_id     INT          AUTO_INCREMENT PRIMARY KEY,
    title          VARCHAR(300) NOT NULL,
    author         VARCHAR(200) NOT NULL,
    genre          VARCHAR(100) NOT NULL,
    total_copies   INT          NOT NULL DEFAULT 1,
    avail_copies   INT          NOT NULL DEFAULT 1,
    added_on       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_catalog (title, author)
);

CREATE TABLE tbl_members (
    member_id      INT          AUTO_INCREMENT PRIMARY KEY,
    full_name      VARCHAR(200) NOT NULL,
    roll_no        VARCHAR(50)  NOT NULL,
    department     VARCHAR(150),
    joined_on      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_rollno (roll_no)
);

CREATE TABLE tbl_lending (
    lending_id     INT          AUTO_INCREMENT PRIMARY KEY,
    catalog_id     INT          NOT NULL,
    member_id      INT          NOT NULL,
    book_title     VARCHAR(300) NOT NULL,
    member_name    VARCHAR(200) NOT NULL,
    lend_date      DATE         NOT NULL,
    return_date    DATE,
    lend_state     ENUM('ACTIVE','RETURNED') DEFAULT 'ACTIVE',
    FOREIGN KEY (catalog_id) REFERENCES tbl_catalog(catalog_id) ON DELETE CASCADE,
    FOREIGN KEY (member_id)  REFERENCES tbl_members(member_id)  ON DELETE CASCADE
);

-- ── Optional sample data ──────────────────────────────────────────────────────
INSERT INTO tbl_catalog (title, author, genre, total_copies, avail_copies) VALUES
('The Great Gatsby',           'F. Scott Fitzgerald', 'Fiction',     5, 5),
('To Kill a Mockingbird',      'Harper Lee',          'Fiction',     4, 4),
('1984',                       'George Orwell',       'Fiction',     6, 6),
('A Brief History of Time',    'Stephen Hawking',     'Science',     3, 3),
('Clean Code',                 'Robert C. Martin',    'Technology',  4, 4),
('Sapiens',                    'Yuval Noah Harari',   'History',     3, 3),
('Introduction to Algorithms', 'Thomas H. Cormen',    'Mathematics', 2, 2),
('The Pragmatic Programmer',   'David Thomas',        'Technology',  3, 3);

INSERT INTO tbl_members (full_name, roll_no, department) VALUES
('Rahul Sharma',   'CS2021001', 'Computer Science'),
('Priya Patel',    'CS2021002', 'Computer Science'),
('Amit Kumar',     'ME2021001', 'Mechanical Engineering'),
('Sneha Verma',    'EC2021001', 'Electronics');

SELECT 'Database setup complete!' AS status;
