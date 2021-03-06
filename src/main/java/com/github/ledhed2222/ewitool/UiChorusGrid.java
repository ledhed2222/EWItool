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

import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

/**
 * @author steve
 *
 */
public class UiChorusGrid extends GridPane {

  CheckBox enableCheck;
  Slider delay1Slider, delay2Slider, vib1Slider, vib2Slider,
  wet1Slider, wet2Slider,
  drySlider,
  feedbackSlider, lfoFreqSlider;

  UiChorusGrid( EWI4000sPatch editPatch, MidiHandler midiHandler ) {

    setId( "editor-grid" );

    RowConstraints fixedRC, vgrowRC;
    fixedRC = new RowConstraints();
    fixedRC.setVgrow( Priority.NEVER );
    vgrowRC = new RowConstraints();
    vgrowRC.setVgrow( Priority.ALWAYS );

    getRowConstraints().addAll( fixedRC, vgrowRC, vgrowRC, vgrowRC, vgrowRC );

    Label mainLabel = new Label( "Chorus" );
    mainLabel.setId( "editor-section-label" );
    add( mainLabel, 0, 0 );

    enableCheck = new CheckBox( "Enable" );
    add( enableCheck, 1,0 );
    enableCheck.setOnAction( (event) -> {
      if (enableCheck.isSelected()) {
        midiHandler.sendLiveControl( 9, 81, 1 );
        editPatch.chorusSwitch = 1;
      } else {
        midiHandler.sendLiveControl( 9, 81, 0 );
        editPatch.chorusSwitch = 0;
      }
    });
    delay1Slider = new Slider( 0.0, 127.0, 0.0 );
    delay1Slider.setOrientation( Orientation.HORIZONTAL );
    delay1Slider.valueProperty().addListener( (observable, oldVal, newVal) -> {
      midiHandler.sendLiveControl( 0, 112, newVal.intValue() );
      editPatch.chorusDelay1 = newVal.intValue();
    });
    add( delay1Slider, 0, 2 );
    add( new BoundBelowControlLabel( "Delay 1", HPos.CENTER, delay1Slider ), 0, 1 );

    delay2Slider = new Slider( 0.0, 127.0, 0.0 );
    delay2Slider.setOrientation( Orientation.HORIZONTAL );
    delay2Slider.valueProperty().addListener( (observable, oldVal, newVal) -> {
      midiHandler.sendLiveControl( 3, 112, newVal.intValue() );
      editPatch.chorusDelay2 = newVal.intValue();
    });
    add( delay2Slider, 0, 4 );
    add( new BoundBelowControlLabel( "Delay 2", HPos.CENTER, delay2Slider ), 0, 3 );

    vib1Slider = new Slider( 0.0, 127.0, 0.0 );
    vib1Slider.setOrientation( Orientation.HORIZONTAL );
    vib1Slider.valueProperty().addListener( (observable, oldVal, newVal) -> {
      midiHandler.sendLiveControl( 1, 112, newVal.intValue() );
      editPatch.chorusModLev1 = newVal.intValue();
    });
    add( vib1Slider, 1, 2 );
    add( new BoundBelowControlLabel( "Vib 1", HPos.CENTER, vib1Slider ), 1, 1 );

    vib2Slider = new Slider( 0.0, 127.0, 0.0 );
    vib2Slider.setOrientation( Orientation.HORIZONTAL );
    vib2Slider.valueProperty().addListener( (observable, oldVal, newVal) -> {
      midiHandler.sendLiveControl( 4, 112, newVal.intValue() );
      editPatch.chorusModLev2 = newVal.intValue();
    });
    add( vib2Slider, 1, 4 );
    add( new BoundBelowControlLabel( "Vib 2", HPos.CENTER, vib2Slider ), 1, 3 );

    wet1Slider = new Slider( 0.0, 127.0, 0.0 );
    wet1Slider.setOrientation( Orientation.HORIZONTAL );
    wet1Slider.valueProperty().addListener( (observable, oldVal, newVal) -> {
      midiHandler.sendLiveControl( 2, 112, newVal.intValue() );
      editPatch.chorusWetLev1 = newVal.intValue();
    });
    add( wet1Slider, 2, 2 );
    add( new BoundBelowControlLabel( "Wet 1", HPos.CENTER, wet1Slider ), 2, 1 );

    wet2Slider = new Slider( 0.0, 127.0, 0.0 );
    wet2Slider.setOrientation( Orientation.HORIZONTAL );
    wet2Slider.valueProperty().addListener( (observable, oldVal, newVal) -> {
      midiHandler.sendLiveControl( 5, 112, newVal.intValue() );
      editPatch.chorusWetLev2 = newVal.intValue();
    });
    add( wet2Slider, 2, 4 );
    add( new BoundBelowControlLabel( "Wet 2", HPos.CENTER, wet2Slider ), 2, 3 );

    drySlider = new Slider( 0.0, 127.0, 0.0 );
    drySlider.setOrientation( Orientation.VERTICAL );
    drySlider.valueProperty().addListener( (observable, oldVal, newVal) -> {
      midiHandler.sendLiveControl( 8, 112, newVal.intValue() );
      editPatch.chorusDryLevel = newVal.intValue();
    });
    GridPane.setRowSpan( drySlider, 4 );
    add( drySlider, 3, 1 );
    add( new BoundBelowControlLabel( "Dry", HPos.CENTER, drySlider ), 3, 0 );

    feedbackSlider = new Slider( 0.0, 127.0, 0.0 );
    feedbackSlider.setOrientation( Orientation.HORIZONTAL );
    feedbackSlider.valueProperty().addListener( (observable, oldVal, newVal) -> {
      midiHandler.sendLiveControl( 62, 112, newVal.intValue() );
      editPatch.chorusFeedback = newVal.intValue();
    });
    add( feedbackSlider, 4, 2 );
    add( new BoundBelowControlLabel( "Feedback", HPos.CENTER, feedbackSlider ), 4, 1 );

    lfoFreqSlider = new Slider( 0.0, 127.0, 0.0 );
    lfoFreqSlider.setOrientation( Orientation.HORIZONTAL );
    lfoFreqSlider.valueProperty().addListener( (observable, oldVal, newVal) -> {
      midiHandler.sendLiveControl( 7, 112, newVal.intValue() );
      editPatch.chorusLFOfreq = newVal.intValue();
    });
    add( lfoFreqSlider, 4, 4 );
    add( new BoundBelowControlLabel( "LFO Freq", HPos.CENTER, lfoFreqSlider ), 4, 3 );

  }

  void setControls( EWI4000sPatch editPatch ) {
    enableCheck.setSelected( editPatch.chorusSwitch == 1 );
    delay1Slider.setValue( editPatch.chorusDelay1 );
    delay2Slider.setValue( editPatch.chorusDelay2 );
    vib1Slider.setValue( editPatch.chorusModLev1 );
    vib2Slider.setValue( editPatch.chorusModLev2 );
    wet1Slider.setValue( editPatch.chorusWetLev1 );
    wet2Slider.setValue( editPatch.chorusWetLev2 );
    drySlider.setValue( editPatch.chorusDryLevel );
    feedbackSlider.setValue( editPatch.chorusFeedback );
    lfoFreqSlider.setValue( editPatch.chorusLFOfreq );
  }

}
