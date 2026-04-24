package lms.ui;

import lms.db.DBConnection;
import lms.util.UI;
import static lms.util.UI.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;
import java.util.*;

public class DashboardPanel extends JPanel {

    private final MainWindow parent;
    private JLabel vTotalBooks, vAvailBooks, vIssuedBooks, vMembers, vGenres;
    private JPanel recentPanel;

    public DashboardPanel(MainWindow parent) {
        this.parent = parent;
        setBackground(BG_APP);
        setLayout(new BorderLayout());
        build();
        refresh();
    }

    private void build() {
        JScrollPane sp = new JScrollPane(content());
        UI.styleScroll(sp);
        sp.getViewport().setBackground(BG_APP);
        add(sp, BorderLayout.CENTER);
    }

    private JPanel content() {
        JPanel root = new JPanel();
        root.setBackground(BG_APP);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(28,28,28,28));

        // header row
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        JPanel ht = new JPanel(); ht.setOpaque(false); ht.setLayout(new BoxLayout(ht,BoxLayout.Y_AXIS));
        JLabel t = title("Dashboard Overview"); t.setAlignmentX(LEFT_ALIGNMENT); ht.add(t);
        JLabel s = subtitle("Welcome back, Administrator"); s.setAlignmentX(LEFT_ALIGNMENT); ht.add(s);
        hdr.add(ht, BorderLayout.WEST);
        JButton refBtn = btnAccent("Refresh");
        refBtn.setPreferredSize(new Dimension(100,36));
        refBtn.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){refresh();}});
        JPanel rbw = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,12)); rbw.setOpaque(false); rbw.add(refBtn);
        hdr.add(rbw, BorderLayout.EAST);
        root.add(hdr);
        root.add(Box.createVerticalStrut(22));

        // stat cards
        JPanel stats = new JPanel(new GridLayout(1,5,14,0));
        stats.setOpaque(false);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        vTotalBooks  = addStat(stats,"Total Books",   "0", ACCENT,       new Color(230,238,255));
        vAvailBooks  = addStat(stats,"Available",     "0", BTN_GREEN,    new Color(230,248,236));
        vIssuedBooks = addStat(stats,"Issued",        "0", BTN_ORANGE,   new Color(255,243,226));
        vMembers     = addStat(stats,"Members",       "0", BTN_BLUE,     new Color(226,245,255));
        vGenres      = addStat(stats,"Genres",        "0", BTN_PURPLE,   new Color(244,236,255));
        root.add(stats);
        root.add(Box.createVerticalStrut(26));

        // quick actions
        JLabel qa = new JLabel("Quick Actions");
        qa.setFont(bold(15)); qa.setForeground(TEXT_DARK);
        root.add(qa);
        root.add(Box.createVerticalStrut(12));

        JPanel actions = new JPanel(new GridLayout(1,4,14,0));
        actions.setOpaque(false);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        addAction(actions,"+ Add Book",    BTN_GREEN,  BTN_GREEN_H,  "Catalog");
        addAction(actions,"Issue Book",    ACCENT,     ACCENT_DARK,  "Issue Book");
        addAction(actions,"Return Book",   BTN_BLUE,   BTN_BLUE_H,   "Return Book");
        addAction(actions,"View Members",  BTN_PURPLE, BTN_PURPLE_H, "Members");
        root.add(actions);
        root.add(Box.createVerticalStrut(26));

        // recent issues
        JLabel rl = new JLabel("Recently Issued");
        rl.setFont(bold(15)); rl.setForeground(TEXT_DARK);
        root.add(rl);
        root.add(Box.createVerticalStrut(10));

        recentPanel = new JPanel();
        recentPanel.setOpaque(false);
        recentPanel.setLayout(new BoxLayout(recentPanel,BoxLayout.Y_AXIS));
        root.add(recentPanel);

        return root;
    }

    private JLabel addStat(JPanel p, String title, String val, Color accent, Color bg) {
        JPanel card = new JPanel(new GridBagLayout()){
            protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),14,14));
                // accent top strip
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),34,14,14));
                g2.fillRect(0,20,getWidth(),14);
                g2.setColor(BORDER_CARD);
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0.5f,0.5f,getWidth()-1,getHeight()-1,14,14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        GridBagConstraints gc=new GridBagConstraints();
        gc.gridx=0; gc.anchor=GridBagConstraints.CENTER;

        JLabel vl = new JLabel(val);
        vl.setFont(bold(28)); vl.setForeground(accent);
        gc.gridy=0; gc.insets=new Insets(18,0,4,0);
        card.add(vl,gc);

        JLabel tl = new JLabel(title);
        tl.setFont(plain(12)); tl.setForeground(TEXT_MUTED);
        gc.gridy=1; gc.insets=new Insets(0,0,16,0);
        card.add(tl,gc);

        p.add(card);
        return vl;
    }

    private void addAction(JPanel p, String label, Color base, Color hover, final String target) {
        JButton btn = new UI.FlatBtn(label,base,hover);
        btn.setFont(bold(13));
        btn.setPreferredSize(new Dimension(150,48));
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){parent.goTo(target);}
        });
        p.add(btn);
    }

    public void refresh() {
        new Thread(new Runnable(){
            public void run(){
                try(Connection c=DBConnection.get(); Statement s=c.createStatement()){
                    ResultSet rs;

                    rs=s.executeQuery("SELECT COUNT(*) FROM tbl_catalog");
                    final int total=rs.next()?rs.getInt(1):0;

                    rs=s.executeQuery("SELECT COALESCE(SUM(avail_copies),0) FROM tbl_catalog");
                    final int avail=rs.next()?rs.getInt(1):0;

                    rs=s.executeQuery("SELECT COUNT(*) FROM tbl_lending WHERE lend_state='ACTIVE'");
                    final int issued=rs.next()?rs.getInt(1):0;

                    rs=s.executeQuery("SELECT COUNT(*) FROM tbl_members");
                    final int members=rs.next()?rs.getInt(1):0;

                    rs=s.executeQuery("SELECT COUNT(DISTINCT genre) FROM tbl_catalog");
                    final int genres=rs.next()?rs.getInt(1):0;

                    rs=s.executeQuery(
                        "SELECT l.book_title, l.member_name, l.lend_date, c.genre " +
                        "FROM tbl_lending l JOIN tbl_catalog c ON l.catalog_id=c.catalog_id " +
                        "WHERE l.lend_state='ACTIVE' ORDER BY l.lending_id DESC LIMIT 6");
                    final java.util.List<String[]> rows=new ArrayList<>();
                    while(rs.next()) rows.add(new String[]{
                        rs.getString("book_title"), rs.getString("member_name"),
                        rs.getDate("lend_date").toString(), rs.getString("genre")
                    });

                    SwingUtilities.invokeLater(new Runnable(){
                        public void run(){
                            vTotalBooks.setText(String.valueOf(total));
                            vAvailBooks.setText(String.valueOf(avail));
                            vIssuedBooks.setText(String.valueOf(issued));
                            vMembers.setText(String.valueOf(members));
                            vGenres.setText(String.valueOf(genres));
                            buildRecent(rows);
                        }
                    });
                } catch(SQLException ex){ ex.printStackTrace(); }
            }
        }).start();
    }

    private void buildRecent(java.util.List<String[]> rows) {
        recentPanel.removeAll();

        // header
        JPanel hdr = recentRow("BOOK TITLE","MEMBER","ISSUE DATE","GENRE",true);
        hdr.setMaximumSize(new Dimension(Integer.MAX_VALUE,38));
        recentPanel.add(hdr);
        recentPanel.add(Box.createVerticalStrut(2));

        if(rows.isEmpty()){
            JLabel empty=new JLabel("  No active issues.");
            empty.setFont(plain(13)); empty.setForeground(TEXT_MUTED);
            recentPanel.add(empty);
        } else {
            for(String[] row : rows){
                JPanel r=recentRow(row[0],row[1],row[2],row[3],false);
                r.setMaximumSize(new Dimension(Integer.MAX_VALUE,44));
                recentPanel.add(r);
                recentPanel.add(Box.createVerticalStrut(3));
            }
        }
        recentPanel.revalidate(); recentPanel.repaint();
    }

    private JPanel recentRow(String a, String b, String c, String d, final boolean hdr){
        JPanel row = new JPanel(new GridLayout(1,4,0,0)){
            protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hdr ? BG_TABLE_HDR : BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8));
                if(!hdr){g2.setColor(BORDER_CARD);g2.setStroke(new BasicStroke(0.8f));g2.draw(new RoundRectangle2D.Float(0.5f,0.5f,getWidth()-1,getHeight()-1,8,8));}
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
        for(String v : new String[]{a,b,c,d}){
            JLabel l=new JLabel(v);
            l.setFont(hdr ? bold(11) : plain(13));
            l.setForeground(hdr ? TEXT_DARK : TEXT_BODY);
            row.add(l);
        }
        return row;
    }
}
