package com.github.ledhed2222.ewitool;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PatchSetIT {

  @TempDir
  Path dir;

  private void assertConverted(String ext, byte[] bodyMarker) throws Exception {
    int len = EWI4000sPatch.EWI_PATCH_LENGTH;
    int count = EWI4000sPatch.EWI_NUM_PATCHES;
    ByteArrayOutputStream patches = new ByteArrayOutputStream();
    for (int i = 0; i < count; i++) {
      byte[] p = EWI4000sPatchTest.samplePatch("Patch " + i, i);
      p[5] = (byte) i;
      patches.write(p);
    }
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bos.write(new byte[64]);
    bos.write(bodyMarker);
    bos.write(patches.toByteArray());
    Path legacy = dir.resolve("bank." + ext);
    Files.write(legacy, bos.toByteArray());

    assertEquals(count, PatchSet.convertLegacy(legacy.toFile()));

    byte[] syx = Files.readAllBytes(dir.resolve("bank.syx"));
    assertArrayEquals(patches.toByteArray(), syx);
    assertEquals("Patch 99", new EWI4000sPatch(Arrays.copyOfRange(syx, 99 * len, 100 * len)).getName());
  }

  @Test
  void convertsBnk() throws Exception {
    assertConverted("bnk", new byte[] { 'B', 'O', 'D', 'Y', 0, 0, 0, 0 });
  }

  @Test
  void convertsSqs() throws Exception {
    // the SQS marker's last 6 bytes are the first patch's header, so only the first 8 precede it
    assertConverted("sqs", Arrays.copyOf(PatchSet.SQS_BODY_START, 8));
  }
}
