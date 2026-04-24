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
import java.time.LocalDate;
import java.util.*;

public class ReturnPanel extends JPanel {

    private final MainWindow parent;
    private JComboBox<String> lendingCombo;
    private JLabel msgLabel, infoLabel;
    private JTable returnedTable;
    private DefaultTableModel returnedModel;
    private Map<String,Integer> lendingMap = new LinkedHashMap<>();

    public ReturnPanel(MainWindow parent) {
        this.parent=parent;
        setBackground(BG_APP);
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        setBorder(BorderFactory.createEmptyBorder(24,24,24,24));

        JPanel hdr=new JPanel(new BorderLayout()); hdr.setOpaque(false);
        JPanel ht=new JPanel(); ht.setOpaque(false); ht.setLayout(new BoxLayout(ht,BoxLayout.Y_AXIS));
        JLabel t=title("Return Book"); t.setAlignmentX(LEFT_ALIGNMENT); ht.add(t);
        JLabel s=subtitle("Process book returns — stock restores automatically"); s.setAlignmentX(LEFT_ALIGNMENT); ht.add(s);
        hdr.add(ht,BorderLayout.WEST);
        add(hdr,BorderLayout.NORTH);

        JPanel twoCol=new JPanel(new GridLayout(1,2,20,0)); twoCol.setOpaque(false);

        // Left form
        JPanel formCard=UI.card(); formCard.setLayout(new BoxLayout(formCard,BoxLayout.Y_AXIS));
        JLabel ft=new JLabel("Return Details"); ft.setFont(bold(15)); ft.setForeground(TEXT_DARK); ft.setAlignmentX(LEFT_ALIGNMENT); formCard.add(ft);
        JSeparator sep=new JSeparator(); sep.setForeground(BORDER_CARD); sep.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));
        formCard.add(Box.createVerticalStrut(10)); formCard.add(sep); formCard.add(Box.createVerticalStrut(14));

        addL(formCard,"SELECT ACTIVE ISSUE *");
        lendingCombo=combo(new String[]{"Loading..."}); lendingCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE,40)); lendingCombo.setAlignmentX(LEFT_ALIGNMENT);
        lendingCombo.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){updateInfo();}});
        formCard.add(lendingCombo); formCard.add(Box.createVerticalStrut(10));

        infoLabel=new JLabel(" "); infoLabel.setFont(plain(12)); infoLabel.setForeground(TEXT_MUTED); infoLabel.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(infoLabel); formCard.add(Box.createVerticalStrut(20));

        // info box
        JPanel infoBox=new JPanel(){
            protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230,240,255)); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
                g2.setColor(new Color(59,109,217,60)); g2.setStroke(new BasicStroke(1)); g2.draw(new RoundRectangle2D.Float(0.5f,0.5f,getWidth()-1,getHeight()-1,10,10));
                g2.dispose();
            }
        };
        infoBox.setOpaque(false); infoBox.setLayout(new BoxLayout(infoBox,BoxLayout.Y_AXIS));
        infoBox.setBorder(BorderFactory.createEmptyBorder(10,14,10,14));
        infoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE,80)); infoBox.setAlignmentX(LEFT_ALIGNMENT);
        JLabel ib=new JLabel("Upon return:"); ib.setFont(bold(12)); ib.setForeground(ACCENT); ib.setAlignmentX(LEFT_ALIGNMENT); infoBox.add(ib);
        infoBox.add(Box.createVerticalStrut(4));
        for(String s2:new String[]{"Book stock increases by 1","Lending record marked Returned","Return date is recorded"}){
            JLabel l=new JLabel("  " + s2); l.setFont(plain(12)); l.setForeground(TEXT_BODY); l.setAlignmentX(LEFT_ALIGNMENT); infoBox.add(l);
        }
        formCard.add(infoBox); formCard.add(Box.createVerticalStrut(20));

        msgLabel=new JLabel(" "); msgLabel.setFont(plain(12)); msgLabel.setForeground(ERR_FG); msgLabel.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(msgLabel); formCard.add(Box.createVerticalStrut(12));

        JButton returnBtn=btnAccent("Process Return"); returnBtn.setPreferredSize(new Dimension(150,40));
        returnBtn.setAlignmentX(LEFT_ALIGNMENT);
        returnBtn.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){doReturn();}});
        formCard.add(returnBtn);

        twoCol.add(formCard);

        // Right: returned history
        JPanel rightCard=UI.card(); rightCard.setLayout(new BorderLayout(0,10));
        JPanel rhdr=new JPanel(new BorderLayout()); rhdr.setOpaque(false);
        JLabel rht=new JLabel("Recently Returned"); rht.setFont(bold(15)); rht.setForeground(TEXT_DARK);
        JButton refBtn=btnPurple("Refresh"); refBtn.setPreferredSize(new Dimension(80,32));
        refBtn.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){loadReturnedTable();}});
        rhdr.add(rht,BorderLayout.WEST); rhdr.add(refBtn,BorderLayout.EAST);
        rightCard.add(rhdr,BorderLayout.NORTH);

        String[] cols={"Book Title","Member","Issue Date","Return Date"};
        returnedModel=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        returnedTable=new JTable(returnedModel);
        returnedTable.setRowHeight(38); returnedTable.setFont(plain(12)); returnedTable.setForeground(TEXT_BODY);
        returnedTable.setGridColor(new Color(235,240,252)); returnedTable.setShowHorizontalLines(true); returnedTable.setShowVerticalLines(false);
        returnedTable.setBackground(BG_ROW_EVEN); returnedTable.setFillsViewportHeight(true); returnedTable.setFocusable(false);
        returnedTable.getTableHeader().setBackground(BG_TABLE_HDR); returnedTable.getTableHeader().setForeground(TEXT_DARK);
        returnedTable.getTableHeader().setFont(bold(11)); returnedTable.getTableHeader().setPreferredSize(new Dimension(0,36));
        JScrollPane sp=new JScrollPane(returnedTable); UI.styleScroll(sp);
        rightCard.add(sp,BorderLayout.CENTER);
        twoCol.add(rightCard);

        JPanel center=new JPanel(new BorderLayout()); center.setOpaque(false);
        center.add(twoCol,BorderLayout.NORTH);
        JScrollPane rootSp=new JScrollPane(center); UI.styleScroll(rootSp); rootSp.getViewport().setBackground(BG_APP);
        add(rootSp,BorderLayout.CENTER);
    }

    private void addL(JPanel p,String t){JLabel l=label(t);l.setAlignmentX(LEFT_ALIGNMENT);p.add(l);p.add(Box.createVerticalStrut(6));}

    public void refresh(){ loadLendingCombo(); loadReturnedTable(); }

    private void loadLendingCombo(){
        lendingMap.clear(); lendingCombo.removeAllItems(); lendingCombo.addItem("-- Select Issue Record --");
        try(Connection c=DBConnection.get(); Statement s=c.createStatement()){
            ResultSet rs=s.executeQuery("SELECT lending_id,book_title,member_name,lend_date FROM tbl_lending WHERE lend_state='ACTIVE' ORDER BY lending_id DESC");
            while(rs.next()){
                String d=rs.getString("book_title")+" -> "+rs.getString("member_name")+" ["+rs.getDate("lend_date")+"]";
                lendingCombo.addItem(d); lendingMap.put(d,rs.getInt("lending_id"));
            }
        } catch(SQLException ex){lendingCombo.addItem("Error");}
        updateInfo();
    }

    private void updateInfo(){
        String sel=(String)lendingCombo.getSelectedItem();
        if(sel==null||!lendingMap.containsKey(sel)){infoLabel.setText(" ");return;}
        infoLabel.setText("<html>"+sel.replace(">","&rarr;")+"</html>");
    }

    private void loadReturnedTable(){
        returnedModel.setRowCount(0);
        try(Connection c=DBConnection.get(); Statement s=c.createStatement()){
            ResultSet rs=s.executeQuery("SELECT book_title,member_name,lend_date,return_date FROM tbl_lending WHERE lend_state='RETURNED' ORDER BY lending_id DESC LIMIT 25");
            while(rs.next()) returnedModel.addRow(new Object[]{rs.getString("book_title"),rs.getString("member_name"),rs.getDate("lend_date"),rs.getDate("return_date")});
        } catch(SQLException ignored){}
    }

    private void doReturn(){
        String sel=(String)lendingCombo.getSelectedItem();
        if(sel==null||!lendingMap.containsKey(sel)){msg("Select an active issue record.",false);return;}
        int lid=lendingMap.get(sel);
        int conf=JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(this),"Process return for:\n"+sel,"Confirm Return",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
        if(conf!=JOptionPane.YES_OPTION) return;
        try(Connection c=DBConnection.get()){
            PreparedStatement ps=c.prepareStatement("SELECT catalog_id FROM tbl_lending WHERE lending_id=?");
            ps.setInt(1,lid); ResultSet rs=ps.executeQuery(); rs.next(); int cid=rs.getInt("catalog_id");
            c.setAutoCommit(false);
            PreparedStatement u1=c.prepareStatement("UPDATE tbl_lending SET lend_state='RETURNED',return_date=? WHERE lending_id=?");
            u1.setDate(1,java.sql.Date.valueOf(LocalDate.now())); u1.setInt(2,lid); u1.executeUpdate();
            PreparedStatement u2=c.prepareStatement("UPDATE tbl_catalog SET avail_copies=avail_copies+1 WHERE catalog_id=?");
            u2.setInt(1,cid); u2.executeUpdate();
            c.commit(); c.setAutoCommit(true);
            msg("Return processed successfully!",true);
            refresh(); parent.refreshDash();
        } catch(SQLException ex){msg("DB Error: "+ex.getMessage(),false);}
    }

    private void msg(String t,boolean ok){msgLabel.setText(t);msgLabel.setForeground(ok?OK_FG:ERR_FG);}
}
