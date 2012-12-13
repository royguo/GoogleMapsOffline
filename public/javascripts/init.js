var downloading_count = 0;
var existMarkers = [];
function CoordMapType(tileSize) {
	this.tileSize = tileSize;
}
//override the method get tiles.
CoordMapType.prototype.getTile = function(coord, zoom, ownerDocument) {
	// map images border
	var div = ownerDocument.createElement('DIV');
	div.innerHTML = 'x = ' + coord.x + ", y = " + coord.y + ", zoom = " + zoom;
	div.style.width = this.tileSize.width + 'px';
	div.style.height = this.tileSize.height + 'px';
	div.style.fontSize = '10';
	div.style.borderStyle = 'solid';
	div.style.borderWidth = '1px';
	div.style.borderColor = '#AAAAAA';
	//appaend locations
	if (map.mapTypeId != 'local') {
		downloading_count = downloading_count + 1;
		if(downloading_count > 0){
			$("#progress-bar").show();
		}
		$.post("/Application/cacheImage", {
			x : coord.x,
			y : coord.y,
			zoom : zoom
		}, function(json) {
			downloading_count = downloading_count - 1;
			if(downloading_count <= 0){
				$("#progress-bar").hide();
			}
		});
	}
	return div;
};
// Add a new map type names "LocalMapType"
function LocalMapType() {
}

LocalMapType.prototype.tileSize = new google.maps.Size(256, 256);
LocalMapType.prototype.maxZoom = 21;
LocalMapType.prototype.minZoom = 3;
LocalMapType.prototype.name = "本地";
LocalMapType.prototype.alt = "显示本地地图";
LocalMapType.prototype.getTile = function(coord, zoom, ownerDocument) {
	var img = ownerDocument.createElement("img");
	img.style.width = this.tileSize.width + "px";
	img.style.height = this.tileSize.height + "px";
	var strURL = "expotile/" + zoom + "/" + coord.x + "/" + coord.y + ".png";
	img.src = strURL;
	return img;
};
var localMapType = new LocalMapType();

//add markers on the map
function addMarker(markerArray) {
  var key = markerArray.x+markerArray.y;
  if($.inArray(key,existMarkers) > -1){
	return;
  }
  existMarkers.push(key);
  marker = new google.maps.Marker({
    position: new google.maps.LatLng(markerArray.x,markerArray.y),
    map: map
  });
  google.maps.event.addListener(marker, 'click', function(event) {
    changePano(markerArray.file,markerArray.offset,markerArray.length);
  });
}
//initialize the page
function initialize(firstMarker) {
  var myLatlng = new google.maps.LatLng(firstMarker.x,firstMarker.y);
  var myOptions = {
    center: myLatlng,
    zoom: 14,
    streetViewControl: false,
    mapTypeControlOptions: {
          mapTypeIds: ["local", google.maps.MapTypeId.ROADMAP]
      }
  };
  map = new google.maps.Map(document.getElementById("map_canvas"),
      myOptions);
  map.mapTypes.set('local', localMapType);
  map.setMapTypeId('local');
  map.overlayMapTypes.insertAt(0, new CoordMapType(new google.maps.Size(256, 256)));
  //get map bounds when bounds changed.
  google.maps.event.addListener(map,"bounds_changed",function(){
	  initMarkers(map.getBounds());
  });
}
// Initial markers on the map with bounds.
function initMarkers(bounds){
	var latMin = bounds.getSouthWest().lat();
	var latMax = bounds.getNorthEast().lat();
	var lagMin = bounds.getSouthWest().lng();
	var lagMax = bounds.getNorthEast().lng();
	$.get("/Application/markerWithBounds",{latMin:latMin,latMax:latMax,lagMin:lagMin,lagMax:lagMax}, function(markers){
	  if (markers.length > 0) {
	     for (var i=0;i<markers.length;i++) {
	       addMarker(markers[i]);
	     }
	   }
     });
}
function getCoordinate(latLng,map){
	var numTiles  = 1 << map.getZoom();
	var projection = map.getProjection();
	var worldCoordinate = projection.fromLatLngToPoint(latLng);
	var pixelCoordinate = new google.maps.Point(worldCoordinate.x*numTiles,worldCoordinate.y*numTiles);
	var tileCoordinate = new google.maps.Point(Math.floor(pixelCoordinate.x/256),Math.floor(pixelCoordinate.y/256));
	return {x:tileCoordinate.x,y:tileCoordinate.y,zoom:map.getZoom()};
}