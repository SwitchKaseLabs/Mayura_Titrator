package main.java;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.*;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.AbstractCategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.FloatDimension;

import com.fazecast.jSerialComm.SerialPort;
  
public class burette_calibration_test extends JPanel implements ItemListener
{
	static JFrame frame1 = new JFrame();
	static JTable table1;
	static JTable table2= new JTable();
	static JPanel p = new JPanel();
	private static int HEADER_HEIGHT = 60;


    static DefaultTableModel model;
	static double wid,hei;
	static String f_date,t_date,c_dat;
	static PrintWriter output_bc;
	static SerialPort sp1;
	static JLabel vol_fill,vol_dose,burette_factor;
	static ScheduledExecutorService exec_bc_fill,exec_bc_dose;
	static int row_cnt = 0,arrow=1; 
	static double dose = 0,dose_counter = 0,b_factor = 0 ;
	static JButton btn_print,btn_test_buretter_calibration,btn_update_buretter_factor,btn_done_buretter_calibration;
	static boolean test = true;
	
	static float prev_burette_factor = 0;
	
	static JScrollPane scrollPane,scrollPane1;
	
	public burette_calibration_test() 
	{
		setLayout(null);
		initialize();
	}
	
	public static void port_setup_bc_test(SerialPort sp) {
		try {
	    	output_bc = new PrintWriter(sp.getOutputStream());
	    	sp1=sp;
		}
		catch(NullPointerException np) {
			JOptionPane.showMessageDialog(null, "Please select the ComPort!");
			 frame1.dispose();
	         frame1 = new JFrame();
	         p=new JPanel();	
	         p.revalidate();
	         p.repaint();
		}
	}
	
	public static void send_afil() {
		if(row_cnt < 3) {
		System.out.println("Sending Fill BC");
		 if(test == true && row_cnt<=3 ) {
			try {
				Thread.sleep(500);
				output_bc.print("<8888>FILL*");
			   	output_bc.flush();
			   	ReformatBuffer.current_state="bct_afil";
			}
			catch(InterruptedException ex){}	
			catch(NullPointerException ee){JOptionPane.showMessageDialog(null, "Please select the ComPort!");}
		}
		}
    }
    
    public static void bct_afill_ok_received() 
    {
		System.out.println("Fill OK BC");
    	exec_bc_fill = Executors.newSingleThreadScheduledExecutor();
    	exec_bc_fill.scheduleAtFixedRate(new Runnable() {
		  @Override
		  public void run() {
			  if(arrow == 1) {vol_fill.setText("Filling ");}
			  if(arrow ==2 ) {vol_fill.setText("Filling >");}
			  if(arrow ==3 ) {vol_fill.setText("Filling >>");}
			  if(arrow ==4 ) {vol_fill.setText("Filling >>>");}
			  if(arrow ==5 ) {vol_fill.setText("Filling >>>>");}
			  if(arrow ==6 ) {vol_fill.setText("Filling >>>>>");}
			  if(arrow ==7 ) {vol_fill.setText("Filling >>>>>>");}
			  if(arrow ==8 ) {vol_fill.setText("Filling >>>>>>>"); }
			  if(arrow ==9 ) {vol_fill.setText("Filling >>>>>>>>"); }
			  if(arrow ==10 ) {
			      vol_fill.setText("Filling >>>>>>>>>");
			      arrow=0;
			  }
			  arrow++;
		  }
		}, 0, 50, TimeUnit.MILLISECONDS);
    }
    
    public static void bct_afill_end_received() {
    	exec_bc_fill.shutdown();
		System.out.println("Fill END BC");

    	String result = (String)JOptionPane.showInputDialog(
	               frame1,
	               "Enter the weight W1 and then PRESS OK!", 
	               "Enter W1",            
	               JOptionPane.PLAIN_MESSAGE,
	               null,            
	               null, 
	               ""
	            );
    	try {
    		double w1 = Double.parseDouble(result);
    		model.setValueAt(w1, row_cnt, 3);	
			model.fireTableDataChanged();	
			if(row_cnt == 0 ) {
				dose =5;
			}
			if(row_cnt == 1) {
				dose =10;
			}
			if(row_cnt == 2) {
				dose=15;
			}
			send_dose();			
    	}
    	catch(NullPointerException ne) {
	        JOptionPane.showMessageDialog(null , "Please enter a value!");
    	}
    	catch (NumberFormatException e) {
	        JOptionPane.showMessageDialog(null , "Please enter a valid value!");
		}
    }
    
    public static void send_dose() {
		System.out.println("SEND DOSE BC");

    	try {
			Thread.sleep(500);
			output_bc.print("<8888>DOSE*");
		   	output_bc.flush();
		   	ReformatBuffer.current_state="bct_dose";
		}
		catch(InterruptedException ex){}	
		catch(NullPointerException ee) 
		{
    		JOptionPane.showMessageDialog(null, "Please select the ComPort!");
		}
    }
    
    public static void bct_dose_ok_received() {
		System.out.println("DOSE OK BC");

    	dose_counter=0;
    	exec_bc_dose = Executors.newSingleThreadScheduledExecutor();
    	exec_bc_dose.scheduleAtFixedRate(new Runnable() {
		  @Override
		  public void run() {
			  if(dose_counter < dose) {
				  dose_counter =dose_counter+((20/60.0)/5);
				  vol_dose.setText("Volume Dosed : "+String.format("%.5f", dose_counter) + "mL");
				  }
				  else {
					  dose_counter =0;
					  exec_bc_dose.shutdown();
					  try {
							 Thread.sleep(300);
							 output_bc.print("<8888>ESCP*");
					    	 output_bc.flush();
					    	 ReformatBuffer.current_state="bct_stpm";
							}
							catch(NullPointerException ex) {
								JOptionPane.showMessageDialog(null, "Please connect to ComPort!");
							}
					  		catch(InterruptedException jb) {}
				  }
		  }
		}, 0, 50, TimeUnit.MILLISECONDS);
    }
    
    public static void bct_stpm_ok_received() {
		System.out.println("STPM OK BC");

    	String result = (String)JOptionPane.showInputDialog(
	               frame1,
	               "Enter the weight W2 and then PRESS OK!", 
	               "Enter W2",            
	               JOptionPane.PLAIN_MESSAGE,
	               null,            
	               null, 
	               ""
	            );
	 	try 
	 	{
	 		double w2 = Double.parseDouble(result);
	 		model.setValueAt(w2, row_cnt, 4);	
			model.fireTableDataChanged();
			
			Double w3 = w2 - Double.parseDouble(model.getValueAt(row_cnt, 3).toString()); 
			model.setValueAt(w3, row_cnt, 5);	
			System.out.println("W3 = "+w3);
			model.fireTableDataChanged();
			row_cnt++;
			send_afil();
	 	}
	 	catch(NullPointerException ne) {
		        JOptionPane.showMessageDialog(null , "Please enter a value!");
	 	}
	 	catch (NumberFormatException e) {
		        JOptionPane.showMessageDialog(null , "Please enter a valid value!");
		}    	
	}
    
    public static float get_burette_factor() {
    	float temp_result =0;
    	Connection con = DbConnection.connect();
		PreparedStatement ps = null;
		String sql ;
		
			sql = "SELECT b_factor FROM burette_factor WHERE SlNo = '1'"; 
		
				try {
					ps = con.prepareStatement(sql);
					ResultSet rs = ps.executeQuery();
					temp_result = Float.parseFloat(rs.getString("b_factor"));
				}
				catch(SQLException e1) {
					JOptionPane.showMessageDialog(null,e1);
				}
				finally {
				    try{
				    ps.close();
				    con.close();
				    } catch(SQLException e1) {
				      System.out.println(e1.toString());
				    }
				}
    	
    	return temp_result;
    }
    
	@SuppressWarnings("removal")
	public static void initialize() {
		frame1.getContentPane().invalidate();
		frame1.getContentPane().validate();
		frame1.getContentPane().repaint();
		
		test_four_column();	
		
		vol_fill = new JLabel("Filling");
		vol_fill.setFont(new Font("Arial", Font.BOLD, (int) Math.round(0.012 * wid)));
		vol_fill.setBounds((int) Math.round(0.013 * wid), (int) Math.round(0.04 * hei), (int) Math.round(0.15 * wid), (int) Math.round(0.0428 * hei));
		frame1.getContentPane().add(vol_fill);
		
		vol_dose = new JLabel("Volume Dosed : ");
		vol_dose.setFont(new Font("Arial", Font.BOLD, (int) Math.round(0.012 * wid)));
		vol_dose.setBounds((int) Math.round(0.22 * wid), (int) Math.round(0.04 * hei), (int) Math.round(0.2 * wid), (int) Math.round(0.0428 * hei));
		frame1.getContentPane().add(vol_dose);
		
		JButton btn_start = new JButton("Start >>>");
		btn_start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btn_start.setEnabled(false);
				send_afil();
			}
		});
		btn_start.setFont(new Font("Arial", Font.BOLD, (int) Math.round(0.009 * wid)));
		btn_start.setBounds((int) Math.round(0.013 * wid), (int) Math.round(0.88 * hei), (int) Math.round(0.0976 * wid), (int) Math.round(0.0428 * hei));
		frame1.getContentPane().add(btn_start);

    }

	public static void add_row_to_four_column(boolean check ,int r,String v1,String v2,String v3,String v4) {
		model.addRow(new Object[0]);
		model.setValueAt(check, r, 0);	
		model.setValueAt(r+1, r, 1);			
		model.setValueAt(v1, r, 2);
		model.setValueAt(v2, r, 3);	
		model.setValueAt(v3, r, 4);	
		model.setValueAt(v4, r, 5);	
		model.fireTableDataChanged();
	}
	
	public static void test_four_column() {
		scrollPane1 = new JScrollPane();
		scrollPane1.setBounds((int) Math.round(0.013 * wid), (int) Math.round(0.1 * hei), (int) Math.round(0.95 * wid), (int) Math.round(0.75 * hei));
		frame1.getContentPane().add(scrollPane1);
		table1 = new JTable();
		table1.setFont(new Font("Times New Roman", Font.BOLD, (int) Math.round(0.012 * wid)));
		
		model = new DefaultTableModel() {
			public Class <?> getColumnClass(int column){
				switch(column) {
				case 0:
					return Boolean.class;
				case 1:
					return Integer.class;
				case 2:
					return String.class;
				case 3:
					return String.class;
				case 4:
					return String.class;
				default:
					return String.class;		
				}
			}
		};
		table1.setModel(model);
		table1.setDefaultEditor(Object.class, null);
		model.addColumn("Select");
		model.addColumn("Trials");
		model.addColumn("Displayed Volume in mL");
		model.addColumn("Weight of Empty Flask(gms){W1}");
		model.addColumn("Weight of Empty Flask(gms){W2}");
		model.addColumn("Weight of Actual Vol of H2O dispensed(gms){W2-W1}");

		table1.setRowHeight((int) Math.round(0.076* hei));
		
		JTableHeader header = table1.getTableHeader();
		header.setPreferredSize(new Dimension( (int) Math.round(0.0612 * hei),  (int) Math.round(0.0612 * hei)));
		header.setFont(new Font("Times New Roman", Font.BOLD, (int) Math.round(0.009765 * wid)));
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		table1.setDefaultRenderer(String.class, centerRenderer);
		header.setDefaultRenderer(centerRenderer);
		
		table1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table1.getColumnModel().getColumn(0).setPreferredWidth((int) Math.round(0.0651 * wid));
		table1.getColumnModel().getColumn(1).setPreferredWidth((int) Math.round(0.0651 * wid));
		table1.getColumnModel().getColumn(2).setPreferredWidth((int) Math.round(0.1302 * wid));
		table1.getColumnModel().getColumn(3).setPreferredWidth((int) Math.round(0.1953 * wid));
		table1.getColumnModel().getColumn(4).setPreferredWidth((int) Math.round(0.1953 * wid));
		table1.getColumnModel().getColumn(5).setPreferredWidth((int) Math.round(0.296875 * wid));
//		table1.getColumnModel().getColumn(0).setCellRenderer( centerRenderer );
		table1.getColumnModel().getColumn(1).setCellRenderer( centerRenderer );
		table1.getColumnModel().getColumn(2).setCellRenderer( centerRenderer );
		table1.getColumnModel().getColumn(3).setCellRenderer( centerRenderer );
		table1.getColumnModel().getColumn(4).setCellRenderer( centerRenderer );
		table1.getColumnModel().getColumn(5).setCellRenderer( centerRenderer );

		scrollPane1.setViewportView(table1);
		
		 add_row_to_four_column(false ,0,"5","","","");
		 add_row_to_four_column(false ,1,"10","","","");
		 add_row_to_four_column(false ,2,"15","","","");
		 
	}
	
	
	public void itemStateChanged(ItemEvent e)
	{
	       
	}
    public static void main(String[] args) {    
    	
    	if(args.length != 0 )
    	{
    		//method_name,method_data,ar,batch,sample_name,normality_val,moisture_val,report_name,titrant_name
    		
    	}
    	
    	Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(frame1.getGraphicsConfiguration());
        int taskHeight=screenInsets.bottom;
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int height=d.height-taskHeight;
        int width=d.width;
        wid = d.getWidth();
        hei = d.getHeight()-taskHeight;
        
        System.out.println(width + "   dfvdvdv " + hei);
        
//        wid = 1280;
//        hei = 720;

        frame1.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame1.setBounds(0,0,(int)wid,(int)hei);
		frame1.add(p);
   		frame1.getContentPane().add(new burette_calibration_test());
		frame1.setResizable(true);
		frame1.setVisible(true);
		frame1.repaint();
		frame1.setTitle("Burette Calibration");
		ImageIcon img = new ImageIcon(("C:\\SQLite\\logo\\logo.png"));
		frame1.setIconImage(img.getImage());
			frame1.addWindowListener(new java.awt.event.WindowAdapter() {
			    @Override
			    public void windowClosing(java.awt.event.WindowEvent windowEvent)
			    {
			    	try {
						Thread.sleep(500);
						output_bc.print("<8888>ESCP*");
						output_bc.flush();
						Thread.sleep(200);
					} catch (NullPointerException ne) {
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					try {
						sp1.closePort();
					} catch (NullPointerException ne) {
					}
			            frame1.dispose();
			            frame1 = new JFrame();
			            p=new JPanel();	
			            p.revalidate();
			            p.repaint();
			    }
			});
    	}
}