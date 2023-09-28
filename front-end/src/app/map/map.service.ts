import {Injectable} from '@angular/core';
import {LngLatBoundsLike, LngLatLike, Map, NavigationControl, Popup} from 'maplibre-gl';
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
        this.enablePopupOnClick();
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

  enablePopupOnClick(): void {
    if (!this.map) return;
    this.map.on('click', this.handleClickOnLayers.bind(this));

    this.map.on('mouseenter', 'location', this.toggleCursorPointer.bind(this, 'pointer'));
    this.map.on('mouseleave', 'location', this.toggleCursorPointer.bind(this, ''));

    this.map.on('mouseenter', 'allocation', this.toggleCursorPointer.bind(this, 'pointer'));
    this.map.on('mouseleave', 'allocation', this.toggleCursorPointer.bind(this, ''));
  }

  handleClickOnLayers(e: any): void {

    const features = this.map.queryRenderedFeatures(e.point);
    const layers = features.map((feat) => {
      return feat.layer.id;
    });


    if (layers.includes('location')) {
      const coordinates = features[0].geometry['coordinates'].slice();
      const properties = features[0].properties;
      while (Math.abs(e.lngLat.lng - coordinates[0]) > 180) {
        coordinates[0] += e.lngLat.lng > coordinates[0] ? 360 : -360;
      }
      const columnInfo = this.getColumnInfo('location');
      const popup = this.createPopup(coordinates, properties, columnInfo);
      popup.addTo(this.map);
    } else if (layers.includes('allocation')) {
      const coordinates = e.lngLat;
      const properties = features[0].properties;
      const columnInfo = this.getColumnInfo('allocation');
      const popup = this.createPopup(coordinates, properties, columnInfo);
      popup.addTo(this.map);
    }
  }

  private getColumnInfo(layerName: string): any {
    const locationColumns = {
      "location": [{
        "name": "facility_id",
        "title": "Facility Region ID",
        "header": true
      }, {
        "name": "demand_count",
        "title": "Number of Demands"
      }, {
        "name": "travel_cost_mean",
        "title": "Average Travel Time (Min.)"
      }],
      "allocation": [{
        "name": "demand_id",
        "title": "Demand Region ID",
        "header": true
      }, {
        "name": "facility_id",
        "title": "Facility"
      }, {
        "name": "travel_cost",
        "title": "Travel time"
      }]
    };
    return locationColumns[layerName];
  };


  createPopup(coordinates: LngLatLike, properties: any, columnInfo: any): Popup {
    let htmlContent = '<div style="padding: 10px; color: #333; background-color: #fff; border: 1px solid rgba(0,0,0,0.2); border-radius: 0;">';
    columnInfo.forEach((column: any) => {
      if (column['header'] == true) {
        htmlContent += `<h6 style="margin: 0 0 10px 0; text-align: center;">${properties[column['name']]}</h6>`;
      } else {
        let value = properties[column['name']];
        if(typeof value === 'number') {
          value = value.toFixed(2);
        }
        htmlContent += `<p style="margin: 0 0 10px 0;"><strong>${column['title']}:</strong> ${value}</p>`;
      }
    });
    htmlContent += '</div>';
    return new Popup()
    .setLngLat(coordinates)
    .setMaxWidth('300px')
    .setHTML(htmlContent);
  }

  toggleCursorPointer(cursorType: string): void {
    this.map.getCanvas().style.cursor = cursorType;
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

