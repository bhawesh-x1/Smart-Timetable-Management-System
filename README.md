# Smart Campus Timetable Management System (`v1.0.0 • College Edition`)
**Poornima Institute of Engineering & Technology (PIET) — Department of Computer Engineering (Session 2026–2027)**

Smart Campus Timetable Management System is a Java Spring Boot enterprise application for colleges. It features an 8-semester auto-generator using CSP algorithms, PIET dataset integration, role-based access, real-time conflict detection, room utilization analytics, and robust export tools for seamless academic scheduling.

---

## 1. Architecture Overview
This enterprise Java system implements a complete **Smart Campus Timetable Management System** modeled pixel-for-pixel on the reference UI screenshots and powered by the real-world scheduling dataset of **PIET Department of Computer Engineering (Session 2026–2027)** across **All 8 Semesters (Sem 1 to Sem 8)**:

- **Spring Boot 3.x + Spring Data JPA / Hibernate Layer (`src/main/java/edu/piet/smartcampus/`):**
  - **Entities (`model/`):** `Department`, `Course`, `Semester`, `ClassSection`, `Student`, `Subject`, `Faculty`, `Room`, `TimeSlot`, `TimetableEntry`.
  - **Repositories (`repository/`):** Spring Data JPA repositories with custom dataset-mode queries.
  - **Services (`service/`):**
    - `CspConflictResolutionService`: Constraint Satisfaction Problem (CSP) validation engine checking Room Double-Booking ($C_1$), Faculty Double-Booking ($C_2$), and Section/Batch Overlaps ($C_3$), plus automated room/instructor conflict resolution and dynamic Faculty Leave Substitution.
    - `TimetableGeneratorService`: 8-Semester Smart Auto-Generator implementing CSP heuristic backtracking with multi-semester campus resource locking.
    - `DatasetSeederService`: Automatic startup seeder for both **Mode A (UI Screenshot Demo Dataset)** and **Mode B (PIET III-Sem Master Excel Dataset — Oct 5 to Oct 9, 2026 + Sem 1–8 Curriculum)**.
    - `ExportService`: Generates downloadable CSV spreadsheets and printable HTML/PDF reports.
  - **Controllers (`controller/`):** `WebPageController` & `SmartCampusRestController`.
- **Zero-Dependency JDK 21 Enterprise Runtime (`src/main/java/edu/piet/smartcampus/runtime/`):**
  - Allows immediate compilation and execution on any machine with JDK 17/21 (`javac` + `java`) without requiring Maven installation or internet access, serving the exact same REST API and web interface on `http://localhost:8085`.
- **Normalized MySQL 8.0+ Schema (`database/mysql_schema.sql`):**
  - Complete DDL script with foreign keys, cascading rules, and composite indexes for `departments`, `courses`, `semesters`, `class_sections`, `students`, `subjects`, `faculty`, `rooms`, `time_slots`, and `timetable_entries`.

---

## 2. Dual-Dataset Mode Switcher
Use the top-header dropdown (`Dataset: Mode A` / `Dataset: Mode B`) to toggle seamlessly at runtime:
1. **Mode A — UI Screenshot Demo Dataset:**
   - **6 KPI Cards:** `TOTAL STUDENTS: 4`, `TOTAL FACULTY: 6`, `TOTAL SUBJECTS: 14`, `CLASSROOMS & LABS: 8`, `TIMETABLE ENTRIES: 90`, `SCHEDULE CONFLICTS: 8`.
   - **Exact Room Utilization Percentages (36 Weekly Slots Standard):**
     - `A-202 (Room 102)`: `25 / 36` occupied (`69.4%` — `Moderate`)
     - `A-209 (CS Lab 1)`: `16 / 36` occupied (`44.4%` — `Optimal`)
     - `C-201`: `12 / 36` occupied (`33.3%` — `Underutilized`)
     - `C-202 (Room 101)`: `8 / 36` occupied (`22.2%` — `Underutilized`)
     - `C-205 (CS Lab 3)`: `4 / 36` occupied (`11.1%` — `Underutilized`)
     - `C-207 (CS Lab 2)`: `17 / 36` occupied (`47.2%` — `Optimal`)
     - `C-208`: `0 / 36` occupied (`0.0%` — `Underutilized`)
     - `SH01 (Seminar Hall 1)`: `8 / 36` occupied (`22.2%` — `Underutilized`)
2. **Mode B — PIET Master Dataset (Sem 1 to Sem 8 with III-Sem Excel Ground Truth):**
   - **All 8 Semesters:** `1CSA`, `2CSA`, `3CSA`, `3CSB`, `3CSC`, `3CSRD`, `4CSA`, `5CSA`, `6CSA`, `7CSA`, `8CSA`.
   - **19 Faculty Members:** `VK`, `MC`, `AK`, `OPS`, `UO`, `PD`, `RV`, `SK`, `AS`, `PG`, `NK`, `SS`, `DS`, `RK`, `PK`, `MK`, `GS`, `AP`, `NPTEL`.
   - **17 Classrooms & Labs:** `A-201` through `SH01`.
   - **All 309 Timetable Cells:** Loaded across Monday (`05-10-2026`) to Friday (`09-10-2026`), including real clash detection.

---

## 3. Running the Application

### Option A: Desktop Application Window (Main.java)
Double-click `launch-desktop-app.bat` or run:
```powershell
java -cp target\classes edu.piet.smartcampus.Main
```
Opens the Smart Campus desktop control hub and launches the standalone app window.

### Option B: Instant Web Browser Run (JDK 17/21 — No Maven Required)
Double-click `start-server.bat` or run in PowerShell/CMD:
```powershell
.\start-server.bat
```
Then open **http://localhost:8085/login** in your browser.

### Option C: Run via Spring Boot & Maven
```bash
mvn spring-boot:run
```
