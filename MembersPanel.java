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

public class MembersPanel extends JPanel {

    private final MainWindow parent;
    private JTable           table;
    private DefaultTableModel model;
    private JTextField        searchBox;
    private JLabel            countLbl;

    private static final String[] COLS = {"ID","Full Name","Roll No","Department","Joined On"};
    private static final String[] DEPTS = {
        "Select Department","Computer Science","Information Technology","Mechanical Engineering",
        "Electronics","Civil Engineering","Chemical Engineering","Physics","Mathematics","Commerce","Arts","Other"
    };

    public MembersPanel(MainWindow parent) {
        this.parent = parent;
        setBackground(BG_APP);
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        setBorder(BorderFactory.createEmptyBorder(24,24,24,24));

        JPanel hdr = new JPanel(new BorderLayout()); hdr.setOpaque(false);
        JPanel ht = new JPanel(); ht.setOpaque(false); ht.setLayout(new BoxLayout(ht,BoxLayout.Y_AXIS));
        JLabel t = title("Members"); t.setAlignmentX(LEFT_ALIGNMENT); ht.add(t);
        JLabel s = subtitle("Manage library members and students"); s.setAlignmentX(LEFT_ALIGNMENT); ht.add(s);
        hdr.add(ht,BorderLayout.WEST);
        countLbl=new JLabel("0 records"); countLbl.setFont(mono(12)); countLbl.setForeground(TEXT_MUTED);
        JPanel cw=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,14)); cw.setOpaque(false); cw.add(countLbl);
        hdr.add(cw,BorderLayout.EAST);
        add(hdr, BorderLayout.NORTH);

        JPanel center=new JPanel(new BorderLayout(0,12)); center.setOpaque(false);
        center.add(buildToolbar(),BorderLayout.NORTH);
        center.add(buildTable(),BorderLayout.CENTER);
        add(center,BorderLayout.CENTER);
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(){
            protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),12,12));
                g2.setColor(BORDER_CARD); g2.setStroke(new BasicStroke(1)); g2.draw(new RoundRectangle2D.Float(0.5f,0.5f,getWidth()-1,getHeight()-1,12,12));
                g2.dispose();
            }
        };
        bar.setOpaque(false); bar.setLayout(new FlowLayout(FlowLayout.LEFT,10,10));

        searchBox=inputField("Search members...");
        searchBox.setPreferredSize(new Dimension(240,36));
        searchBox.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            public void insertUpdate(javax.swing.event.DocumentEvent e){reload();}
            public void removeUpdate(javax.swing.event.DocumentEvent e){reload();}
            public void changedUpdate(javax.swing.event.DocumentEvent e){reload();}
        });
        bar.add(searchBox);

        JButton addBtn=btnGreen("+ Add Member"); addBtn.setPreferredSize(new Dimension(120,36)); addBtn.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){showAddDialog();}});
        JButton editBtn=btnBlue("Edit"); editBtn.setPreferredSize(new Dimension(80,36)); editBtn.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){showEditDialog();}});
        JButton delBtn=btnRed("Delete"); delBtn.setPreferredSize(new Dimension(80,36)); delBtn.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){deleteSelected();}});
        JButton refBtn=btnPurple("Refresh"); refBtn.setPreferredSize(new Dimension(85,36)); refBtn.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){reload();}});

        bar.add(addBtn); bar.add(editBtn); bar.add(delBtn); bar.add(refBtn);
        return bar;
    }

    private JPanel buildTable() {
        model=new DefaultTableModel(COLS,0){public boolean isCellEditable(int r,int c){return false;}};
        table=new JTable(model){
            public Component prepareRenderer(TableCellRenderer r,int row,int col){
                Component c=super.prepareRenderer(r,row,col);
                if(!isRowSelected(row)) c.setBackground(row%2==0?BG_ROW_EVEN:BG_ROW_ODD);
                else c.setBackground(BG_ROW_SEL);
                return c;
            }
        };
        table.setRowHeight(40); table.setFont(plain(13)); table.setForeground(TEXT_BODY);
        table.setGridColor(new Color(235,240,252)); table.setShowHorizontalLines(true); table.setShowVerticalLines(false);
        table.setSelectionBackground(BG_ROW_SEL); table.setFillsViewportHeight(true); table.setFocusable(false);
        table.setIntercellSpacing(new Dimension(0,1));
        JTableHeader hdr=table.getTableHeader();
        hdr.setBackground(BG_TABLE_HDR); hdr.setForeground(TEXT_DARK); hdr.setFont(bold(12));
        hdr.setPreferredSize(new Dimension(0,42)); hdr.setBorder(new MatteBorder(0,0,1,0,BORDER_CARD));
        hdr.setReorderingAllowed(false);
        int[] w={60,220,130,210,150};
        for(int i=0;i<w.length;i++) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        JPanel card=new JPanel(new BorderLayout()){
            protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),12,12));
                g2.setColor(BORDER_CARD); g2.setStroke(new BasicStroke(1)); g2.draw(new RoundRectangle2D.Float(0.5f,0.5f,getWidth()-1,getHeight()-1,12,12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        JScrollPane sp=new JScrollPane(table); UI.styleScroll(sp);
        card.add(sp,BorderLayout.CENTER);
        return card;
    }

    public void reload() {
        model.setRowCount(0);
        String q=searchBox!=null?searchBox.getText().trim():"";
        try(Connection c=DBConnection.get()){
            String sql="SELECT member_id,full_name,roll_no,department,joined_on FROM tbl_members"+
                (q.isEmpty()?"":" WHERE LOWER(full_name) LIKE LOWER(?) OR LOWER(roll_no) LIKE LOWER(?)")+
                " ORDER BY full_name";
            PreparedStatement ps=c.prepareStatement(sql);
            if(!q.isEmpty()){ps.setString(1,"%"+q+"%");ps.setString(2,"%"+q+"%");}
            ResultSet rs=ps.executeQuery(); int cnt=0;
            while(rs.next()){
                model.addRow(new Object[]{
                    rs.getInt("member_id"),rs.getString("full_name"),rs.getString("roll_no"),
                    rs.getString("department"),rs.getTimestamp("joined_on").toString().substring(0,10)
                });
                cnt++;
            }
            countLbl.setText(cnt+" record"+(cnt!=1?"s":""));
        } catch(SQLException ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"DB Error",JOptionPane.ERROR_MESSAGE);}
    }

    private void showAddDialog() {
        JDialog dlg=makeDialog("Add Member",440,360);
        JPanel p=formPanel();
        JTextField nameF=inputField("Full name"); JTextField rollF=inputField("Roll number");
        JComboBox<String> deptC=combo(DEPTS);
        JLabel msgL=new JLabel(" "); msgL.setFont(plain(12)); msgL.setForeground(ERR_FG);
        addRow(p,"Full Name *",nameF); addRow(p,"Roll No *",rollF); addRow(p,"Department",deptC); addRow(p,"",msgL);
        JButton save=btnGreen("Save"); save.setPreferredSize(new Dimension(100,38));
        JButton cancel=btnRed("Cancel"); cancel.setPreferredSize(new Dimension(90,38));
        cancel.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){dlg.dispose();}});
        save.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                String nm=nameF.getText().trim(), rl=rollF.getText().trim();
                if(nm.isEmpty()||rl.isEmpty()){msgL.setText("Name and Roll No required.");return;}
                String dept=(String)deptC.getSelectedItem();
                if(dept!=null&&dept.equals("Select Department")) dept="";
                try(Connection c=DBConnection.get()){
                    PreparedStatement chk=c.prepareStatement("SELECT member_id FROM tbl_members WHERE LOWER(roll_no)=LOWER(?)");
                    chk.setString(1,rl);
                    if(chk.executeQuery().next()){msgL.setText("Roll number already exists.");return;}
                    PreparedStatement ins=c.prepareStatement("INSERT INTO tbl_members(full_name,roll_no,department) VALUES(?,?,?)");
                    ins.setString(1,nm);ins.setString(2,rl);ins.setString(3,dept);
                    ins.executeUpdate(); dlg.dispose(); reload(); parent.refreshDash();
                } catch(SQLException ex){msgL.setText("DB Error: "+ex.getMessage());}
            }
        });
        JPanel br=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); br.setOpaque(false); br.add(cancel); br.add(save);
        p.add(br); dlg.add(p); dlg.setVisible(true);
    }

    private void showEditDialog() {
        int row=table.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Select a row to edit.","Info",JOptionPane.INFORMATION_MESSAGE);return;}
        int id=(int)model.getValueAt(row,0);
        JDialog dlg=makeDialog("Edit Member",440,360);
        JPanel p=formPanel();
        JTextField nameF=inputField("Full name"); nameF.setText((String)model.getValueAt(row,1));
        JTextField rollF=inputField("Roll No"); rollF.setText((String)model.getValueAt(row,2));
        JComboBox<String> deptC=combo(DEPTS); deptC.setSelectedItem(model.getValueAt(row,3));
        JLabel msgL=new JLabel(" "); msgL.setFont(plain(12)); msgL.setForeground(ERR_FG);
        addRow(p,"Full Name *",nameF); addRow(p,"Roll No *",rollF); addRow(p,"Department",deptC); addRow(p,"",msgL);
        JButton save=btnBlue("Update"); save.setPreferredSize(new Dimension(100,38));
        JButton cancel=btnRed("Cancel"); cancel.setPreferredSize(new Dimension(90,38));
        cancel.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){dlg.dispose();}});
        save.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                String nm=nameF.getText().trim(), rl=rollF.getText().trim();
                if(nm.isEmpty()||rl.isEmpty()){msgL.setText("Name and Roll No required.");return;}
                String dept=(String)deptC.getSelectedItem();
                if(dept!=null&&dept.equals("Select Department")) dept="";
                try(Connection c=DBConnection.get()){
                    PreparedStatement upd=c.prepareStatement("UPDATE tbl_members SET full_name=?,roll_no=?,department=? WHERE member_id=?");
                    upd.setString(1,nm);upd.setString(2,rl);upd.setString(3,dept);upd.setInt(4,id);
                    upd.executeUpdate(); dlg.dispose(); reload();
                } catch(SQLException ex){msgL.setText("DB Error: "+ex.getMessage());}
            }
        });
        JPanel br=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); br.setOpaque(false); br.add(cancel); br.add(save);
        p.add(br); dlg.add(p); dlg.setVisible(true);
    }

    private void deleteSelected(){
        int row=table.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Select a row to delete.","Info",JOptionPane.INFORMATION_MESSAGE);return;}
        int id=(int)model.getValueAt(row,0);
        try(Connection c=DBConnection.get()){
            PreparedStatement chk=c.prepareStatement("SELECT COUNT(*) FROM tbl_lending WHERE member_id=? AND lend_state='ACTIVE'");
            chk.setInt(1,id); ResultSet rs=chk.executeQuery(); rs.next();
            if(rs.getInt(1)>0){JOptionPane.showMessageDialog(this,"Cannot delete: member has active issues.","Cannot Delete",JOptionPane.WARNING_MESSAGE);return;}
        } catch(SQLException ex){return;}
        int conf=JOptionPane.showConfirmDialog(this,"Delete this member?","Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(conf!=JOptionPane.YES_OPTION) return;
        try(Connection c=DBConnection.get()){
            PreparedStatement del=c.prepareStatement("DELETE FROM tbl_members WHERE member_id=?");
            del.setInt(1,id); del.executeUpdate(); reload(); parent.refreshDash();
        } catch(SQLException ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
    }

    private JDialog makeDialog(String t,int w,int h){JDialog d=new JDialog((JFrame)SwingUtilities.getWindowAncestor(this),t,true);d.setSize(w,h);d.setLocationRelativeTo(this);d.setResizable(false);d.getContentPane().setBackground(BG_CARD);return d;}
    private JPanel formPanel(){JPanel p=new JPanel();p.setBackground(BG_CARD);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.setBorder(BorderFactory.createEmptyBorder(20,24,20,24));return p;}
    private void addRow(JPanel p,String lbl,JComponent field){if(!lbl.isEmpty()){JLabel l=label(lbl);l.setAlignmentX(LEFT_ALIGNMENT);p.add(l);p.add(Box.createVerticalStrut(4));}field.setAlignmentX(LEFT_ALIGNMENT);field.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));p.add(field);p.add(Box.createVerticalStrut(12));}
}
