package healthcare;

import javacard.framework.Util;
import javacardx.framework.tlv.BERTLV;
import javacardx.framework.tlv.TLVException;

public class Information {
	
	// MAX SIZE DEFINE
	public static final byte MAX_RECORD_OF_DIAGNOSIS = (byte) 0x0A;
	public static final byte MAX_RECORD_OF_MEDICINE = (byte) 0x0A;
	public static final byte MAX_INFO_NAME_SIZE = (byte) 64;
	public static final byte MAX_INFO_CMND_SIZE = (byte) 16;
	public static final byte MAX_INFO_BIRTHDAY_SIZE = (byte) 10;
	public static final byte MAX_INFO_BLOOD_CLASS_SIZE = (byte) 3;
	public static final byte MAX_INFO_TOTAL_SIZE = MAX_INFO_NAME_SIZE + MAX_INFO_CMND_SIZE + MAX_INFO_BIRTHDAY_SIZE +MAX_INFO_BLOOD_CLASS_SIZE;	
	
	//TAG DEFINE
	public static final byte PERSONALINFO_TAG = (byte) 0x01;
	public static final byte DEDICATEINFO_TAG = (byte) 0x02;
	public static final byte MEDICINAL_HISTORY_TAG = (byte) 0x03;
	public static final byte FAMILY_HISTORY_TAG = (byte) 0x04;
	public static final byte ALLERGIC_HISTORY_TAG = (byte) 0x05;
	public static final byte TIME_TAG  = (byte) 0x06;
	public static final byte HOSPITAL_TAG = (byte)0x07;
	public static final byte DOCTOR_TAG = (byte) 0x08;
	public BERTLV data;
	
	public void addPersonalInfo(byte byteArrays, short offset, short len){
		
	}
}
