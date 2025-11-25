package com.heavyclient;

import com.heavyclient.utils.RouteUtils;
import com.soap.generated.*;
import jakarta.xml.bind.JAXBElement;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.List;


/**
 * Client SOAP pour le service de routage
 * Permet de calculer des itinéraires entre deux points
 */
public class Main {

    private static final String SEPARATOR = "=".repeat(60);
    private static HeavyClientUI ui;

    public static void main(String[] args) {

        // Configure User-Agent for OSM tile requests BEFORE creating any UI
        // This must be set early to comply with OSM Tile Usage Policy
        System.setProperty("http.agent", "HeavyClient/1.0 (Java Educational Project; Contact: your-email@example.com)");

        // Also set alternative property names that may be used by different HTTP clients
        System.setProperty("http.agent.vendor", "HeavyClient");
        System.setProperty("http.agent.version", "1.0");

        java.util.logging.Logger.getLogger("org.jxmapviewer").setLevel(java.util.logging.Level.INFO);

        ui = new HeavyClientUI();
        ui.showUI();
        printHeader();
        IRoutingService client = initializeService();

        AMQNotificationListener listener = new AMQNotificationListener(client, ui);
        listener.StartListening();

        try {
            ItineraryResult result = requestItinerary(client);

            if (isValidResult(result)) {
                displayResults(result);
                log("Process completed successfully");
            }

        } catch (Exception e) {
            error("Fatal error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace(System.err);
        }

        System.out.println("Client lourd en écoute... Appuyez sur Entrée pour quitter.");
        new java.util.Scanner(System.in).nextLine();

        System.out.println("Client Lourd eteint.");

    }

    /**
     * Affiche l'en-tête de l'application
     */
    private static void printHeader() {
        System.out.println(SEPARATOR);
        System.out.println("JAVA HEAVY SOAP CLIENT - ROUTING SERVICE");
        System.out.println(SEPARATOR);
    }

    /**
     * Initialise le service SOAP
     */
    private static IRoutingService initializeService() {
        log("Initializing SOAP service...");
        RoutingService service = new RoutingService();

        log("Getting service port...");
        IRoutingService client = service.getBasicHttpBindingIRoutingService();

        log("Service initialized successfully");
        return client;
    }

    /**
     * Effectue une requête d'itinéraire
     */
    private static ItineraryResult requestItinerary(IRoutingService client) {
        String originLat = "45.758";
        String originLon = "4.835";
        String originCity = "Lyon";
        String destLat = "48.8566";
        String destLon = "2.3522";
        String destCity = "Paris";

        System.out.println("\nRequest parameters:");
        System.out.println("  Origin: " + originCity + " (" + originLat + ", " + originLon + ")");
        System.out.println("  Destination: " + destCity + " (" + destLat + ", " + destLon + ")");

        log("Calling SOAP service GetItinerary()...");
        ItineraryResult result = client.getItinerary(
                originLat, originLon, originCity,
                destLat, destLon, destCity
        );

        log("Response received");
        return result;
    }

    /**
     * Vérifie si le résultat est valide
     */
    private static boolean isValidResult(ItineraryResult result) {
        if (result == null) {
            error("Result is null");
            return false;
        }

        log("Checking success status...");
        Boolean success = result.isSuccess();

        if (success == null || !success) {
            String message = extractMessage(result.getMessage());
            error("Service returned error: " + message);
            return false;
        }

        log("Request successful, extracting data...");

        if (result.getData() == null || result.getData().getValue() == null) {
            error("No data in response");
            return false;
        }

        return true;
    }

    /**
     * Affiche les résultats de l'itinéraire
     */
    public static void displayResults(ItineraryResult result) {
        ItineraryData data = result.getData().getValue();
        List<GeoPosition> routePositions = RouteUtils.extractRoute(data);
        System.out.println("\n" + SEPARATOR);
        System.out.println("ITINERARY RESULTS");
        System.out.println(SEPARATOR);
        ui.drawRoute(routePositions);
        displaySummary(data);
        displaySteps(data);

        System.out.println(SEPARATOR);
    }

    /**
     * Affiche le résumé de l'itinéraire (distance et durée totales)
     */
    private static void displaySummary(ItineraryData data) {
        Double distance = data.getTotalDistance();
        Double duration = data.getTotalDuration();

        String distanceStr = distance != null ? String.format("%.2f km", distance / 1000) : "N/A";
        String durationStr = duration != null ? formatDuration(duration) : "N/A";

        System.out.println("Total Distance: " + distanceStr);
        System.out.println("Total Duration: " + durationStr);
    }

    /**
     * Affiche les étapes de l'itinéraire
     */
    private static void displaySteps(ItineraryData data) {
        JAXBElement<ArrayOfStep> stepsElement = data.getSteps();

        if (stepsElement == null || stepsElement.getValue() == null) {
            log("No steps in response");
            return;
        }

        ArrayOfStep steps = stepsElement.getValue();

        if (steps.getStep() == null || steps.getStep().isEmpty()) {
            log("No steps available");
            return;
        }

        System.out.println("\nSteps (" + steps.getStep().size() + "):");
        System.out.println(SEPARATOR);

        int stepNumber = 1;
        for (Step step : steps.getStep()) {
            System.out.println(stepNumber + ". " + formatStepInfo(step));
            stepNumber++;
        }
    }

    /**
     * Extrait le message d'une JAXBElement
     */
    private static String extractMessage(JAXBElement<String> messageElement) {
        if (messageElement != null && messageElement.getValue() != null) {
            return messageElement.getValue();
        }
        return "Unknown error";
    }

    /**
     * Formate les informations d'une étape
     */
    private static String formatStepInfo(Step step) {
        StringBuilder info = new StringBuilder();

        JAXBElement<String> instructionsElement = step.getInstructions();
        if (instructionsElement != null && instructionsElement.getValue() != null) {
            info.append(instructionsElement.getValue());
        } else {
            info.append("No instructions");
        }

        info.append(" [");

        Double distance = step.getDistance();
        Double duration = step.getDuration();

        if (distance != null) {
            info.append(String.format("%.0fm", distance));
        }

        if (duration != null) {
            if (distance != null) {
                info.append(", ");
            }
            info.append(formatDuration(duration));
        }

        info.append("]");

        return info.toString();
    }

    /**
     * Formate une durée en secondes en format lisible
     */
    private static String formatDuration(double seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int secs = (int) (seconds % 60);

        if (hours > 0) {
            return String.format("%dh %dmin %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dmin %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }

    /**
     * Log un message d'information
     */
    private static void log(String message) {
        System.out.println("[INFO] " + message);
    }

    /**
     * Log un message d'erreur
     */
    private static void error(String message) {
        System.err.println("[ERROR] " + message);
    }
}
