<div align="center">

# ⚙️ TaskFlow
### A Java Workflow Engine

**Plan tasks. Manage dependencies. Execute workflows.**

A console-based workflow engine built with Java that organizes tasks, checks dependencies, retries failures, and records execution history.

![Java](https://img.shields.io/badge/Java-OOP-orange?style=for-the-badge&logo=openjdk&logoColor=white)
![Status](https://img.shields.io/badge/Status-In%20Development-blue?style=for-the-badge)
![Project](https://img.shields.io/badge/Project-Console%20Application-6C63FF?style=for-the-badge)

</div>

---

## ✨ Overview

**TaskFlow** is a Java project focused on the logic behind workflow execution. Tasks can depend on other tasks, and the engine checks those dependencies before deciding what can run. It also includes retry handling, circular-dependency detection, file persistence, and timestamped execution logs.

The goal is to explore how core Java and object-oriented programming can be used to build a small, practical workflow system.

## 🚀 Features

| Feature | Description |
| --- | --- |
| 📝 Task management | Add tasks and display the current task list. |
| 🔗 Dependency handling | Validate dependencies and wait for prerequisite tasks to complete. |
| ▶️ Workflow execution | Execute eligible tasks in dependency order. |
| 🔁 Retry mechanism | Retry unsuccessful tasks up to three attempts. |
| 🔄 Cycle detection | Check for circular dependencies before execution. |
| 💾 Save and load | Persist task information in a text file. |
| 📜 Execution logs | Record workflow events with timestamps. |
| 📊 Workflow summary | Review task totals and their current statuses. |
| ♻️ Reset workflow | Reset task statuses to prepare for another run. |
| 🧹 Clear logs | Clear the saved execution history. |

## 🧰 Built With

- **Java** — application logic and object-oriented design
- **Arrays and control flow** — storing and processing tasks
- **File I/O** — saving and loading task data
- **Exception handling** — handling file-related errors
- **Git & GitHub** — version control

## 📁 Project Structure

```text
WorkFlow-Engine/
├── Main.java
├── Task.java
├── Workflow.java
├── DependencyChecker.java
├── WorkflowEngine.java
├── FileManager.java
├── ExecutionLogger.java
├── tasks.txt
└── execution.log
```

> The exact files present may vary as the project evolves. `tasks.txt` and `execution.log` are data files created or used while the program runs.

## ⚡ Getting Started

### Prerequisites

- Java Development Kit (JDK) installed
- A terminal or an IDE such as Visual Studio Code

Check that Java is available:

```bash
java -version
javac -version
```

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/zaaraa-hub/WorkFlow-Engine.git
   ```

2. Open the project directory:

   ```bash
   cd WorkFlow-Engine
   ```

3. Compile the Java source files:

   ```bash
   javac *.java
   ```

4. Start the application:

   ```bash
   java Main
   ```

## 🕹️ How It Works

1. Add tasks through the console menu.
2. Set dependencies where a task needs another task to finish first.
3. Run the workflow.
4. The engine checks for circular dependencies and executes eligible tasks.
5. Failed tasks are retried; tasks whose prerequisites are unavailable may be skipped.
6. Save task information and review the execution logs or workflow summary.

## 🧠 Concepts Practiced

This project provides hands-on practice with:

- Classes, objects, fields, constructors, and methods
- Conditional statements and loops
- Arrays and task processing
- Dependency validation and basic cycle detection
- File reading and writing
- Exception handling
- Breaking a program into focused classes

## 🗺️ Roadmap

- [ ] Test the full workflow from start to finish
- [ ] Review edge cases and improve input validation
- [ ] Refine project structure and documentation
- [ ] Evaluate whether critical-path calculation would be a useful addition

## 👩‍💻 Author

**Zaara Mulani**

- GitHub: [@zaaraa-hub](https://github.com/zaaraa-hub)

---

<div align="center">

*Built as a hands-on Java project to explore workflow execution and core programming concepts.*

</div>
