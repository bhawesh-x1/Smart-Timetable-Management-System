package edu.piet.smartcampus.runtime;

import com.sun.net.httpserver.HttpServer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Dual-Mode Desktop & Browser Application Launcher for Smart Campus Timetable Management System.
 * Launches the embedded Java HTTP/REST server and gives 1-click access to:
 *   1) Standalone Chromium Desktop App Window (--app=http://localhost:PORT/login)
 *   2) Full Web Browser session (http://localhost:PORT/login)
 */
public class SmartCampusDesktopApp {

    private static final Color NAVY_BG = new Color(26, 34, 52);
    private static final Color ROYAL_BLUE = new Color(30, 107, 241);
    private static final Color EMERALD = new Color(16, 185, 129);
    private static final Color CYAN = new Color(14, 165, 233);
    private static final Color PURPLE = new Color(139, 92, 246);
    private static final Color CORAL_RED = new Color(239, 68, 68);
    private static final Color SLATE_BG = new Color(241, 245, 249);
    private static final Color BORDER_GRAY = new Color(226, 232, 240);
    private static final Color TEXT_DARK = new Color(15, 23, 42);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);

    private final int port;
    private HttpServer ownedServer;
    private JTextArea logArea;
    private JLabel statusLabel;
    private JLabel urlLabel;

    public SmartCampusDesktopApp(int port) {
        this.port = port;
    }

    public static void launch(String[] args) {
        int port = 8085;
        boolean headless = GraphicsEnvironment.isHeadless();
        boolean forceApp = false;
        boolean forceBrowser = false;
        boolean noAutoOpen = false;

        if (args != null) {
            for (String arg : args) {
                if ("--headless".equalsIgnoreCase(arg) || "--server".equalsIgnoreCase(arg)) {
                    headless = true;
                } else if ("--app".equalsIgnoreCase(arg)) {
                    forceApp = true;
                } else if ("--browser".equalsIgnoreCase(arg)) {
                    forceBrowser = true;
                } else if ("--no-open".equalsIgnoreCase(arg)) {
                    noAutoOpen = true;
                } else {
                    try {
                        port = Integer.parseInt(arg);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        SmartCampusDesktopApp app = new SmartCampusDesktopApp(port);
        int activePort = app.ensureServerRunning();

        if (headless) {
            return;
        }

        final int boundPort = activePort;
        final boolean openApp = forceApp || (!forceBrowser && !noAutoOpen);
        final boolean openBrowser = forceBrowser;

        SwingUtilities.invokeLater(() -> {
            app.buildAndShowWindow(boundPort);
            if (openApp) {
                app.openDesktopAppWindow(boundPort, "/login");
            } else if (openBrowser) {
                app.openInDefaultBrowser(boundPort, "/login");
            }
        });
    }

    public int ensureServerRunning() {
        if (SmartCampusServer.isServerReachable(port)) {
            return port;
        }
        for (int candidate = port; candidate <= port + 10; candidate++) {
            if (SmartCampusServer.isServerReachable(candidate)) {
                return candidate;
            }
            try {
                ownedServer = SmartCampusServer.startServer(candidate);
                return candidate;
            } catch (IOException ignored) {
                // Port occupied by another process; try next candidate
            }
        }
        throw new IllegalStateException("Unable to start Smart Campus Server on ports " + port + "-" + (port + 10));
    }

    private void buildAndShowWindow(int activePort) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JFrame frame = new JFrame("Smart Campus Timetable System — v1.0.0 College Edition (PIET)");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(760, 540);
        frame.setMinimumSize(new Dimension(680, 480));
        frame.setLocationRelativeTo(null);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdownAndExit(frame);
            }
        });

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(SLATE_BG);

        // Top Navy Brand Bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY_BG);
        header.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel brandCol = new JPanel();
        brandCol.setLayout(new BoxLayout(brandCol, BoxLayout.Y_AXIS));
        brandCol.setOpaque(false);

        JLabel titleLbl = new JLabel("SMART CAMPUS TIMETABLE SYSTEM");
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel subLbl = new JLabel("PIET Dept. of Computer Engineering  •  All 8 Semesters & CSP Studio  •  v1.0.0");
        subLbl.setForeground(new Color(148, 163, 184));
        subLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subLbl.setBorder(new EmptyBorder(4, 0, 0, 0));

        brandCol.add(titleLbl);
        brandCol.add(subLbl);

        JPanel statusCol = new JPanel();
        statusCol.setLayout(new BoxLayout(statusCol, BoxLayout.Y_AXIS));
        statusCol.setOpaque(false);

        statusLabel = new JLabel("  ● SERVER ONLINE  ");
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(6, 78, 59));
        statusLabel.setForeground(new Color(110, 231, 183));
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        statusLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));

        urlLabel = new JLabel("http://localhost:" + activePort);
        urlLabel.setForeground(new Color(56, 189, 248));
        urlLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        urlLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        urlLabel.setBorder(new EmptyBorder(6, 0, 0, 0));

        statusCol.add(statusLabel);
        statusCol.add(urlLabel);

        header.add(brandCol, BorderLayout.WEST);
        header.add(statusCol, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // Center Content Panel
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(SLATE_BG);
        body.setBorder(new EmptyBorder(18, 22, 18, 22));

        // Info Card
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GRAY, 1),
                new EmptyBorder(16, 18, 16, 18)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 175));

        JLabel cardTitle = new JLabel("Launch Modes (Desktop Application & Web Browser)");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        cardTitle.setForeground(TEXT_DARK);

        JLabel credsInfo = new JLabel("<html><div style='color:#475569;font-size:11.5px;line-height:1.45;'>"
                + "Use <b>Desktop App Window</b> for a standalone native window (no browser tabs/URL bar), or <b>Open in Web Browser</b>.<br>"
                + "<b>Demo Credentials:</b> Admin <code>admin@piet.edu / admin123</code> &bull; Faculty <code>rajesh.verma@piet.edu / faculty123</code> &bull; Student <code>bhaweshs1@piet.edu / student123</code>"
                + "</div></html>");

        JPanel btnGrid = new JPanel(new GridLayout(1, 3, 10, 0));
        btnGrid.setOpaque(false);

        JButton appBtn = createStyledButton("Launch Desktop App", ROYAL_BLUE);
        appBtn.addActionListener(e -> openDesktopAppWindow(activePort, "/login"));

        JButton browserBtn = createStyledButton("Open in Browser", CYAN);
        browserBtn.addActionListener(e -> openInDefaultBrowser(activePort, "/login"));

        JButton verifyBtn = createStyledButton("Run CSP Check", EMERALD);
        verifyBtn.addActionListener(e -> runEmbeddedVerification());

        btnGrid.add(appBtn);
        btnGrid.add(browserBtn);
        btnGrid.add(verifyBtn);

        card.add(cardTitle, BorderLayout.NORTH);
        card.add(credsInfo, BorderLayout.CENTER);
        card.add(btnGrid, BorderLayout.SOUTH);

        body.add(card);
        body.add(Box.createVerticalStrut(14));

        // Log Panel
        JPanel logHeader = new JPanel(new BorderLayout());
        logHeader.setOpaque(false);
        logHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        JLabel logTitle = new JLabel("Runtime Activity & Server Log");
        logTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        logTitle.setForeground(TEXT_MUTED);
        logHeader.add(logTitle, BorderLayout.WEST);

        body.add(logHeader);
        body.add(Box.createVerticalStrut(6));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(15, 23, 42));
        logArea.setForeground(new Color(226, 232, 240));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBorder(new EmptyBorder(10, 12, 10, 12));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_GRAY, 1));
        body.add(scroll);

        root.add(body, BorderLayout.CENTER);

        // Footer Action Bar
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_GRAY),
                new EmptyBorder(10, 22, 10, 22)
        ));

        JLabel noteLbl = new JLabel(ownedServer != null
                ? "Embedded Java server started by this Desktop App."
                : "Attached to active background Smart Campus server on port " + activePort + ".");
        noteLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        noteLbl.setForeground(TEXT_MUTED);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBtns.setOpaque(false);

        JButton directDashBtn = createStyledButton("Quick Dashboard (Skip Login)", PURPLE);
        directDashBtn.addActionListener(e -> openDesktopAppWindow(activePort, "/dashboard?demo=1"));

        JButton exitBtn = createStyledButton(ownedServer != null ? "Stop Server & Exit" : "Close Window", CORAL_RED);
        exitBtn.addActionListener(e -> shutdownAndExit(frame));

        rightBtns.add(directDashBtn);
        rightBtns.add(exitBtn);

        footer.add(noteLbl, BorderLayout.WEST);
        footer.add(rightBtns, BorderLayout.EAST);
        root.add(footer, BorderLayout.SOUTH);

        frame.setContentPane(root);
        frame.setVisible(true);

        appendLog("Smart Campus Enterprise Runtime active on http://localhost:" + activePort);
        appendLog("Animated Login URL: http://localhost:" + activePort + "/login");
        appendLog("Mode A (UI Demo): 90 slots | Mode B (PIET Sem 1-8): 309 Sem-3 master cells.");
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isPressed() ? bg.darker() : (getModel().isRollover() ? bg.brighter() : bg);
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setRolloverEnabled(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        return btn;
    }

    public void openDesktopAppWindow(int activePort, String path) {
        String targetUrl = "http://localhost:" + activePort + path;
        String browserExe = findChromiumExecutable();
        if (browserExe != null) {
            try {
                new ProcessBuilder(
                        browserExe,
                        "--app=" + targetUrl,
                        "--window-size=1360,860"
                ).start();
                appendLog("Launched standalone Desktop App Window (" + new File(browserExe).getName() + "): " + targetUrl);
                return;
            } catch (IOException ex) {
                appendLog("App mode launch failed (" + ex.getMessage() + "), falling back to browser...");
            }
        }
        openInDefaultBrowser(activePort, path);
    }

    public void openInDefaultBrowser(int activePort, String path) {
        String targetUrl = "http://localhost:" + activePort + path;
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(targetUrl));
                appendLog("Opened in default Web Browser: " + targetUrl);
            } else {
                appendLog("Open manually in browser: " + targetUrl);
            }
        } catch (Exception ex) {
            appendLog("Could not launch browser: " + ex.getMessage() + " -> URL: " + targetUrl);
        }
    }

    private void runEmbeddedVerification() {
        SmartCampusDataStore ds = SmartCampusServer.getDataStore();
        Map<String, Object> report = ds.runAutoGenerator("MODE_B", "ALL", "STRICT", true, true, true, true);
        appendLog("CSP Auto-Generator Check -> Generated: " + report.get("totalSlotsGenerated")
                + " slots | Hard Clashes: " + report.get("hardClashes")
                + " | Soft Warnings: " + report.get("softWarningCount"));
    }

    private static String findChromiumExecutable() {
        List<String> candidates = Arrays.asList(
                "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
                "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe",
                "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"
        );
        for (String c : candidates) {
            if (new File(c).isFile()) {
                return c;
            }
        }
        return null;
    }

    private void appendLog(String msg) {
        if (logArea == null) return;
        String ts = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + ts + "] " + msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void shutdownAndExit(JFrame frame) {
        if (ownedServer != null) {
            ownedServer.stop(0);
        }
        frame.dispose();
        System.exit(0);
    }
}
