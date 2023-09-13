export class MapUtils {
  toWKTPolygon(spatialQuery: Array<number[]>): string {
    let wkt = 'POLYGON((';
    for (let coordinate of spatialQuery) {
      wkt += coordinate[0] + " " + coordinate[1] + ", ";
    }
    wkt += spatialQuery[0][0] + " " + spatialQuery[0][1] + "))";
    return wkt;
  }
}
