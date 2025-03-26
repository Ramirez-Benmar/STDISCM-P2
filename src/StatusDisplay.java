// Programmed by: Benmar Ramirez

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class StatusDisplay extends Thread {
    private final Map<Integer, String> instanceNames = Collections.synchronizedMap(new HashMap<>());
    private final Map<Integer, String> activeParties = Collections.synchronizedMap(new HashMap<>());
    private final LinkedList<String> recentCompletions = new LinkedList<>();
    private String leftoverInfo = "";
    private volatile boolean running = true;
    private final String logFileName;

    public StatusDisplay() {
        this.logFileName = "completion_log_" + 
            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt";
    }

    // Register dungeon instance
    public void registerInstance(int id, String name) {
        instanceNames.put(id, name);
    }

    // Set a party in an instance
    public void setPartyInInstance(int id, String party) {
        activeParties.put(id, party);
    }

    // Clear a party from an instance
    public void clearParty(int id) {
        activeParties.remove(id);
    }

    // Mark an instance as active or empty
    public void markActive(int id, boolean active) {
        if (!active) {
            activeParties.remove(id);
        }
    }

    // Log a dungeon completion
    public void logCompletion(String entry) {
        synchronized (recentCompletions) {
            if (recentCompletions.size() >= 10) {
                recentCompletions.removeFirst();
            }
            recentCompletions.add(entry);
        }
        appendLogToFile(entry);
    }

    // Update leftover player information
    public void updateLeftover(String info) {
        leftoverInfo = info;
    }

    // Stop the display thread
    public void stopDisplay() {
        running = false;
    }

    // Clear console screen
    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // Print current dungeon status
    private void printStatus() {
        System.out.println("***** Dungeon Instance Status *****");
        synchronized (instanceNames) {
            for (Map.Entry<Integer, String> entry : instanceNames.entrySet()) {
                int id = entry.getKey();
                String name = entry.getValue();
                String party = activeParties.getOrDefault(id, "None");
                String status = activeParties.containsKey(id) ? "Active" : "Empty";
                System.out.println("[" + name + " | ID: " + id + "] - " + status + " | Party: " + party);
            }
        }

        System.out.println("\n--- Recent Party Completions ---");
        synchronized (recentCompletions) {
            if (recentCompletions.isEmpty()) {
                System.out.println("No completions yet.");
            } else {
                for (String log : recentCompletions) {
                    System.out.println(log);
                }
            }
        }

        System.out.println("\n<<< Leftover Players: " + leftoverInfo + " >>>");
    }

    // Append a log entry to the file
    private void appendLogToFile(String entry) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFileName, true))) {
            writer.write(entry);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (running) {
            clearScreen();
            printStatus();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted state
            }
        }
    }
}
