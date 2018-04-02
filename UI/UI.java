package UI;

import java.awt.Container;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import db.DBProcess;

public class UI  extends JFrame {
	
	final int padding_up = 30;
	final int padding_left = 35; 
	final int colBlank = 100;
	final int rowBlank = 30;
	final int imgHeight = 550;
	
	JTextField jTextField_from;
	JTextField jTextField_to;  
	JTextField jTextField_topk;
	JTextField jTextField_length;
	
	JLabel jLabel_from; 
	JLabel jLabel_to; 
	JLabel jLabel_topk; 
	JLabel jLabel_length; 
	JLabel jLabel_img;
	
	JButton jButton_submit; //submit
	JTable jTable;
	Image img = null;
	Container contentPane;
	JScrollPane jScrollPane_result; //model
	DefaultTableModel model;
	
	UI(){
		
		try{
			File srcImg = new File("src/image/example.jpg");
			img = ImageIO.read(srcImg);
			
			this.jLabel_img = new JLabel(new ImageIcon(img));
			this.jLabel_img.setLocation(padding_left-20, padding_up);
			this.jLabel_img.setSize(550, imgHeight);
			
			this.jLabel_from = new JLabel("From");
			this.jLabel_from.setLocation(padding_left+colBlank*0, padding_up+imgHeight+rowBlank);
			this.jLabel_from.setSize(100, 30);
			
			this.jLabel_to = new JLabel("To");
			this.jLabel_to.setLocation(padding_left+colBlank*1, padding_up+imgHeight+rowBlank);
			this.jLabel_to.setSize(100, 30);
			
			this.jLabel_topk = new JLabel("Top K");
			this.jLabel_topk.setLocation(padding_left+colBlank*2, padding_up+imgHeight+rowBlank);
			this.jLabel_topk.setSize(100, 30);
			
			this.jLabel_length = new JLabel("Route Length");
			this.jLabel_length.setLocation(padding_left+colBlank*3, padding_up+imgHeight+rowBlank);
			this.jLabel_length.setSize(100, 30);
			
			this.jTextField_from = new JTextField("0"); 
			this.jTextField_from.setLocation(padding_left+colBlank*0, padding_up+imgHeight+rowBlank*2);
			this.jTextField_from.setSize(80,30);
			
			this.jTextField_to = new JTextField("1"); 
			this.jTextField_to.setLocation(padding_left+colBlank*1, padding_up+imgHeight+rowBlank*2);
			this.jTextField_to.setSize(80,30);
			
			this.jTextField_topk = new JTextField("2");
			this.jTextField_topk.setLocation(padding_left+colBlank*2, padding_up+imgHeight+rowBlank*2);
			this.jTextField_topk.setSize(80,30);

			this.jTextField_length = new JTextField("2"); 
			this.jTextField_length.setLocation(padding_left+colBlank*3, padding_up+imgHeight+rowBlank*2);
			this.jTextField_length.setSize(80,30);
			
			jButton_submit = new JButton("submit"); //submit
			jButton_submit.setLocation(padding_left+colBlank*4, padding_up+imgHeight+rowBlank);
			jButton_submit.setSize(100,60);
			MyActionListener listener = new MyActionListener();
			jButton_submit.addActionListener((ActionListener) listener);
			
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			contentPane = getContentPane();
			setLayout(null);
			
			String[] label = {"FROM", "TO", "PATH", "LENGTH"};
			model = new DefaultTableModel(label, 0){
				public boolean isCellEditable(int rowIndex, int mCollndex){
					return true;
				}
			};
			
			jTable = new JTable(model);
			jTable.setModel(model);
			
			jScrollPane_result = new JScrollPane(jTable);
			jScrollPane_result.setSize(550,150);
			jScrollPane_result.setLocation(padding_left-20, padding_up+imgHeight+rowBlank*4);

			add(this.jLabel_img);
			add(this.jLabel_from);
			add(this.jLabel_to);
			add(this.jLabel_topk);
			add(this.jLabel_length);
			add(this.jTextField_from);
			add(this.jTextField_to);
			add(this.jTextField_topk);
			add(this.jTextField_length);
			
			add(jButton_submit);
			add(jScrollPane_result);
			setTitle("Top-k Route Computation");
			setSize(600,900);
			setVisible(true);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	class MyActionListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {

			JButton btn=(JButton)e.getSource();
			
			if(btn.getText().equals("submit"))
			{	
				String dbURL = "jdbc:oracle:thin:@localhost:1521:ORCL";
				String username = "scott";
				String password = "tiger";
				
				int from = Integer.parseInt(jTextField_from.getText());
				int to = Integer.parseInt(jTextField_to.getText());
				int topk = Integer.parseInt(jTextField_topk.getText());
				int length = Integer.parseInt(jTextField_length.getText());
				
				int dataSize = 1500000;
				int beaconNum = 30;
				int emTime = 6000;

				LinkedList<LinkedList<Object>> EmResult;
				DBProcess dbp = new DBProcess(dbURL, username, password);

				dbp.createTable("BEACON_"+dataSize, "output\\result\\EMData_"+dataSize+"_"+beaconNum+"_0.25_0.35_0.65_"+emTime+".txt");
				EmResult = dbp.recursiveQuery("BEACON_"+dataSize, from, to, length, topk);
				
				//row값들 초기화
				for (int i = 0; i < model.getRowCount();) 
					model.removeRow(0);
				
				for(LinkedList<Object> list : EmResult) {
					String[] row = new String[4];
					for(int i=0,j =0; j<row.length; i++) {
						if(i!=2)
							row[j++] = (String) list.get(i);
					}
					model.addRow(row);
				}
				
				jTable = new JTable(model);
				jTable.setModel(model);
				jScrollPane_result = new JScrollPane(jTable);
				add(jScrollPane_result);
			}
		}
		
	}

	public static void main(String[] args) {
		
		UI example=new UI();

	}

}
