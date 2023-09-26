import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class MapUtilService {

  createWktFromPolygon(spatialQuery: Array<number[]>): string {
    let wkt = 'POLYGON((';
    for (let coordinate of spatialQuery) {
      wkt += coordinate[0] + " " + coordinate[1] + ", ";
    }
    wkt += spatialQuery[0][0] + " " + spatialQuery[0][1] + "))";
    return wkt;
  }

  createPolygonFromWkt(wkt: string): Array<number[]> {
    let spatialQuery: Array<number[]> = [];
    let coordinates = wkt.split('(')[2].split('),')[0].split(', ');
    for (let coordinate of coordinates) {
      let splittedCoordinate = coordinate.split(' ');
      spatialQuery.push([parseFloat(splittedCoordinate[0]), parseFloat(splittedCoordinate[1])]);
    }
    return spatialQuery;
  }

  createGeoJsonFeaturesFromWkt(wkt: string): any {
    return {
      "type": "FeatureCollection",
      "features": [
        {
          "type": "Feature",
          "properties": {},
          "geometry":
            {
              "type": "Polygon",
              "coordinates": [
                this.createPolygonFromWkt(wkt)
              ]
            }
        }
      ]
    };
  }

}

