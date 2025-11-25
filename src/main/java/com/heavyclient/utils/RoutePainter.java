package com.heavyclient.utils;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Dessine une route entre plusieurs points GPS sur la carte
 * Avec contour noir et ligne colorée pour un meilleur rendu
 */
public class RoutePainter implements Painter<JXMapViewer> {

    private final List<GeoPosition> track;
    private Color color = Color.RED;
    private boolean antiAlias = true;

    public RoutePainter(List<GeoPosition> track) {
        this.track = new ArrayList<>(track);
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        g = (Graphics2D) g.create();

        // Gestion du viewport (important pour le bon positionnement)
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        // Anti-aliasing pour un rendu plus lisse
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        // Dessiner d'abord le contour noir (plus épais)
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(4));
        drawRoute(g, map);

        // Puis dessiner la ligne de couleur par-dessus
        g.setColor(color);
        g.setStroke(new BasicStroke(2));
        drawRoute(g, map);

        g.dispose();
    }

    /**
     * Dessine la route en convertissant les positions GPS en pixels
     */
    private void drawRoute(Graphics2D g, JXMapViewer map) {
        int lastX = 0, lastY = 0;
        boolean first = true;

        for (GeoPosition gp : track) {
            // Conversion GPS → pixels
            Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());

            if (!first) {
                g.drawLine(lastX, lastY, (int) pt.getX(), (int) pt.getY());
            }

            first = false;
            lastX = (int) pt.getX();
            lastY = (int) pt.getY();
        }
    }

    /**
     * Change la couleur de la route
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Active/désactive l'anti-aliasing
     */
    public void setAntiAlias(boolean antiAlias) {
        this.antiAlias = antiAlias;
    }
}
