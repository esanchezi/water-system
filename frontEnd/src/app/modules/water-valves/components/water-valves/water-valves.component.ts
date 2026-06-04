import { AfterViewInit, Component,ViewChild } from '@angular/core';
import { MapDirectionsService, MapDirectionsRenderer, GoogleMap, MapMarker } from '@angular/google-maps';

interface Marker {
  position: google.maps.LatLngLiteral;
  label: string;
  title: string;
}

@Component({
  selector: 'app-water-valves',
  templateUrl: './water-valves.component.html',
  styleUrls: ['./water-valves.component.css']
})
export class WaterValvesComponent implements AfterViewInit {
  @ViewChild(GoogleMap) map!: GoogleMap;

  center: google.maps.LatLngLiteral = { lat: 21.0448504, lng: -101.57076045 };
  zoom = 15;

  markers: google.maps.MarkerOptions[] = [
    {
      position: { lat: 21.0491275, lng: -101.5712965 },
      label: 'Jazmin',  // No es necesario usar 'as google.maps.MarkerLabel'
      title: 'Marker A'
    },
    {
      position: { lat: 21.0485568, lng: -101.5718088 },
      label: 'B',
      title: 'Marker B'
    },
    {
      position: { lat: 21.048363, lng: -101.5715103 },
      label: 'X',
      title: 'Marker B'
    },
    {
      position: { lat: 21.0481571, lng: -101.5708647 },
      label: 'C',
      title: 'Marker C'
    }/*,
    {
      position: { lat: 21.0510369, lng: -101.5769701 },
      label: '1',
      title: 'Marker D'
    },*/
  ];

  directionsService = new google.maps.DirectionsService();
  directionsRenderer = new google.maps.DirectionsRenderer({
    suppressMarkers: true // Oculta los marcadores predeterminados de inicio y fin
  });

  ngAfterViewInit() {
    if (this.map.googleMap) {
      this.initMap();
    } else {
      console.error('Map is not initialized.');
    }
  }

  initMap() {
    const map = this.map.googleMap;

    if (!map) {
      console.error('GoogleMap instance is not available.');
      return;
    }

    this.directionsRenderer.setMap(map);

    this.calculateRoute();
  }

  calculateRoute() {
    if (this.markers.length < 2) {
      console.error('Not enough markers to calculate route.');
      return;
    }

    const waypoints = this.markers.slice(1, -1).map(marker => ({
      location: marker.position!,
      stopover: true
    }));

    const request: google.maps.DirectionsRequest = {
      origin: this.markers[0].position!,
      destination: this.markers[this.markers.length - 1].position!,
      waypoints: waypoints,
      travelMode: google.maps.TravelMode.DRIVING,
      optimizeWaypoints: false 
    };

    this.directionsService.route(request, (result, status) => {
      if (status === google.maps.DirectionsStatus.OK) {
        this.directionsRenderer.setDirections(result);
      } else {
        console.error(`Error fetching directions: ${status}`);
      }
    });
  }
}