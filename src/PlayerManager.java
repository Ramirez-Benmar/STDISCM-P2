// Programmed by: Benmar Ramirez

import java.util.*;
import java.util.stream.IntStream;

class PlayerQueue {
    private final Queue<String> tanks = new LinkedList<>();
    private final Queue<String> healers = new LinkedList<>();
    private final Queue<String> dps = new LinkedList<>();

    public synchronized void addPlayer(String role, int id) {
        switch (role.toLowerCase()) {
            case "tank" -> tanks.add("Tank-" + id);
            case "healer" -> healers.add("Healer-" + id);
            case "dps" -> dps.add("DPS-" + id);
        }
    }

    public synchronized boolean canFormParty() {
        return !tanks.isEmpty() && !healers.isEmpty() && dps.size() >= 3;
    }

    public synchronized String[] createParty() {
        if (!canFormParty()) return null;
        return new String[]{tanks.poll(), healers.poll(), dps.poll(), dps.poll(), dps.poll()};
    }

    public synchronized String getLeftoverSummary() {
        return "Tanks: " + tanks.size() + " | Healers: " + healers.size() + " | DPS: " + dps.size();
    }
}

class InputCollector {
    private static int dungeonCount;
    private static int minDuration;
    private static int maxDuration;

    public static void collectPlayerInputs(PlayerQueue queue) {
        Scanner scanner = new Scanner(System.in);

        dungeonCount = getValidInt(scanner, "Enter number of Dungeon Instances (must be >= 1): ", 1, Integer.MAX_VALUE);
        minDuration = getValidInt(scanner, "Enter minimum dungeon completion time (in sec, must be >= 0): ", 0, Integer.MAX_VALUE);
        maxDuration = getValidInt(scanner, "Enter maximum dungeon completion time (in sec, must be between " + Math.max(1, minDuration) + " and 15): ", Math.max(1, minDuration), 15);

        Map<String, Integer> playerCounts = Map.of(
                "Tank", getValidInt(scanner, "Enter number of Tanks (>= 0): ", 0, Integer.MAX_VALUE),
                "Healer", getValidInt(scanner, "Enter number of Healers (>= 0): ", 0, Integer.MAX_VALUE),
                "DPS", getValidInt(scanner, "Enter number of DPS (min 3 required): ", 3, Integer.MAX_VALUE)
        );

        playerCounts.forEach((role, count) ->
                IntStream.rangeClosed(1, count).forEach(id -> queue.addPlayer(role, id))
        );

        System.out.println("\nRegistered Players & Dungeon Settings:");
        System.out.println("Dungeon Instances: " + dungeonCount);
        System.out.println("Time Range: " + minDuration + " - " + maxDuration + " sec");
        System.out.println("Tanks: " + playerCounts.get("Tank") + " | Healers: " + playerCounts.get("Healer") + " | DPS: " + playerCounts.get("DPS"));
    }

    /**
     * Reads an integer from the scanner ensuring that it falls within the given range.
     */
    private static int getValidInt(Scanner scanner, String prompt, int min, int max) {
        int value;
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                value = scanner.nextInt();
                if (value < min || value > max) {
                    System.out.println("Invalid! Value must be between " + min + " and " + max + ".");
                } else {
                    break;
                }
            } else {
                System.out.println("Invalid input! Please enter a valid integer.");
                scanner.next(); // Clear invalid input
            }
        }
        return value;
    }

    public static int getDungeonCount() {
        return dungeonCount;
    }

    public static int getMinDuration() {
        return minDuration;
    }

    public static int getMaxDuration() {
        return maxDuration;
    }
}
