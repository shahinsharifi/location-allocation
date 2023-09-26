import {Injectable} from "@angular/core";
import MapboxDraw from "@mapbox/mapbox-gl-draw";
import {
  IControl,
  Map
} from 'maplibre-gl';
import {Store} from "@ngrx/store";
import {AppState} from "../../core/state/app.state";
import {RegionSelection} from "../region-selection";
import bbox from "@turf/bbox";
import booleanIntersects from "@turf/boolean-intersects";
import proj4 from "proj4";
import {MapUtilService} from "./maputils.service";
import {mapActions} from "../state/map.actions";
import {SelectionService} from "./selection.service";


@Injectable({
  providedIn: 'root'
})
export class DrawingService {

  private map: Map = null;
  private regionSelection: RegionSelection;
  private draw: MapboxDraw = null;
  private mapUtils = new MapUtilService();

  constructor(private store: Store<AppState>, private selectionService: SelectionService) {
  }

  public setMap(map: Map): void {
    this.map = map;
    this.selectionService.setMap(map);
  }

  enableDrawing(regionSelection?: RegionSelection): void {
    if (!this.map) return;
    this.regionSelection = regionSelection;
    if (this.draw == null) {
      this.draw = new MapboxDraw(this.getMapboxDrawOptions());
      this.map.addControl(this.draw as unknown as IControl);
      this.map.on('draw.create', this.onDrawEnd.bind(this));
      this.draw.changeMode(MapboxDraw.constants.modes.DRAW_POLYGON);
      this.map.getCanvasContainer().style.cursor = 'crosshair';
    }
  }

  disableDrawing(): void {
    if (this.map == null || this.draw == null) return;
    this.map.removeControl(this.draw as unknown as IControl);
    this.map.getCanvasContainer().style.cursor = '';
    this.draw = null;
  }



  onDrawEnd(): void {
    const drawnFeatures = this.draw.getAll().features;
    if (drawnFeatures.length > 0) {
      const drawnPolygon = drawnFeatures[0];
      this.processDrawnPolygon(drawnPolygon);
      this.clearAllDrawings();
      setTimeout(() => {
        this.disableDrawing();
        this.enableDrawing();
      }, 100);
    }
  }

  getMapboxDrawOptions(): object {
    return {
      displayControlsDefault: false,
      controls: {
        polygon: false,
        trash: false
      },
      styles: this.getDrawingStyles()
    };
  }

  getDrawingStyles(): object[] {
    return [
      {
        'id': 'polygon-fill',
        'type': 'fill',
        'paint': {
          'fill-color': [
            "interpolate",
            ["linear"],
            ["zoom"],
            8,
            "rgba(102, 153, 204, 0.5)",
            18,
            '#6699CC'
          ],
          'fill-outline-color': '#03a9f4',
          'fill-opacity': 0.5
        }
      },
      {
        'id': 'polygon-stroke',
        'type': 'line',
        'paint': {
          'line-color': '#03a9f4',
          'line-width': [
            "interpolate",
            ["linear"],
            ["zoom"],
            8,
            0.5,
            18,
            5
          ]
        }
      },
      {
        'id': 'gl-draw-polygon-and-line-vertex',
        'type': 'circle',
        'paint': {
          'circle-color': '#03a9f4',
          'circle-stroke-color': '#aaccee'
        }
      }
    ];
  }

  processDrawnPolygon(polygon: any): void {
    const boundingBox = bbox(polygon);
    const queryBox = proj4('EPSG:4326', 'EPSG:3857', boundingBox);
    const candidateIntersectingFeatures = this.map.queryRenderedFeatures(
      [[queryBox[0], queryBox[1]], [queryBox[2], queryBox[3]]], {
        layers: ['region']
      }
    );

    const trulyIntersectingFeatures = candidateIntersectingFeatures.filter(
      feature => booleanIntersects(polygon, feature)
    );

    trulyIntersectingFeatures.forEach(feature => {
      this.map.setFeatureState({
        id: feature.properties['id'],
        source: 'region',
        sourceLayer: 'region',
      }, {highlight: true});
    });

    const spatialQuery = <Array<number[]>>polygon.geometry['coordinates'][0];
    this.regionSelection = {
      ...this.regionSelection,
      wkt: this.mapUtils.createWktFromPolygon(spatialQuery),
      selectedRegions: trulyIntersectingFeatures.length
    };

    this.store.dispatch(
      mapActions.regionsSelected(
        this.selectionService.selectFeatures(this.regionSelection)
      )
    );
  }

  clearAllDrawings(): void {
    if (this.draw) {
      const allFeatures = this.draw.getAll();
      allFeatures.features.forEach(feature => {
        this.draw.delete(feature.id.toString());
      });
    }
  }

  public clearSelection(): void {
    this.clearAllDrawings();
    this.selectionService.clearSelection();
    console.log(this.regionSelection);
    if(this.regionSelection && this.regionSelection.active) {
      setTimeout(() => {
        this.disableDrawing();
        this.enableDrawing();
      }, 100);
    }
  }

}
