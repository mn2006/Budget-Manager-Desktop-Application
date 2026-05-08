====================================================
  Budget Management Desktop Application
  CS251 - Introduction to Software Engineering
  Cairo University - Faculty of Computers and AI
====================================================

TEAM:
  - Mahmoud Nasser Mahmoud  (20242321)
  - Abdelrahman Gamal Ahmed (20242198)
  - Timour Fayez Jaras      (20242081)
  - Salma Mohamed Mostafa   (20221074)

====================================================
HOW TO RUN
====================================================

Requirements:
  - Java 17 or higher  →  https://adoptium.net
  - Maven 3.8+         →  https://maven.apache.org/download.cgi

Steps:
  1. Unzip the project
  2. Open terminal inside the BudgetApp/ folder
  3. Run:
        mvn clean javafx:run

The database file (budget_app.db) is created automatically
in the folder where you run the command.

First run → Setup Screen (enter budget + dates)
Next runs  → Dashboard loads automatically

====================================================
PROJECT STRUCTURE
====================================================

src/main/java/com/budgetapp/
├── App.java                        ← Entry point
├── module-info.java                ← JavaFX module config
├── model/
│   ├── Category.java               ← Enum  (OCP)
│   ├── Expense.java                ← Entity (SRP)
│   ├── BudgetCycle.java            ← Entity with State Machine
│   ├── CalculationService.java     ← Interface (DIP + OCP)
│   └── CalculationEngine.java      ← Implementation (SRP)
├── data/
│   ├── Persistence.java            ← Interface (DIP)
│   ├── SQLiteDB.java               ← Singleton Pattern + all DB ops
│   └── PersistenceFactory.java     ← Factory Pattern
├── controller/
│   ├── BudgetController.java       ← US1
│   ├── ExpenseController.java      ← US2, US5, US6
│   ├── DashboardController.java    ← US3, US4 Bonus, US5
│   └── HistoryController.java      ← US4
├── view/
│   ├── SetupScreen.java            ← US1 boundary
│   ├── QuickEntryScreen.java       ← US2 boundary
│   ├── DashboardScreen.java        ← US3 + US4 Bonus boundary
│   └── HistoryScreen.java          ← US4 + US6 boundary
└── exceptions/
    ├── InvalidAmountException.java
    ├── NotFoundException.java
    └── BudgetExceededException.java

====================================================
DESIGN PATTERNS
====================================================
  1. Singleton  → SQLiteDB   (one DB instance)
  2. Factory    → PersistenceFactory  (creates Persistence objects)
  3. Observer   → DashboardController notifies DashboardScreen
                  on budget threshold events

====================================================
SOLID PRINCIPLES
====================================================
  SRP → Each class has exactly one responsibility
  OCP → CalculationService & Persistence are interfaces;
        new implementations need zero changes to existing code
  DIP → Controllers depend on Persistence interface,
        never on SQLiteDB directly

====================================================
TOOLS USED
====================================================
  JavaFX 21        UI framework
  SQLite + JDBC    Local persistence (offline)
  Maven            Build & dependency management
  PlantUML         UML diagrams
  Excalidraw       Architecture sketches
  Figma            UI mockups
  GitHub           Version control
