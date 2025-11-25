package com.heavyclient.utils;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointRenderer;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Renderer personnalisé pour les waypoints (marqueurs sur la carte)
 * Dessine des cercles colorés avec bordure pour marquer les points importants
 */
public class CustomWaypointRenderer implements WaypointRenderer<Waypoint> {

    private Color fillColor = new Color(255, 100, 100, 200); // Rouge semi-transparent
    private Color borderColor = Color.WHITE;
    private int size = 12;
    private int borderWidth = 2;

    @Override
    public void paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint waypoint) {
        g = (Graphics2D) g.create();

        // Anti-aliasing pour un rendu lisse
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Convertir position GPS en pixels
        Point2D point = map.getTileFactory().geoToPixel(waypoint.getPosition(), map.getZoom());

        Rectangle rect = map.getViewportBounds();
        int x = (int) (point.getX() - rect.getX());
        int y = (int) (point.getY() - rect.getY());

        // Dessiner le cercle de remplissage
        g.setColor(fillColor);
        g.fillOval(x - size / 2, y - size / 2, size, size);

        // Dessiner la bordure
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(borderWidth));
        g.drawOval(x - size / 2, y - size / 2, size, size);

        g.dispose();
    }

    // Setters pour personnalisation

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }
}
