package CardForm;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import static com.sun.javacard.apduio.Apdu.CLA;
import static com.sun.javacard.apduio.Apdu.INS;
import static com.sun.javacard.apduio.Apdu.P1;
import static com.sun.javacard.apduio.Apdu.P2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.apduio.CadClientInterface;
import com.sun.javacard.apduio.CadDevice;
import com.sun.javacard.apduio.CadTransportException;

/**
 * 
 * @author Ariya
 */
public class ApduIO {
	private static final byte[] aid = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x01,
			0x01, 0x01, 0x01, 0x01 };
	// code of CLA
	public final byte Healthcare_CLA = (byte) 0x80;

	// code of INS
	public final byte SAVE_CARD_INFO_INS = (byte) 0x01;
	public final byte EDIT_CARD_INFO_INS = (byte) 0x05;
	public final byte READ_PERSONAL_INFO_INS = (byte) 0x02;
	public final byte READ_HEALTH_CARE_INFO_INS = (byte) 0x03;
	public final byte VERIFY_PIN_INS = (byte) 0x04;

	// SIZE
	public final byte PIN_SIZE = (byte) 0x06;
	// ERROR CODE
	public final short PIN_IS_WRONG_SW = (short) 0x0460;
	public final short PIN_IS_INVALID_SW = (short) 0x0461;
	public final short OUT_OF_MEMORY = (short) 0x0360;
	private static Socket sock;
	CadClientInterface cad;
	Apdu apdu;

	/*
	 * Get error from byte array sw1sw2 from apdu response Return null if sw1sw2
	 * =0x9000 -> no error Else return string to notify error
	 */
	public static String getError(byte[] sw1sw2) {
		String s = null;
		short code = (short) (((sw1sw2[0] & 0x00FF) << 8) | (sw1sw2[1] & 0x00FF));
		return s;
	}

	public void saveCardInfo(Information info) {
		final int sendByteLength = Information.INFO_BIRTHDAY_SIZE
				+ Information.INFO_BLOOD_CLASS_SIZE
				+ Information.INFO_CMND_SIZE + Information.INFO_NAME_SIZE;
		apdu.setLe(0x00);
		apdu.setLc(sendByteLength);
		apdu.command[CLA] = Healthcare_CLA;
		apdu.command[INS] = SAVE_CARD_INFO_INS;
		apdu.command[P1] = 0;
		apdu.command[P2] = 0;
		byte[] sendBytes = info.getSendBytes();
		// convert Information to sendBytes
		apdu.setDataIn(sendBytes);
		try {   
			cad.exchangeApdu(apdu);
			String s = getError(apdu.sw1sw2);
			if (s == null) {
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CadTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void readCardInfo() {		
		apdu.setLe(Information.INFO_TOTAL_SIZE);
		apdu.setLc(0);
		apdu.command[CLA] = Healthcare_CLA;
		apdu.command[INS] = READ_HEALTH_CARE_INFO_INS;
		apdu.command[P1] = 0;
		apdu.command[P2] = 0;		
		// convert Information to sendBytes		
		try {
			cad.exchangeApdu(apdu);
			String s = getError(apdu.sw1sw2);
			if (s == null) {

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CadTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
			else
				System.out.println("select successful");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CadTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void initApplet() throws IOException, CadTransportException {
		// TODO code application l0x80 logic here
		sock = new Socket("localhost", 9100);
		InputStream is = sock.getInputStream();
		OutputStream os = sock.getOutputStream();
		cad = CadDevice.getCadClientInstance(CadDevice.PROTOCOL_T1, is, os);
		cad.powerUp();
		apdu = new Apdu();
		/*
		 * send apdu to select
		 */
		selectApplet(cad, apdu);

	}
}