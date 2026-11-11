package com.github.ledhed2222.ewitool;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HexFormat;
import java.util.Random;

import org.junit.jupiter.api.Test;

class EWI4000sPatchTest {

  static byte[] samplePatch( String name, long seed ) {
    byte[] blob = new byte[EWI4000sPatch.EWI_PATCH_LENGTH];
    new Random( seed ).nextBytes( blob );
    blob[0] = (byte) 0xf0;
    blob[1] = 0x47;
    blob[2] = 0x64;
    blob[3] = 0x7f;
    blob[4] = EWI4000sPatch.EWI_SAVE;
    for (int i = 0; i < EWI4000sPatch.EWI_PATCHNAME_LENGTH; i++)
      blob[12 + i] = (byte) (i < name.length() ? name.charAt( i ) : ' ');
    blob[EWI4000sPatch.EWI_PATCH_LENGTH - 1] = MidiHandler.MIDI_SYSEX_TRAILER;
    return blob;
  }

  @Test
  void decodeThenEncodeRoundTrips() {
    byte[] original = samplePatch( "Round Trip", 42 );
    EWI4000sPatch patch = new EWI4000sPatch( original.clone() );
    patch.patchBlob = new byte[EWI4000sPatch.EWI_PATCH_LENGTH];
    patch.encodeBlob();
    assertArrayEquals( original, patch.patchBlob );
  }

  @Test
  void decodesNameAndHeaderFields() {
    byte[] blob = samplePatch( "Sax Lead", 1 );
    blob[5] = 17;
    EWI4000sPatch patch = new EWI4000sPatch( blob );
    assertEquals( "Sax Lead", patch.getName() );
    assertEquals( 17, patch.internalPatchNum );
    assertFalse( patch.isEmpty() );
  }

  @Test
  void newPatchIsEmpty() {
    assertTrue( new EWI4000sPatch().isEmpty() );
  }

  @Test
  void setNamePadsAndWritesBlob() {
    EWI4000sPatch patch = new EWI4000sPatch( samplePatch( "Old", 2 ) );
    assertTrue( patch.setName( "New" ) );
    assertEquals( "New", patch.getName() );
    assertEquals( "New" + " ".repeat( 29 ), new String( patch.patchBlob, 12, 32 ) );
  }

  @Test
  void setNameRejectsEmptyAndTooLong() {
    EWI4000sPatch patch = new EWI4000sPatch( samplePatch( "Keep", 3 ) );
    assertFalse( patch.setName( "" ) );
    assertFalse( patch.setName( "x".repeat( 33 ) ) );
    assertEquals( "Keep", patch.getName() );
  }

  @Test
  void setInternalPatchNumAndModeUpdateBlob() {
    EWI4000sPatch patch = new EWI4000sPatch( samplePatch( "P", 4 ) );
    patch.setInternalPatchNum( (byte) 99 );
    patch.setPatchMode( EWI4000sPatch.EWI_EDIT );
    assertEquals( 99, patch.patchBlob[5] );
    assertEquals( EWI4000sPatch.EWI_EDIT, patch.patchBlob[4] );
  }

  @Test
  void hexRoundTrips() {
    byte[] blob = samplePatch( "Hex", 5 );
    String hex = EWI4000sPatch.toHex( blob, false );
    assertEquals( HexFormat.of().formatHex( blob ), hex );
    assertArrayEquals( blob, new EWI4000sPatch( hex ).patchBlob );
  }

  @Test
  void toHexRejectsWrongLength() {
    assertEquals( "Too short", EWI4000sPatch.toHex( new byte[10], false ) );
    assertEquals( "Too long", EWI4000sPatch.toHex( new byte[300], false ) );
  }

  @Test
  void toHexWithSpaces() {
    String hex = EWI4000sPatch.toHex( samplePatch( "S", 6 ), true );
    assertTrue( hex.startsWith( "f0 47 64 7f " ) );
    assertEquals( EWI4000sPatch.EWI_PATCH_LENGTH * 3, hex.length() );
  }
}
