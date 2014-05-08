package CardForm;

import java.util.Arrays;


public class Information {
	public static final byte MAX_RECORD_OF_DIAGNOSIS = (byte) 0x0A;
	public static final byte MAX_RECORD_OF_MEDICINE = (byte) 0x0A;
	public static final byte INFO_NAME_SIZE = (byte) 64;
	public static final byte INFO_CMND_SIZE = (byte) 16;
	public static final byte INFO_BIRTHDAY_SIZE = (byte) 10;
	public static final byte INFO_BLOOD_CLASS_SIZE = (byte) 3;
	public static final byte INFO_TOTAL_SIZE = INFO_NAME_SIZE + INFO_CMND_SIZE + INFO_BIRTHDAY_SIZE +INFO_BLOOD_CLASS_SIZE;
	public final byte CLUSTER_SIZE = 32;	
	public  byte[] name;
	public byte[] CMND;
	public byte[] birthDay;
	public byte[] passport;
	public byte[] bloodClass;
	public byte[] diagnosisHistory;
	public byte[] medicineHistory;	
	public Information(){
		this.name = new byte[INFO_NAME_SIZE];
		this.CMND = new byte[INFO_CMND_SIZE];
		this.birthDay = new byte[INFO_BIRTHDAY_SIZE];
		this.passport = new byte[CLUSTER_SIZE];
		this.bloodClass = new byte[INFO_BLOOD_CLASS_SIZE];
//		diagnosisHistory = new byte[CLUSTER_SIZE];
//		medicineHistory = new byte[CLUSTER_SIZE];
	}
	
	public Information(String name, String CMND, String birthDay, String bloodClass){				
		//this.name = name.getBytes();		
		this.name = new byte[INFO_NAME_SIZE];
		this.CMND = new byte[INFO_CMND_SIZE];
		this.birthDay = new byte[INFO_BIRTHDAY_SIZE];
		this.passport = new byte[CLUSTER_SIZE];
		this.bloodClass = new byte[INFO_BLOOD_CLASS_SIZE];
		Utility.copyArray(name.getBytes(),(byte)0,this.name,(byte)0,(byte)name.getBytes().length);
		Utility.copyArray(CMND.getBytes(),(byte)0,this.CMND,(byte)0,(byte)CMND.getBytes().length);
		Utility.copyArray(birthDay.getBytes(),(byte)0,this.birthDay,(byte)0,(byte)birthDay.getBytes().length);
		Utility.copyArray(bloodClass.getBytes(),(byte)0,this.bloodClass,(byte)0,(byte)bloodClass.getBytes().length);
	}
	
	public byte[] getSendBytes(){
		byte sendByteLength = INFO_BIRTHDAY_SIZE+INFO_BLOOD_CLASS_SIZE+INFO_CMND_SIZE+INFO_NAME_SIZE;
		byte[] sendBytes = new byte[sendByteLength];
		int offset = 0;
		Utility.copyArray(this.name,(byte)0,sendBytes,(byte)0,INFO_NAME_SIZE);
		offset += INFO_NAME_SIZE;
		Utility.copyArray(this.CMND,(byte)0,sendBytes,(byte)offset,INFO_CMND_SIZE);
		offset += INFO_CMND_SIZE;			
		Utility.copyArray(this.birthDay,(byte)0,sendBytes,(byte)offset,INFO_BIRTHDAY_SIZE);
		offset += INFO_BIRTHDAY_SIZE;
		Utility.copyArray(this.name,(byte)0,sendBytes,(byte)offset,INFO_BLOOD_CLASS_SIZE);
		return sendBytes;
	}
	
	public Information(byte[] cdata)
	{
		this.name = new byte[INFO_NAME_SIZE];
		this.CMND = new byte[INFO_CMND_SIZE];
		this.birthDay = new byte[INFO_BIRTHDAY_SIZE];
		this.passport = new byte[CLUSTER_SIZE];
		this.bloodClass = new byte[INFO_BLOOD_CLASS_SIZE];
		int offset = 0;
		Utility.copyArray(cdata,(byte)0,this.name,(byte)0,INFO_NAME_SIZE);
		offset += INFO_NAME_SIZE;
		Utility.copyArray(cdata,(byte)offset,this.CMND,(byte) 0,INFO_CMND_SIZE);
		offset += INFO_CMND_SIZE;			
		Utility.copyArray(cdata,(byte)offset,this.birthDay,(byte)0,INFO_BIRTHDAY_SIZE);
		offset += INFO_BIRTHDAY_SIZE;
		Utility.copyArray(cdata,(byte)offset,this.bloodClass,(byte)0,INFO_BLOOD_CLASS_SIZE);
		
	}
	
	public String getString(byte[] bytes){
		return new String(Arrays.toString(bytes));		
	}	
}
