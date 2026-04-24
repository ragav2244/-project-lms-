package lms.ui;

import lms.db.DBConnection;
import lms.util.UI;
import static lms.util.UI.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;
import java.util.*;

public class CatalogPanel extends JPanel {

    private final MainWindow parent;
    private JTable           table;
    private DefaultTableModel model;
    private JTextField        searchBox;
    private JLabel            countLbl;

    private static final String[] COLS = {"ID","Title","Author","Genre","Total","Available","Status"};
    private static final String[] GENRES = {
        "Select Genre","Fiction","Non-Fiction","Science","Technology",
        "History","Biography","Mathematics","Literature","Philosophy",
        "Self-Help","Business","Arts","Law","Medicine","Reference","Other"
    };

    public CatalogPanel(MainWindow parent) {
        this.parent = parent;
        setBackground(BG_APP);
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        setBorder(BorderFactory.createEmptyBorder(24,24,24,24));

        // ── Top header ──
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);

        JPanel ht = new JPanel(); ht.setOpaque(false); ht.setLayout(new BoxLayout(ht,BoxLayout.Y_AXIS));
        JLabel t = title("Book Catalog"); t.setAlignmentX(LEFT_ALIGNMENT); ht.add(t);
        JLabel s = subtitle("Manage your complete book collection"); s.setAlignmentX(LEFT_ALIGNMENT); ht.add(s);
        hdr.add(ht, BorderLayout.WEST);

        countLbl = new JLabel("0 records");
        countLbl.setFont(mono(12)); countLbl.setForeground(TEXT_MUTED);
        JPanel cw = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,14)); cw.setOpaque(false); cw.add(countLbl);
        hdr.add(cw, BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        // ── Toolbar + table ──
        JPanel center = new JPanel(new BorderLayout(0,12));
        center.setOpaque(false);
        center.add(buildToolbar(), BorderLayout.NORTH);
        center.add(buildTable(),   BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel() {
            protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),12,12));
                g2.setColor(BORDER_CARD);
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0.5f,0.5f,getWidth()-1,getHeight()-1,12,12));
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setLayout(new FlowLayout(FlowLayout.LEFT,10,10));

        searchBox = inputField("Search books...");
        searchBox.setPreferredSize(new Dimension(240,36));
        searchBox.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            public void insertUpdate(javax.swing.event.DocumentEvent e){reload();}
            public void removeUpdate(javax.swing.event.DocumentEvent e){reload();}
            public void changedUpdate(javax.swing.event.DocumentEvent e){reload();}
        });
        bar.add(searchBox);

        JButton addBtn  = btnGreen("+ Add Book");    addBtn.setPreferredSize(new Dimension(110,36));  addBtn.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){showAddDialog();}});
        JButton editBtn = btnBlue("Edit");           editBtn.setPreferredSize(new Dimension(80,36));  editBtn.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){showEditDialog();}});
        JButton delBtn  = btnRed("Delete");          delBtn.setPreferredSize(new Dimension(80,36));   delBtn.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){deleteSelected();}});
        JButton refBtn  = btnPurple("Refresh");      refBtn.setPreferredSize(new Dimension(85,36));   refBtn.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){reload();}});

        bar.add(addBtn); bar.add(editBtn); bar.add(delBtn); bar.add(refBtn);
        return bar;
    }

    private JPanel buildTable() {
        model = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row%2==0?BG_ROW_EVEN:BG_ROW_ODD);
                else c.setBackground(BG_ROW_SEL);
                return c;
            }
        };
        styleTable();

        JPanel card = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),12,12));
                g2.setColor(BORDER_CARD);
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0.5f,0.5f,getWidth()-1,getHeight()-1,12,12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        JScrollPane sp = new JScrollPane(table);
        UI.styleScroll(sp);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private void styleTable() {
        table.setRowHeight(40);
        table.setFont(plain(13));
        table.setForeground(TEXT_BODY);
        table.setBackground(BG_ROW_EVEN);
        table.setGridColor(new Color(235,240,252));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(BG_ROW_SEL);
        table.setFillsViewportHeight(true);
        table.setFocusable(false);
        table.setIntercellSpacing(new Dimension(0,1));

        JTableHeader hdr = table.getTableHeader();
        hdr.setBackground(BG_TABLE_HDR);
        hdr.setForeground(TEXT_DARK);
        hdr.setFont(bold(12));
        hdr.setPreferredSize(new Dimension(0,42));
        hdr.setBorder(new MatteBorder(0,0,1,0,BORDER_CARD));
        hdr.setReorderingAllowed(false);

        int[] w={60,280,200,120,70,90,90};
        for(int i=0;i<w.length;i++) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for(int i:new int[]{0,4,5}) table.getColumnModel().getColumn(i).setCellRenderer(center);
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
    }

    public void reload() {
        model.setRowCount(0);
        String q = searchBox!=null ? searchBox.getText().trim() : "";
        try(Connection c=DBConnection.get()) {
            String sql = "SELECT catalog_id,title,author,genre,total_copies,avail_copies FROM tbl_catalog" +
                (q.isEmpty() ? "" : " WHERE LOWER(title) LIKE LOWER(?) OR LOWER(author) LIKE LOWER(?)") +
                " ORDER BY title";
            PreparedStatement ps = c.prepareStatement(sql);
            if(!q.isEmpty()){ ps.setString(1,"%"+q+"%"); ps.setString(2,"%"+q+"%"); }
            ResultSet rs=ps.executeQuery(); int cnt=0;
            while(rs.next()){
                int avail=rs.getInt("avail_copies");
                model.addRow(new Object[]{
                    rs.getInt("catalog_id"), rs.getString("title"), rs.getString("author"),
                    rs.getString("genre"), rs.getInt("total_copies"), avail,
                    avail>0?"Available":"Out of Stock"
                });
                cnt++;
            }
            countLbl.setText(cnt+" record"+(cnt!=1?"s":""));
        } catch(SQLException ex){ JOptionPane.showMessageDialog(this,ex.getMessage(),"DB Error",JOptionPane.ERROR_MESSAGE); }
    }

    // ── Add Dialog ────────────────────────────────────────────────────────────
    private void showAddDialog() {
        JDialog dlg = makeDialog("Add New Book", 460, 400);
        JPanel p = formPanel();

        JTextField titleF  = inputField("Book title");
        JTextField authorF = inputField("Author name");
        JComboBox<String> genreC = combo(GENRES);
        JTextField qtyF    = inputField("e.g. 3");
        JLabel msgL = new JLabel(" "); msgL.setFont(plain(12)); msgL.setForeground(ERR_FG);

        addRow(p,"Title *",   titleF);
        addRow(p,"Author *",  authorF);
        addRow(p,"Genre *",   genreC);
        addRow(p,"Quantity *",qtyF);
        addRow(p,"",msgL);

        JButton save = btnGreen("Save Book"); save.setPreferredSize(new Dimension(130,38));
        JButton cancel = btnRed("Cancel");   cancel.setPreferredSize(new Dimension(90,38));
        cancel.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){dlg.dispose();}});
        save.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                String ti=titleF.getText().trim(), au=authorF.getText().trim();
                String ge=(String)genreC.getSelectedItem(), qt=qtyF.getText().trim();
                if(ti.isEmpty()||au.isEmpty()){msgL.setText("Title and Author required.");return;}
                if(ge==null||ge.equals("Select Genre")){msgL.setText("Select a genre.");return;}
                int qty; try{qty=Integer.parseInt(qt);if(qty<=0)throw new NumberFormatException();}
                catch(NumberFormatException ex){msgL.setText("Quantity must be positive number.");return;}
                try(Connection c=DBConnection.get()){
                    PreparedStatement chk=c.prepareStatement("SELECT catalog_id FROM tbl_catalog WHERE LOWER(title)=LOWER(?) AND LOWER(author)=LOWER(?)");
                    chk.setString(1,ti); chk.setString(2,au);
                    if(chk.executeQuery().next()){msgL.setText("Book with same title & author exists.");msgL.setForeground(WARN_FG);return;}
                    PreparedStatement ins=c.prepareStatement("INSERT INTO tbl_catalog(title,author,genre,total_copies,avail_copies) VALUES(?,?,?,?,?)");
                    ins.setString(1,ti);ins.setString(2,au);ins.setString(3,ge);ins.setInt(4,qty);ins.setInt(5,qty);
                    ins.executeUpdate();
                    dlg.dispose(); reload(); parent.refreshDash();
                } catch(SQLException ex){msgL.setText("DB Error: "+ex.getMessage()); msgL.setForeground(ERR_FG);}
            }
        });

        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); btnRow.setOpaque(false);
        btnRow.add(cancel); btnRow.add(save);
        p.add(btnRow);
        dlg.add(p); dlg.setVisible(true);
    }

    // ── Edit Dialog ───────────────────────────────────────────────────────────
    private void showEditDialog() {
        int row = table.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Select a row to edit.","Info",JOptionPane.INFORMATION_MESSAGE);return;}
        int id         = (int)model.getValueAt(row,0);
        String oldTitle= (String)model.getValueAt(row,1);
        String oldAuth = (String)model.getValueAt(row,2);
        String oldGenre= (String)model.getValueAt(row,3);
        int oldTotal   = (int)model.getValueAt(row,4);

        JDialog dlg = makeDialog("Edit Book", 460, 400);
        JPanel p = formPanel();

        JTextField titleF  = inputField("Book title"); titleF.setText(oldTitle);
        JTextField authorF = inputField("Author name"); authorF.setText(oldAuth);
        JComboBox<String> genreC = combo(GENRES); genreC.setSelectedItem(oldGenre);
        JTextField qtyF    = inputField("Total quantity"); qtyF.setText(String.valueOf(oldTotal));
        JLabel msgL = new JLabel(" "); msgL.setFont(plain(12)); msgL.setForeground(ERR_FG);

        addRow(p,"Title *",   titleF);
        addRow(p,"Author *",  authorF);
        addRow(p,"Genre *",   genreC);
        addRow(p,"Total Qty *",qtyF);
        addRow(p,"",msgL);

        JButton save   = btnBlue("Update"); save.setPreferredSize(new Dimension(100,38));
        JButton cancel = btnRed("Cancel");  cancel.setPreferredSize(new Dimension(90,38));
        cancel.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){dlg.dispose();}});
        save.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                String ti=titleF.getText().trim(), au=authorF.getText().trim();
                String ge=(String)genreC.getSelectedItem(), qt=qtyF.getText().trim();
                if(ti.isEmpty()||au.isEmpty()){msgL.setText("Title and Author required.");return;}
                if(ge==null||ge.equals("Select Genre")){msgL.setText("Select a genre.");return;}
                int qty; try{qty=Integer.parseInt(qt);if(qty<=0)throw new NumberFormatException();}
                catch(NumberFormatException ex){msgL.setText("Quantity must be positive number.");return;}
                try(Connection c=DBConnection.get()){
                    // Get current avail to adjust
                    PreparedStatement cur=c.prepareStatement("SELECT total_copies,avail_copies FROM tbl_catalog WHERE catalog_id=?");
                    cur.setInt(1,id); ResultSet rs=cur.executeQuery(); rs.next();
                    int curTotal=rs.getInt("total_copies"), curAvail=rs.getInt("avail_copies");
                    int diff=qty-curTotal; int newAvail=Math.max(0,curAvail+diff);
                    PreparedStatement upd=c.prepareStatement("UPDATE tbl_catalog SET title=?,author=?,genre=?,total_copies=?,avail_copies=? WHERE catalog_id=?");
                    upd.setString(1,ti);upd.setString(2,au);upd.setString(3,ge);upd.setInt(4,qty);upd.setInt(5,newAvail);upd.setInt(6,id);
                    upd.executeUpdate();
                    dlg.dispose(); reload(); parent.refreshDash();
                } catch(SQLException ex){msgL.setText("DB Error: "+ex.getMessage());}
            }
        });

        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); btnRow.setOpaque(false);
        btnRow.add(cancel); btnRow.add(save);
        p.add(btnRow);
        dlg.add(p); dlg.setVisible(true);
    }

    // ── Delete ────────────────────────────────────────────────────────────────
    private void deleteSelected() {
        int row=table.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Select a row to delete.","Info",JOptionPane.INFORMATION_MESSAGE);return;}
        int id=(int)model.getValueAt(row,0);
        String name=(String)model.getValueAt(row,1);
        try(Connection c=DBConnection.get()){
            PreparedStatement chk=c.prepareStatement("SELECT COUNT(*) FROM tbl_lending WHERE catalog_id=? AND lend_state='ACTIVE'");
            chk.setInt(1,id); ResultSet rs=chk.executeQuery(); rs.next();
            if(rs.getInt(1)>0){JOptionPane.showMessageDialog(this,"Cannot delete: copies currently issued.","Cannot Delete",JOptionPane.WARNING_MESSAGE);return;}
        } catch(SQLException ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);return;}

        int conf=JOptionPane.showConfirmDialog(this,"Delete \""+name+"\"? This cannot be undone.","Confirm Delete",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(conf!=JOptionPane.YES_OPTION) return;
        try(Connection c=DBConnection.get()){
            PreparedStatement del=c.prepareStatement("DELETE FROM tbl_catalog WHERE catalog_id=?");
            del.setInt(1,id); del.executeUpdate();
            reload(); parent.refreshDash();
        } catch(SQLException ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JDialog makeDialog(String t, int w, int h) {
        JDialog d=new JDialog((JFrame)SwingUtilities.getWindowAncestor(this),t,true);
        d.setSize(w,h); d.setLocationRelativeTo(this); d.setResizable(false);
        d.getContentPane().setBackground(BG_CARD); return d;
    }
    private JPanel formPanel(){
        JPanel p=new JPanel(); p.setBackground(BG_CARD);
        p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20,24,20,24)); return p;
    }
    private void addRow(JPanel p, String lbl, JComponent field){
        if(!lbl.isEmpty()){
            JLabel l=label(lbl); l.setAlignmentX(LEFT_ALIGNMENT); p.add(l);
            p.add(Box.createVerticalStrut(4));
        }
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        p.add(field); p.add(Box.createVerticalStrut(12));
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c){
            super.getTableCellRendererComponent(t,v,sel,foc,r,c);
            setHorizontalAlignment(SwingConstants.CENTER);
            String val=v!=null?v.toString():"";
            boolean ok=val.equals("Available");
            if(!sel){ setBackground(ok?OK_BG:ERR_BG); setForeground(ok?OK_FG:ERR_FG); setFont(bold(11)); }
            return this;
        }
    }
}
