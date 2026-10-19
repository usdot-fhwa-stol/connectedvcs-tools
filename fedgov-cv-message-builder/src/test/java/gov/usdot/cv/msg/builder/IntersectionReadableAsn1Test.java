package gov.usdot.cv.msg.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import gov.usdot.cv.msg.builder.message.IntersectionMessage;

/**
 * Checks the readable ASN.1 text and hex that the Encode popup shows for MAP and RGA,
 * with and without the MessageFrame.
 *
 * Same style as IntersectionSituationDataBuilderTest: JUnit 4 and fixtures read by relative
 * path (run from the fedgov-cv-message-builder folder, as Maven does). The tests go through
 * the real native decoder, so they need an up-to-date asn1c jni library in the local Maven
 * repository (rebuild it with fedgov-cv-lib-asn1c/build_jni.sh and mvn install).
 */
public class IntersectionReadableAsn1Test {

    // messageType in the fixture: "Frame+Map"
    private static final String MAP_FIXTURE = "src/test/resources/samplemap.json";
    // messageType in the fixture: "RGA"
    private static final String RGA_FIXTURE = "src/test/resources/samplerga18.json";
    // Decodes to well over 64 KB of text (about 164,000 characters).
    private static final String LARGE_RGA_FIXTURE = "src/test/resources/samplerga12.json";

    // Values of the messageType field. "Frame+Map", "RGA" and "Frame+RGA" are taken from the
    // fixtures and the UI payload.
    private static final String TYPE_MAP = "Map";
    private static final String TYPE_FRAME_MAP = "Frame+Map";
    private static final String TYPE_RGA = "RGA";
    private static final String TYPE_FRAME_RGA = "Frame+RGA";

    // The builder strips the first 8 hex characters (the 4-byte MessageFrame header)
    // for the types without a frame.
    private static final int FRAME_HEADER_HEX_CHARS = 8;
    private static final int OLD_NATIVE_BUFFER_LIMIT = 65535;

    private IntersectionSituationDataBuilder builder;

    @Before
    public void setup() {
        builder = new IntersectionSituationDataBuilder();
    }

    // ---- MAP -------------------------------------------------------------

    @Test
    public void framePlusMapReadableStringIsAsn1TextWithFrame() throws IOException {
        String text = readableString(buildAs(MAP_FIXTURE, TYPE_FRAME_MAP));

        assertTrue(text.startsWith("MessageFrame ::= {"));
        assertTrue(text.contains("messageId: 18"));
        assertTrue(text.contains("MapData ::= {"));
        assertBalanced(text);
    }

    @Test
    public void mapReadableStringIsAsn1TextWithoutFrame() throws IOException {
        String text = readableString(buildAs(MAP_FIXTURE, TYPE_MAP));

        assertTrue(text.startsWith("MapData ::= {"));
        assertFalse(text.contains("MessageFrame ::="));
        assertBalanced(text);
    }

    @Test
    public void mapHexWithoutFrameIsTheFramedHexMinusItsHeader() throws IOException {
        String framed = buildAs(MAP_FIXTURE, TYPE_FRAME_MAP).getHexString();
        String bare = buildAs(MAP_FIXTURE, TYPE_MAP).getHexString();

        assertTrue("framed MAP hex should start with message id 18 (0x0012)", framed.startsWith("0012"));
        assertEquals(framed.substring(FRAME_HEADER_HEX_CHARS), bare);
    }

    // ---- RGA -------------------------------------------------------------

    @Test
    public void frameRgaReadableStringIsAsn1TextWithFrame() throws IOException {
        String text = readableString(buildAs(RGA_FIXTURE, TYPE_FRAME_RGA));

        assertTrue(text.startsWith("MessageFrame ::= {"));
        assertTrue(text.contains("messageId: 43"));
        assertTrue(text.contains("RoadGeometryAndAttributes ::= {"));
        assertBalanced(text);
    }

    @Test
    public void rgaReadableStringIsAsn1TextWithoutFrame() throws IOException {
        String text = readableString(buildAs(RGA_FIXTURE, TYPE_RGA));

        assertTrue(text.startsWith("RoadGeometryAndAttributes ::= {"));
        assertFalse(text.contains("MessageFrame ::="));
        assertBalanced(text);
    }

    @Test
    public void rgaHexWithoutFrameIsTheFramedHexMinusItsHeader() throws IOException {
        String framed = buildAs(RGA_FIXTURE, TYPE_FRAME_RGA).getHexString();
        String bare = buildAs(RGA_FIXTURE, TYPE_RGA).getHexString();

        assertTrue("framed RGA hex should start with message id 43 (0x002b)", framed.startsWith("002b"));
        assertEquals(framed.substring(FRAME_HEADER_HEX_CHARS), bare);
    }

    /**
     * Regression test for the 64 KB native buffer: the old decoder cut long text at 65,535
     * characters, which left the braces unbalanced.
     */
    @Test
    public void largeRgaReadableStringIsNotTruncated() throws IOException {
        String text = readableString(builder.build(read(LARGE_RGA_FIXTURE)));

        assertTrue("text was cut at the old 64 KB native buffer limit, length=" + text.length(),
                text.length() > OLD_NATIVE_BUFFER_LIMIT);
        assertBalanced(text);
    }

    // ---- the fallback when the ASN.1 text cannot be produced --------------

    @Test
    public void readableOrFallbackKeepsTheEncodeWorkingWhenDecodingFails() {
        String text = builder.readableOrFallback(reason -> { throw new IllegalStateException(reason); }, "boom");

        assertEquals("ASN.1 text unavailable: boom", text);
    }

    @Test
    public void readableOrFallbackReturnsTheDecodedTextWhenDecodingWorks() {
        assertEquals("MapData ::= {}", builder.readableOrFallback(text -> text, "MapData ::= {}"));
    }

    // ---- helpers ---------------------------------------------------------

    /** Builds the fixture with its messageType field replaced, leaving the rest of the JSON as is. */
    private IntersectionMessage buildAs(String path, String type) throws IOException {
        String json = read(path);
        String typeField = "\"messageType\": \"" + type + "\"";
        String withType = json.replaceFirst("\"messageType\"\\s*:\\s*\"[^\"]*\"", typeField);

        assertTrue("fixture has no messageType field: " + path, withType.contains(typeField));
        return builder.build(withType);
    }

    private static String readableString(IntersectionMessage message) {
        String text = message.getReadableString();

        assertNotNull(text);
        assertFalse("decode fell back to the error text: " + text,
                text.startsWith("ASN.1 text unavailable"));
        return text;
    }

    private static String read(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    private static void assertBalanced(String text) {
        int depth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                assertTrue("closing brace without an opening one at index " + i, depth >= 0);
            }
        }
        assertTrue("unbalanced braces (" + depth + " still open): the text is cut off or damaged",
                depth == 0);
    }
}
