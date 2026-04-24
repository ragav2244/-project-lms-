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

public class IssuePanel extends JPanel {

    private final MainWindow parent;
    private JComboBox<String> bookCombo, memberCombo;
    private JTextField dateField;
    private JLabel msgLabel, stockLabel;
    private JTable activeTable;
    private DefaultTableModel activeModel;
    private Map<String,Integer> bookMap   = new LinkedHashMap<>();
    private Map<String,Integer> memberMap = new LinkedHashMap<>();

    public IssuePanel(MainWindow parent) {
        this.parent=parent;
        setBackground(BG_APP);
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        setBorder(BorderFactory.createEmptyBorder(24,24,24,24));

        JPanel hdr=new JPanel(new BorderLayout()); hdr.setOpaque(false);
        JPanel ht=new JPanel(); ht.setOpaque(false); ht.setLayout(new BoxLayout(ht,BoxLayout.Y_AXIS));
        JLabel t=title("Issue Book"); t.setAlignmentX(LEFT_ALIGNMENT); ht.add(t);
        JLabel s=subtitle("Issue a book to a member — stock updates automatically"); s.setAlignmentX(LEFT_ALIGNMENT); ht.add(s);
        hdr.add(ht,BorderLayout.WEST);
        add(hdr,BorderLayout.NORTH);

        JPanel twoCol=new JPanel(new GridLayout(1,2,20,0)); twoCol.setOpaque(false);

        // ── Left form ──
        JPanel formCard=UI.card(); formCard.setLayout(new BoxLayout(formCard,BoxLayout.Y_AXIS));

        JLabel ft=new JLabel("Issue Details"); ft.setFont(bold(15)); ft.setForeground(TEXT_DARK); ft.setAlignmentX(LEFT_ALIGNMENT); formCard.add(ft);
        JSeparator sep=new JSeparator(); sep.setForeground(BORDER_CARD); sep.setMaximumSize(new Dimension(Integer.MAX_VALUE,1)); formCard.add(Box.createVerticalStrut(10)); formCard.add(sep); formCard.add(Box.createVerticalStrut(14));

        addL(formCard,"SELECT BOOK *");
        bookCombo=combo(new String[]{"Loading..."});
        bookCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE,40)); bookCombo.setAlignmentX(LEFT_ALIGNMENT);
        bookCombo.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){updateStock();}});
        formCard.add(bookCombo); formCard.add(Box.createVerticalStrut(6));

        stockLabel=new JLabel(" "); stockLabel.setFont(plain(12)); stockLabel.setForeground(OK_FG); stockLabel.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(stockLabel); formCard.add(Box.createVerticalStrut(12));

        addL(formCard,"SELECT MEMBER *");
        memberCombo=combo(new String[]{"Loading..."}); memberCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE,40)); memberCombo.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(memberCombo); formCard.add(Box.createVerticalStrut(16));

        addL(formCard,"ISSUE DATE");
        dateField=inputField(""); dateField.setEditable(false);
        dateField.setText(LocalDate.now().toString());
        dateField.setMaximumSize(new Dimension(Integer.MAX_VALUE,40)); dateField.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(dateField); formCard.add(Box.createVerticalStrut(20));

        msgLabel=new JLabel(" "); msgLabel.setFont(plain(12)); msgLabel.setForeground(ERR_FG); msgLabel.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(msgLabel); formCard.add(Box.createVerticalStrut(12));

        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); btnRow.setOpaque(false); btnRow.setAlignmentX(LEFT_ALIGNMENT);
        JButton issueBtn=btnGreen("Issue Book"); issueBtn.setPreferredSize(new Dimension(130,40));
        issueBtn.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){doIssue();}});
        JButton clrBtn=btnRed("Clear"); clrBtn.setPreferredSize(new Dimension(80,40));
        clrBtn.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){clearForm();}});
        btnRow.add(issueBtn); btnRow.add(Box.createHorizontalStrut(10)); btnRow.add(clrBtn);
        formCard.add(btnRow);

        twoCol.add(formCard);

        // ── Right: active issues table ──
        JPanel rightCard=UI.card(); rightCard.setLayout(new BorderLayout(0,10));
        JLabel rt=new JLabel("Currently Issued"); rt.setFont(bold(15)); rt.setForeground(TEXT_DARK);
        rightCard.add(rt,BorderLayout.NORTH);

        String[] cols={"Book Title","Member","Issue Date"};
        activeModel=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        activeTable=new JTable(activeModel);
        activeTable.setRowHeight(38); activeTable.setFont(plain(12)); activeTable.setForeground(TEXT_BODY);
        activeTable.setGridColor(new Color(235,240,252)); activeTable.setShowHorizontalLines(true); activeTable.setShowVerticalLines(false);
        activeTable.setBackground(BG_ROW_EVEN); activeTable.setFillsViewportHeight(true); activeTable.setFocusable(false);
        activeTable.getTableHeader().setBackground(BG_TABLE_HDR); activeTable.getTableHeader().setForeground(TEXT_DARK);
        activeTable.getTableHeader().setFont(bold(11)); activeTable.getTableHeader().setPreferredSize(new Dimension(0,36));
        JScrollPane sp=new JScrollPane(activeTable); UI.styleScroll(sp);
        rightCard.add(sp,BorderLayout.CENTER);
        twoCol.add(rightCard);

        JPanel center=new JPanel(new BorderLayout()); center.setOpaque(false);
        center.add(twoCol,BorderLayout.NORTH);
        JScrollPane rootSp=new JScrollPane(center); UI.styleScroll(rootSp); rootSp.getViewport().setBackground(BG_APP);
        add(rootSp,BorderLayout.CENTER);
    }

    private void addL(JPanel p,String t){JLabel l=label(t);l.setAlignmentX(LEFT_ALIGNMENT);p.add(l);p.add(Box.createVerticalStrut(6));}

    public void refresh() {
        loadBooks(); loadMembers(); loadActiveTable();
        dateField.setText(LocalDate.now().toString());
    }

    private void loadBooks() {
        bookMap.clear(); bookCombo.removeAllItems(); bookCombo.addItem("-- Select Book --");
        try(Connection c=DBConnection.get(); Statement s=c.createStatement()){
            ResultSet rs=s.executeQuery("SELECT catalog_id,title,author,avail_copies FROM tbl_catalog WHERE avail_copies>0 ORDER BY title");
            while(rs.next()){
                String d=rs.getString("title")+" - "+rs.getString("author")+" ["+rs.getInt("avail_copies")+" avail]";
                bookCombo.addItem(d); bookMap.put(d,rs.getInt("catalog_id"));
            }
        } catch(SQLException ex){bookCombo.addItem("Error loading");}
        updateStock();
    }

    private void loadMembers() {
        memberMap.clear(); memberCombo.removeAllItems(); memberCombo.addItem("-- Select Member --");
        try(Connection c=DBConnection.get(); Statement s=c.createStatement()){
            ResultSet rs=s.executeQuery("SELECT member_id,full_name,roll_no FROM tbl_members ORDER BY full_name");
            while(rs.next()){
                String d=rs.getString("full_name")+" ("+rs.getString("roll_no")+")";
                memberCombo.addItem(d); memberMap.put(d,rs.getInt("member_id"));
            }
        } catch(SQLException ex){memberCombo.addItem("Error loading");}
    }

    private void updateStock(){
        String sel=(String)bookCombo.getSelectedItem();
        if(sel==null||!bookMap.containsKey(sel)){stockLabel.setText(" ");return;}
        try(Connection c=DBConnection.get()){
            PreparedStatement ps=c.prepareStatement("SELECT avail_copies FROM tbl_catalog WHERE catalog_id=?");
            ps.setInt(1,bookMap.get(sel)); ResultSet rs=ps.executeQuery();
            if(rs.next()){int av=rs.getInt(1);stockLabel.setText(av+" cop"+(av>1?"ies":"y")+" available");stockLabel.setForeground(av>2?OK_FG:(av>0?WARN_FG:ERR_FG));}
        } catch(SQLException ignored){}
    }

    private void loadActiveTable(){
        activeModel.setRowCount(0);
        try(Connection c=DBConnection.get(); Statement s=c.createStatement()){
            ResultSet rs=s.executeQuery("SELECT book_title,member_name,lend_date FROM tbl_lending WHERE lend_state='ACTIVE' ORDER BY lending_id DESC LIMIT 25");
            while(rs.next()) activeModel.addRow(new Object[]{rs.getString("book_title"),rs.getString("member_name"),rs.getDate("lend_date").toString()});
        } catch(SQLException ignored){}
    }

    private void doIssue(){
        String bSel=(String)bookCombo.getSelectedItem(), mSel=(String)memberCombo.getSelectedItem();
        if(bSel==null||!bookMap.containsKey(bSel)){msg("Select a book.",false);return;}
        if(mSel==null||!memberMap.containsKey(mSel)){msg("Select a member.",false);return;}
        int bookId=bookMap.get(bSel), memberId=memberMap.get(mSel);
        try(Connection c=DBConnection.get()){
            PreparedStatement av=c.prepareStatement("SELECT avail_copies,title FROM tbl_catalog WHERE catalog_id=?");
            av.setInt(1,bookId); ResultSet rs=av.executeQuery(); rs.next();
            if(rs.getInt("avail_copies")<=0){msg("Book not available.",false);return;}
            String bookTitle=rs.getString("title");
            PreparedStatement dupChk=c.prepareStatement("SELECT lending_id FROM tbl_lending WHERE catalog_id=? AND member_id=? AND lend_state='ACTIVE'");
            dupChk.setInt(1,bookId); dupChk.setInt(2,memberId);
            if(dupChk.executeQuery().next()){msg("Member already has this book.",false);return;}

            PreparedStatement mn=c.prepareStatement("SELECT full_name FROM tbl_members WHERE member_id=?");
            mn.setInt(1,memberId); ResultSet mr=mn.executeQuery(); mr.next(); String memberName=mr.getString("full_name");

            c.setAutoCommit(false);
            PreparedStatement ins=c.prepareStatement("INSERT INTO tbl_lending(catalog_id,member_id,book_title,member_name,lend_date,lend_state) VALUES(?,?,?,?,?,?)");
            ins.setInt(1,bookId);ins.setInt(2,memberId);ins.setString(3,bookTitle);ins.setString(4,memberName);
            ins.setDate(5,java.sql.Date.valueOf(LocalDate.now()));ins.setString(6,"ACTIVE");ins.executeUpdate();
            PreparedStatement upd=c.prepareStatement("UPDATE tbl_catalog SET avail_copies=avail_copies-1 WHERE catalog_id=?");
            upd.setInt(1,bookId); upd.executeUpdate();
            c.commit(); c.setAutoCommit(true);

            msg("Book issued successfully to "+memberName+"!",true);
            clearForm(); refresh(); parent.refreshDash();
        } catch(SQLException ex){msg("DB Error: "+ex.getMessage(),false);}
    }

    private void clearForm(){bookCombo.setSelectedIndex(0);memberCombo.setSelectedIndex(0);msgLabel.setText(" ");stockLabel.setText(" ");}
    private void msg(String t,boolean ok){msgLabel.setText(t);msgLabel.setForeground(ok?OK_FG:ERR_FG);}
}
