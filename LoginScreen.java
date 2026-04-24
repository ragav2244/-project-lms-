package lms.ui;

import lms.db.DBConnection;
import lms.util.UI;
import static lms.util.UI.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class LoginScreen extends JFrame {

    private JTextField    userField;
    private JPasswordField passField;
    private JLabel        msgLabel;
    private JButton       loginBtn;

    public LoginScreen() {
        UI.applyDefaults();
        buildUI();
    }

    private void buildUI() {
        setTitle("LibMS — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(920, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(buildLeft());
        root.add(buildRight());
        setContentPane(root);
    }

    // ── Left blue branding panel ──────────────────────────────────────────────
    private JPanel buildLeft() {
        JPanel p = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, new Color(28,56,121), getWidth(), getHeight(), new Color(59,109,217));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // decorative circles
                g2.setColor(new Color(255,255,255,18));
                g2.fillOval(-60,-60,260,260);
                g2.fillOval(getWidth()-120, getHeight()-120, 240, 240);
                g2.setColor(new Color(255,255,255,10));
                g2.fillOval(80, 120, 300, 300);
                g2.dispose();
            }
        };
        p.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = GridBagConstraints.RELATIVE;
        g.anchor = GridBagConstraints.CENTER;

        JLabel icon = new JLabel("📚");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 54));
        g.insets = new Insets(0,0,16,0);
        p.add(icon, g);

        JLabel appName = new JLabel("LibMS");
        appName.setFont(bold(38));
        appName.setForeground(Color.WHITE);
        g.insets = new Insets(0,0,8,0);
        p.add(appName, g);

        JLabel tagLine = new JLabel("Library Management System");
        tagLine.setFont(plain(15));
        tagLine.setForeground(new Color(200,220,255));
        g.insets = new Insets(0,0,36,0);
        p.add(tagLine, g);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255,255,255,60));
        sep.setPreferredSize(new Dimension(220,1));
        g.insets = new Insets(0,0,28,0);
        p.add(sep, g);

        for (String feat : new String[]{"Manage Book Catalog", "Member Registration", "Issue & Return Books", "Dashboard Analytics"}) {
            JLabel fl = new JLabel("✓   " + feat);
            fl.setFont(plain(14));
            fl.setForeground(new Color(200,220,255));
            g.insets = new Insets(5,0,5,0);
            p.add(fl, g);
        }
        return p;
    }

    // ── Right login form ──────────────────────────────────────────────────────
    private JPanel buildRight() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(new Color(246,249,255));

        JPanel card = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),18,18));
                g2.setColor(BORDER_CARD);
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0.5f,0.5f,getWidth()-1,getHeight()-1,18,18));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(360, 420));
        card.setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;

        JLabel welcome = new JLabel("Welcome Back");
        welcome.setFont(bold(26));
        welcome.setForeground(TEXT_DARK);
        welcome.setHorizontalAlignment(SwingConstants.CENTER);
        gc.gridy=0; gc.insets=new Insets(32,36,4,36);
        card.add(welcome, gc);

        JLabel sub = new JLabel("Sign in to your admin account");
        sub.setFont(plain(13));
        sub.setForeground(TEXT_MUTED);
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        gc.gridy=1; gc.insets=new Insets(0,36,24,36);
        card.add(sub, gc);

        JLabel ul = label("USERNAME");
        gc.gridy=2; gc.insets=new Insets(0,36,5,36);
        card.add(ul, gc);

        userField = inputField("admin");
        userField.setPreferredSize(new Dimension(280,40));
        gc.gridy=3; gc.insets=new Insets(0,36,14,36);
        card.add(userField, gc);

        JLabel pl = label("PASSWORD");
        gc.gridy=4; gc.insets=new Insets(0,36,5,36);
        card.add(pl, gc);

        passField = passField();
        passField.setPreferredSize(new Dimension(280,40));
        gc.gridy=5; gc.insets=new Insets(0,36,10,36);
        card.add(passField, gc);

        msgLabel = new JLabel(" ");
        msgLabel.setFont(plain(12));
        msgLabel.setForeground(ERR_FG);
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gc.gridy=6; gc.insets=new Insets(0,36,6,36);
        card.add(msgLabel, gc);

        loginBtn = btnAccent("Sign In");
        loginBtn.setPreferredSize(new Dimension(280,42));
        loginBtn.setFont(bold(14));
        gc.gridy=7; gc.insets=new Insets(0,36,10,36);
        card.add(loginBtn, gc);

        JLabel hint = new JLabel("Demo: admin / admin123");
        hint.setFont(mono(11));
        hint.setForeground(TEXT_MUTED);
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        gc.gridy=8; gc.insets=new Insets(0,36,28,36);
        card.add(hint, gc);

        outer.add(card);

        loginBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { doLogin(); }
        });
        passField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { doLogin(); }
        });
        userField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { passField.requestFocus(); }
        });

        return outer;
    }

    private void doLogin() {
        String u = userField.getText().trim();
        String p = new String(passField.getPassword()).trim();

        if (u.isEmpty() || p.isEmpty()) { msg("Please fill all fields.", false); return; }
        if (!u.equals("admin") || !p.equals("admin123")) { msg("Invalid credentials.", false); passField.setText(""); return; }

        loginBtn.setEnabled(false);
        loginBtn.setText("Connecting...");

        new Thread(new Runnable() {
            public void run() {
                final boolean ok = DBConnection.test();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (!ok) {
                            msg("Cannot connect to database. Check MySQL.", false);
                            loginBtn.setEnabled(true);
                            loginBtn.setText("Sign In");
                            return;
                        }
                        msg("Login successful!", true);
                        Timer t = new Timer(500, new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                dispose();
                                new MainWindow().setVisible(true);
                            }
                        });
                        t.setRepeats(false);
                        t.start();
                    }
                });
            }
        }).start();
    }

    private void msg(String text, boolean ok) {
        msgLabel.setText(text);
        msgLabel.setForeground(ok ? OK_FG : ERR_FG);
    }
}
