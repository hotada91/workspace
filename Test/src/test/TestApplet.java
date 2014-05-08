//
// ??? beginTransaction   commitTransaction
//-----------------------------------------




// PUT DATA and GET DATA - Example for the ISO/IEC 7816-4 commands PUT DATA and GET DATA
// File: PGDATA.java
// This applet realize a portable data memory with standard ISO/IEC 7816-4 commands.
// In the administrative phase it is possible to write after a successful
// PIN verification data objects into the memory. For adminsitration is also a 
// delete memory command after successful PIN verification available. In the 
// operative phase it is possible to read data without restriction from the card. 
// All data objects are organised in ASN.1 BER TLV coded data objects in the 
// format: DO_1 || DO_2 || ...
//
// In an typical use case the PUT DATA and VERIFY commands will be used in the 
// administrative phase an the GET DATA command will be used for the operational phase.
//
// Package AID: 'D2 76 00 00 60 50 03'
// Applet AID:  'D2 76 00 00 60 41 03'
//
// Source code based on java card specification version 2.1.
// Compiled and tested with JDK 1.3.1 Java Card Application Studio (JCAST) V 2.2,
// JLoad 2.0, Java Card Converter 2.1.2, IFDSIM 4.0 and UniverSIM Java Card 64 kB from G&D.
//
//---------------------------------------------------------------------------------------
// Specification of ISO/IEC 7816-4 case 3 command SELECT FILE
//   command APDU               CLA = '00' || INS = 'A4' ||
//                              P1 (= '04' = select by DF Name) || P2 (='00') ||
//                              Lc (= length of DF Name) || DATA (= DF Name)
//   response APDU (all case)   SW1 || SW2
//
// Specification of ISO/IEC 7816-4 case 3 command VERIFY
//   command APDU               CLA = '00' || INS = '20' || P1 (= '00') ||
//                              P2 ('01' = PIN)
//                              Lc (= length of PIN) || DATA (= PIN)
//   response APDU (all case)   SW1 || SW2
//
// Specification of ISO/IEC 7816-4 case 3 command PUT DATA
//   command APDU               CLA = '00' || INS = 'DA' ||
//                              P1 (= '00') || P2 (= TAG ['40' ... 'FE']) ||
//                              Lc (= length of DATA) ||
//                              DATA (value field of the DO, with length Lc)
//   response APDU (all case)   SW1 || SW2
//
// Specification of ISO/IEC 7816-4 case 2 command GET DATA
//   command APDU               CLA = '00' || INS = 'CA' ||
//                              P1 (= '00') || P2 (= TAG ['40' ... 'FE'])
//                              Le (= length of expected data)
//   response APDU (good case)  DATA (value field of the DO, with length Le) ||
//                              SW1 || SW2
//   response APDU (bad case)   SW1 || SW2
//
// Specification of the proprietary case 1 command DELETE DATA
//   command APDU               CLA = '80' || INS = '06' || P1 (= '00') || P2 (= '00')
//                              Lc (= '00')
//   response APDU (all case)   SW1 || SW2
//
//---------------------------------------------------------------------------------------
// Implementation Notes
//    * based of the GET DATA and PUT DATA command from ISO/IEC 7816-4 CD from 12. July 2002
//    * DOs are simple BER-TLV coded
//    * no support of different DFs
//    * no secure messaging support (= class is alway '00')
//    * TAG:    length of the tag value is 1 byte, tag value: '40' ... 'FE'
//    * LENGTH: length of the length value is 1 byte
//    * VALUE:  length of the value is 127 byte, because of the limitations
//              of a 1 byte length byte (BER-TLV convention). For test reasons 
//              it is set to 9 byte but could be changed with SIZE_MEMORY.
//    * abbreviations: DO - data object
//
//---------------------------------------------------------------------------------------
// Test Strategy
//  				good case tests:
//     					1.1 verify correct PIN
//     					2.1 write DO, read same DO
//     					2.2 try all allowed tag values for DOs
//     					2.3 write smallest DO, read same DO
//     					2.4 write largest DO, read same DO
//     					3.1 delete DO memory
//
//  				bad case tests:
//    					1.1 check access conditions of DELETE
//    					1.2 check access conditions of PUT DATA
//    					1.3 check access conditions of GET DATA
//    					2.1 write DO, read another DO
//    					2.2 try some of the not allowed tag values for DOs (border test)
//    					2.3 write DO with length 0
//    					2.4 write DO with length greater than allowed
//    					3.1 verify wrong PIN, check PIN error counter up till max. value
//    					4.1 fill whole memory with smallest possible DOs
//
//---------------------------------------------------------------------------------------
// This source code is under GNU general public license (see www.opensource.org for details).
// Please send corrections and ideas for extensions to Wolfgang Rankl (www.wrankl.de)
// Copyright 2003-2004 by Wolfgang Rankl, Munich
//---------------------------------------------------------------------------------------
// 13. April 2004 - V 2: improved documentation, 2nd published version
//  7. April 2004 - V 1: initial runnable version, 1st published version
//---------------------------------------------------------------------------------------

package test;             // this is the package name
import javacard.framework.*;    // import all neccessary packages for java card

public class TestApplet extends Applet {
  final static byte    PROP_CLASS  = (byte) 0x80;     // Class of the proprietary APDU commands

  final static byte    INS_SELECT  = (byte) 0xA4;     // instruction for the ISO/IEC 7816-4 SELECT FILE command
  final static byte    INS_VERIFY  = (byte) 0x20;     // instruction for the ISO/IEC 7816-4 VERIFY command
  final static byte    INS_PUTDATA = (byte) 0xDA;     // instruction for the ISO/IEC 7816-4 PUT DATA command
  final static byte    INS_GETDATA = (byte) 0xCA;     // instruction for the ISO/IEC 7816-4 GET DATA command
  final static byte    INS_DELETE  = (byte) 0x06;     // instruction for the proprietary DELETE DATA command

  final static short   SW_PIN_FAILED =     (short) 0x63C0;  // returncode for PIN verification failed
                                                            // the last nibble shows the number of remaining tries
  final static short   SW_DATA_NOT_FOUND = (short) 0x6A88;  // referenced data not found

  // definitions for the data storage area
  final static short   SIZE_MEMORY =     (short) (9);      // size of the data storage area in byte
  static byte[]        memory;                             // this is the data storage area for the application
  static short         memory_NoOfDOs;                     // number of DO stored in the memory

  // definitions for the storage of the DOs
  final static short LEN_TAG = (short) 1;   // length of the DO tag information in bytes
  final static short LEN_LEN = (short) 1;   // length of the DO length information in bytes

  // constants and variables for the PIN management
  final static byte[]  DEFAULT_PIN = {(byte) 0x12,(byte) 0x34}; // default PIN value
  final static byte    PIN_SIZE =          (byte) 2;             // size of the PIN in byte
  final static byte    DEFAULT_PIN_MAXEC = (byte) 3;             // default value of the PIN error counter
  final static byte    PIN_ID =            (byte) 1;             // PIN identifier, PIN 1 = card owner PIN
  static OwnerPIN      pin;                                      // the PIN object

  // error codes of some methods
  // the value of this error codes is outside the memory size and thus
  // they can easily identified, this simplifies the return value of the appropriate methods
  private static final short DO_PROPPER_SET =       (short) (SIZE_MEMORY + 1);  // DO is propper set
  private static final short NOT_ENOUGH_SPACE =     (short) (SIZE_MEMORY + 2);  // not enough space for a new DO
  private static final short DO_HAVE_WRONG_LENGTH = (short) (SIZE_MEMORY + 3);  // presented DO have a different length than stored DO
  private static final short DO_NOT_FOUND =         (short) (SIZE_MEMORY + 4);  // presented DO not found in the memory

//---------------------------------------------------------------------------------------
  //----- installation and registration of the applet
  public static void install(byte[] buffer, short offset, byte length) {
    memory = new byte[SIZE_MEMORY];  // create data storage object for the application

    // create and set the PIN object for the card owner
    pin = new OwnerPIN(DEFAULT_PIN_MAXEC, PIN_SIZE);
    pin.update(DEFAULT_PIN, (short)(0), PIN_SIZE);

    new TestApplet().register();         // register the application within the JCRE
  }  // install

  //---------------------------------------------------------------------------------------
  //----- this is the command dispatcher
  public void process(APDU apdu) {
    byte[] cmd_apdu = apdu.getBuffer();

    if (cmd_apdu[ISO7816.OFFSET_CLA] == ISO7816.CLA_ISO7816) {
      //----- it is the ISO/IEC 7816 class
      switch(cmd_apdu[ISO7816.OFFSET_INS]) {  // check the instruction byte
        case INS_SELECT:                      // it is a SELECT FILE instruction
          cmdSELECT(apdu);
          break;
        case INS_VERIFY:                      // it is a VERIFY instruction
          cmdVERIFY(apdu);
          break;
        case INS_PUTDATA:                     // it is a PUT DATA instruction
          cmdPUTDATA(apdu);
          break;
        case INS_GETDATA:                     // it is a GET DATA instruction
          cmdGETDATA(apdu);
          break;
        default :                             // the instruction in the command apdu is not supported
          ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
      }  // switch
    }  // if
    else if (cmd_apdu[ISO7816.OFFSET_CLA] == PROP_CLASS) {
      //----- it is the proprietary class
      switch(cmd_apdu[ISO7816.OFFSET_INS]) {  // check the instruction byte
        case INS_DELETE:                      // it is a DELETE DATA instruction
          cmdDELETE(apdu);
          break;
        default :                             // the instruction in the command apdu is not supported
          ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
      }  // switch
    }  // else if
    else {                                    // the class in the command apdu is not supported
      ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
    }  // else
  }  // process

  //---------------------------------------------------------------------------------------
  //----- program code for the APDU command SELECT FILE
  private void cmdSELECT(APDU apdu) {
    byte[] cmd_apdu = apdu.getBuffer();

    //----- check preconditions in the APDU header
    // check if P1='04'
    if (cmd_apdu[ISO7816.OFFSET_P1] != 0x04) {
      ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
    } // if
    // check if P2='00'
    if (cmd_apdu[ISO7816.OFFSET_P2] != 0x00) {
      ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
    } // if
    short lc = (short)(cmd_apdu[ISO7816.OFFSET_LC] & 0x00FF);  // get Lc (command length)
    receiveAPDUBody(apdu);                                     // get the command body

    //----- command functionality
    if (JCSystem.getAID().equals(cmd_apdu, ISO7816.OFFSET_CDATA, (byte) lc) == false) {
      ISOException.throwIt(ISO7816.SW_APPLET_SELECT_FAILED);
    } // if

    //----- prepare response APDU
    ISOException.throwIt(ISO7816.SW_NO_ERROR);   // command proper executed
  }  // cmdSELECT

  //---------------------------------------------------------------------------------------
  //----- program code for the APDU command VERIFY
  private void cmdVERIFY(APDU apdu) {
    byte[] cmd_apdu = apdu.getBuffer();

    //----- check preconditions in the APDU header
    // check if P1='00'
    if (cmd_apdu[ISO7816.OFFSET_P1] != 0) {
      ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
    } // if
    short lc = (short)(cmd_apdu[ISO7816.OFFSET_LC] & 0x00FF);  // get Lc (command length)
    receiveAPDUBody(apdu);                                     // get the command body

    if (cmd_apdu[ISO7816.OFFSET_P2] == PIN_ID) {    
      if (lc != PIN_SIZE) {                    // Lc = length of PIN ?
        ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
      } //if
      if (pin.check(cmd_apdu, ISO7816.OFFSET_CDATA, PIN_SIZE) == false) {
        // PIN verification not successful
        short tries = pin.getTriesRemaining();
        ISOException.throwIt( (short) (SW_PIN_FAILED + tries));  // send error counter in APDU back
      } // if
    } // if
    else {          // it is not a valid PIN identifier
      ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
    } // else
    // PIN verification successful
    ISOException.throwIt(ISO7816.SW_NO_ERROR);               // command proper executed
  }  // cmdVERIFY

  //---------------------------------------------------------------------------------------
  //----- program code for the APDU command PUT DATA
  private void cmdPUTDATA(APDU apdu) {
    byte[] cmd_apdu = apdu.getBuffer();

    //----- check preconditions in the APDU header
    // check if P1=0
    if (cmd_apdu[ISO7816.OFFSET_P1] != 0) {
      ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
    } // if
    // check if P2 contents an allowed tag value
    short tag = (short) (cmd_apdu[ISO7816.OFFSET_P2] & (short) 0x00FF);
    if ((tag < (short) 0x0040) || (tag > (short) 0x00FE)) {
      ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
    } // if
    short lc = (short)(cmd_apdu[ISO7816.OFFSET_LC] & (short) 0x00FF);  // calculate Lc (expected length)
    receiveAPDUBody(apdu);     // get the command body

    //----- check precoditions of security status
    if (pin.isValidated() == false) {
      ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
    } // if

    //----- command functionality
    short result = SetDO(lc, tag, apdu);

    //----- prepare response APDU
    if (result == DO_PROPPER_SET) {
      ISOException.throwIt(ISO7816.SW_NO_ERROR);   // command proper executed
    } // if
    else if (result == NOT_ENOUGH_SPACE) {
      ISOException.throwIt(ISO7816.SW_FILE_FULL);   // not enough free memory
    } // else if
    else if (result == DO_HAVE_WRONG_LENGTH) {
      short index = GetIdxToDO((short) tag);
      short len = memory[(short) (index + LEN_TAG)];  // get the correct length of the DO
      ISOException.throwIt((short) (ISO7816.SW_WRONG_LENGTH + len));   // send correct length back
    }  // else if
    else { // this case should never happen, there must be an error in code above
      ISOException.throwIt(ISO7816.SW_UNKNOWN);   // unknown error
    }  // else
  }  // cmdPUTDATA

  //---------------------------------------------------------------------------------------
  //----- program code for the APDU command GET DATA
  private void cmdGETDATA(APDU apdu) {
    byte[] cmd_apdu = apdu.getBuffer();

    //----- check preconditions in the APDU header
    // check if P1=0
    if (cmd_apdu[ISO7816.OFFSET_P1] != 0) {
      ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
    } // if
    // check if P2 contents an allowed tag value
    short tag = (short) (cmd_apdu[ISO7816.OFFSET_P2] & (short) 0x00FF);
    if ((tag < 0x40) || (tag > 0xFE)) {
      ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
    } // if
    short le = (short)(cmd_apdu[ISO7816.OFFSET_LC] & 0x00FF);  // calculate Le (expected length)
    if (le > SIZE_MEMORY) {
      ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
    } // if

    //----- command functionality
    short index = (short) GetIdxToDO(tag);
    if (index == DO_NOT_FOUND) {             // DO not found
      ISOException.throwIt(SW_DATA_NOT_FOUND);
    } // if
    short len = (short) memory[(short) (index+LEN_TAG)];  // length of the DO value
    len = (short) (len);                          // calculate length of the whole DO
    if (le != len) {
      ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
    } // if
    index = (short) (index + LEN_TAG + LEN_LEN);     // index to the value of the DO

    //----- now all preparations are done, the data object can be send to the IFD -----
    apdu.setOutgoing();                           // set transmission to outgoing data
    apdu.setOutgoingLength((short)le);            // set the number of bytes to send to the IFD
    apdu.sendBytesLong(memory, (short)index, (short)le); // send the requested the number of bytes to the IFD, data field of the DO
  }  // cmdGETDATA

  //---------------------------------------------------------------------------------------
  //----- program code for the APDU command  
  private void cmdDELETE(APDU apdu) {
    byte[] cmd_apdu = apdu.getBuffer();

    //----- check preconditions in the APDU header
    // check if P1=0
    if (cmd_apdu[ISO7816.OFFSET_P1] != 0) {
      ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
    } // if
    // check if P2=0
    if (cmd_apdu[ISO7816.OFFSET_P2] != 0) {
      ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
    } // if
    short lc = (short)(cmd_apdu[ISO7816.OFFSET_LC] & 0x00FF);  // get Lc (command length)
    if (lc != 0) {
      ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
    } // if

    //----- check precoditions of security status
    if (pin.isValidated() == false) {
      ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
    } // if

    //----- now all preparations are done, the data storage area can be deleted
    Util.arrayFillNonAtomic(memory, (short) 0, SIZE_MEMORY, (byte) 0x00);   // overwrite the data storage area with '00'
    memory_NoOfDOs = 0;    // no stored DOs anymore
    ISOException.throwIt(ISO7816.SW_NO_ERROR);               // command proper executed
  }  // cmdDELETEDATA

  //---------------------------------------------------------------------------------------
  //----- receive the body of the command APDU
  public void receiveAPDUBody(APDU apdu) {
    byte[] buffer = apdu.getBuffer();
    short lc = (short)(buffer[ISO7816.OFFSET_LC] & 0x00FF);  // calculate Lc (expected length)
    // check if Lc != number of received bytes of the command APDU body
    if (lc != apdu.setIncomingAndReceive()) {
      ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
    } // if
  }  // receiveAPDUBody

  //---------------------------------------------------------------------------------------
  //----- calculate the index to the first free byte in the memory
  public short GetIdxToFreeSpace () {
    short tag, len, index, docounter;

    index = 0;    //set index to first byte of the memory
    for (docounter = 0; docounter < memory_NoOfDOs; docounter ++) {
      tag = memory[index];                          // get a valid tag, for debugging reasons
      len = memory[(short) (index + LEN_TAG)];      // get length of the DO
      index = (short) (index + len + LEN_TAG + 1);  // calculate the new index to the next DO
    } // for
    return index;       // give the index of the first free byte back
  } // GetIdxToFreeSpace

  //---------------------------------------------------------------------------------------
  //----- get the index to a specific DO in the memory
  public short GetIdxToDO (short tag) {
    short xtag, len, index, docounter;

    index = 0;    //set index to first byte of the memory
    for (docounter = 0; docounter < memory_NoOfDOs; docounter ++) {
      xtag = memory[index];               // get a valid tag
      if (xtag == tag) return index;      // tag found
      else  {    // tag not found
        len = memory[(short) (index + LEN_TAG)];      // get length of the DO
        index = (short) (index + len + LEN_TAG + 1);  // calculate the new index to the next DO
      } // else
    } // for
    return (short) DO_NOT_FOUND;          // tag not found
  } // GetIdxToDO

  //---------------------------------------------------------------------------------------
  //----- set a DO to a new value or create a new DO if it is not existing
  public short SetDO (short lc, short tag, APDU apdu) {
    byte[] cmd_apdu = apdu.getBuffer();
    short index = GetIdxToDO(tag);
    if (index == (short) DO_NOT_FOUND) {
      //----- DO not found -> create a new one
      short index2free = GetIdxToFreeSpace();
      short freesize = (short) (SIZE_MEMORY - index2free);   // calculate size of free space
      short DOsize = (short) (lc+LEN_TAG+LEN_LEN);           // calculate size of the new DO 
      if (DOsize <= freesize) {
        // it is enough space for a new DO
        memory[index2free] = (byte) tag;              // set DO tag
        memory[(short) (index2free+LEN_TAG)] = (byte) lc;              // set DO length
        // copy the DO atomic into the memory
        Util.arrayCopy(cmd_apdu, (short)((ISO7816.OFFSET_CDATA) & 0x00FF), memory, (short) (index2free+LEN_TAG+LEN_LEN), lc);
        memory_NoOfDOs++;      //increase the number of stored DOs
        return DO_PROPPER_SET;
      }  // if
      else  // it is not enough space for a new DO
        return NOT_ENOUGH_SPACE;
    }  // if
    else {
      //----- DO found -> update it
      if (lc != memory[(short) (index+LEN_TAG)]) { // DO have wrong length
        return DO_HAVE_WRONG_LENGTH;
      }  // if
      else {                    // DO have right length -> update it
        // copy the DO atomic into the memory
        Util.arrayCopy(cmd_apdu, (short)((ISO7816.OFFSET_CDATA) & 0x00FF), memory, (short) (index+LEN_TAG+LEN_LEN), lc);
        return DO_PROPPER_SET;
      }  // else
    }  // else
  }  // SetDO

}  // class
