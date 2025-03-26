// Programmed by: Benmar Ramirez

public class MainLFG {

    public static void main(String[] args) {
        // Initialize player queue and collect inputs
        PlayerQueue playerQueue = initializePlayerQueue();

        // Retrieve dungeon configurations
        int totalDungeons = InputCollector.getDungeonCount();
        int minTime = InputCollector.getMinDuration();
        int maxTime = InputCollector.getMaxDuration();

        // Start the status display thread
        StatusDisplay display = new StatusDisplay();
        display.start();

        // Initialize the DungeonController
        DungeonController controller = new DungeonController(totalDungeons, minTime, maxTime, display);

        // Process parties and assign them to dungeons
        processParties(playerQueue, controller);

        // Display leftover players summary
        displayLeftovers(playerQueue);

        // Wait for all dungeons to complete
        waitForDungeonCompletion(controller);

        // Finalize display and updates
        finalizeDisplay(display, playerQueue);
    }

    // Initializes the player queue by collecting inputs
    private static PlayerQueue initializePlayerQueue() {
        PlayerQueue playerQueue = new PlayerQueue();
        InputCollector.collectPlayerInputs(playerQueue);
        return playerQueue;
    }

    // Processes parties and assigns them to available dungeons
    private static void processParties(PlayerQueue playerQueue, DungeonController controller) {
        while (playerQueue.canFormParty()) {
            String[] party = playerQueue.createParty();
            controller.launchDungeon(party);
        }
    }

    // Outputs a summary of leftover players
    private static void displayLeftovers(PlayerQueue playerQueue) {
        System.out.println("\n>>> Leftover Players:");
        System.out.println(playerQueue.getLeftoverSummary());
    }

    // Waits until all dungeon instances are free
    private static void waitForDungeonCompletion(DungeonController controller) {
        while (!controller.allInstancesFree()) {
            sleepSafely(1000);
        }
    }

    // Finalizes the display and updates with any remaining players
    private static void finalizeDisplay(StatusDisplay display, PlayerQueue playerQueue) {
        sleepSafely(3000);
        display.updateLeftover(playerQueue.getLeftoverSummary());
        sleepSafely(3000);
        display.stopDisplay();
    }

    // Utility method to safely sleep without redundancy
    private static void sleepSafely(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }
}
