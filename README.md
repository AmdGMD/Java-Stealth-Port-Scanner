# Port Scanner

Port Scanner is a lightweight, fast, and multi-threaded port scanner written in Java 8 for command-line interfaces (CLI). It is designed to be clear, simple, and easy to understand for beginners while maintaining essential security scanning features.

---

## Features

- **ASCII Art Banner:** Clean visual header displayed on startup.
- **Interactive CLI Mode:** Prompts the user step-by-step for easy configuration without memorizing complex command flags.
- **Input Validation:** Robust IP address and port validation to prevent invalid inputs, non-numeric values, or accidental flags.
- **Multi-Threaded Scanning:** Customizable thread execution count for optimal performance and scan speed.
- **Banner Grabbing & Service Detection:** Detects standard services (HTTP, SSH, FTP, MySQL, etc.) and retrieves service headers.
- **Subnet Scanning Support:** Supports CIDR notation (e.g., `192.168.1.1/24`) to scan entire network ranges.
- **Result Logging:** Optional feature to export scan outputs to a text file.

---

## Compilation and Execution

### 1. Compile the Code
Open your terminal or PowerShell in the project directory and run:

`javac PortScannerCLI.java`

### 2. Run the Tool
To launch the interactive menu:

`java PortScannerCLI`

To display the help menu:

`java PortScannerCLI --help`

---

## Requirements

- Java Development Kit (JDK) 8 or higher.
