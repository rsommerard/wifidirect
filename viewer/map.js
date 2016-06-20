var config = require('./config');

var URL = config.url;
var markers = [];
var _map;
var isMapInitialized = false;
var isIntervalSetted = false;

function computeCenter(positions) {
  if (positions.length == 0) {
    return 0;
  }

  var latitude = 0;
  var longitude = 0;

  for (var i = 0; i < positions.length; i++) {
    var position = positions[i];

    latitude += position.lat;
    longitude += position.lon;
  }

  latitude /= positions.length;
  longitude /= positions.length;

  return {lat: latitude, lng: longitude};
}

function initMap() {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function () {
    if (xhttp.readyState == 4 && xhttp.status == 200) {
      var positions = JSON.parse(xhttp.responseText);
      var cntr = computeCenter(positions);

      if (isMapInitialized === false) {
        _map = new google.maps.Map(document.getElementById('map'), {
          zoom: 15,
          center: cntr
        });
        isMapInitialized = true;
      } else {
        _map.setCenter(cntr);
      }

      deleteMarkers();
      for (var i = 0; i < positions.length; i++) {
        var position = positions[i];
        addMarker(position);
      }
    }
  };
  xhttp.open('GET', URL + '/locations');
  xhttp.send();

  if (isIntervalSetted === false) {
    setInterval(initMap, 1000);
    isIntervalSetted = true;
  }
}

function clearMarkers() {
  for (var i = 0; i < markers.length; i++) {
    markers[i].setMap(null);
  }
}

function deleteMarkers() {
  clearMarkers();
  markers = [];
}

function addMarker(pos) {
  var marker = new google.maps.Marker({
    position: {lat: pos.lat, lng: pos.lon},
    map: _map
    //title: pos.id
  });

  markers.push(marker);
}
