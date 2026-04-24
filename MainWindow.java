package lms.ui;

import lms.util.UI;
import static lms.util.UI.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class MainWindow extends JFrame {

    private CardLayout cards;
    private JPanel     content;
    private String     active = "Dashboard";

    private static final String[] PAGES  = {"Dashboard","Catalog","Members","Issue Book","Return Book","Lending Log"};
    private static final String[] ICONS  = {"⊞","📚","👤","↑","↓","📋"};

    private JPanel[]   navBtns;
    private JLabel     pageTitle;

    // panels
    private DashboardPanel   dashPanel;
    private CatalogPanel     catalogPanel;
    private MembersPanel     membersPanel;
    private IssuePanel       issuePanel;
    private ReturnPanel      returnPanel;
    private LendingLogPanel  logPanel;

    public MainWindow() {
        buildUI();
    }

    private void buildUI() {
        setTitle("LibMS — Library Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1350, 820);
        setMinimumSize(new Dimension(1150, 680));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_APP);
        root.add(buildSidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_APP);
        main.add(buildTopBar(), BorderLayout.NORTH);

        cards   = new CardLayout();
        content = new JPanel(cards);
        content.setBackground(BG_APP);

        dashPanel    = new DashboardPanel(this);
        catalogPanel = new CatalogPanel(this);
        membersPanel = new MembersPanel(this);
        issuePanel   = new IssuePanel(this);
        returnPanel  = new ReturnPanel(this);
        logPanel     = new LendingLogPanel(this);

        content.add(dashPanel,    "Dashboard");
        content.add(catalogPanel, "Catalog");
        content.add(membersPanel, "Members");
        content.add(issuePanel,   "Issue Book");
        content.add(returnPanel,  "Return Book");
        content.add(logPanel,     "Lending Log");

        main.add(content, BorderLayout.CENTER);
        root.add(main, BorderLayout.CENTER);
        setContentPane(root);
        goTo("Dashboard");
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sb = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                GradientPaint gp = new GradientPaint(0,0,new Color(20,38,86),0,getHeight(),new Color(35,60,120));
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        sb.setPreferredSize(new Dimension(210, 0));
        sb.setOpaque(false);
        sb.setLayout(new BorderLayout());

        // Logo
        JPanel logo = new JPanel(new GridBagLayout());
        logo.setOpaque(false);
        logo.setPreferredSize(new Dimension(210, 72));
        logo.setBorder(new MatteBorder(0,0,1,0,new Color(255,255,255,30)));
        JLabel logoLabel = new JLabel("📚  LibMS");
        logoLabel.setFont(bold(17));
        logoLabel.setForeground(Color.WHITE);
        logo.add(logoLabel);
        sb.add(logo, BorderLayout.NORTH);

        // Nav items
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(BorderFactory.createEmptyBorder(14,0,14,0));

        navBtns = new JPanel[PAGES.length];
        for (int i = 0; i < PAGES.length; i++) {
            final String pg = PAGES[i];
            final String ic = ICONS[i];
            JPanel btn = makeNavBtn(pg, ic);
            navBtns[i] = btn;
            nav.add(btn);
            nav.add(Box.createVerticalStrut(2));
        }
        sb.add(nav, BorderLayout.CENTER);

        // Bottom logout
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setPreferredSize(new Dimension(210,60));
        bottom.setBorder(new MatteBorder(1,0,0,0,new Color(255,255,255,30)));

        JButton logout = new JButton("  ⎋  Logout") {
            private boolean hov=false;
            {
                setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
                setFont(bold(13)); setForeground(new Color(180,205,245));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter(){
                    public void mouseEntered(MouseEvent e){hov=true;setForeground(Color.WHITE);repaint();}
                    public void mouseExited(MouseEvent e){hov=false;setForeground(new Color(180,205,245));repaint();}
                });
            }
            protected void paintComponent(Graphics g){
                if(hov){Graphics2D g2=(Graphics2D)g.create();g2.setColor(new Color(255,255,255,15));g2.fillRect(0,0,getWidth(),getHeight());g2.dispose();}
                super.paintComponent(g);
            }
        };
        logout.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                int r=JOptionPane.showConfirmDialog(MainWindow.this,"Logout?","Confirm",JOptionPane.YES_NO_OPTION);
                if(r==JOptionPane.YES_OPTION){dispose();new LoginScreen().setVisible(true);}
            }
        });
        bottom.add(logout, BorderLayout.CENTER);
        sb.add(bottom, BorderLayout.SOUTH);

        return sb;
    }

    private JPanel makeNavBtn(final String name, final String icon) {
        JPanel p = new JPanel(new BorderLayout()) {
            boolean hov=false;
            {
                setOpaque(false);
                setMaximumSize(new Dimension(210,44));
                setPreferredSize(new Dimension(210,44));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setBorder(BorderFactory.createEmptyBorder(0,12,0,12));
                addMouseListener(new MouseAdapter(){
                    public void mouseEntered(MouseEvent e){hov=true;repaint();}
                    public void mouseExited(MouseEvent e){hov=false;repaint();}
                    public void mouseClicked(MouseEvent e){goTo(name);}
                });
            }
            protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel=name.equals(active);
                if(sel){
                    g2.setColor(BG_SIDE_SEL);
                    g2.fill(new RoundRectangle2D.Float(8,4,getWidth()-16,getHeight()-8,10,10));
                } else if(hov){
                    g2.setColor(new Color(255,255,255,15));
                    g2.fill(new RoundRectangle2D.Float(8,4,getWidth()-16,getHeight()-8,10,10));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        JLabel lbl = new JLabel("  " + icon + "  " + name);
        lbl.setFont(name.equals(active) ? bold(13) : plain(13));
        lbl.setForeground(name.equals(active) ? Color.WHITE : new Color(180,205,245));
        p.add(lbl, BorderLayout.CENTER);
        p.putClientProperty("label",lbl);
        p.putClientProperty("page",name);
        return p;
    }

    // ── Top bar ───────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g){
                g.setColor(BG_TOPBAR);
                g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(BORDER_CARD);
                g.fillRect(0,getHeight()-1,getWidth(),1);
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0,64));
        bar.setBorder(BorderFactory.createEmptyBorder(0,28,0,28));

        pageTitle = new JLabel("Dashboard");
        pageTitle.setFont(bold(20));
        pageTitle.setForeground(TEXT_DARK);
        bar.add(pageTitle, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,16,0));
        right.setOpaque(false);

        final JLabel clock = new JLabel();
        clock.setFont(mono(13));
        clock.setForeground(TEXT_MUTED);
        Timer clkTimer = new Timer(1000, new ActionListener(){
            public void actionPerformed(ActionEvent e){
                java.time.LocalTime t=java.time.LocalTime.now();
                clock.setText(String.format("%02d:%02d:%02d",t.getHour(),t.getMinute(),t.getSecond()));
            }
        });
        clkTimer.start();

        JLabel adminLbl = new JLabel("Administrator");
        adminLbl.setFont(bold(13));
        adminLbl.setForeground(TEXT_DARK);

        right.add(clock);
        right.add(adminLbl);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    public void goTo(String name) {
        active = name;
        cards.show(content, name);
        pageTitle.setText(name);

        if (name.equals("Dashboard"))   dashPanel.refresh();
        if (name.equals("Issue Book"))  issuePanel.refresh();
        if (name.equals("Return Book")) returnPanel.refresh();
        if (name.equals("Lending Log")) logPanel.refresh();
        if (name.equals("Catalog"))     catalogPanel.reload();
        if (name.equals("Members"))     membersPanel.reload();

        for (JPanel btn : navBtns) {
            String pg  = (String) btn.getClientProperty("page");
            JLabel lbl = (JLabel) btn.getClientProperty("label");
            boolean sel = pg.equals(name);
            lbl.setForeground(sel ? Color.WHITE : new Color(180,205,245));
            lbl.setFont(sel ? bold(13) : plain(13));
            btn.repaint();
        }
    }

    public void refreshDash() { dashPanel.refresh(); }
}
