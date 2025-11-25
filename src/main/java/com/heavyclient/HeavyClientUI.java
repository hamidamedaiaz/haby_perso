package com.heavyclient;

import com.heavyclient.utils.CustomWaypointRenderer;
import com.heavyclient.utils.RoutePainter;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Main UI component for the Heavy Client application.
 * Displays an interactive OpenStreetMap with route visualization and notifications.
 *
 * @author Heavy Client Team
 * @version 1.0
 * @since 2025-11-24
 */
public class HeavyClientUI extends JFrame {

    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 700;
    private static final int NOTIFICATION_HEIGHT = 150;

    private JXMapViewer mapViewer;
    private JTextArea notificationArea;

    /**
     * Constructs the main UI window with map and notification components.
     */
    public HeavyClientUI() {
        setTitle("JAVA Heavy Client Application");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initMap();
        initNotifications();
        initAttribution();

        setVisible(true);
    }

    /**
     * Initializes the map viewer with OpenStreetMap tiles.
     * Uses the official JXMapViewer2 library with custom TileFactoryInfo for OSM.
     * Note: User-Agent is configured in Main class to comply with OSM Tile Usage Policy.
     */
    private void initMap() {
        // === 1) Create the tile provider (OSM standard) ===
        TileFactoryInfo info = new TileFactoryInfo(
                0, 17, 18,  // min, max, total zoom
                256,        // tile size
                true, true, // x/y orientation
                "http://tile.openstreetmap.org",
                "x", "y", "z"
        ) {
            @Override
            public String getTileUrl(int x, int y, int zoom) {
                return this.baseURL + "/" + zoom + "/" + x + "/" + y + ".png";
            }
        };
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);

        // === 2) Optional: add a cache (recommended) ===
        try {
            File cacheDir = new File(System.getProperty("user.home"), ".heavyclient-cache");
            if (!cacheDir.exists() && !cacheDir.mkdirs()) {
                System.err.println("Failed to create cache directory: " + cacheDir.getAbsolutePath());
            }
            tileFactory.setLocalCache(new FileBasedLocalCache(cacheDir, false));
        } catch (Exception e) {
            System.err.println("Cache initialization failed: " + e.getMessage());
        }

        // === 3) Create the map viewer ===
        mapViewer = new JXMapViewer();
        mapViewer.setTileFactory(tileFactory);

        // === 4) Initial center and zoom ===
        GeoPosition start = new GeoPosition(45.758, 4.835); // Lyon
        mapViewer.setZoom(8);
        mapViewer.setAddressLocation(start);

        // === 5) Add to UI ===
        add(mapViewer, BorderLayout.CENTER);
    }

    /**
     * Initializes the notification area at the bottom of the window.
     */
    private void initNotifications() {
        notificationArea = new JTextArea();
        notificationArea.setEditable(false);
        notificationArea.setBackground(Color.BLACK);
        notificationArea.setForeground(Color.GREEN);

        JScrollPane scrollPane = new JScrollPane(notificationArea);
        scrollPane.setPreferredSize(new Dimension(WINDOW_WIDTH, NOTIFICATION_HEIGHT));

        add(scrollPane, BorderLayout.SOUTH);
    }

    /**
     * Initializes the attribution label as required by OpenStreetMap Tile Usage Policy.
     * This must be visible on the map at all times (typically bottom-right corner).
     */
    private void initAttribution() {
        JLabel attributionLabel = new JLabel("© OpenStreetMap contributors");
        attributionLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        attributionLabel.setForeground(Color.BLACK);
        attributionLabel.setBackground(new Color(255, 255, 255, 200));
        attributionLabel.setOpaque(true);
        attributionLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        attributionLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Make attribution clickable to link to OSM
        attributionLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new java.net.URI("https://www.openstreetmap.org/copyright"));
                } catch (Exception ex) {
                    System.err.println("Could not open browser: " + ex.getMessage());
                }
            }
        });

        // Add "Report map issue" link as recommended by OSM Tile Usage Policy
        JLabel reportIssueLabel = new JLabel("Report map issue");
        reportIssueLabel.setFont(new Font("SansSerif", Font.PLAIN, 9));
        reportIssueLabel.setForeground(Color.BLUE);
        reportIssueLabel.setBackground(new Color(255, 255, 255, 200));
        reportIssueLabel.setOpaque(true);
        reportIssueLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        reportIssueLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        reportIssueLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new java.net.URI("https://www.openstreetmap.org/fixthemap"));
                } catch (Exception ex) {
                    System.err.println("Could not open browser: " + ex.getMessage());
                }
            }
        });

        // Add attribution to map viewer (bottom-right position)
        mapViewer.setLayout(new BorderLayout());
        JPanel attributionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        attributionPanel.setOpaque(false);
        attributionPanel.add(reportIssueLabel);
        attributionPanel.add(new JLabel(" | "));
        attributionPanel.add(attributionLabel);
        mapViewer.add(attributionPanel, BorderLayout.SOUTH);
    }

    /**
     * Adds a notification message to the notification area.
     *
     * @param msg the message to display
     */
    public void addNotification(String msg) {
        SwingUtilities.invokeLater(() -> notificationArea.append(msg + "\n"));
    }

    /**
     * Displays a route on the map.
     *
     * @param positions list of GPS positions representing the route
     */
    public void showRoute(List<GeoPosition> positions) {
        drawRoute(positions);
    }

    /**
     * Makes the UI visible.
     */
    public void showUI() {
        setVisible(true);
    }

    /**
     * Draws a route on the map with waypoints and automatic zoom.
     * Creates a compound painter with route line and waypoint markers at start and end positions.
     *
     * @param positions list of GPS positions representing the route
     */
    public void drawRoute(List<GeoPosition> positions) {
        if (positions == null || positions.isEmpty()) {
            return;
        }

        RoutePainter routePainter = new RoutePainter(positions);
        routePainter.setColor(Color.BLUE);

        Set<Waypoint> waypoints = new HashSet<>();
        if (positions.size() >= 2) {
            waypoints.add(new DefaultWaypoint(positions.get(0)));
            waypoints.add(new DefaultWaypoint(positions.get(positions.size() - 1)));
        }

        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(waypoints);

        CustomWaypointRenderer renderer = new CustomWaypointRenderer();
        renderer.setSize(16);
        renderer.setFillColor(new Color(255, 50, 50, 220));
        renderer.setBorderColor(Color.WHITE);
        waypointPainter.setRenderer(renderer);

        List<Painter<JXMapViewer>> painters = new ArrayList<>();
        painters.add(routePainter);
        painters.add(waypointPainter);

        CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(compoundPainter);
        mapViewer.zoomToBestFit(new HashSet<>(positions), 0.7);

        addNotification("Route displayed: " + positions.size() + " points");
        repaint();
    }
}
