import {Injectable} from '@angular/core';
import {LngLatBoundsLike, Map, NavigationControl} from 'maplibre-gl';
import {CommandService} from "../core/http/command.service";
import {Session, SessionStatus} from "../session/session";
import {VectorTileLayer} from "./vector-tile-layer";
import {LayerVisibility} from "./layer-visibility";
import {DrawingService} from "./service/drawing.service";
import {RegionSelection} from "./region-selection";


@Injectable({
  providedIn: 'root'
})
export class MapService {

  private map: Map | null = null;
  private layerVisibility?: LayerVisibility; // Used a '?' to mark it as optional

  constructor(
    private commandService: CommandService,
    private drawingService: DrawingService
  ) {
  }


  public initializeMap(map: Map, session: Session | null): void {
    this.map = map;
    this.drawingService.setMap(map);
    this.map.addControl(new NavigationControl());
    this.map.getCanvasContainer().style.cursor = '';
    this.loadBaseLayer();

    if (session?.id && [
      SessionStatus.RUNNING,
      SessionStatus.COMPLETED,
      SessionStatus.INTERRUPTED
    ].includes(session.status)) {
      this.loadResultLayer(session.id).then(() => console.log('Loaded result layer'));
    }

    if (this.layerVisibility) {
      this.updateLayerVisibility(this.layerVisibility);
    }
  }


  loadBaseLayer(): void {
    if (!this.map) return;

    this.map.boxZoom.disable();
    this.commandService.execute(`tiles/base`, 'GET', null)
    .subscribe((layer: VectorTileLayer) => {
      const layerObject: VectorTileLayer = layer as VectorTileLayer;
      layerObject.metadata = {
        'bounds': layer.bounds
      };
      this.map!.addLayer(layerObject);
      this.map!.fitBounds(layer.bounds as LngLatBoundsLike, {padding: 20});
    });
  }

  public async loadResultLayer(sessionId?: string): Promise<void> {
    if (!this.map || !sessionId) return;
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
        `tiles/allocation/${sessionId}`, 'GET', null
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
          const locationLayer: VectorTileLayer = layer[0] as VectorTileLayer;
          locationLayer.metadata = {
            'bounds': layer[0].bounds
          };
          this.map!.addLayer(locationLayer);

          const allocationLayer: VectorTileLayer = layer[1] as VectorTileLayer;
          locationLayer.metadata = {
            'bounds': layer[0].bounds
          };
          this.map!.addLayer(allocationLayer);

          this.map.fitBounds(locationLayer.bounds as LngLatBoundsLike, {padding: 20});
        } else {
          const locationLayer: VectorTileLayer = layer[0] as VectorTileLayer;
          locationLayer.metadata = {
            'bounds': layer[0].bounds
          };
          this.map!.addLayer(locationLayer);
          this.map.fitBounds(locationLayer.bounds as LngLatBoundsLike, {padding: 20});
        }
        // this.enablePopupOnClick();
      });
    } catch (error) {
      console.error('Error while loading or adding image:', error);
    }
  }


  public enableDrawing(regionSelection?: RegionSelection): void {
    this.drawingService.enableDrawing(regionSelection);
  }

  public disableDrawing(): void {
    this.drawingService.disableDrawing();
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

  toggleLayer(layerName: string): void {
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

  zoomToLayer(layerName: string): void {
    if (this.map!.getLayer(layerName)) {
      const layerObject: VectorTileLayer = this.map!.getStyle().layers?.find(layer => layer.id === layerName) as VectorTileLayer;
      const bounds = layerObject.metadata?.bounds;
      if (bounds) {
        this.map!.fitBounds(bounds as LngLatBoundsLike, {padding: 20});
      }
    }
  }

  public clearSelection(): void {
    if (this.map) {
      this.drawingService.clearSelection();
    }
  }

  public resetMap(): void {
    if (this.map) {
      this.clearSelection();
      this.updateLayerVisibility({
        region: true,
        location: false,
        allocation: false
      });
      this.zoomToLayer('region');
    }
  }

  public getMap(): Map | null {
    return this.map;
  }

  public destroyMap(): void {
    if (this.map) {
      this.map.remove();
      this.map = null;
    }
  }

}

