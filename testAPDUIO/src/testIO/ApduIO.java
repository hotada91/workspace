/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testIO;

import static com.sun.javacard.apduio.Apdu.CLA;
import static com.sun.javacard.apduio.Apdu.INS;
import static com.sun.javacard.apduio.Apdu.P1;
import static com.sun.javacard.apduio.Apdu.P2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;

import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.apduio.CadClientInterface;
import com.sun.javacard.apduio.CadDevice;
import com.sun.javacard.apduio.CadTransportException;

/**
 * 
 * @author Ariya
 */
public class ApduIO {

	final static short SW_VERIFICATION_FAILED = 0x6300;
	// signal the the PIN validation is required
	// for a credit or a debit transaction
	final static short SW_PIN_VERIFICATION_REQUIRED = 0x6301;
	// signal invalid transaction amount
	// amount > MAX_TRANSACTION_AMOUNT or amount < 0
	final static short SW_INVALID_TRANSACTION_AMOUNT = 0x6A83;

	// signal that the balance exceed the maximum
	final static short SW_EXCEED_MAXIMUM_BALANCE = 0x6A84;
	// signal the the balance becomes negative
	final static short SW_NEGATIVE_BALANCE = 0x6A85;
	final static short SW_SUCCESSFUL = (short) 0x9000;
	private static final String ERR_VERIFICATION_FAILED = "Verification failed";
	private static final String ERR_INVALID_TRANSACTION_AMOUNT = "Invalid transaction amount";
	private static final String ERR_EXCEED_MAXIMUM_BALANCE = "Exceed maximum balance";
	private static final String ERR_NEGATIVE_BALANCE = "Negative balance";
	private static final byte[] aid = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x02,
			0x03, 0x04, 0x05, 0x06 };
	private static Socket sock;
	CadClientInterface cad;	
	Apdu apdu = new Apdu();
	/**
	 * @param args
	 *            the command line arguments
	 * @throws NoSuchPortException
	 * @throws PortInUseException
	 */

	/*
	 * Get error from byte array sw1sw2 from apdu response Return null if sw1sw2
	 * =0x9000 -> no error Else return string to notify error
	 */
	public static String getError(byte[] sw1sw2) {
		short code = (short) ((( sw1sw2[0] & 0x00FF) << 8) | ( sw1sw2[1] & 0x00FF));	
		switch (code) {
		case SW_EXCEED_MAXIMUM_BALANCE:
			return ERR_EXCEED_MAXIMUM_BALANCE;
		case SW_INVALID_TRANSACTION_AMOUNT:
			return ERR_INVALID_TRANSACTION_AMOUNT;
		case SW_NEGATIVE_BALANCE:
			return ERR_NEGATIVE_BALANCE;
		case SW_VERIFICATION_FAILED:
			return ERR_VERIFICATION_FAILED;
		case SW_SUCCESSFUL:
			return null;
		default:
			return "Unknow error";
		}
	}

	public static void getBalance(CadClientInterface cad, Apdu apdu) {
		int balance;
		String s;
		apdu.setLe(0x02);
		apdu.setLc(0x00);
		apdu.command[CLA] = (byte) 0x80;
		apdu.command[INS] = (byte) 0x50;
		apdu.command[P1] = 0x00;
		apdu.command[P2] = 0x00;
		try {
			cad.exchangeApdu(apdu);
			s = getError(apdu.sw1sw2);
			if (s == null)
				if (apdu.dataOut.length != 0) {
					balance = 0;
					for (int i = 0; i< apdu.dataOut.length; i++){
						balance = balance* 256 + (int)(apdu.dataOut[i] & 0x000000FF);						
					}
					System.out.println("Your current balance: " + balance);
				}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CadTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(apdu);

	}

	public static void credit(CadClientInterface cad, Apdu apdu, short amount) {
		byte[] apduReq = new byte[2];
		int balance;
		apduReq[0] = (byte) amount;
		apdu.setDataIn(apduReq, 1);
		apdu.command[CLA] = (byte) 0x80;
		apdu.command[INS] = (byte) 0x30;
		apdu.command[P1] = 0x00;
		apdu.command[P2] = 0x00;
		try {
			cad.exchangeApdu(apdu);
			System.out.println(apdu);
			String s = getError(apdu.sw1sw2);
			if (s == null) {
				if (apdu.dataOut.length != 0) {
					balance = (apdu.dataOut[0] & 0x000000FF) * 256	;					
					System.out.println("Your current balance: " + balance);
				}
			} else
				System.out.println(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CadTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(apdu);

	}

	public static void debit(CadClientInterface cad, Apdu apdu, short amount) {
		byte[] apduReq = new byte[2];
		int balance;
		apduReq[0] = (byte) amount;
		apdu.setDataIn(apduReq, 1);
		apdu.command[CLA] = (byte) 0x80;
		apdu.command[INS] = (byte) 0x40;
		apdu.command[P1] = 0x00;
		apdu.command[P2] = 0x00;
		try {
			cad.exchangeApdu(apdu);
			String s = getError(apdu.sw1sw2);
			if (s == null) {
				if (apdu.dataOut.length != 0) {
					balance = (apdu.dataOut[0] & 0x000000FF) * 256
							+ (apdu.dataOut[1] & 0x000000FF);
					System.out.println("Your current balance: " + balance);
				}
			} else
				System.out.println(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CadTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(apdu);

	}

	public static boolean verifyPin(CadClientInterface cad, Apdu apdu,
			byte[] pinToVerify) {
		apdu.setDataIn(pinToVerify);
		apdu.command[CLA] = (byte) 0x80;
		apdu.command[INS] = (byte) 0x20;
		apdu.command[P1] = 0x00;
		apdu.command[P2] = 0x00;
		try {
			cad.exchangeApdu(apdu);
			String s = getError(apdu.sw1sw2);
			if (s == null)
				return true;
			// notify error
			System.out.println(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (CadTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return false;
	}

	public static void selectApplet(CadClientInterface cad, Apdu apdu) {
		apdu.command[CLA] = 0x00;
		apdu.command[INS] = (byte) 0xA4;
		apdu.command[P1] = 0x04;
		apdu.command[P2] = 0x00;
		apdu.setDataIn(aid, aid.length);
		try {
			cad.exchangeApdu(apdu);
			String s = getError(apdu.sw1sw2);
			if (s != null)
				System.out.println("Select applet error");
			else System.out.println("select successful");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CadTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void initApplet(String[] args) throws IOException,
			CadTransportException, NoSuchPortException, PortInUseException {
		// TODO code application l0x80 logic here

		CadClientInterface cad;	
		sock = new Socket("localhost", 9100);
		InputStream is = sock.getInputStream();
		OutputStream os = sock.getOutputStream();
		cad = CadDevice.getCadClientInstance(CadDevice.PROTOCOL_T1, is, os);		
		cad.powerUp();
		Apdu apdu = new Apdu();
		/*
		 * send apdu to select
		 */
		selectApplet(cad, apdu);

		/*
		 * verify PIN
		 */

		/*
//		 * send apdu to verify
//		 */
//		byte[] pinToVerify;
//		do {
//			System.out.println("Enter your PIN: ");
//			BufferedReader bufferRead = new BufferedReader(
//					new InputStreamReader(System.in));
//			String s = bufferRead.readLine();
//			pinToVerify = s.getBytes();
//			for (int i = 0; i < pinToVerify.length; i++) {
//				pinToVerify[i] = (byte) (pinToVerify[i] - '0');
//			}
//		} while (!verifyPin(cad, apdu, pinToVerify));
//		Scanner scanner;
//		boolean jumpCon = true;
//		do {
//			System.out.println("1) Show balance");
//			System.out.println("2) Credit");
//			System.out.println("3) Dedit");
//			System.out.println("4) Cancle");
//			System.out.println("Select operation: ");
//			scanner = new Scanner(System.in);
//			int op = scanner.nextInt();
//			short moneyToCredit;
//			switch (op) {
//			case 1:
//				getBalance(cad, apdu);
//				break;
//			case 2:
//				System.out.println("Enter money to credit: ");
//				moneyToCredit = scanner.nextShort();
//				if (moneyToCredit <= 0)
//					break;
//				credit(cad, apdu, moneyToCredit);
//				break;
//			case 3:
//				System.out.println("Enter money to debit: ");
//				moneyToCredit = scanner.nextShort();
//				if (moneyToCredit <= 0)
//					break;
//				debit(cad, apdu, moneyToCredit);
//				break;
//			case 4:
//				scanner.close();
//				jumpCon = false;
//				break;
//			default:
//				break;
//			}
//			// scanner.close();
//		} while (jumpCon);
//		cad.powerDown();		
	}
}
