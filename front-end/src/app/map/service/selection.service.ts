import {Injectable} from "@angular/core";
import {Map} from 'maplibre-gl';
import {RegionSelection} from "../region-selection";
import {MapUtilService} from "./maputils.service";
import booleanIntersects from "@turf/boolean-intersects";
import bbox from "@turf/bbox";
import proj4 from "proj4";
import {Store} from "@ngrx/store";
import {AppState} from "../../core/state/app.state";
import {mapActions} from "../state/map.actions";

@Injectable({
  providedIn: 'root'
})
export class SelectionService {

  private map: Map = null;
  private regionSelection: RegionSelection = {
    active: false,
    wkt: null,
    selectedRegions: 0
  };

  constructor(private store: Store<AppState>, private mapUtils: MapUtilService) {
  }

  // Method to set the map instance
  public setMap(map: Map): void {
    this.map = map;
  }


  selectFeatures(selection: RegionSelection): RegionSelection {
    if (!this.map) {
      console.error('Map instance not set in SelectionService.');
      return null;
    }

    this.clearSelection();

    const polygon = this.mapUtils.createGeoJsonFeaturesFromWkt(selection.wkt);
    if (polygon.features.length > 0) {
      const boundingBox = bbox(polygon);
      const queryBox = proj4('EPSG:4326', 'EPSG:3857', boundingBox);
      const candidateIntersectingFeatures = this.map.queryRenderedFeatures(
        [[queryBox[0], queryBox[1]], [queryBox[2], queryBox[3]]], {
          layers: ['region']
        });

      const drawnPolygon = polygon.features[0];
      const trulyIntersectingFeatures = candidateIntersectingFeatures.filter(
        feature => booleanIntersects(drawnPolygon, feature)
      );

      trulyIntersectingFeatures.forEach(feature => {
        this.map.setFeatureState({
          id: feature.properties['id'],
          source: 'region',
          sourceLayer: 'region',
        }, {highlight: true});
      });

      this.regionSelection = {
        ...this.regionSelection,
        wkt: selection.wkt,
        selectedRegions: trulyIntersectingFeatures.length
      };
      return this.regionSelection;
    }
    return null;
  }

  public clearSelection(): void {
    if (!this.map) {
      console.error('Map instance not set in SelectionService.');
      return;
    }
    if (!this.regionSelection.wkt && this.regionSelection.selectedRegions == 0) return;

    this.map.removeFeatureState({
      source: 'region',
      sourceLayer: 'region'
    });
    this.regionSelection = {
      ...this.regionSelection,
      wkt: null,
      selectedRegions: 0
    };
    this.store.dispatch(mapActions.clearSelection(this.regionSelection));
  }

}

