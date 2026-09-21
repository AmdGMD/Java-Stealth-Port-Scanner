import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
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

    // Store open ports found during scan
    private static List<String> openPortsList = Collections.synchronizedList(new ArrayList<String>());

    // Main entry point
    public static void main(String[] args) {
        printBanner();

        // Check if user wants help
        if (args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))) {
            printHelpMenu();
            return;
        }

        // Run interactive mode if no command arguments given
        runInteractiveMenu();
    }

    // Displays simple menu to collect user input safely
    private static void runInteractiveMenu() {
        Scanner scanner = new Scanner(System.in);

        // 1. Get Target IP with Validation
        String target = "";
        while (true) {
            System.out.print("[?] Enter Target IP or Subnet (e.g., 127.0.0.1 or 192.168.1.1/24): ");
            target = scanner.nextLine().trim();
            if (isValidIPOrSubnet(target)) {
                break;
            } else {
                System.out.println("[-] Invalid IP Address! Please enter a valid IP (e.g., 192.168.1.1 or 127.0.0.1).");
            }
        }

        // 2. Get Start Port
        int startPort = 0;
        while (startPort < 1 || startPort > 65535) {
            System.out.print("[?] Enter Start Port (1 - 65535): ");
            try {
                startPort = Integer.parseInt(scanner.nextLine().trim());
                if (startPort < 1 || startPort > 65535) {
                    System.out.println("[-] Port must be between 1 and 65535.");
                }
            } catch (Exception e) {
                System.out.println("[-] Please enter numbers only.");
            }
        }

        // 3. Get End Port
        int endPort = 0;
        while (endPort < startPort || endPort > 65535) {
            System.out.print("[?] Enter End Port (" + startPort + " - 65535): ");
            try {
                endPort = Integer.parseInt(scanner.nextLine().trim());
                if (endPort < startPort || endPort > 65535) {
                    System.out.println("[-] End port must be greater than or equal to Start Port.");
                }
            } catch (Exception e) {
                System.out.println("[-] Please enter numbers only.");
            }
        }

        // 4. Get Threads Count
        int threads = 0;
        while (threads < 1 || threads > 500) {
            System.out.print("[?] Enter Threads Count (1 - 500, Default 50): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                threads = 50; // Default value
            } else {
                try {
                    threads = Integer.parseInt(input);
                } catch (Exception e) {
                    System.out.println("[-] Please enter numbers only.");
                }
            }
        }

        // 5. Ask for Banner Grabbing
        boolean grabBanner = false;
        System.out.print("[?] Enable Service Banner Detection? (y/n): ");
        String bannerAnswer = scanner.nextLine().trim().toLowerCase();
        if (bannerAnswer.equals("y") || bannerAnswer.equals("yes")) {
            grabBanner = true;
        }

        // 6. Ask for Output File
        System.out.print("[?] Save results to file? (Enter filename or press Enter to skip): ");
        String outputFile = scanner.nextLine().trim();
        if (outputFile.equalsIgnoreCase("n") || outputFile.equalsIgnoreCase("no") || outputFile.isEmpty()) {
            outputFile = null; // Do not save
        }

        // Start scanning process
        List<String> targetsList = parseTargets(target);
        for (int i = 0; i < targetsList.size(); i++) {
            startScan(targetsList.get(i), startPort, endPort, threads, grabBanner, outputFile);
        }
    }

    // Validates if input is localhost, valid IPv4, or IPv4 with CIDR (/24)
    private static boolean isValidIPOrSubnet(String ip) {
        if (ip == null || ip.isEmpty() || ip.startsWith("-")) {
            return false;
        }

        if (ip.equalsIgnoreCase("localhost")) {
            return true;
        }

        // Handle CIDR notation like 192.168.1.0/24
        String ipPart = ip;
        if (ip.contains("/")) {
            String[] parts = ip.split("/");
            if (parts.length != 2) return false;
            ipPart = parts[0];
            try {
                int mask = Integer.parseInt(parts[1]);
                if (mask < 1 || mask > 32) return false;
            } catch (Exception e) {
                return false;
            }
        }

        // Validate standard IPv4 format (X.X.X.X)
        String[] blocks = ipPart.split("\\.");
        if (blocks.length != 4) {
            return false;
        }

        for (int i = 0; i < blocks.length; i++) {
            try {
                int value = Integer.parseInt(blocks[i]);
                if (value < 0 || value > 255) {
                    return false;
                }
            } catch (Exception e) {
                return false; // Contains letters or invalid format
            }
        }

        return true;
    }

    // Prints tool banner
    private static void printBanner() {
        System.out.println("==================================================================");
        System.out.println("  ██████╗  ██████╗ ██████╗ ████████╗███████╗██████╗ ███████╗");
        System.out.println("  ██╔══██╗██╔═══██╗██╔══██╗╚══██╔══╝██╔════╝██╔══██╗██╔════╝");
        System.out.println("  ██████╔╝██║   ██║██████╔╝   ██║   ███████╗██████╔╝███████╗");
        System.out.println("  ██╔═══╝ ██║   ██║██╔══██╗   ██║   ╚════██║██╔═══╝ ╚════██║");
        System.out.println("  ██║     ╚██████╔╝██║  ██║   ██║   ███████║██║     ███████║");
        System.out.println("  ╚═╝      ╚═════╝ ╚═╝  ╚═╝   ╚═╝   ╚══════╝╚═╝     ╚══════╝");
        System.out.println("                    PORT SPINNER v1 (Java 8)              ");
        System.out.println("==================================================================");
    }

    // Prints help instructions
    private static void printHelpMenu() {
        System.out.println("USAGE:");
        System.out.println("  java PortScannerCLI");
        System.out.println("  java PortScannerCLI --help\n");
    }

    // Converts subnet IP (like 192.168.1.1/24) into individual IPs
    private static List<String> parseTargets(String input) {
        List<String> list = new ArrayList<String>();
        if (input.contains("/24")) {
            String subnet = input.substring(0, input.lastIndexOf('.'));
            for (int i = 1; i < 255; i++) {
                list.add(subnet + "." + i);
            }
        } else {
            list.add(input);
        }
        return list;
    }

    // Runs multi-threaded scan manager
    private static void startScan(final String target, int startPort, int endPort, int threads, final boolean grabBanner, String outputFile) {
        System.out.println("\n[+] Starting Scan on: " + target);
        System.out.println("[+] Port Range      : " + startPort + " to " + endPort);
        System.out.println("[+] Active Threads  : " + threads);
        System.out.println("------------------------------------------------------------------");

        long startTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int p = startPort; p <= endPort; p++) {
            final int port = p;
            executor.execute(new Runnable() {
                public void run() {
                    checkPort(target, port, grabBanner);
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(60, TimeUnit.MINUTES);
        } catch (Exception ignored) {
        }

        long endTime = System.currentTimeMillis();
        double totalSeconds = (endTime - startTime) / 1000.0;

        System.out.println("------------------------------------------------------------------");
        System.out.println("[+] Finished target: " + target + " in " + totalSeconds + "s");
        System.out.println("[+] Open ports found: " + openPortsList.size());

        if (outputFile != null) {
            saveToFile(outputFile, target, totalSeconds);
        }
    }

    // Connects to a single port using standard Socket
    private static void checkPort(String target, int port, boolean grabBanner) {
        Socket socket = new Socket();
        try {
            // Timeout 300 ms
            socket.connect(new InetSocketAddress(InetAddress.getByName(target), port), 300);

            String banner = "";
            if (grabBanner) {
                banner = fetchBanner(socket, port);
            }

            String serviceName = getServiceName(port);
            String result = "Port " + port + " [OPEN] - Service: " + serviceName + " | Banner: " + (banner.isEmpty() ? "N/A" : banner);

            openPortsList.add(result);
            System.out.println("[+] " + result);

        } catch (Exception ignored) {
            // Port is closed or filtered
        } finally {
            try {
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }

    // Reads service response banner
    private static String fetchBanner(Socket socket, int port) {
        try {
            socket.setSoTimeout(1000);
            OutputStream out = socket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            if (port == 80 || port == 8080) {
                out.write("HEAD / HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes());
                out.flush();
            } else {
                out.write("\r\n".getBytes());
                out.flush();
            }

            String response = in.readLine();
            return response != null ? response.trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    // Simple service lookup
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

    // Saves result to text file
    private static void saveToFile(String filePath, String target, double duration) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(filePath, true));
            writer.println("Port Scanner Log");
            writer.println("Target: " + target);
            writer.println("Time: " + duration + " seconds");
            writer.println("--------------------------------------------------");
            for (int i = 0; i < openPortsList.size(); i++) {
                writer.println(openPortsList.get(i));
            }
            writer.close();
            System.out.println("[+] Log saved to: " + filePath);
        } catch (IOException e) {
            System.out.println("[-] Could not save file: " + e.getMessage());
        }
    }
}
