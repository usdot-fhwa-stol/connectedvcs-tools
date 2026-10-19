package gov.usdot.cv.msg.builder.util;

import gov.usdot.cv.mapencoder.MapData;

public class J2735MAPHelper {

    private static final String MAP_START = "MapData ::= {";

    private J2735MAPHelper() {
        // utility class: only static methods
    }

    /** Decoded ASN.1 text including the MessageFrame wrapper. */
    public static String getReadableMAPplusFrame(MapData message) {
        return decodeFramed(J2735Helper.getBytes(message));
    }

    /** Decoded ASN.1 text trimmed to the MapData payload, like getReadableTIM does for TIM. */
    public static String getReadableMAP(MapData message) {
        return trimToMapData(getReadableMAPplusFrame(message));
    }

    /**
     * Decodes MessageFrame bytes with the native decoder. Package-private so tests can reach it.
     *
     * @throws IllegalArgumentException if the bytes cannot be decoded
     */
    static String decodeFramed(byte[] framed) {
        try {
            return new TIMValidator().validateTIM(framed);
        } catch (SemiValidatorException e) {
            throw new IllegalArgumentException("Failed to decode MAP message: " + e.getMessage(), e);
        }
    }

    /** Cuts the decoded text down to the MapData payload. Package-private so tests can reach it. */
    static String trimToMapData(String decoded) {
        int start = decoded.indexOf(MAP_START);
        if (start < 0) {
            return decoded;
        }

        String map = decoded.substring(start).trim();
        int lastOuterBrace = map.lastIndexOf('}');   // closing brace of the MessageFrame
        if (lastOuterBrace >= 0) {
            map = map.substring(0, lastOuterBrace).trim();
        }
        return map;
    }
}