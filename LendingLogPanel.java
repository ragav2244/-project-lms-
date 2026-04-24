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

public class LendingLogPanel extends JPanel {

    private final MainWindow parent;
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> filterCombo;
    private JLabel countLbl;

    private static final String[] COLS={"ID","Book Title","Member","Issue Date","Return Date","Status"};

    public LendingLogPanel(MainWindow parent){
        this.parent=parent;
        setBackground(BG_APP);
        setLayout(new BorderLayout());
        build();
    }

    private void build(){
        setBorder(BorderFactory.createEmptyBorder(24,24,24,24));

        JPanel hdr=new JPanel(new BorderLayout()); hdr.setOpaque(false);
        JPanel ht=new JPanel(); ht.setOpaque(false); ht.setLayout(new BoxLayout(ht,BoxLayout.Y_AXIS));
        JLabel t=title("Lending Log"); t.setAlignmentX(LEFT_ALIGNMENT); ht.add(t);
        JLabel s=subtitle("Complete history of all book issues and returns"); s.setAlignmentX(LEFT_ALIGNMENT); ht.add(s);
        hdr.add(ht,BorderLayout.WEST);
        countLbl=new JLabel("0 records"); countLbl.setFont(mono(12)); countLbl.setForeground(TEXT_MUTED);
        JPanel cw=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,14)); cw.setOpaque(false); cw.add(countLbl);
        hdr.add(cw,BorderLayout.EAST);
        add(hdr,BorderLayout.NORTH);

        JPanel center=new JPanel(new BorderLayout(0,12)); center.setOpaque(false);

        // toolbar
        JPanel bar=new JPanel(){
            protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),12,12));
                g2.setColor(BORDER_CARD); g2.setStroke(new BasicStroke(1)); g2.draw(new RoundRectangle2D.Float(0.5f,0.5f,getWidth()-1,getHeight()-1,12,12));
                g2.dispose();
            }
        };
        bar.setOpaque(false); bar.setLayout(new FlowLayout(FlowLayout.LEFT,10,10));
        JLabel fl=new JLabel("Filter:"); fl.setFont(plain(13)); fl.setForeground(TEXT_BODY);
        filterCombo=combo(new String[]{"All","Active Only","Returned Only"});
        filterCombo.setPreferredSize(new Dimension(180,36));
        filterCombo.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){refresh();}});
        JButton refBtn=btnPurple("Refresh"); refBtn.setPreferredSize(new Dimension(85,36));
        refBtn.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){refresh();}});
        bar.add(fl); bar.add(filterCombo); bar.add(refBtn);
        center.add(bar,BorderLayout.NORTH);

        // table
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
        JTableHeader hd=table.getTableHeader();
        hd.setBackground(BG_TABLE_HDR); hd.setForeground(TEXT_DARK); hd.setFont(bold(12));
        hd.setPreferredSize(new Dimension(0,42)); hd.setBorder(new MatteBorder(0,0,1,0,BORDER_CARD));
        hd.setReorderingAllowed(false);
        int[] w={60,300,200,110,110,100};
        for(int i=0;i<w.length;i++) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusRend());

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
        center.add(card,BorderLayout.CENTER);
        add(center,BorderLayout.CENTER);
    }

    public void refresh(){
        model.setRowCount(0);
        String f=(String)filterCombo.getSelectedItem();
        String where="";
        if("Active Only".equals(f)) where=" WHERE lend_state='ACTIVE'";
        else if("Returned Only".equals(f)) where=" WHERE lend_state='RETURNED'";
        try(Connection c=DBConnection.get(); Statement s=c.createStatement()){
            ResultSet rs=s.executeQuery("SELECT lending_id,book_title,member_name,lend_date,return_date,lend_state FROM tbl_lending"+where+" ORDER BY lending_id DESC");
            int cnt=0;
            while(rs.next()){
                String rd=rs.getDate("return_date")!=null?rs.getDate("return_date").toString():"-";
                model.addRow(new Object[]{
                    rs.getInt("lending_id"), rs.getString("book_title"), rs.getString("member_name"),
                    rs.getDate("lend_date").toString(), rd,
                    rs.getString("lend_state").equals("ACTIVE")?"Active":"Returned"
                });
                cnt++;
            }
            countLbl.setText(cnt+" record"+(cnt!=1?"s":""));
        } catch(SQLException ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"DB Error",JOptionPane.ERROR_MESSAGE);}
    }

    private static class StatusRend extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c){
            super.getTableCellRendererComponent(t,v,sel,foc,r,c);
            setHorizontalAlignment(SwingConstants.CENTER);
            String val=v!=null?v.toString():"";
            boolean active=val.equals("Active");
            if(!sel){setBackground(active?WARN_BG:OK_BG);setForeground(active?WARN_FG:OK_FG);setFont(bold(11));}
            return this;
        }
    }
}
