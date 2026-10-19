package gov.usdot.cv.msg.builder.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Pure string tests for the readable-ASN.1 helpers.
 *
 * They do not need the native asn1c library, because they only check how the
 * decoder's text is trimmed. They need J2735MAPHelper.trimToMapData and
 * J2945RGAHelper.trimToPayload to be package-private (or wider) static methods.
 */
public class ReadableAsn1HelpersTest {

    // Shaped like the decoder's real output for a MAP frame (shortened).
    private static final String MAP_FRAME =
            "MessageFrame ::= {\n"
          + "    messageId: 18\n"
          + "    value: MapData: MapData ::= {\n"
          + "        msgIssueRevision: 38\n"
          + "        intersections: IntersectionGeometryList ::= {\n"
          + "            IntersectionGeometry ::= {\n"
          + "                roadAuthorityID: fullRdAuthID: { 0.21.342 }\n"
          + "            }\n"
          + "        }\n"
          + "    }\n"
          + "}\n";

    // Shaped like the decoder's real output for an RGA frame (shortened).
    private static final String RGA_FRAME =
            "MessageFrame ::= {\n"
          + "    messageId: 43\n"
          + "    value: RoadGeometryAndAttributes: RoadGeometryAndAttributes ::= {\n"
          + "        dataSetSet: dataSetSet ::= {\n"
          + "            RGADataSet ::= {\n"
          + "                movementsContainer: movementsContainer ::= {\n"
          + "                    RGAMovementsLayers ::= {\n"
          + "                        movementsContainer-ID: 1\n"
          + "                    }\n"
          + "                }\n"
          + "            }\n"
          + "        }\n"
          + "    }\n"
          + "}\n";

    @Test
    public void mapTrim_removesFrameAndKeepsMapData() {
        String out = J2735MAPHelper.trimToMapData(MAP_FRAME);

        assertTrue(out.startsWith("MapData ::= {"));
        assertTrue(out.endsWith("}"));
        assertTrue(out.contains("msgIssueRevision: 38"));
        assertFalse(out.contains("MessageFrame"));
        assertFalse(out.contains("messageId"));
        assertBalanced(out);
    }

    @Test
    public void mapTrim_returnsInputUnchangedWhenMarkerMissing() {
        String text = "Decoding failed for MessageFrame: message is truncated or incomplete";
        assertEquals(text, J2735MAPHelper.trimToMapData(text));
    }

    @Test
    public void rgaTrim_removesFrameAndKeepsPayload() {
        String out = J2945RGAHelper.trimToPayload(RGA_FRAME);

        assertTrue(out.startsWith("RoadGeometryAndAttributes ::= {"));
        assertTrue(out.endsWith("}"));
        assertTrue(out.contains("movementsContainer-ID: 1"));
        assertFalse(out.contains("MessageFrame"));
        assertFalse(out.contains("messageId"));
        assertBalanced(out);
    }

    @Test
    public void rgaTrim_returnsInputUnchangedWhenNoValueLine() {
        String text = "Decoding failed for MessageFrame: message is truncated or incomplete";
        assertEquals(text, J2945RGAHelper.trimToPayload(text));
    }

    /**
     * The trim code assumes the last '}' is the MessageFrame's. If the native
     * decoder ever truncates its output again, the text ends up unbalanced.
     * This documents why the balance check is used as a canary.
     */
    @Test
    public void truncatedDecoderOutput_isDetectedAsUnbalanced() {
        String truncated = RGA_FRAME.substring(0, RGA_FRAME.length() / 2);
        assertFalse(isBalanced(truncated));
    }

    // ---- inputs that cannot be decoded ------------------------------------

    @Test
    public void rgaDecodeRejectsInvalidHex() {
        try {
            J2945RGAHelper.getReadableRGA("zz");
            fail("expected an IllegalArgumentException for hex that is not hex");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("Failed to decode RGA message"));
        }
    }

    @Test
    public void rgaWithFrameDecodeRejectsHexOfOddLength() {
        try {
            J2945RGAHelper.getReadableRGAplusFrame("abc");
            fail("expected an IllegalArgumentException for hex of odd length");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("Failed to decode RGA message"));
        }
    }

    @Test
    public void mapDecodeFramedRejectsBytesThatAreNotAMessage() {
        try {
            J2735MAPHelper.decodeFramed(new byte[] {0x00, 0x01, 0x02});
            fail("expected an IllegalArgumentException for bytes that are not a MessageFrame");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("Failed to decode MAP message"));
        }
    }

    // ---- helpers ---------------------------------------------------------

    static boolean isBalanced(String text) {
        int depth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth < 0) {
                    return false;
                }
            }
        }
        return depth == 0;
    }

    private static void assertBalanced(String text) {
        assertTrue("braces are not balanced", isBalanced(text));
    }
}
