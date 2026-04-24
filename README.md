# LibMS2 — Library Management System
### Light Professional UI | Java Swing + MySQL

---

## Login
- **Username:** admin
- **Password:** admin123

---

## Database Setup (Do this FIRST)

1. Open **MySQL Workbench**
2. Open the file `setup.sql`
3. Run it — it creates database `lms_db` and all tables

Tables created:
- `tbl_catalog` — books
- `tbl_members` — students/members
- `tbl_lending` — issue/return records

---

## Run in VS Code

### Step 1 — Open Project
`File → Open Folder → select LibMS2`

### Step 2 — Add MySQL JAR
- In the left panel scroll down to **JAVA PROJECTS**
- Expand **LibMS2 → Referenced Libraries**
- Right-click **Add JAR**
- Select `lib/mysql-connector-j-9.6.0.jar`

### Step 3 — Run
- Open `src/lms/Main.java`
- Click the **▶ Run** button above `main()`

---

## Compile manually (terminal)

**Windows:**
```
javac -cp "lib\mysql-connector-j-9.6.0.jar" -d build\classes src\lms\db\*.java src\lms\util\*.java src\lms\ui\*.java src\lms\Main.java
java -cp "build\classes;lib\mysql-connector-j-9.6.0.jar" lms.Main
```

**Mac/Linux:**
```
javac -cp "lib/mysql-connector-j-9.6.0.jar" -d build/classes src/lms/db/*.java src/lms/util/*.java src/lms/ui/*.java src/lms/Main.java
java -cp "build/classes:lib/mysql-connector-j-9.6.0.jar" lms.Main
```

---

## Modules

| Module | What it does |
|--------|-------------|
| Login | Secure admin login with DB check |
| Dashboard | Stats: books, available, issued, members |
| Catalog | Add / Edit / Delete books, search |
| Members | Add / Edit / Delete members |
| Issue Book | Issue book to member, stock reduces |
| Return Book | Process return, stock restores |
| Lending Log | Full history with Active/Returned filter |
