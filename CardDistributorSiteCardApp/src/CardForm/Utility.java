package CardForm;

public class Utility {
	public static void copyArray(byte[] source, byte sOffset, byte[] des, byte dOffset, byte byteLength){
		byte i;
		for (i=0; i< byteLength; i++ ){
			if ( (dOffset + i) < des.length && (sOffset + i) < source.length ){ 
				des[dOffset + i] = source[sOffset + i]; 
			}
		}
	}
	
}
