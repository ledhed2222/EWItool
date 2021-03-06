package com.github.ledhed2222.ewitool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer.MarginType;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.transform.Scale;

public class CurrentPatchSetTab extends Tab {

  Button[] patchButtons;
  Button saveButton, printButton;
  SharedData sharedData;
  ScratchPad scratchPad;
  MidiHandler midiHandler;
  Tab patchEditorTab;

  CurrentPatchSetTab( SharedData pSharedData, ScratchPad pScratchPad, MidiHandler pMidiHandler, Tab pPatchEditorTab ) {

    setText( "Current Patch Set" );
    setClosable( false );

    sharedData = pSharedData;
    scratchPad = pScratchPad;
    midiHandler = pMidiHandler;
    patchEditorTab = pPatchEditorTab;

    GridPane gp = new GridPane();
    gp.setId( "current-patch-set-grid" );

    ColumnConstraints[] ccs;
    ccs = new ColumnConstraints[ 2 ];

    // Label cols are constrained to <=30 pixels wide
    ccs[0] = new ColumnConstraints( 10.0, 30.0, 30.0 );
    ccs[0].setHalignment( HPos.CENTER );
    // Button cols can grow indefinitely
    ccs[1] = new ColumnConstraints( 40.0, 90.0, Double.MAX_VALUE );
    ccs[1].setHgrow( Priority.ALWAYS );
    gp.getColumnConstraints().addAll( ccs[0], ccs[1] );
    gp.getColumnConstraints().addAll( ccs[0], ccs[1] );
    gp.getColumnConstraints().addAll( ccs[0], ccs[1] );
    gp.getColumnConstraints().addAll( ccs[0], ccs[1] );
    gp.getColumnConstraints().addAll( ccs[0], ccs[1] );

    RowConstraints rc;
    rc = new RowConstraints();
    rc.setVgrow( Priority.ALWAYS );

    patchButtons = new Button[100];

    for (int r = 0; r < 20; r++) {
      gp.getRowConstraints().add( rc );
      for (int c = 0; c < 5; c++) {
        int ix = (c * 20 ) + r;
        gp.add( new Label( Integer.toString( ix ) ), c * 2, r );
        patchButtons[ix] = new Button( "<Not Loaded>" );  
        patchButtons[ix].setUserData( ix );  // store the visible patch number 
        patchButtons[ix].setMinWidth( 40.0 );
        patchButtons[ix].setPrefWidth( 90.0 );
        patchButtons[ix].setMaxWidth( Double.MAX_VALUE );
        patchButtons[ix].setOnAction( new PatchButtonEventHandler() );
        patchButtons[ix].setOnDragDetected( (me) -> { 
          Debugger.log( "DEBUG - Drag detected from button #" + ix );
          Dragboard db = patchButtons[ix].startDragAndDrop( TransferMode.MOVE );
          ClipboardContent content = new ClipboardContent();
          content.putString( String.valueOf( ix ) );
          db.setContent(content);
          me.consume();
        });
        patchButtons[ix].setOnDragOver( (de) -> {
          if (de.getGestureSource() != patchButtons[ix]) de.acceptTransferModes( TransferMode.MOVE );
          de.consume();
        });
        patchButtons[ix].setOnDragEntered( (de) -> {
          if (de.getGestureSource() != patchButtons[ix]) {
            patchButtons[ix].setId( "current-patch-set-grid-dropover" );
          }
        });
        patchButtons[ix].setOnDragExited( (de) -> {
           patchButtons[ix].setId( "" );
        });
        patchButtons[ix].setOnDragDropped( (e) -> {
          Debugger.log( "DEBUG - Drop detected on button #" + ix );
          Dragboard db = e.getDragboard();
          if (db.hasString()) {
            int srcIx = Integer.parseInt( db.getString() );
            sharedData.swapPatches( ix, srcIx );
            midiHandler.sendPatch( sharedData.ewiPatchList[ix], EWI4000sPatch.EWI_SAVE );
            midiHandler.sendPatch( sharedData.ewiPatchList[srcIx], EWI4000sPatch.EWI_SAVE );
            updateLabels();
            sharedData.setStatusMessage( "Patches " + ix + " and " + srcIx + " swapped" );
          }
          e.setDropCompleted( true );
          e.consume();
        });
        gp.add( patchButtons[ix], (c * 2 ) + 1, r );
      }
    }

    saveButton = new Button( "Save to Library" );
    saveButton.setOnAction( (ae) -> {
      TextInputDialog tid = new TextInputDialog( ".syx" );
      tid.setTitle( "EWItool - Save Patch Set to Library" );
      tid.setHeaderText( "Enter filename for Patch Set (end with .syx)" );
      Optional<String> res = tid.showAndWait();
      res.ifPresent( setName -> {
        try {
          switch( PatchSet.save( sharedData.ewiPatchList, setName )) {
          case OK:
            Alert al = new Alert( AlertType.INFORMATION );
            al.setTitle( "EWItool - Save Patch Set to Library" );
            al.setContentText( "Patch Set " + setName + " saved to library." );
            al.showAndWait();
            sharedData.setStatusMessage( "Patch set " + setName + " saved to library" );
            break;
          case ALREADY_EXISTS:
            Alert aeal = new Alert( AlertType.ERROR );
            aeal.setTitle( "EWItool - Save Patch Set to Library" );
            aeal.setContentText( "A Patch set with that name already exists, please use a different name." );
            aeal.showAndWait();
            break;
          case NO_PERMISSION:
            Alert npal = new Alert( AlertType.ERROR );
            npal.setTitle( "EWItool - Save Patch Set to Library" );
            npal.setContentText( "Could not write to Library directory." );
            npal.showAndWait();
            break;
          }
        } catch( Exception e ) {
          e.printStackTrace();
        }
      });
    });
    gp.add( saveButton, 5, 20 );
    
    printButton = new Button( "Print" );
    printButton.setOnAction( (ae) -> {
      PrinterJob pj = PrinterJob.createPrinterJob();
      if (pj != null && pj.showPrintDialog( gp.getScene().getWindow() )) {
        PageLayout layout = pj.getPrinter().createPageLayout( Paper.A4, PageOrientation.LANDSCAPE, MarginType.DEFAULT );
        double scaleX = layout.getPrintableWidth() / gp.getBoundsInParent().getWidth();
        double scaleY = layout.getPrintableHeight() / gp.getBoundsInParent().getHeight();
        gp.getTransforms().add( new Scale( scaleX, scaleY ) );
        if (pj.printPage( layout, gp ))
          pj.endJob();       
        gp.getTransforms().clear();
      }
    });
    
    gp.add( printButton, 7, 20 );
    
    DateTimeFormatter format = DateTimeFormatter.ofPattern( "MMM d yyy hh:mm a" );
    Label dateLabel = new Label( LocalDateTime.now().format( format ) );
    GridPane.setColumnSpan( dateLabel, 2 );
    gp.add( dateLabel, 8, 20 );

    setContent( gp );   
  }

  /**
   * refresh the button labels eg. after reloading data from EWI
   * ONLY call this from the main GUI thread.
   */
  public void updateLabels() { 
    for (int p = 0; p <EWI4000sPatch.EWI_NUM_PATCHES; p++ ) {
      patchButtons[p].setText( sharedData.ewiPatchList[p].getName() );
    }
    ((PatchEditorTab) patchEditorTab).populateCombo( sharedData );
  }
  
  public void reset() {
    for (Button b : patchButtons) b.setText( "<Not Loaded>" );
    patchEditorTab.setDisable( true );
    setDisable( true );
  }

  // The Patch Actions dialog that appears when a button is pressed
  class PatchButtonEventHandler implements EventHandler<ActionEvent> {

    @Override
    public void handle( ActionEvent ae ) {

      Dialog<ButtonType> dialog = new Dialog<>();
      dialog.setTitle( "EWItool - Patch Actions" );
      String buttonText = ((Button)ae.getSource()).getText();
      ButtonType editType = new ButtonType( "Edit" );
      dialog.getDialogPane().getButtonTypes().add( editType );
      ButtonType copyType = new ButtonType( "Copy to Scratchpad" );
      if (!buttonText.equals("<Empty>")) dialog.getDialogPane().getButtonTypes().add( copyType );
      ButtonType pasteType = new ButtonType( "Paste from Scratchpad" );
      dialog.getDialogPane().getButtonTypes().add(pasteType );
      ButtonType renameType = new ButtonType( "Rename" );
      if (!buttonText.equals("<Empty>")) dialog.getDialogPane().getButtonTypes().add( renameType );
      dialog.getDialogPane().getButtonTypes().add( ButtonType.CANCEL );
      GridPane gp = new GridPane();
      gp.add( new Label( "Patch: " ), 0, 0 );
      TextField nameField = new TextField( ((Button)ae.getSource()).getText() );
      gp.add( nameField, 2, 0 );
      gp.add( new Label( " Scratchpad Patch for Pasting: " ), 3, 0 );
      ChoiceBox<String> spChoice = new ChoiceBox<>();
      for (int p = 0; p < scratchPad.patchList.size(); p++) {
        spChoice.getItems().add( scratchPad.patchList.get( p ).getName() );
      }
      gp.add( spChoice, 4, 0 );

      dialog.getDialogPane().setContent( gp );

      Optional<ButtonType> rc = dialog.showAndWait();

      if (rc.get() == editType) {
        // select the patch on the Patch editor tab top combo
        ((PatchEditorTab) patchEditorTab).patchesCombo.getSelectionModel().select( (int) ((Button)ae.getSource()).getUserData() );
        // switch to the Patch editor tab
        patchEditorTab.getTabPane().getSelectionModel().select( patchEditorTab );
  
      } else if ( rc.get() == copyType) {
        scratchPad.addPatch( sharedData.ewiPatchList[ (int) ((Button)ae.getSource()).getUserData() ] );

      } else if ( rc.get() == pasteType) {
        if (spChoice.getSelectionModel().getSelectedIndex() >= 0) {
          int pNum = (int) ((Button)ae.getSource()).getUserData();
          EWI4000sPatch tmpPatch = scratchPad.patchList.get( spChoice.getSelectionModel().getSelectedIndex() );
          tmpPatch.setInternalPatchNum( (byte) sharedData.ewiPatchNums[pNum] );
          sharedData.ewiPatchList[pNum] = tmpPatch;
          patchButtons[pNum].setText( tmpPatch.getName() );
          midiHandler.sendPatch( tmpPatch, EWI4000sPatch.EWI_SAVE );
          sharedData.setStatusMessage( "Patch #" + pNum + " saved to EWI" );
          ((PatchEditorTab) patchEditorTab).populateCombo( sharedData );
        } else {
          Alert alert = new Alert( AlertType.WARNING, "No Patch selected from Scratchpad list" );
          alert.showAndWait();
        }

      } else if ( rc.get() == renameType) {
        int pNum = (int) ((Button)ae.getSource()).getUserData();
        if (sharedData.ewiPatchList[pNum].setName( nameField.getText() )) {
          patchButtons[pNum].setText( nameField.getText() );
          Alert alert = new Alert( AlertType.INFORMATION, "Patch renamed" );
          alert.setTitle( "EWItool - Rename Patch" );
          alert.setHeaderText( "" );
          alert.showAndWait();
          midiHandler.sendPatch( sharedData.ewiPatchList[pNum], EWI4000sPatch.EWI_SAVE );
          sharedData.setStatusMessage( "Patch #" + pNum + " saved to EWI" );
          ((PatchEditorTab) patchEditorTab).populateCombo( sharedData );
        } else {
          Alert alert = new Alert( AlertType.WARNING, "Could not rename to '" + nameField.getText() + "'" );
          alert.setTitle( "EWItool - Rename Patch" );
          alert.setHeaderText( "" );
          alert.showAndWait();
        }       
      }
    }
  }

}
