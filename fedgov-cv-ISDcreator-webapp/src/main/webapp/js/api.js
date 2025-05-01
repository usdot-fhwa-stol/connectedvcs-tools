
const esri_elevation_url = "/msp/esrimap/api/elevation";
const google_places_autocomplete_url = "/msp/googlemap/api/places/autocomplete";
const google_places_searchText_url = "/msp/googlemap/api/places/searchText"; 
const intersection_url = "/api.geonames.org/findNearestIntersectionJSON"
async function getElevation(dot, latlon, i, j, callback){
    try {
      const response = await fetch(esri_elevation_url + "/" + latlon.lat + '/' + latlon.lon);
      if (!response.ok) {
        throw new Error('Server response was not ok');
      }
      const result = await response.json();
      let elev = result?.z;
      if (elev == null || elev == undefined) {
        elev = -9999; // any sea value is set to -9999 by default. This brings it back to sea level as we know it
      } else {
        elev = Math.round(elev);
      }
      callback(elev, i, j, latlon, dot);
    } catch (error) {
      callback(-9999, i, j, latlon, dot);
    }
  }

async function getComputedElevation(latlon) {
  return new Promise((resolve, reject) => {
    $.ajax({
      url: esri_elevation_url + "/" + latlon.lat + '/' + latlon.lon,
      success: function (result) {
        let elev = result?.z;
        if (elev == null || elev === undefined) {
          elev = -9999;
        } else {
          elev = Math.round(elev);
        }
        resolve(elev);
      },
      error: function (error) {
        console.log("ERROR GETTING ELEVATION: " + error);
        resolve(-9999);
      }
    });
  });
}

async function getElev(lat, lon) {
  try {
    const response = await fetch(esri_elevation_url + "/" + lat + '/' + lon,{mode: 'cors'});
    if (!response.ok) {
      throw new Error('Server response was not ok');
    }
    const result = await response.json();
    let elev = result?.z;
    if (elev == null || elev == undefined) {
      elev = -9999; //any sea value is set to -9999 by default. This brings it back to sea level as we know it
    } else {
      elev = Math.round(elev);
    }
    return elev;
  } catch (error) {
    console.error(error);
    return -9999;
  }
}

async function getNearestIntersectionJSON(feature, lat, lon) {
  document.getElementById('intersection_name').value =  feature.get("intersectionName")? feature.get("intersectionName") : "Temporary Name";
  try {
    const response = await fetch(`${intersection_url}?lat=${lat}&lng=${lon}&username=NA`);
    const result = await response.json();
    if (result.intersection) {
      let name = result.intersection.street1 + " & " + result.intersection.street2;
      document.getElementById('intersection_name').value = name;
      feature.set("intersectionName", name);
    } else {
      console.log("intersection not found");      
    }
  } catch (error) {
    console.error("Error fetching nearest intersection");
  }
}


/***
 * Purpose: Display list of autocomplete places suggested by predictionPlaces returned by google places API.
 * @params input search place text
 * @event update dropdown with a list of suggested places and allow to click on a place to move the map center location.
 */
async function populateAutocompleteSearchPlacesDropdown(map, inputText){
  let search_place_dropdown = $("#dropdown-menu-search");
  await $.ajax({
              type: 'POST',
              url: google_places_autocomplete_url,
              data: JSON.stringify({inputText: inputText}),
              headers: {
                  'Content-Type': 'application/json'
              },
              success: function(response){
                  if(!response){
                      console.error("Failed to retrieve places suggestions from server!");
                      return;
                  }
                  let suggestions = response["suggestions"];
                  suggestions = Object.values(suggestions);
                  for(let key in suggestions){
                      let suggestedResult = suggestions[key]["placePrediction"]["text"]["text"];
                      let place_item = $("<li><a><i class=\"fa fa-map-marker\" style=\"cursor: not-allowed\"></i> <span style=\"margin-left: 5px; cursor: pointer; width: 200px; display: inline-flex; overflow: hidden; text-overflow: ellipsis;\">"+suggestedResult+"</span></a></li> ");
                      place_item.click((event) => {
                          clickPlaceHandler(map, event);
                      });
                      search_place_dropdown.append(place_item);
                  }
                  search_place_dropdown.show();
              },
              error: function(error){
                  search_place_dropdown.hide();
                  console.error(error);
              }
          });
}


/***
 * Purpose: Handler for a click event to navigate to a place
 */
function clickPlaceHandler(map, event){
  let place = event.target.innerText;
  if(place.length>0){
      $("#address-search").val(place);
      $("#dropdown-menu-search").hide();
      updatePlaceLocationView(map, place);
  }
}

/***
* Purpose: Move map view to a location returned by google places API for a given input place.
* @params input place full text
* @event Update map center view with new place location.
*/
function updatePlaceLocationView(map, inputPlaceText){
  $.ajax({
      url: google_places_searchText_url,
      data: JSON.stringify({inputText: inputPlaceText}),
      type: 'POST',
      headers: {
          "Content-Type": "application/json"
      },
      success: function(response){
          try {
              let location = response["location"];
              let search_lat = location.lat;
              let search_lon = location.lng;
              let viewZoom = 18;
              setCookie("isd_latitude", search_lat, 365);
              setCookie("isd_longitude", search_lon, 365);
              setCookie("isd_zoom", viewZoom, 365);
              map.getView().setCenter(new ol.proj.fromLonLat([search_lon, search_lat]));
              map.getView().setZoom(viewZoom);
          }
          catch (err) {
              console.log("No vectors to reset view");
          }
      },
      error: function(error){
          console.error(error);
      }
  });
}


export {
  getElevation,
  getElev,
  getNearestIntersectionJSON,
  populateAutocompleteSearchPlacesDropdown,
  getComputedElevation
}