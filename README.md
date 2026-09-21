# Java Network & Stealth Port Scanner (v1.0)

A multi-threaded, stealth-focused network and port scanner written in pure Java. Designed to identify open ports, map running services, and execute throttled scanning to evade Intrusion Detection Systems (IDS) and firewall rate-limits.

## Key Features
- **Multi-threaded Architecture:** Accelerated scanning using `ExecutorService` for high performance.
- **Stealth & IDS Evasion Mode:** Implements configurable pause intervals (throttling) and port randomization to bypass security logging.
- **Service Detection:** Automatically identifies common services running on open ports (HTTP, SSH, FTP, MySQL, etc.).
- **Terminal UI & ANSI Colors:** Clean, color-coded CLI output for enhanced readability.
- **Report Generation:** Export scan findings automatically to formatted `.txt` log files.

## Tech Stack
- **Language:** Java 8+
- **Networking:** `java.net.Socket`, `java.net.InetSocketAddress`
- **Concurrency:** `java.util.concurrent.ExecutorService`

## How to Run

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/AmdGmd/Java-Stealth-Port-Scanner.git](https://github.com/AmdGmd/Java-Stealth-Port-Scanner.git)
   cd Java-Stealth-Port-Scanner