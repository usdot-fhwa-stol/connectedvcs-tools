/**
 * Created by martzth on 2/15/2017.
 * Updated 3/2017 by martzth
 */


/**
 * Purpose: to lookup addresses using the bing API
 * @params: input text -> address, city, and state
 * @event: sets lat/long cookie and moves map
 *
 * @deprecated for google geocomplete in main.js
 */

/*

/**
 * Purpose: set cookie so map loads to same position
 * @params: name, lat/lon or zoom, days of expiration (365)
 * @event: sets cookie
 */

function setCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires="+ d.toUTCString();
    document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
}



