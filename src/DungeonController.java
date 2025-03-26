// Programmed by: Benmar Ramirez

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

// DungeonController Class
class DungeonController {
    private final int maxInstances;
    private final int minTime;
    private final int maxTime;
    private final Semaphore availableSlots;
    private final Queue<Integer> freeIds;
    private final Map<Integer, String> instanceNames;
    private final Map<Integer, Integer> instanceToPartyMap; // Tracks party number for each instance
    private final StatusDisplay display;
    private int partyCount = 0;

    public DungeonController(int maxInstances, int minTime, int maxTime, StatusDisplay display) {
        this.maxInstances = maxInstances;
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.display = display;

        availableSlots = new Semaphore(maxInstances);
        freeIds = new ArrayDeque<>(); // Use ArrayDeque for better queue performance
        instanceNames = new HashMap<>();
        instanceToPartyMap = new HashMap<>();

        initializeInstances();
    }

    private void initializeInstances() {
        for (int i = 1; i <= maxInstances; i++) {
            freeIds.offer(i);
            String name = NameGenerator.generateName();
            instanceNames.put(i, name);
            display.registerInstance(i, name);
        }
    }

    public void launchDungeon(String[] party) {
        try {
            availableSlots.acquire();
            int instanceId = acquireInstance();
            synchronized (this) {
                partyCount++; // Increment party count safely
            }
            instanceToPartyMap.put(instanceId, partyCount); // Map the instance ID to the party number
            display.setPartyInInstance(instanceId, "Party " + partyCount);

            DungeonThread dungeon = new DungeonThread(minTime, maxTime);
            dungeon.start();
            display.markActive(instanceId, true);

            handlePostDungeonTasks(dungeon, instanceId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
        }
    }

    private int acquireInstance() {
        return freeIds.poll();
    }

    private void handlePostDungeonTasks(DungeonThread dungeon, int instanceId) {
        new Thread(() -> {
            try {
                dungeon.join();
                int duration = dungeon.getRunDuration();
                synchronized (this) {
                    int partyNumber = instanceToPartyMap.get(instanceId); // Get the correct party number
                    display.markActive(instanceId, false);
                    display.clearParty(instanceId);
                    display.logCompletion("Party " + partyNumber + " finished in " +
                            instanceNames.get(instanceId) + " (ID: " + instanceId + ") in " +
                            duration + " sec.");
                    instanceToPartyMap.remove(instanceId); // Clean up the mapping
                }
                releaseInstance(instanceId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore the interrupted status
            }
        }).start();
    }

    private void releaseInstance(int instanceId) {
        freeIds.offer(instanceId);
        availableSlots.release();
    }

    public boolean allInstancesFree() {
        return availableSlots.availablePermits() == maxInstances;
    }
}

// DungeonThread Class
class DungeonThread extends Thread {
    private final int minTime;
    private final int maxTime;
    private int runDuration;

    public DungeonThread(int minTime, int maxTime) {
        this.minTime = minTime;
        this.maxTime = maxTime;
    }

    @Override
    public void run() {
        runDuration = ThreadLocalRandom.current().nextInt(minTime, maxTime + 1);
        try {
            Thread.sleep(runDuration * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
        }
    }

    public int getRunDuration() {
        return runDuration;
    }
}

// NameGenerator Class
class NameGenerator {
    private static final Random RANDOM = new Random();
    private static final String[] VOWELS = {"a", "e", "i", "o", "u"};
    private static final String[] CONSONANTS = {"b", "c", "d", "f", "g", "h", "j", "k", "l", "m",
            "n", "p", "r", "s", "t", "v", "w", "z"};

    public static String generateName() {
        int syllableCount = RANDOM.nextInt(3) + 1; // Random syllable count between 1 and 3
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < syllableCount; i++) {
            String syllable = generateSyllable();
            if (i == 0) {
                syllable = Character.toUpperCase(syllable.charAt(0)) + syllable.substring(1);
            }
            name.append(syllable);
        }
        return name.toString();
    }

    private static String generateSyllable() {
        return CONSONANTS[RANDOM.nextInt(CONSONANTS.length)] +
                VOWELS[RANDOM.nextInt(VOWELS.length)];
    }
}
