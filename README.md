# Kali Terminal Simulator

A **Java Swing application** that simulates a Linux-like terminal environment with a virtual file system, command execution, and styled terminal output.

---

## 📋 Features

- Realistic **Linux-style terminal interface** with green-on-black theme
- Custom **blinking caret** (cursor) effect
- Built-in **virtual file system** (with directories, files, and permissions)
- Supports **command execution** including:  
  - File navigation: `pwd`, `ls`, `cd`
  - File management: `cat`, `touch`, `rm`, `cp`, `mv`, `chmod`, `chown`
  - Utilities: `grep`, `head`, `tail`, `diff`, `sort`
  - System commands: `whoami`, `uname`, `date`, `cal`, `man`, `shutdown`, `reboot`
- Supports **input/output redirection** with `>` and `>>`
- Includes a hidden `flag.txt` for **CTF-style challenges** 🎯

---

## 🛠️ Technologies Used

- **Java 8+**
- **Swing** (UI)
- **AWT** (Graphics & Event Handling)
- **OOP Principles** (Encapsulation, Composition, Abstraction)

---

## 📂 Project Structure

```graphql
src/
└── main/
    └── java/
        └── mycput/
            └── ac/
                └── za/
                    └── terminalapp/
                        ├── TerminalApp.java
                        ├── CommandRegistry.java
                        ├── FileSystemManager.java
                        ├── VirtualFile.java
                        ├── VirtualDirectory.java
                        └── Command.java
