/*!
 * JavaScript function to calculate the geodetic distance between two points specified by latitude/longitude using the Vincenty inverse formula for ellipsoids.
 *
 * Taken from http://movable-type.co.uk/scripts/latlong-vincenty.html and optimized / cleaned up by Mathias Bynens <http://mathiasbynens.be/>
 * Based on the Vincenty direct formula by T. Vincenty, “Direct and Inverse Solutions of Geodesics on the Ellipsoid with application of nested equations”, Survey Review, vol XXII no 176, 1975 <http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf>
 *
 * @param   {Number} lat1, lon1: first point in decimal degrees
 * @param   {Number} lat2, lon2: second point in decimal degrees
 * @returns {Number} distance in metres between points
 */
function inverseVincenty(lat1, lon1, lat2, lon2) {
    let a = 6378137,
        b = 6356752.314245,
        f = 1 / 298.257223563 // WGS-84 ellipsoid params
        let φ1 = toRad(lat1), λ1 = toRad(lon1);
        let φ2 = toRad(lat2), λ2 = toRad(lon2);

        let L = λ2 - λ1;
        let tanU1 = (1-f) * Math.tan(φ1), cosU1 = 1 / Math.sqrt((1 + tanU1*tanU1)), sinU1 = tanU1 * cosU1;
        let tanU2 = (1-f) * Math.tan(φ2), cosU2 = 1 / Math.sqrt((1 + tanU2*tanU2)), sinU2 = tanU2 * cosU2;

        let sinλ, cosλ, sinSqσ, sinσ=0, cosσ=0, σ=0, sinα, cosSqα=0, cos2σM=0, C;

        let λ = L, λʹ, iterations = 0, antimeridian = Math.abs(L) > Math.PI;
        do {
            sinλ = Math.sin(λ);
            cosλ = Math.cos(λ);
            sinSqσ = (cosU2*sinλ) * (cosU2*sinλ) + (cosU1*sinU2-sinU1*cosU2*cosλ) * (cosU1*sinU2-sinU1*cosU2*cosλ);
            if (sinSqσ == 0) break; // co-incident points
            sinσ = Math.sqrt(sinSqσ);
            cosσ = sinU1*sinU2 + cosU1*cosU2*cosλ;
            σ = Math.atan2(sinσ, cosσ);
            sinα = cosU1 * cosU2 * sinλ / sinσ;
            cosSqα = 1 - sinα*sinα;
            cos2σM = (cosSqα != 0) ? (cosσ - 2*sinU1*sinU2/cosSqα) : 0; // equatorial line: cosSqα=0 (§6)
            C = f/16*cosSqα*(4+f*(4-3*cosSqα));
            λʹ = λ;
            λ = L + (1-C) * f * sinα * (σ + C*sinσ*(cos2σM+C*cosσ*(-1+2*cos2σM*cos2σM)));
            let iterationCheck = antimeridian ? Math.abs(λ)-Math.PI : Math.abs(λ);
            if (iterationCheck > Math.PI) throw new Error('λ > π');
        } while (Math.abs(λ-λʹ) > 1e-12 && ++iterations<1000);
        if (iterations >= 1000) throw new Error('Formula failed to converge');

        let uSq = cosSqα * (a*a - b*b) / (b*b);
        let A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
        let B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
        let Δσ = B*sinσ*(cos2σM+B/4*(cosσ*(-1+2*cos2σM*cos2σM)-
            B/6*cos2σM*(-3+4*sinσ*sinσ)*(-3+4*cos2σM*cos2σM)));

        let s = b*A*(σ-Δσ);

        let α1 = Math.atan2(cosU2*sinλ,  cosU1*sinU2-sinU1*cosU2*cosλ);
        let α2 = Math.atan2(cosU1*sinλ, -sinU1*cosU2+cosU1*sinU2*cosλ);

        α1 = (α1 + 2*Math.PI) % (2*Math.PI); // normalise to 0..360
        α2 = (α2 + 2*Math.PI) % (2*Math.PI); // normalise to 0..360

        return {
            distance:       s,
            bearing: s==0 ? NaN : toDeg(α1)
        };
};


/*!
 * JavaScript function to calculate the destination point given start point latitude / longitude (numeric degrees), bearing (numeric degrees) and distance (in m).
 *
 * Taken from http://movable-type.co.uk/scripts/latlong-vincenty-direct.html and optimized / cleaned up by Mathias Bynens <http://mathiasbynens.be/>
 * Based on the Vincenty direct formula by T. Vincenty, “Direct and Inverse Solutions of Geodesics on the Ellipsoid with application of nested equations”, Survey Review, vol XXII no 176, 1975 <http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf>
 */

function directVincenty(lat1, lon1, brng, dist) {
    let a = 6378137,
        b = 6356752.3142,
        f = 1 / 298.257223563, // WGS-84 ellipsiod
        s = dist,
        alpha1 = toRad(brng),
        sinAlpha1 = Math.sin(alpha1),
        cosAlpha1 = Math.cos(alpha1),
        tanU1 = (1 - f) * Math.tan(toRad(lat1)),
        cosU1 = 1 / Math.sqrt((1 + tanU1 * tanU1)), sinU1 = tanU1 * cosU1,
        sigma1 = Math.atan2(tanU1, cosAlpha1),
        sinAlpha = cosU1 * sinAlpha1,
        cosSqAlpha = 1 - sinAlpha * sinAlpha,
        uSq = cosSqAlpha * (a * a - b * b) / (b * b),
        A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq))),
        B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq))),
        sigma = s / (b * A),
        sigmaP = 2 * Math.PI;
    while (Math.abs(sigma - sigmaP) > 1e-12) {
        let cos2SigmaM = Math.cos(2*sigma1 + sigma),
            sinSigma = Math.sin(sigma),
            cosSigma = Math.cos(sigma),
            deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
        sigmaP = sigma;
        sigma = s / (b * A) + deltaSigma;
    };
    let tmp = sinU1 * sinSigma - cosU1 * cosSigma * cosAlpha1,
        lat2 = Math.atan2(sinU1 * cosSigma + cosU1 * sinSigma * cosAlpha1, (1 - f) * Math.sqrt(sinAlpha * sinAlpha + tmp * tmp)),
        lambda = Math.atan2(sinSigma * sinAlpha1, cosU1 * cosSigma - sinU1 * sinSigma * cosAlpha1),
        C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha)),
        L = lambda - (1 - C) * f * sinAlpha * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM))),
        revAz = Math.atan2(sinAlpha, -tmp); // final bearing
    return {lat: toDeg(lat2), lon: lon1 + toDeg(L)};
};

function toRad(degrees){
    let pi = Math.PI;
    return degrees * (pi/180);
}

function toDeg(radians){
    let pi = Math.PI;
    return radians * (180/pi);
}