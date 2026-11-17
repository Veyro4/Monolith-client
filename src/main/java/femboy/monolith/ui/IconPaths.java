package femboy.monolith.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class IconPaths {
    private static final Map<String, String> MARKS = new HashMap<>();

    static {
        MARKS.put("combat", "pl 14.5 17.5 3 6 3 3 6 3 17.5 14.5|l 13 19 19 13|l 16 16 20 20|l 19 21 21 19");
        MARKS.put("movement", "p M4.89 10.36A2.5 2.5 0 0 1 9.84 9.66C10.18 12.14 9.44 14.01 7.95 14.22C6.47 14.43 5.23 12.83 4.89 10.36Z"
                + "|p M8.34 16.94A2 1.25 -8 1 1 8.68 19.42A2 1.25 -8 1 1 8.34 16.94Z"
                + "|p M14.16 6.66A2.5 2.5 0 0 1 19.11 7.36C18.77 9.83 17.53 11.43 16.05 11.22C14.56 11.01 13.82 9.14 14.16 6.66Z"
                + "|p M15.66 13.94A2 1.25 8 1 1 15.32 16.42A2 1.25 8 1 1 15.66 13.94Z");
        MARKS.put("utility", "p M4.92 16.61L11.37 10.15A4.25 4.25 0 0 1 17.1 4.78L13.93 7.95L16.05 10.07L19.22 6.9A4.25 4.25 0 0 1 13.85 12.63L7.39 19.08A1.75 1.75 0 0 1 4.92 16.61Z");
        MARKS.put("visuals", "p M4.25 19.75L13.75 10.25"
                + "|p M17 3.75C17 5.7 18.3 7 20.25 7C18.3 7 17 8.3 17 10.25C17 8.3 15.7 7 13.75 7C15.7 7 17 5.7 17 3.75Z"
                + "|p M9.25 3.75C9.25 4.8 9.95 5.5 11 5.5C9.95 5.5 9.25 6.2 9.25 7.25C9.25 6.2 8.55 5.5 7.5 5.5C8.55 5.5 9.25 4.8 9.25 3.75Z"
                + "|p M18.5 12.75C18.5 13.8 19.2 14.5 20.25 14.5C19.2 14.5 18.5 15.2 18.5 16.25C18.5 15.2 17.8 14.5 16.75 14.5C17.8 14.5 18.5 13.8 18.5 12.75Z");
        MARKS.put("hud", "r 3.75 3.75 7 10 2|r 3.75 16.25 7 4 1.5|r 13.25 3.75 7 4 1.5|r 13.25 10.25 7 10 2");
        MARKS.put("donut", "p M19.14 7.88A8.25 8.25 0 1 1 16.13 4.86A2.4 2.4 0 0 0 19.14 7.88Z|c 12 12 2.75");
        MARKS.put("friends", "c 9.25 8.75 3"
                + "|p M3.75 19.25c0-2.83 2.47-4.5 5.5-4.5s5.5 1.67 5.5 4.5a1 1 0 0 1-1 1h-9a1 1 0 0 1-1-1Z"
                + "|p M13.85 5.3A2.75 2.75 0 1 1 14.82 10.17M15 13.75C17 12.9 20.25 15 20.25 18.25v1a1 1 0 0 1-1 1H18");
        MARKS.put("scripts", "p M8.25 7.75 4 12l4.25 4.25M15.75 7.75 20 12l-4.25 4.25M13.5 5.5l-3 13");
        MARKS.put("configs", "p M3.75 6H13.5M17.5 6H20.25M3.75 12H6.5M10.5 12H20.25M3.75 18H11.5M15.5 18H20.25"
                + "|c 15.5 6 2|c 8.5 12 2|c 13.5 18 2");
        MARKS.put("themes", "p M12 3.5l1.9 5.1a2.5 2.5 0 0 0 1.5 1.5l5.1 1.9l-5.1 1.9a2.5 2.5 0 0 0 -1.5 1.5l-1.9 5.1l-1.9 -5.1a2.5 2.5 0 0 0 -1.5 -1.5l-5.1 -1.9l5.1 -1.9a2.5 2.5 0 0 0 1.5 -1.5z");
        MARKS.put("client", "p M13 3.75 5.75 13H12l-1 7.25L18.25 11H12Z");
        MARKS.put("language", "p M3.75 6.5A2.25 2.25 0 0 1 6 4.25h12a2.25 2.25 0 0 1 2.25 2.25v8A2.25 2.25 0 0 1 18 16.75H8.25L3.75 20Z");
        MARKS.put("guiscale", "p M4 9V4H9M4 4L9 9M20 9V4H15M20 4L15 9M20 15V20H15M20 20L15 15M4 15V20H9M4 20L9 15");
        MARKS.put("glass", "p M11.54 3.99A1 1 0 0 1 12.46 3.99L18.52 7.11A1 1 0 0 1 18.52 8.89L12.46 12.01A1 1 0 0 1 11.54 12.01L5.48 8.89A1 1 0 0 1 5.48 7.11Z"
                + "|p M3.75 12L11.54 16.01A1 1 0 0 0 12.46 16.01L20.25 12M3.75 16L11.54 20.01A1 1 0 0 0 12.46 20.01L20.25 16");
        MARKS.put("font", "p M5.75 7.25v-2h12.5v2M12 5.25v13.5M9.5 18.75h5");
        MARKS.put("stream", "p M10.03 6.5C10.65 6.34 11.3 6.25 12 6.25C15.75 6.25 18.25 8.75 20.25 12C19.64 12.99 18.98 13.92 18.25 14.72M13.97 17.5C13.35 17.66 12.7 17.75 12 17.75C8.25 17.75 5.75 15.25 3.75 12C4.36 11.01 5.02 10.08 5.75 9.28M12.6 9.06A3 3 0 0 1 14.94 11.4M11.4 14.94A3 3 0 0 1 9.06 12.6"
                + "|p M4.25 4.25L19.75 19.75");
        MARKS.put("sounds", "p M5 19.25V13.5M8.5 19.25V7.5M12 19.25V11M15.5 19.25V4.75M19 19.25V9.5");
        MARKS.put("blur", "p M8.75 7.5C6.8 9.64 4.26 12.57 4.26 15.69A4.49 4.49 0 0 0 13.23 15.69C13.23 12.57 10.7 9.64 8.75 7.5Z"
                + "|p M17 4C15.75 5.38 14.12 7.25 14.12 9.25A2.88 2.88 0 0 0 19.88 9.25C19.88 7.25 18.25 5.38 17 4Z");
        MARKS.put("search", "c 10.5 10.5 5.75|l 14.6 14.6 19.25 19.25");
        MARKS.put("close", "l 8 8 16 16|l 16 8 8 16");
    }

    private IconPaths() {
    }

    static boolean has(String id) {
        return MARKS.containsKey(id);
    }

    static BufferedImage render(String id, int pixels, int argb) {
        String mark = MARKS.get(id);
        BufferedImage image = new BufferedImage(pixels, pixels, BufferedImage.TYPE_INT_ARGB);
        if (mark == null) {
            return image;
        }
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        double unit = pixels / 24.0;
        g.scale(unit, unit);
        g.setColor(new Color(argb, true));
        g.setStroke(new BasicStroke(1.5F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (String part : mark.split("\\|")) {
            String[] bits = part.trim().split("\\s+");
            switch (bits[0]) {
                case "p" -> g.draw(path(part.substring(1)));
                case "c" -> g.draw(new Ellipse2D.Double(d(bits[1]) - d(bits[3]), d(bits[2]) - d(bits[3]), d(bits[3]) * 2, d(bits[3]) * 2));
                case "r" -> g.draw(new RoundRectangle2D.Double(d(bits[1]), d(bits[2]), d(bits[3]), d(bits[4]), d(bits[5]) * 2, d(bits[5]) * 2));
                case "l" -> {
                    Path2D.Double line = new Path2D.Double();
                    line.moveTo(d(bits[1]), d(bits[2]));
                    line.lineTo(d(bits[3]), d(bits[4]));
                    g.draw(line);
                }
                case "pl" -> {
                    Path2D.Double line = new Path2D.Double();
                    line.moveTo(d(bits[1]), d(bits[2]));
                    for (int i = 3; i + 1 < bits.length; i += 2) {
                        line.lineTo(d(bits[i]), d(bits[i + 1]));
                    }
                    g.draw(line);
                }
                default -> {
                }
            }
        }
        g.dispose();
        return image;
    }

    private static double d(String value) {
        return Double.parseDouble(value);
    }

    private static Path2D.Double path(String d) {
        Path2D.Double path = new Path2D.Double();
        List<Object> tokens = tokens(d);
        double cx = 0;
        double cy = 0;
        double sx = 0;
        double sy = 0;
        double lastCtrlX = 0;
        double lastCtrlY = 0;
        char last = 0;
        char cmd = 'M';
        int i = 0;
        while (i < tokens.size()) {
            if (tokens.get(i) instanceof Character ch) {
                cmd = ch;
                i++;
            }
            boolean rel = Character.isLowerCase(cmd);
            double ox = rel ? cx : 0;
            double oy = rel ? cy : 0;
            switch (Character.toUpperCase(cmd)) {
                case 'M' -> {
                    cx = n(tokens, i) + ox;
                    cy = n(tokens, i + 1) + oy;
                    i += 2;
                    path.moveTo(cx, cy);
                    sx = cx;
                    sy = cy;
                    cmd = rel ? 'l' : 'L';
                    last = 'M';
                }
                case 'L' -> {
                    cx = n(tokens, i) + ox;
                    cy = n(tokens, i + 1) + oy;
                    i += 2;
                    path.lineTo(cx, cy);
                    last = 'L';
                }
                case 'H' -> {
                    cx = n(tokens, i) + ox;
                    i++;
                    path.lineTo(cx, cy);
                    last = 'L';
                }
                case 'V' -> {
                    cy = n(tokens, i) + oy;
                    i++;
                    path.lineTo(cx, cy);
                    last = 'L';
                }
                case 'C' -> {
                    double c1x = n(tokens, i) + ox;
                    double c1y = n(tokens, i + 1) + oy;
                    double c2x = n(tokens, i + 2) + ox;
                    double c2y = n(tokens, i + 3) + oy;
                    cx = n(tokens, i + 4) + ox;
                    cy = n(tokens, i + 5) + oy;
                    i += 6;
                    path.curveTo(c1x, c1y, c2x, c2y, cx, cy);
                    lastCtrlX = c2x;
                    lastCtrlY = c2y;
                    last = 'C';
                }
                case 'S' -> {
                    double c1x = last == 'C' || last == 'S' ? cx * 2 - lastCtrlX : cx;
                    double c1y = last == 'C' || last == 'S' ? cy * 2 - lastCtrlY : cy;
                    double c2x = n(tokens, i) + ox;
                    double c2y = n(tokens, i + 1) + oy;
                    cx = n(tokens, i + 2) + ox;
                    cy = n(tokens, i + 3) + oy;
                    i += 4;
                    path.curveTo(c1x, c1y, c2x, c2y, cx, cy);
                    lastCtrlX = c2x;
                    lastCtrlY = c2y;
                    last = 'S';
                }
                case 'Q' -> {
                    double c1x = n(tokens, i) + ox;
                    double c1y = n(tokens, i + 1) + oy;
                    cx = n(tokens, i + 2) + ox;
                    cy = n(tokens, i + 3) + oy;
                    i += 4;
                    path.quadTo(c1x, c1y, cx, cy);
                    lastCtrlX = c1x;
                    lastCtrlY = c1y;
                    last = 'Q';
                }
                case 'T' -> {
                    double c1x = last == 'Q' || last == 'T' ? cx * 2 - lastCtrlX : cx;
                    double c1y = last == 'Q' || last == 'T' ? cy * 2 - lastCtrlY : cy;
                    cx = n(tokens, i) + ox;
                    cy = n(tokens, i + 1) + oy;
                    i += 2;
                    path.quadTo(c1x, c1y, cx, cy);
                    lastCtrlX = c1x;
                    lastCtrlY = c1y;
                    last = 'T';
                }
                case 'A' -> {
                    double rx = n(tokens, i);
                    double ry = n(tokens, i + 1);
                    double rot = n(tokens, i + 2);
                    boolean large = n(tokens, i + 3) != 0;
                    boolean sweep = n(tokens, i + 4) != 0;
                    double x = n(tokens, i + 5) + ox;
                    double y = n(tokens, i + 6) + oy;
                    i += 7;
                    arc(path, cx, cy, rx, ry, rot, large, sweep, x, y);
                    cx = x;
                    cy = y;
                    last = 'A';
                }
                case 'Z' -> {
                    path.closePath();
                    cx = sx;
                    cy = sy;
                    last = 'Z';
                    i++;
                }
                default -> i++;
            }
        }
        return path;
    }

    private static double n(List<Object> tokens, int index) {
        if (index >= tokens.size() || !(tokens.get(index) instanceof Double value)) {
            return 0;
        }
        return value;
    }

    private static List<Object> tokens(String d) {
        List<Object> out = new ArrayList<>();
        int i = 0;
        int len = d.length();
        while (i < len) {
            char c = d.charAt(i);
            if (Character.isWhitespace(c) || c == ',') {
                i++;
                continue;
            }
            if (Character.isLetter(c)) {
                out.add(c);
                i++;
                continue;
            }
            int start = i;
            if (c == '+' || c == '-') {
                i++;
            }
            while (i < len && (Character.isDigit(d.charAt(i)) || d.charAt(i) == '.')) {
                i++;
            }
            if (i > start) {
                try {
                    out.add(Double.parseDouble(d.substring(start, i)));
                } catch (NumberFormatException ignored) {
                    break;
                }
            } else {
                i++;
            }
        }
        return out;
    }

    private static void arc(Path2D.Double path, double x1, double y1, double rx, double ry, double rot,
                            boolean large, boolean sweep, double x2, double y2) {
        if (rx == 0 || ry == 0) {
            path.lineTo(x2, y2);
            return;
        }
        rx = Math.abs(rx);
        ry = Math.abs(ry);
        double phi = Math.toRadians(rot);
        double cos = Math.cos(phi);
        double sin = Math.sin(phi);
        double hx = (x1 - x2) / 2;
        double hy = (y1 - y2) / 2;
        double px = cos * hx + sin * hy;
        double py = -sin * hx + cos * hy;
        double lambda = (px * px) / (rx * rx) + (py * py) / (ry * ry);
        if (lambda > 1) {
            double grow = Math.sqrt(lambda);
            rx *= grow;
            ry *= grow;
        }
        double rx2 = rx * rx;
        double ry2 = ry * ry;
        double num = rx2 * ry2 - rx2 * py * py - ry2 * px * px;
        double den = rx2 * py * py + ry2 * px * px;
        double coef = den == 0 ? 0 : Math.sqrt(Math.max(0, num / den));
        if (large == sweep) {
            coef = -coef;
        }
        double cpx = coef * rx * py / ry;
        double cpy = -coef * ry * px / rx;
        double cx = cos * cpx - sin * cpy + (x1 + x2) / 2;
        double cy = sin * cpx + cos * cpy + (y1 + y2) / 2;
        double ux = (px - cpx) / rx;
        double uy = (py - cpy) / ry;
        double vx = (-px - cpx) / rx;
        double vy = (-py - cpy) / ry;
        double start = Math.atan2(uy, ux);
        double delta = Math.atan2(ux * vy - uy * vx, ux * vx + uy * vy);
        if (!sweep && delta > 0) {
            delta -= Math.PI * 2;
        } else if (sweep && delta < 0) {
            delta += Math.PI * 2;
        }
        int steps = Math.max(8, (int) Math.ceil(Math.abs(delta) / (Math.PI / 36)));
        for (int s = 1; s <= steps; s++) {
            double t = start + delta * s / steps;
            double tc = Math.cos(t);
            double ts = Math.sin(t);
            path.lineTo(cx + rx * tc * cos - ry * ts * sin, cy + rx * tc * sin + ry * ts * cos);
        }
    }
}
