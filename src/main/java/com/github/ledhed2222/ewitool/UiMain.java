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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;

public class UiMain extends Application {

  static final String  APP_NAME = "EWItool";
  static final String  APP_VERSION = "2.4.4";
  static final int     COPYRIGHT_YEAR = 2025;
  static final String  RELEASE_STATUS = "Production";
  static final String  LEAD_AUTHOR = "S.Merrony & ledhed2222";

  public  static final String  ICON = "/logo.png";
  private static final String  CSS  = "/ewitool.css";
  private static final int     SCENE_PREF_WIDTH = 1100;
  private static final int     SCENE_PREF_HEIGHT = 750;
  private static final String  WINDOW_TITLE = APP_NAME + " - EWI4000s Patch Handling Tool";
  private static final String  ONLINE_HELP = "https://github.com/ledhed2222/EWItool/wiki/Using-EWItool";

  public enum Status { OK, ALREADY_EXISTS, NO_PERMISSION }
  
  MenuBar mainMenuBar;
  Menu patchMenu;
  TabPane tabPane;
  Tab scratchPadTab, patchSetsTab, currentPatchSetTab, patchEditorTab;
  UiStatusBar statusBar;
  MidiHandler midiHandler;
  volatile SharedData sharedData;

  public static void boot(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage mainStage) {

    Debugger.log( "DEBUG - EWItool version: " + APP_VERSION );
    BorderPane root = new BorderPane();
    Scene scene = new Scene( root, SCENE_PREF_WIDTH, SCENE_PREF_HEIGHT );
    String resource = this.getClass().getResource(CSS).toExternalForm();
    scene.getStylesheets().add(resource);
    mainStage.setTitle(WINDOW_TITLE);

    sharedData = new SharedData();  // Create this 1st - holds all info shared across objects

    UserPrefs userPrefs = new UserPrefs();
    ScratchPad scratchPad = new ScratchPad( sharedData, userPrefs );

    statusBar = new UiStatusBar( sharedData );
    sharedData.addObserver( statusBar );
    root.setBottom( statusBar );

    midiHandler = new MidiHandler( sharedData, userPrefs );

    mainMenuBar = new MainMenuBar( mainStage, userPrefs, midiHandler );
    root.setTop( mainMenuBar );

    tabPane = new TabPane();

    scratchPadTab = new ScratchPadTab( sharedData, scratchPad );
    patchEditorTab = new PatchEditorTab( sharedData, scratchPad, midiHandler );
    currentPatchSetTab = new CurrentPatchSetTab( sharedData, scratchPad, midiHandler, patchEditorTab );
    patchSetsTab = new PatchSetsTab( sharedData, scratchPad, userPrefs, midiHandler, currentPatchSetTab );   

    tabPane.getTabs().addAll( scratchPadTab, 
                              patchSetsTab, 
                              currentPatchSetTab,
                              patchEditorTab
                            );

    currentPatchSetTab.setDisable( true );
    // FIXME Uncomment before release    patchEditorTab.setDisable( true );
    patchEditorTab.setDisable( true );

    tabPane.getSelectionModel().selectedItemProperty().addListener( (tab, oldtab, newtab) -> {
      if (newtab == patchEditorTab) patchMenu.setDisable( false );
      if (oldtab == patchEditorTab) patchMenu.setDisable( true );
    });

    // MIDI port assignment change listeners
    userPrefs.midiInPort.addListener( (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
        Debugger.log( "Debug - Noticed that IN Port Changed to : " + newValue );
        midiHandler.restart();
    });
    userPrefs.midiOutPort.addListener( (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
        Debugger.log( "Debug - Noticed that OUT Port Changed to : " + newValue );
        midiHandler.restart();
    });

    // customise icon
    mainStage.getIcons().add( new Image( this.getClass().getResourceAsStream( ICON )));

    mainStage.setOnCloseRequest( (we) -> {
      Debugger.log( "DEBUG - clean exit" );
      midiHandler.close();
      Platform.exit();
      System.exit( 0 );           
    });

    root.setCenter( tabPane );
    mainStage.setScene(scene);
    mainStage.show();
  }

  private class MainMenuBar extends MenuBar {

    Menu fileMenu, midiMenu, ewiMenu, helpMenu;
    Menu generateSubmenu, processSubmenu;
    MenuItem exitItem,
    portsItem, // panicItem, // monitorItem,
    fetchAllItem,
    storeItem, revertItem, copyItem,
    helpItem, aboutItem;

    @SuppressWarnings( "unused" )
    MainMenuBar( Stage mainStage, UserPrefs userPrefs, MidiHandler midiHandler ) {

      fileMenu = new Menu( "_File" );
      //fileMenu.setAccelerator( KeyCombination.keyCombination( "Alt+F"));
      exitItem = new MenuItem( "E_xit" );
      exitItem.setOnAction( (ae) -> {
        Debugger.log( "DEBUG - clean exit" );
        midiHandler.close();
        Platform.exit();
        System.exit( 0 );           
      });
      fileMenu.getItems().addAll(exitItem );

      midiMenu = new Menu( "_MIDI" );
      portsItem = new MenuItem( "_Ports" );
      portsItem.addEventHandler( ActionEvent.ANY, new PortsItemEventHandler( userPrefs ) );
      //panicItem = new MenuItem( "Panic (All Notes Off)" );

      // this will stream MIDI messages to System.out if debugging is enabled in Debugging class
      MidiMonitor monitor = new MidiMonitor( sharedData );
      
      midiMenu.getItems().addAll( portsItem );

      ewiMenu = new Menu( "_EWI" );
      fetchAllItem = new MenuItem( "Fetch _All Patches" );
      fetchAllItem.setOnAction( (ae) -> {
        Debugger.log( "DEBUG - Fetch All..." );
        if (!midiHandler.requestDeviceID()) {
          Alert errAlert = new Alert( AlertType.ERROR, "Not connected to an EWI4000s");
          errAlert.setTitle( "EWItool - Error" );
          errAlert.initOwner( mainStage );
          errAlert.showAndWait();
        } else {
          Alert busyAlert = new Alert( AlertType.INFORMATION, "Fetching all patches.  Please wait..." );
          busyAlert.setTitle( "EWItool" );
          busyAlert.setHeaderText( null );
          busyAlert.initOwner( mainStage );
          busyAlert.show();
          sharedData.clear();
          for (int p = 0; p < EWI4000sPatch.EWI_NUM_PATCHES; p++) {
            midiHandler.requestPatch( p );
            busyAlert.setTitle( (p + 1) + " of 100" );
          }
          busyAlert.close();
          ((CurrentPatchSetTab) currentPatchSetTab).updateLabels();
          ((PatchEditorTab) patchEditorTab).populateCombo( sharedData );
          currentPatchSetTab.setDisable( false );
          patchEditorTab.setDisable( false );
          tabPane.getSelectionModel().select( currentPatchSetTab );
        }
      });
      ewiMenu.getItems().addAll( fetchAllItem );

      patchMenu = new Menu( "_Patch" );
      patchMenu.setDisable( true );
      storeItem = new MenuItem( "Store to EWI" );
      storeItem.setAccelerator( KeyCombination.keyCombination( "Ctrl+S" ) );
      storeItem.setOnAction( (ae) -> ((PatchEditorTab) patchEditorTab).store() );
      revertItem = new MenuItem( "Revert Edits" );
      revertItem.setAccelerator( KeyCombination.keyCombination( "Ctrl+Z" ) );
      revertItem.setOnAction( (ae) -> ((PatchEditorTab) patchEditorTab).revert() );
      copyItem = new MenuItem( "Copy to Scratchpad" );
      copyItem.setAccelerator( KeyCombination.keyCombination( "Ctrl+C" ) );
      copyItem.setOnAction( (ae) -> ((PatchEditorTab) patchEditorTab).copyToScratchPad() );
      
      generateSubmenu = new Menu( "_Generate" );
      MenuItem genDefaultItem = new MenuItem( "_Default Patch" );
      genDefaultItem.setOnAction( (ae) -> ((PatchEditorTab) patchEditorTab).defaultPatch() );
      MenuItem genRandomItem = new MenuItem( "_Random Patch" );
      genRandomItem.setOnAction( (ae) -> ((PatchEditorTab) patchEditorTab).randomPatch() );
      generateSubmenu.getItems().addAll( genDefaultItem, genRandomItem );
      
      processSubmenu = new Menu( "_Process" );
      MenuItem dryItem = new MenuItem( "Make _Dry" );
      dryItem.setOnAction( (ae) -> ((PatchEditorTab) patchEditorTab).makeDry() );
      MenuItem maxVolItem = new MenuItem( "Maximise _Volume" );
      maxVolItem.setOnAction( (ae) -> ((PatchEditorTab) patchEditorTab).makeMaxVol() );
      MenuItem rmNoiseItem = new MenuItem( "Remove _Noise" );
      rmNoiseItem.setOnAction( (ae) -> ((PatchEditorTab) patchEditorTab).makeNoNoise() );
      MenuItem rand10PctItem = new MenuItem( "_Randomise by 10%" );
      rand10PctItem.setOnAction( (ae) -> ((PatchEditorTab) patchEditorTab).randomiseBy10pct() );
      MenuItem mergeItem = new MenuItem( "_Merge with..." );
      mergeItem.setOnAction( (ae) -> ((PatchEditorTab) patchEditorTab).mergePatchesUi() );
      processSubmenu.getItems().addAll( dryItem, maxVolItem, rmNoiseItem, rand10PctItem, mergeItem );
      patchMenu.getItems().addAll( storeItem, revertItem, copyItem, 
                                   new SeparatorMenuItem(), 
                                   generateSubmenu, processSubmenu );

      helpMenu = new Menu( "_Help" );
      helpItem = new MenuItem( "Online _Help" );
      helpItem.setOnAction( (ae) -> getHostServices().showDocument( ONLINE_HELP ) );
      aboutItem = new MenuItem( "_About " + UiMain.APP_NAME );
      aboutItem.setOnAction( (ae) -> {
        Alert aboutAlert = new Alert( AlertType.INFORMATION );
        aboutAlert.setTitle( "About EWItool" );
        aboutAlert.setHeaderText( "EWItool version " + APP_VERSION + " (" + RELEASE_STATUS + ")" );
        aboutAlert.setContentText( "Copyright " + COPYRIGHT_YEAR + " " + LEAD_AUTHOR );
        aboutAlert.initOwner( mainStage );
        aboutAlert.showAndWait();
      });
      helpMenu.getItems().addAll( helpItem, aboutItem );

      getMenus().addAll( fileMenu, midiMenu, ewiMenu, patchMenu, helpMenu );

    }

  }
}
