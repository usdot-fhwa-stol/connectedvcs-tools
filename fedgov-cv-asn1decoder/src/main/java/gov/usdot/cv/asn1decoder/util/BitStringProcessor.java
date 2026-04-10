package gov.usdot.cv.asn1decoder.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BitStringProcessor {

    /**
     * AllowedManeuvers bit names (index = bit position)
     */
    private static final String[] MANEUVER_NAMES = {
        "maneuverStraightAllowed",          // 0
        "maneuverLeftAllowed",              // 1
        "maneuverRightAllowed",             // 2
        "maneuverUTurnAllowed",             // 3
        "maneuverLeftTurnOnRedAllowed",     // 4
        "maneuverRightTurnOnRedAllowed",    // 5
        "maneuverLaneChangeAllowed",        // 6
        "maneuverNoStoppingAllowed",        // 7
        "yieldAllwaysRequired",             // 8
        "goWithHalt",                       // 9
        "caution",                          // 10
        "reserved1"                         // 11
    };

    /**
     * Entry point for MAP processing
     */
    public static String processMapBitStrings(String decoded) {
        if (decoded == null) return null;

        decoded = replaceManeuvers(decoded);

        return decoded;
    }

    /**
     * Replace ALL maneuvers occurrences
     */
    private static String replaceManeuvers(String input) {

        Pattern pattern = Pattern.compile(
            "maneuvers:\\s*([0-9A-Fa-f ]+)\\s*\\((\\d+) bits unused\\)"
        );

        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {

            String hex = matcher.group(1).trim();
            int unusedBits = Integer.parseInt(matcher.group(2));

            String decoded = decodeBitString(hex, unusedBits, MANEUVER_NAMES);

            matcher.appendReplacement(
                result,
                "maneuvers: " + Matcher.quoteReplacement(decoded)
            );
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Generic BIT STRING decoder
     */
    private static String decodeBitString(String hex, int unusedBits, String[] names) {

        String[] bytesStr = hex.split("\\s+");
        byte[] bytes = new byte[bytesStr.length];

        for (int i = 0; i < bytesStr.length; i++) {
            bytes[i] = (byte) Integer.parseInt(bytesStr[i], 16);
        }

        int totalBits = bytes.length * 8 - unusedBits;

        StringBuilder sb = new StringBuilder("{ ");

        for (int i = 0; i < totalBits && i < names.length; i++) {

            int byteIndex = i / 8;
            int bitIndex = 7 - (i % 8); // MSB-first

            if (((bytes[byteIndex] >> bitIndex) & 1) == 1) {
                sb.append(names[i]).append(" ");
            }
        }

        sb.append("}");
        return sb.toString();
    }
}   