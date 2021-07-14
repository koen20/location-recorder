<template>
  <main>
    <div id="mapid"></div>
    <div id="timeline-datepicker">
      <section class="date-selector">
        <label for="datepicker">Date:</label>
        <input type="date" id="datepicker">
        <button v-on:click="setDate">Select</button>
      </section>
      <div id="timeline"></div>
    </div>
  </main>
</template>

<script>
import L from 'leaflet'
import axios from 'axios';

delete L.Icon.Default.prototype._getIconUrl;

L.Icon.Default.mergeOptions({
  iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
  iconUrl: require('leaflet/dist/images/marker-icon.png'),
  shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
});

export default {
  name: "Timeline",
  mounted() {
    var today = new Date();
    var dd = String(today.getDate()).padStart(2, '0');
    var mm = String(today.getMonth() + 1).padStart(2, '0');
    var yyyy = today.getFullYear();
    today = yyyy + "-" + mm + '-' + dd;
    var dateStringSplit = today.split("-")
    var dateP = new Date(parseInt(dateStringSplit[0]), parseInt(dateStringSplit[1]) - 1, parseInt(dateStringSplit[2]));
    var millisecondsStart = Math.round(dateP.getTime() / 1000);
    var millisecondsEnd = Math.round(dateP.getTime() / 1000 + 86400);
    initMap()
    setMap(millisecondsStart, millisecondsEnd);
    setTimeline(today)
  },
  methods: {
    setDate() {
      console.log("Set date")
      var dateString = document.getElementById("datepicker").value;
      var dateStringSplit = dateString.split("-")
      var dateP = new Date(parseInt(dateStringSplit[0]), parseInt(dateStringSplit[1]) - 1, parseInt(dateStringSplit[2]));
      var millisecondsStart = Math.round(dateP.getTime() / 1000);
      var millisecondsEnd = Math.round(dateP.getTime() / 1000 + 86400);
      setMap(millisecondsStart, millisecondsEnd);
      setTimeline(dateString);
    }
  }
}
import "leaflet/dist/leaflet.css";

var locations = [];
var map;
var markers;
var timelineJson;
var host = "http://127.0.0.1:9936/"

function initMap() {
  map = L.map('mapid').setView([51.505, -0.09], 13);
  L.tileLayer('https://{s}.tile.osm.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://osm.org/copyright">OpenStreetMap</a> contributors', detectRetina: true
  }).addTo(map);
}


async function setMap(startTime, endTime) {
  clearMap();
  const response = await axios.get(host + "api/info?startTime=" + startTime + "&endTime=" + endTime,
      {
        transformResponse: (res) => {
          // Do your own parsing here if needed ie JSON.parse(res);
          return res;
        }
      }
  )
  var jsonArray = JSON.parse(response.data);
  locations = [];
  for (let i = 0; i < jsonArray.length; i++) {
    var item = jsonArray[i];
    locations.push([item.lat, item.lon]);
  }
  var polyline = L.polyline(locations, {color: 'red'}).addTo(map);
// zoom the map to the polyline
  map.fitBounds(polyline.getBounds());
}

function clearMap() {
  try {
    map.removeLayer(markers);
  } catch (e) {
    console.log(e)
  }
  for (let i in map._layers) {
    if (map._layers[i]._path != undefined) {
      try {
        map.removeLayer(map._layers[i]);
      } catch (e) {
        console.log("problem with " + e + map._layers[i]);
      }
    }
  }
}

async function setTimeline(date) {
  document.getElementById("timeline").innerHTML = "Laden..."

  const response = await axios.get(host + "api/timeline?date=" + date,
      {
        transformResponse: (res) => {
          // Do your own parsing here if needed ie JSON.parse(res);
          return res;
        }
      }
  )

  console.log(response.data);
  timelineJson = JSON.parse(response.data);
  var jsonArray = timelineJson.stops;
  var jsonArrayRoutes = timelineJson.routes;
  markers = L.layerGroup();
  document.getElementById("timeline").innerHTML = ""
  for (let i = 0; i < jsonArray.length; i++) {
    var item = jsonArray[i];
    var buttonText = ""
    /*if (!item.locationUserAdded) {
      buttonText = "<button class='button-add-stop' id='" + item.start + item.end + "'>Opslaan</button>"
    }*/
    document.getElementById("timeline").innerHTML = document.getElementById("timeline").innerHTML + ("<p class='stop'>" + timestampToString(item.start) + " - " + timestampToString(item.end) + "<br>" + item.location + buttonText + "</p>");

    var marker = L.marker([item.lat, item.lon]);
    marker.bindPopup(item.location + "<br>Start: " + timestampToString(item.start) + "<br>Einde: " + timestampToString(item.end));
    markers.addLayer(marker);
    for (let k = 0; k < jsonArrayRoutes.length; k++) {
      var itemR = jsonArrayRoutes[k];
      if (itemR.start === item.end) {
        var icon = ""
        if (itemR.movementType === "walking") {
          icon = "img/walking.svg"
        } else if (itemR.movementType === "driving") {
          icon = "img/car.svg"
        }
        document.getElementById("timeline").innerHTML = document.getElementById("timeline").innerHTML + ("<p class='route'><img class='route-icon' src=\"" + icon + "\">" + (itemR.distance / 1000).toFixed(1) + " km(" + itemR.speed.toFixed(1) + "km/h)</p>");
      }
    }
  }
  map.addLayer(markers);

  /*
  $(".button-add-stop").click(function() {
    console.log("button clicked")
    var id = $(this).attr('id');
    var jsonArray = timelineJson.stops;
    for (i = 0; i < jsonArray.length; i++) {
      var item = jsonArray[i];
      if(item.start + (item.end + "") === id){
        var newName = prompt("Please enter the name of the stop", item.name)
        $.post(host + "stopImproved mysql, added timeline test?name=" + newName + "&lat=" + item.lat + "&lon=" + item.lon, function (data, status) {
          setDate();
        });
      }
    }

  });*/
}

function timestampToString(timestamp) {
  var date = new Date(timestamp);
  return date.getDate() + "-" + (date.getMonth() + 1) + " " + date.getHours() + ":" + date.getMinutes();
}


</script>

<style scoped>
main {
  display: grid;
  grid-template-columns: 2fr 1fr;
}

@media only screen and (max-width: 800px) {
  main {
    grid-template-columns: unset;
    grid-template-rows: 1fr 2fr;
  }
}

#timeline {
  height: 100%;
  z-index: 100;
  overflow: auto;
  min-height: 0;
  min-width: 0;
}
</style>