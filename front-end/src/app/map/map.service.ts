import {Injectable} from '@angular/core';
import {IControl, LayerSpecification, LngLatBoundsLike, Map, NavigationControl} from 'maplibre-gl';
import {VectorTileLayer} from './vector-tile-layer';
import {CommandService} from '../core/http/command.service';
import MapboxDraw from '@mapbox/mapbox-gl-draw';
import booleanIntersects from '@turf/boolean-intersects';
import {Session, SessionStatus} from '../session/session';
import {Store} from '@ngrx/store';
import {AppState} from '../core/state/app.state';
import {MapUtils} from './map-utils';
import {mapActions} from "./state/map.actions";

@Injectable({
  providedIn: 'root'
})
export class MapService {
  private map: Map = null;
  private draw: MapboxDraw = null;
  private mapUtils = new MapUtils();

  constructor(
    private commandService: CommandService,
    private store: Store<AppState>
  ) {
  }

  initializeMap(map: Map, session: Session | null): void {
    this.map = map;
    this.map.addControl(new NavigationControl());

    if (session === null || session.id === null) {
      this.loadBaseLayer();
    } else if (
      session.status === SessionStatus.RUNNING ||
      session.status === SessionStatus.COMPLETED ||
      session.status === SessionStatus.INTERRUPTED
    ) {
      this.loadResultLayer(session.id);
    }
  }

  initializeDrawing(): void {
    if (this.map == null) return;
    this.draw = new MapboxDraw({
      displayControlsDefault: false,
      controls: {
        polygon: false,
        trash: false
      }
    });
    this.map.addControl(this.draw as unknown as IControl);
    this.map.getCanvasContainer().style.cursor = 'crosshair';
    this.map.on('draw.create', this.selectFeatures.bind(this));
    this.map.on('draw.delete', this.clearSelection.bind(this));
    this.map.on('draw.update', this.selectFeatures.bind(this));
  }

  activateDrawing(): void {
    if (this.map == null || this.draw == null) return;
    this.map.getCanvasContainer().style.cursor = 'crosshair';
    this.draw.changeMode(MapboxDraw.constants.modes.DRAW_POLYGON);
  }

  deActivateDrawing(): void {
    if (this.map == null || this.draw == null) return;
    this.clearSelection();
    this.map.getCanvasContainer().style.cursor = '';
  }

  sessionStateChange(session: Session): void {
    if (session == null) return;
    if (session.id != null && (session.status == SessionStatus.RUNNING ||
      session.status == SessionStatus.COMPLETED ||
      session.status == SessionStatus.INTERRUPTED)) {
      this.loadResultLayer(session.id).then(() => {
        if (this.draw == null) {
          this.initializeDrawing();
        }
      });
    }
  }

  private loadBaseLayer(): void {
    this.map.boxZoom.disable();
    if (!this.map) return;
    this.commandService.execute(
      `tiles/base`, 'GET', 'json', null, true
    ).subscribe((layer: VectorTileLayer) => {
      this.map.addLayer(layer as LayerSpecification);
      this.map.addLayer({
        'id': 'region-highlighted',
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

  private async loadResultLayer(sessionId?: string): Promise<void> {
    if (!this.map) return;

    const loadImageAndAdd = (): Promise<void> => {
      return new Promise((resolve, reject) => {
        if (this.map.hasImage('facility')) {
          resolve();
          return;
        }
        this.map.loadImage('/assets/icon.png', (error, image) => {
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
      });
    } catch (error) {
      console.error('Error while loading or adding image:', error);
    }
  }

  private selectFeatures(): void {
    let polygon = this.draw.getAll();

    if (polygon.features.length > 0) {
      const drawnPolygon = polygon.features[0];
      const features = this.map.queryRenderedFeatures({layers: ['region']});
      const intersectedFeatures = features.filter(feature =>
        booleanIntersects(drawnPolygon, feature)
      );

      const region_codes = intersectedFeatures.map(feature =>
        feature.properties['region_code']
      );
      const spatialQuery = <Array<number[]>>drawnPolygon.geometry['coordinates'][0];

      this.store.dispatch(mapActions.regionsSelected({
        spatialQuery: this.mapUtils.toWKTPolygon(spatialQuery),
        numSelectedRegions: region_codes.length
      }));

      this.map.setFilter('region-highlighted', ['in', 'region_code', ...region_codes]);
      this.draw.deleteAll(); // The drawn polygon should disappear
    } else {
      this.map.setFilter('region-highlighted', ['in', 'region_code', '']);
      this.clearSelection();
    }
  }

  clearSelection(): void {
    if (this.draw == null) return;
    this.draw.deleteAll();
    this.map.setFilter('region-highlighted', ['in', 'region_code', '']);
    this.store.dispatch(mapActions.clearSelection());
  }

  showLayer(layerName: string): void {
    if (this.map.getLayer(layerName) && this.map.getLayoutProperty(layerName, 'visibility') === 'none') {
      this.map.setLayoutProperty(layerName, 'visibility', 'visible');
    }
  }

  hideLayer(layerName: string): void {
    if (this.map.getLayer(layerName) && this.map.getLayoutProperty(layerName, 'visibility') !== 'none') {
      this.map.setLayoutProperty(layerName, 'visibility', 'none');
    }
  }
}
