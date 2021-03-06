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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import javafx.application.Platform;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * This class represents the persistent set of patches that the 
 * user is working on. (AKA 'Clipboard' in EWItool < v0.9)
 * 
 * @author steve
 *
 */
public class ScratchPad {
  
  ObservableList<EWI4000sPatch> patchList;
  SharedData sharedData;
  UserPrefs userPrefs;
  
  public static final String SCRATCHPAD_NAME = "SCRATCHPAD.BIN";
  
  ScratchPad( SharedData pSharedData, UserPrefs pPrefs ) {
    sharedData = pSharedData;
    userPrefs = pPrefs;
    patchList = FXCollections.observableArrayList();
  }
  
  // load the scratchpad from disk
  public boolean load() {
    if (userPrefs.getLibraryLocation().equals( "<Not Chosen>" )) return false;
    Path spPath = Paths.get( userPrefs.getLibraryLocation(), SCRATCHPAD_NAME );
    try {
      byte[] allBytes = Files.readAllBytes( spPath );
      if (allBytes.length > 200) {
        Debugger.log( "DEBUG - Scratchpad: bytes read: " + allBytes.length );
        patchList.clear();
        for (int byteOffset = 0; byteOffset < allBytes.length; byteOffset += EWI4000sPatch.EWI_PATCH_LENGTH ) {
          EWI4000sPatch ep = new EWI4000sPatch();
          ep.patchBlob = Arrays.copyOfRange( allBytes, byteOffset, byteOffset + EWI4000sPatch.EWI_PATCH_LENGTH  );
          ep.decodeBlob();
          patchList.add( ep );
        }
         Platform.runLater( () -> sharedData.setScratchPadCount( patchList.size() ) );
      }
    } catch( IOException e ) {
      return false;
    }
    return true;  
  }
  
  // write the scratchpad to disk
  public boolean store() {
    Path spPath = Paths.get( userPrefs.getLibraryLocation(), SCRATCHPAD_NAME );
    try {
      Files.deleteIfExists( spPath );
      Files.createFile( spPath );
      for (int p = 0; p < patchList.size(); p++){
        Files.write( spPath, patchList.get( p ).patchBlob, StandardOpenOption.APPEND );
      }
       Platform.runLater( () -> sharedData.setScratchPadCount( patchList.size() ) );
    } catch( IOException e ) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public boolean addPatch( EWI4000sPatch patch ) { // TODO - check for duplicate name
    patchList.add( patch );
    store();
    return true;
  }
  
  public boolean removePatch( int ix ) {
    patchList.remove( ix );
    store();
    return true;
  }
  
  public boolean renamePatch( int x, String newName ) {
    if (patchList.get( x ).setName( newName )) {
      store();
      return true;
    } else
      return false;    
  }
  
  public boolean clearAll() {
    Path spPath = Paths.get( userPrefs.getLibraryLocation(), SCRATCHPAD_NAME );
    try {
      Files.delete( spPath );
      Files.createFile( spPath );
       Platform.runLater( () -> sharedData.setScratchPadCount( 0 ) );
    } catch( IOException e ) {
      e.printStackTrace();
      return false;
    }
    patchList.clear();
    return true;
  }
  
  // does the specified patch name already exist in the scratchpad?
  public boolean exists( String name ) {
    // TODO: test for extant patch with same name
    return false;
  }
  
  
}
