/**
 * PortsItemEventHandler - this class is just an event handler for the
 *                         MIDI Ports menu item.
 * 
 * This file is part of EWItool.

    EWItool is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    EWItool is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with EWItool.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @author S.Merrony
 * 
 * v.2.0  Catch MidiUnavailableException properly
 */
package com.github.ledhed2222.ewitool;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider;

public class PortsItemEventHandler implements EventHandler<ActionEvent> {
  
  UserPrefs userPrefs;
  
  PortsItemEventHandler( UserPrefs pPrefs ){
    userPrefs = pPrefs;
  }

  @Override
  public void handle( ActionEvent arg0 ) {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle( "EWItool - Select MIDI Ports" );
    dialog.getDialogPane().getButtonTypes().addAll( ButtonType.CANCEL, ButtonType.OK );
    GridPane gp = new GridPane();
    
    gp.add( new Label( "MIDI In Ports" ), 0, 0 );
    gp.add( new Label( "MIDI Out Ports" ), 1, 0 );

    ListView<String> inView, outView;
    List<String> inPortList = new ArrayList<>(),
                 outPortList = new ArrayList<>();
    ObservableList<String> inPorts = FXCollections.observableArrayList( inPortList ),
                           outPorts = FXCollections.observableArrayList( outPortList );
    inView = new ListView<>( inPorts );
    outView = new ListView<>( outPorts );
       
    String lastInDevice = userPrefs.getMidiInPort();
    String lastOutDevice = userPrefs.getMidiOutPort();
    int ipIx = -1, opIx = -1;
    
    MidiDevice device;
    MidiDevice.Info[] infos = CoreMidiDeviceProvider.getMidiDeviceInfo();
    for ( MidiDevice.Info info : infos ) {
      try {
        device = MidiSystem.getMidiDevice(info);
        String displayName = MidiHandler.getMidiDeviceDisplayName(info);
        if (!( device instanceof Sequencer ) && !( device instanceof Synthesizer )) {
          if (device.getMaxReceivers() != 0) {
            opIx++;
            outPorts.add(displayName);
            if (displayName.equals(lastOutDevice)) {
              outView.getSelectionModel().clearAndSelect(opIx);
            }
            Debugger.log( "DEBUG - Found OUT Port: " + info.getName() + " - " + info.getDescription() );
          } else if (device.getMaxTransmitters() != 0) {
            ipIx++;
            inPorts.add(displayName);
            if (displayName.equals(lastInDevice)) {
              inView.getSelectionModel().clearAndSelect(ipIx);
            }
            Debugger.log( "DEBUG - Found IN Port: " + info.getName() + " - " + info.getDescription() );
          }
        }
      } catch (MidiUnavailableException ex) {
        ex.printStackTrace();
      }
    }
 
    gp.add( inView, 0, 1 );
    gp.add( outView, 1, 1 );
    dialog.getDialogPane().setContent( gp );
    
    Optional<ButtonType> rc = dialog.showAndWait();
    
    if (rc.get() == ButtonType.OK) {
      if (outView.getSelectionModel().getSelectedIndex() != -1) {
        userPrefs.setMidiOutPort(outView.getSelectionModel().getSelectedItem());
      }
      if (inView.getSelectionModel().getSelectedIndex() != -1) {
        userPrefs.setMidiInPort(inView.getSelectionModel().getSelectedItem());
      } 
    }
  }

}
