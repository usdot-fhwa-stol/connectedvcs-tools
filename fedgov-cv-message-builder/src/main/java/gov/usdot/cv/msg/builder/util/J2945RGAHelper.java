package gov.usdot.cv.msg.builder.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class J2945RGAHelper {

    // Matches the frame's "value: <TypeName>: " prefix. The payload starts right after it.
    private static final Pattern PAYLOAD_START = Pattern.compile("value:\\s*[\\w-]+:\\s*");

    private J2945RGAHelper() {
        // utility class: only static methods
    }

    /**
     * Decoded ASN.1 text including the MessageFrame wrapper.
     *
     * @throws IllegalArgumentException if the hex is invalid or the message cannot be decoded
     */
    public static String getReadableRGAplusFrame(String framedHex) {
        try {
            byte[] framed = Hex.decodeHex(framedHex.toCharArray());
            return new TIMValidator().validateTIM(framed);
        } catch (DecoderException | SemiValidatorException e) {
            throw new IllegalArgumentException("Failed to decode RGA message: " + e.getMessage(), e);
        }
    }

    /** Decoded ASN.1 text without the MessageFrame wrapper. */
    public static String getReadableRGA(String framedHex) {
        return trimToPayload(getReadableRGAplusFrame(framedHex));
    }

    /** Cuts the decoded text down to the payload. Package-private so tests can reach it. */
    static String trimToPayload(String decoded) {
        Matcher m = PAYLOAD_START.matcher(decoded);
        if (!m.find()) {
            return decoded;
        }

        String payload = decoded.substring(m.end()).trim();
        int lastOuterBrace = payload.lastIndexOf('}');   // closing brace of the MessageFrame
        if (lastOuterBrace >= 0) {
            payload = payload.substring(0, lastOuterBrace).trim();
        }
        return payload;
    }
}