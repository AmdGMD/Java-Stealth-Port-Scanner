# Port Scanner CLI

Port Scanner CLI is a lightweight, fast, and multi-threaded port scanner written in Java 8. It is designed to be clear, simple, and easy to use via an interactive command-line interface while providing essential network scanning features.

---

## Features

- **Interactive CLI Mode:** Step-by-step guidance to input scan parameters safely.
- **Input Validation:** Built-in checks for IPv4 addresses, subnet formats, and valid port ranges.
- **Multi-Threaded Performance:** Configurable thread execution pool for fast and parallel scanning.
- **Banner Grabbing & Service Detection:** Identifies common network services (HTTP, SSH, FTP, MySQL, etc.) and fetches banners.
- **Subnet Scanning Support:** Supports CIDR notation (e.g., `192.168.1.1/24`) to scan entire subnets.
- **Result Export:** Option to save scan results directly to a custom text log file.

---

## Compilation and Execution

### 1. Compile the Code
Open your terminal or PowerShell in the project directory and run:

`javac PortScannerCLI.java`

### 2. Run the Tool
Launch the interactive CLI menu:

`java PortScannerCLI`

Display the help menu:

`java PortScannerCLI --help`

---

## Requirements

- **Java Development Kit (JDK):** Version 8 or higher.
