import {Injectable} from '@angular/core';
import {IControl, LayerSpecification, LngLatBoundsLike, Map, NavigationControl} from 'maplibre-gl';
import {MapUtils} from "./map-utils";
import MapboxDraw from "@mapbox/mapbox-gl-draw";
import {CommandService} from "../core/http/command.service";
import {Session, SessionStatus} from "../session/session";
import {VectorTileLayer} from "./vector-tile-layer";
import booleanIntersects from "@turf/boolean-intersects";
import {mapActions} from "./state/map.actions";
import bbox from "@turf/bbox";
import proj4 from "proj4";
import {AppState} from "../core/state/app.state";
import {Store} from "@ngrx/store";
import {LayerVisibility} from "./layer-visibility";

@Injectable({
  providedIn: 'root'
})
export class MapService {

  private map: Map = null;
  private draw: MapboxDraw = null;
  private mapUtils = new MapUtils();
  constructor(private store: Store<AppState>, private commandService: CommandService){}

  public initializeMap(map: Map, session: Session | null): void {
    this.map = map;
    this.map.addControl(new NavigationControl());

    if (session === null || session.id === null) {
      this.loadBaseLayer();
    } else if (
      session.status === SessionStatus.RUNNING ||
      session.status === SessionStatus.COMPLETED ||
      session.status === SessionStatus.INTERRUPTED
    ) {
      this.loadResultLayer(session.id).then(() => console.log('Loaded result layer'));
    }
  }

  private initializeDrawing(): void {
    if (this.map == null) return;
    this.draw = new MapboxDraw(this.getMapboxDrawOptions());
    this.map.addControl(this.draw as unknown as IControl);
    this.map.getCanvasContainer().style.cursor = 'crosshair';
    this.map.on('draw.create', this.selectFeatures.bind(this));
    this.map.on('draw.delete', this.clearSelection.bind(this));
    this.map.on('draw.update', this.selectFeatures.bind(this));
  }

  private getMapboxDrawOptions(): object {
    return {
      displayControlsDefault: false,
      controls: {
        polygon: false,
        trash: false
      },
      styles: this.getDrawingStyles()
    };
  }

  private getDrawingStyles(): object[] {
    return [
      {
        'id': 'polygon-fill',
        'type': 'fill',
        'paint': {
          'fill-color': '#6699CC',
          'fill-outline-color': '#03a9f4',
          'fill-opacity': 0.6
        }
      },
      {
        'id': 'polygon-stroke',
        'type': 'line',
        'paint': {
          'line-color': '#03a9f4'
        }
      },
      {
        'id': 'gl-draw-polygon-and-line-vertex',
        'type': 'circle',
        'paint': {
          'circle-color': '#03a9f4',
          'circle-stroke-color': '#FFFFFF'
        }
      }
    ];
  }


  public enableDrawing(): void {
    if (this.map == null) return;
    if(this.draw == null) this.initializeDrawing();
    this.draw.changeMode(MapboxDraw.constants.modes.DRAW_POLYGON);
    this.map.getCanvasContainer().style.cursor = 'crosshair';
  }

  public disableDrawing(): void {
    if (this.map == null || this.draw == null) return;
    this.draw.deleteAll();
    this.draw=null;
    this.map.getCanvasContainer().style.cursor = '';
  }

  private loadBaseLayer(): void {
    this.map.boxZoom.disable();
    if (!this.map) return;
    this.commandService.execute(
      `tiles/base`, 'GET', 'json', null, true
    ).subscribe((layer: VectorTileLayer) => {
      this.map.addLayer(layer as LayerSpecification);
      this.map.addLayer({
        'id': 'region-selection',
        'type': 'fill',
        'source': layer.source,
        'source-layer': layer['source-layer'],
        'paint': {
          'fill-outline-color': '#484896',
          'fill-color': '#6e599f',
          'fill-opacity': 0.75
        },
        'filter': ['in', 'region_code', '']
      } as LayerSpecification);
      this.map.fitBounds(layer.bounds as LngLatBoundsLike, {padding: 20});
      this.initializeDrawing();
    });
  }



  public async loadResultLayer(sessionId?: string): Promise<void> {
    if (!this.map) return;
    const loadImageAndAdd = (): Promise<void> => {
      return new Promise((resolve, reject) => {
        if (this.map.hasImage('facility')) {
          resolve();
          return;
        }
        this.map.loadImage('/assets/icons/facility.png', (error, image) => {
          if (error) {
            reject(error);
            return;
          }
          this.map.addImage('facility', image);
          resolve();
        });
      });
    };

    try {
      await loadImageAndAdd();
      this.commandService.execute(
        `tiles/allocation/${sessionId}`, 'GET', 'json', null, true
      ).subscribe((layer: VectorTileLayer) => {
        if (this.map.getLayer("allocation")) {
          this.map.removeLayer('allocation');
          this.map.removeSource('allocation');
        }
        if (this.map.getLayer("location")) {
          this.map.removeLayer('location');
          this.map.removeSource('location');
        }
        if (layer instanceof Array) {
          this.map.addLayer(layer[0] as LayerSpecification);
          this.map.addLayer(layer[1] as LayerSpecification);
          this.map.fitBounds(layer[0].bounds as LngLatBoundsLike, {padding: 20});
        } else {
          this.map.addLayer(layer as LayerSpecification);
          this.map.fitBounds(layer.bounds as LngLatBoundsLike, {padding: 20});
        }
        if (this.draw == null) {
          this.initializeDrawing();
        }
      });
    } catch (error) {
      console.error('Error while loading or adding image:', error);
    }
  }

  private selectFeatures(): void {
    let polygon = this.draw.getAll();

    if (polygon.features.length > 0) {
      const boundingBox = bbox(polygon);
      const queryBox = proj4('EPSG:4326', 'EPSG:3857', boundingBox);
      const candidateIntersectingFeatures = this.map.queryRenderedFeatures([[queryBox[0], queryBox[1]], [queryBox[2], queryBox[3]]], {
        layers: ['region']
      });

      const drawnPolygon = polygon.features[0];
      const trulyIntersectingFeatures = candidateIntersectingFeatures.filter(feature =>
        booleanIntersects(drawnPolygon, feature)
      );

      trulyIntersectingFeatures.forEach(feature => {
        this.map.setFeatureState({
          id: feature.properties['id'],
          source: 'region',
          sourceLayer: 'region',
        }, {highlight: true});
      });

      const spatialQuery = <Array<number[]>>drawnPolygon.geometry['coordinates'][0];
      this.store.dispatch(mapActions.regionsSelected({
        active: true,
        wkt: this.mapUtils.toWKTPolygon(spatialQuery),
        selectedRegions: trulyIntersectingFeatures.length
      }));
      this.draw.deleteAll(); // The drawn polygon should disappear
    } else {
      this.clearSelection();
    }
  }

  public clearSelection(): void {
    if (this.draw == null) return;
    this.draw.deleteAll();
    this.map.setFeatureState({source: 'region', sourceLayer: 'region'}, {highlight: false});
  }

  public updateLayerVisibility(visibility: LayerVisibility): void {
    if (this.map == null) return;
    Object.keys(visibility).forEach(layerName => {
      if (this.map.getLayer(layerName)) {
        const visibilityValue = visibility[layerName] ? 'visible' : 'none';
        this.map.setLayoutProperty(layerName, 'visibility', visibilityValue);
      }
    });
  }


  public toggleLayer(layerName: string): void {
    if (!this.map) return;
    if (this.map.getLayer(layerName)) {
      const visibility = this.map.getLayoutProperty(layerName, 'visibility');
      if (visibility === 'none') {
        this.map.setLayoutProperty(layerName, 'visibility', 'visible');
        //  this.store.dispatch(mapActions.toggleLayerVisible({layerName}));
      } else {
        this.map.setLayoutProperty(layerName, 'visibility', 'none');
        //   this.store.dispatch(mapActions.toggleLayerHidden({layerName}));
      }
    }
  }

  public zoomToLayer(layerName: string): void {
    if (this.map!.getLayer(layerName)) {
      const layerObject: VectorTileLayer = this.map!.getStyle().layers?.find(layer => layer.id === layerName) as VectorTileLayer;
      const bounds = layerObject?.bounds;
      if (bounds) {
        this.map!.fitBounds(bounds as LngLatBoundsLike, {padding: 20});
      }
    }
  }

  public destroyMap(): void {
    if (this.map) {
      this.map.remove();
      this.map = null;
    }
    if (this.draw) {
      this.draw = null;
    }
  }

  public getMap(): Map | null {
    return this.map;
  }
}

