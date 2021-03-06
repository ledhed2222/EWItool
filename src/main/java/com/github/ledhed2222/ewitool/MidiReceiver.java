/**
 * This file is part of EWItool.
 *
 *  EWItool is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  EWItool is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with EWItool.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.ledhed2222.ewitool;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.SysexMessage;

import com.github.ledhed2222.ewitool.SendMsg.MidiMsgType;
import javafx.application.Platform;

public class MidiReceiver implements Receiver {

  SharedData sharedData;
  MidiMonitorMessage mmsg;

  /**
   * @param pSharedData
   */
  public MidiReceiver( SharedData pSharedData ) {
    sharedData = pSharedData;
    mmsg = new MidiMonitorMessage();
    mmsg.direction = MidiMonitorMessage.MidiDirection.RECEIVED;
  }

  @Override
  public synchronized void send( MidiMessage message, long timeStamp ) {
    
    // we are currently only interested in SysEx messages from the EWI
    // Unfortunately the structure of the SysEx messages is not consistent - we 
    // have to examine the first few bytes...

    if (message instanceof SysexMessage) {
      byte[] messageBytes = ((SysexMessage) message).getData();
      
      if (sharedData.getMidiMonitoring()) {
        mmsg.type = MidiMsgType.SYSEX;
        mmsg.bytes = ((SysexMessage) message).getData();
        sharedData.monitorQ.add( mmsg );
      }

      if (messageBytes[0] == MidiHandler.MIDI_SYSEX_AKAI_ID && 
          messageBytes[1] == MidiHandler.MIDI_SYSEX_AKAI_EWI4K /* &&
	        messageBytes[2] == MidiHandler.MIDI_SYSEX_ALLCHANNELS */
         ) { // PATCH or QuickPC

        if (messageBytes[3] == MidiHandler.MIDI_PRESET_DUMP) {
          if (messageBytes.length != (MidiHandler.EWI_SYSEX_PRESET_DUMP_LEN - 1)) {
            System.err.println( "Error - Invalid preset dump SysEx received from EWI (" + messageBytes.length + " bytes)" );
            return;
          }
          // PATCH...
          EWI4000sPatch thisPatch = new EWI4000sPatch();
          thisPatch.patchBlob[0] = (byte) 0b11110000; // 0xf0
          for (int b = 0; b < (MidiHandler.EWI_SYSEX_PRESET_DUMP_LEN - 1); b++) thisPatch.patchBlob[b+1] = messageBytes[b];
          thisPatch.patchBlob[MidiHandler.EWI_SYSEX_PRESET_DUMP_LEN - 1] = (byte) 0b11110111; // 0xf7
          thisPatch.decodeBlob();
          if (thisPatch.header[3] == MidiHandler.MIDI_SYSEX_ALLCHANNELS) {
            int thisPatchNum = thisPatch.internalPatchNum;   
            if (thisPatchNum < 0 || thisPatchNum >= EWI4000sPatch.EWI_NUM_PATCHES) {
              System.err.println( "Error - Invalid patch number (" + thisPatchNum + ") received from EWI");
            } else {
              // adjust thisPatchNum to be displayed version of the patch number
              if (thisPatchNum == 99)
                thisPatchNum = 0;
              else
                thisPatchNum++;
              sharedData.ewiPatchList[thisPatchNum] = thisPatch ;
              if (thisPatchNum == 99) sharedData.setLastPatchLoaded( thisPatchNum );
              sharedData.patchQ.add( thisPatchNum );
              Debugger.log( "DEBUG - MidiReceiver: Patch number: " + thisPatchNum + " received" );
            }
          }
          return;
        }

        if (messageBytes[3] == MidiHandler.MIDI_QUICKPC_DUMP) {
          if (messageBytes.length != (MidiHandler.EWI_SYSEX_QUICKPC_DUMP_LEN - 1)) {
            System.err.println( "Error - Invalid preset QuickPC dump SysEx received from EWI (" + messageBytes.length + " bytes)" );
            return;
          }
          // QUICKPC...
          for (int qpc = 0; qpc < MidiHandler.EWI_NUM_QUICKPCS; qpc++) 
            sharedData.quickPCs[qpc] = messageBytes[qpc + 5];
          sharedData.loadedQuickPCs = true;
          Debugger.log( "DEBUG - MidiReceiver: " + sharedData.quickPCs.length + " Quick PCs received" );
          return;
        }
      }

      if (messageBytes[0] == MidiHandler.MIDI_SYSEX_NONREALTIME && 
          messageBytes[1] == 0x00) { // DEVICE ID
        if (messageBytes.length != (MidiHandler.EWI_SYSEX_ID_RESPONSE_LEN - 1)) {
          sharedData.deviceIdQ.add( SharedData.DeviceIdResponse.WRONG_LENGTH );
          return;
        }
        if (messageBytes[4] != MidiHandler.MIDI_SYSEX_AKAI_ID) {
          sharedData.deviceIdQ.add( SharedData.DeviceIdResponse.NOT_AKAI );
          return;
        }
        if (messageBytes[5] != MidiHandler.MIDI_SYSEX_AKAI_EWI4K) {
          sharedData.deviceIdQ.add( SharedData.DeviceIdResponse.NOT_EWI4000S );
          return;
        }
        // Could get firmware version here too if needed...
        Debugger.log( "DEBUG - MidiReceiver got correct EWI4000s Device ID" );
        sharedData.deviceIdQ.add( SharedData.DeviceIdResponse.IS_EWI4000S);
        // must use runLater as not on GUI thread here...
        Platform.runLater( () -> {
          sharedData.setEwiAttached( true );
        } );
        return;
      }

      System.err.println( "Warning - Unrecognised SysEx type of length " + messageBytes.length + 
                          " received from EWI, starting: " + messageBytes[0] + " " + messageBytes[1] + 
                          " " + messageBytes[2] );
    }
  }

  @Override
  public void close() {

  }

}
