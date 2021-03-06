/**
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

package com.github.ledhed2222.ewitool;

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UserPrefs {
  
  private Preferences p;
  
  // these need to be exposed so that they can be observed
  public StringProperty midiInPort, midiOutPort;
  
  private static final String PREFS_NODE = "ewitool";
  private static final String MIDI_IN_PORT = "MIDI_IN_PORT";
  private static final String MIDI_OUT_PORT = "MIDI_OUT_PORT";
  private static final String LIBRARY_LOCATION = "LIBRARY_LOCATION";

  private static final String EXPORT_SUBDIR = "export";
  
  UserPrefs() {
    p = Preferences.userRoot().node( PREFS_NODE );
    midiInPort = new SimpleStringProperty( getMidiInPort() );
    midiOutPort = new SimpleStringProperty( getMidiOutPort() );
  }
  
  public String getMidiInPort() {
    return p.get( MIDI_IN_PORT, "<Not Chosen>" );
  }
  public void setMidiInPort( String ip ) {
    p.put( MIDI_IN_PORT, ip );
    midiInPort.set( ip );  // This must be last as it notifies change
  }
  public String getMidiOutPort() {
    return p.get( MIDI_OUT_PORT, "<Not Chosen>" );
  }
  public void setMidiOutPort( String op ) {
    p.put( MIDI_OUT_PORT, op );
    midiOutPort.set( op );  // This must be last as it notifies change
  }

  public String getLibraryLocation() {
      String location = p.get( LIBRARY_LOCATION, "<Not Chosen>" );
      if (!location.equals( "<Not Chosen>" )) {
          File llFile = new File( location );
          if (!llFile.exists()) {
              return "<Not Chosen>";
          }
      }
      return location;
  }
  public void setLibraryLocation( String ll ) {
    p.put( LIBRARY_LOCATION, ll );
  }

  public String getExportLocation() {
    return getLibraryLocation() + System.getProperty( "file.separator" ) + 
           EXPORT_SUBDIR;
  }

  public boolean clearPrefs() {
    try {
      p.clear();
    } catch( BackingStoreException e ) {
      return false;
    }
    return true;
  }

}
