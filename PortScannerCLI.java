import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PortScannerCLI {

    private static final List<String> openPortsList = Collections.synchronizedList(new ArrayList<>());
    
    //(ANSI Escape Codes)
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String CYAN = "\u001B[36m";
    public static final String BOLD = "\u001B[1m";

    public static void main(String[] args) {
        printBanner();

        //(e.g., java PortScannerCLI -t 127.0.0.1 -p 1-100)
        if (args.length > 0) {
            handleCLIArgs(args);
        } else {
            runInteractiveMenu();
        }
    }

    private static void printBanner() {
        System.out.println(CYAN + BOLD);
        System.out.println("  ██████╗  ██████╗ ██████╗ ████████╗███████╗██████╗ ███████╗");
        System.out.println("  ██╔══██╗██╔═══██╗██╔══██╗╚══██╔══╝██╔════╝██╔══██╗██╔════╝");
        System.out.println("  ██████╔╝██║   ██║██████╔╝   ██║   ███████╗██████╔╝███████╗");
        System.out.println("  ██╔═══╝ ██║   ██║██╔══██╗   ██║   ╚════██║██╔═══╝ ╚════██║");
        System.out.println("  ██║     ╚██████╔╝██║  ██║   ██║   ███████║██║     ███████║");
        System.out.println("  ╚═╝      ╚═════╝ ╚═╝  ╚═╝   ╚═╝   ╚══════╝╚═╝     ╚══════╝");
        System.out.println("             Advanced Stealth Scanner v1.0             " + RESET);
        System.out.println(YELLOW + "============================================================" + RESET);
    }

    private static void runInteractiveMenu() {
        Scanner scanner = new Scanner(System.in);

        System.out.print(BOLD + "[?] Target IP / Hostname: " + RESET);
        String target = scanner.nextLine().trim();

        System.out.print(BOLD + "[?] Start Port (e.g. 1): " + RESET);
        int startPort = scanner.nextInt();

        System.out.print(BOLD + "[?] End Port (e.g. 1024): " + RESET);
        int endPort = scanner.nextInt();

        System.out.print(BOLD + "[?] Connection Timeout in ms (Default 300): " + RESET);
        int timeout = scanner.nextInt();

        System.out.print(BOLD + "[?] Threads Count (e.g. 20): " + RESET);
        int threadCount = scanner.nextInt();

        System.out.println("\n" + BOLD + "--- Stealth Mode Options ---" + RESET);
        System.out.print(BOLD + "[?] Enable Pause/Sleep between batches to evade IDS? (y/n): " + RESET);
        boolean stealthMode = scanner.next().equalsIgnoreCase("y");

        int batchIntervalSeconds = 0;
        int pauseDurationSeconds = 0;

        if (stealthMode) {
            System.out.print(BOLD + "    └─> Scan duration before pausing (e.g. 10 seconds): " + RESET);
            batchIntervalSeconds = scanner.nextInt();
            System.out.print(BOLD + "    └─> Pause duration (e.g. 5 seconds sleep): " + RESET);
            pauseDurationSeconds = scanner.nextInt();
        }

        scanner.nextLine(); // Clear buffer
        System.out.print(BOLD + "[?] Save results to output file? (y/n): " + RESET);
        String saveOption = scanner.nextLine().trim();

        executeScan(target, startPort, endPort, timeout, threadCount, stealthMode, batchIntervalSeconds, pauseDurationSeconds, saveOption.equalsIgnoreCase("y"));
        scanner.close();
    }

    private static void executeScan(String target, int startPort, int endPort, int timeout, int threadCount, 
                                    boolean stealthMode, int batchInterval, int pauseDuration, boolean saveFile) {
        
        System.out.println("\n" + GREEN + "[+] Initiating scan against: " + target + RESET);
        if (stealthMode) {
            System.out.println(YELLOW + "[!] Stealth Mode Active: Will pause every " + batchInterval + "s for " + pauseDuration + "s." + RESET);
        }

        long startTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<Integer> ports = new ArrayList<>();
        for (int p = startPort; p <= endPort; p++) ports.add(p);
        
        //(Port Randomization)
        if (stealthMode) {
            Collections.shuffle(ports);
        }

        long lastPauseTime = System.currentTimeMillis();

        for (int i = 0; i < ports.size(); i++) {
            int port = ports.get(i);

            //(Throttling / Stealth Sleep)
            if (stealthMode && (System.currentTimeMillis() - lastPauseTime) >= (batchInterval * 1000L)) {
                System.out.println(YELLOW + "[*] IDS Evasion: Pausing scan for " + pauseDuration + " seconds..." + RESET);
                try {
                    Thread.sleep(pauseDuration * 1000L);
                } catch (InterruptedException ignored) {}
                lastPauseTime = System.currentTimeMillis();
            }

            executor.execute(() -> scanPort(target, port, timeout));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.err.println(RED + "[-] Execution interrupted." + RESET);
        }

        long endTime = System.currentTimeMillis();
        double duration = (endTime - startTime) / 1000.0;

        System.out.println("\n" + CYAN + "============================================================" + RESET);
        System.out.println(GREEN + BOLD + "[+] Scan Complete in " + String.format("%.2f", duration) + " seconds." + RESET);
        System.out.println(GREEN + BOLD + "[+] Total Open Ports: " + openPortsList.size() + RESET);
        System.out.println(CYAN + "============================================================" + RESET);

        if (saveFile) {
            saveResultsToFile(target, duration);
        }
    }

    private static void scanPort(String ip, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), timeout);
            String service = getServiceName(port);
            String output = String.format("Port %-5d [OPEN]  --->  Service: %s", port, service);
            
            openPortsList.add(output);
            System.out.println(GREEN + "[+] " + output + RESET);
        } catch (Exception ignored) {
            // Port is closed or filtered
        }
    }

    private static String getServiceName(int port) {
        switch (port) {
            case 21: return "FTP";
            case 22: return "SSH";
            case 23: return "Telnet";
            case 25: return "SMTP";
            case 53: return "DNS";
            case 80: return "HTTP";
            case 110: return "POP3";
            case 143: return "IMAP";
            case 443: return "HTTPS";
            case 3306: return "MySQL";
            case 3389: return "RDP";
            case 8080: return "HTTP-Proxy";
            default: return "Unknown";
        }
    }

    private static void saveResultsToFile(String target, double duration) {
        String fileName = "stealth_scan_" + target.replaceAll("[^a-zA-Z0-9]", "_") + ".txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("PortScannerPro v1.0 Report");
            writer.println("Target: " + target);
            writer.println("Duration: " + String.format("%.2f", duration) + " seconds");
            writer.println("--------------------------------------------------");
            for (String line : openPortsList) {
                writer.println(line);
            }
            System.out.println(GREEN + "[+] Results saved to: " + fileName + RESET);
        } catch (IOException e) {
            System.err.println(RED + "[-] File save failed: " + e.getMessage() + RESET);
        }
    }

    private static void handleCLIArgs(String[] args) {
        System.out.println(YELLOW + "[!] Running in default Mode..." + RESET);
        runInteractiveMenu();
    }
}
