package CardForm;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.sun.javacard.apduio.CadTransportException;

public class ApplicationForm {

	private JFrame frame;
	private JTextField NameInput;
	private JTextField cmndInput;
	private JTextField birthDateInput;
	private JLabel birthDayLabel;
	private JButton submitButton;
	private JTextField BloodClassInput;
	private JLabel lblNewLabel;

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
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 1.0, 0.0, 1.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, Double.MIN_VALUE };
		frame.getContentPane().setLayout(gridBagLayout);

		JLabel NameLabel = new JLabel("Name");
		GridBagConstraints gbc_NameLabel = new GridBagConstraints();
		gbc_NameLabel.gridwidth = 3;
		gbc_NameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_NameLabel.gridx = 3;
		gbc_NameLabel.gridy = 0;
		frame.getContentPane().add(NameLabel, gbc_NameLabel);

		NameInput = new JTextField();
		GridBagConstraints gbc_NameInput = new GridBagConstraints();
		gbc_NameInput.gridwidth = 3;
		gbc_NameInput.insets = new Insets(5, 5, 5, 5);
		gbc_NameInput.fill = GridBagConstraints.HORIZONTAL;
		gbc_NameInput.gridx = 8;
		gbc_NameInput.gridy = 0;
		frame.getContentPane().add(NameInput, gbc_NameInput);
		NameInput.setColumns(10);

		JLabel cmndLabel = new JLabel("CMND");
		GridBagConstraints gbc_cmndLabel = new GridBagConstraints();
		gbc_cmndLabel.gridwidth = 3;
		gbc_cmndLabel.insets = new Insets(0, 0, 5, 5);
		gbc_cmndLabel.gridx = 3;
		gbc_cmndLabel.gridy = 1;
		frame.getContentPane().add(cmndLabel, gbc_cmndLabel);

		cmndInput = new JTextField();
		cmndLabel.setLabelFor(cmndInput);
		GridBagConstraints gbc_cmndInput = new GridBagConstraints();
		gbc_cmndInput.ipady = 5;
		gbc_cmndInput.ipadx = 5;
		gbc_cmndInput.gridwidth = 3;
		gbc_cmndInput.insets = new Insets(5, 5, 5, 5);
		gbc_cmndInput.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmndInput.gridx = 8;
		gbc_cmndInput.gridy = 1;
		frame.getContentPane().add(cmndInput, gbc_cmndInput);
		cmndInput.setColumns(10);
		
		birthDayLabel = new JLabel("Birth date");
		GridBagConstraints gbc_birthDayLabel = new GridBagConstraints();
		gbc_birthDayLabel.gridwidth = 3;
		gbc_birthDayLabel.insets = new Insets(0, 0, 5, 5);
		gbc_birthDayLabel.gridx = 3;
		gbc_birthDayLabel.gridy = 2;
		frame.getContentPane().add(birthDayLabel, gbc_birthDayLabel);
		birthDayLabel.setLabelFor(birthDateInput);
		
				birthDateInput = new JTextField();
				GridBagConstraints gbc_birthDateInput = new GridBagConstraints();
				gbc_birthDateInput.ipady = 5;
				gbc_birthDateInput.ipadx = 5;
				gbc_birthDateInput.gridwidth = 3;
				gbc_birthDateInput.insets = new Insets(5, 5, 5, 5);
				gbc_birthDateInput.fill = GridBagConstraints.HORIZONTAL;
				gbc_birthDateInput.gridx = 8;
				gbc_birthDateInput.gridy = 2;
				frame.getContentPane().add(birthDateInput, gbc_birthDateInput);
				birthDateInput.setColumns(10);
		
		lblNewLabel = new JLabel("Blood Class");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 4;
		gbc_lblNewLabel.gridy = 3;
		frame.getContentPane().add(lblNewLabel, gbc_lblNewLabel);
		
		BloodClassInput = new JTextField();
		GridBagConstraints gbc_BloodClassInput = new GridBagConstraints();
		gbc_BloodClassInput.ipady = 5;
		gbc_BloodClassInput.ipadx = 5;
		gbc_BloodClassInput.insets = new Insets(5, 5, 5, 5);
		gbc_BloodClassInput.fill = GridBagConstraints.HORIZONTAL;
		gbc_BloodClassInput.gridx = 8;
		gbc_BloodClassInput.gridy = 3;
		frame.getContentPane().add(BloodClassInput, gbc_BloodClassInput);
		BloodClassInput.setColumns(10);
		
				submitButton = new JButton("Submit");
				submitButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						ApduIO apduio = new ApduIO();
						try {
							apduio.initApplet();					
							Information info = new Information(NameInput.getText(),cmndInput.getText(),birthDateInput.getText(),BloodClassInput.getText());
							apduio.saveCardInfo(info);							
						}catch (IOException | CadTransportException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}				
					}
				});
				GridBagConstraints gbc_submitButton = new GridBagConstraints();
				gbc_submitButton.anchor = GridBagConstraints.NORTH;
				gbc_submitButton.insets = new Insets(20, 10, 5, 5);
				gbc_submitButton.gridx = 8;
				gbc_submitButton.gridy = 5;
				frame.getContentPane().add(submitButton, gbc_submitButton);
		frame.setVisible(true);
	}

}
