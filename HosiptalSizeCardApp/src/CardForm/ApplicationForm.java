package CardForm;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.sun.javacard.apduio.CadTransportException;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.Dimension;
import javax.swing.JTextPane;
import java.awt.BorderLayout;
import javax.swing.JTextArea;

public class ApplicationForm {

	private JFrame frame;
	private JMenuBar menuBar;
	private JMenuItem tieuSuKhamChuaBenhMenuItem;
	private JMenuItem thongTinHanhChinhMenuItem;
	private JMenuItem tienSuDiUngMenuItem;
	private JTextArea contentTextArea;
	private static ApduIO apduio = new ApduIO();
	private static Information info;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ApplicationForm window = new ApplicationForm();
					window.frame.setVisible(true);										
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ApplicationForm() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();		
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		menuBar = new JMenuBar();
		menuBar.setSize(new Dimension(0, 50));
		frame.setJMenuBar(menuBar);
		
		thongTinHanhChinhMenuItem = new JMenuItem("Thong Tin Hanh Chinh");
		thongTinHanhChinhMenuItem.setSize(new Dimension(0, 50));
		menuBar.add(thongTinHanhChinhMenuItem);
		
		tieuSuKhamChuaBenhMenuItem = new JMenuItem("Tien su kham chua benh");
		menuBar.add(tieuSuKhamChuaBenhMenuItem);
		
		tienSuDiUngMenuItem = new JMenuItem("Tien su di ung va tien su gia dinh");
		menuBar.add(tienSuDiUngMenuItem);
		
		contentTextArea = new JTextArea();
		frame.getContentPane().add(contentTextArea, BorderLayout.NORTH);
		frame.setVisible(true);
		try {
			apduio.initApplet();											
			//apduio.saveCardInfo(info);
			info = apduio.readCardInfo();						
		}catch (IOException | CadTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		contentTextArea.setText(info.getString(info.name) + "\n" + info.getString(info.CMND) + "\n" + info.getString(info.birthDay) + "\n" + info.getString(info.bloodClass));
	}

}
