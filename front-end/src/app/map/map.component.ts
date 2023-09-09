import {Component, Input, OnDestroy, OnInit, ViewEncapsulation,} from '@angular/core';
import {NgxMapLibreGLModule} from "@maplibre/ngx-maplibre-gl";
import {
  LayerSpecification,
  LngLatBoundsLike,
  Map,
  PointLike,
  Popup,
  StyleSpecification
} from "maplibre-gl";
import {Observable, Subscription} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {VectorTileLayer} from "./vector-tile-layer";
import {MatDialogModule} from "@angular/material/dialog";
import style from '../../assets/style-de-at.json';
import {Session} from "../session/session";
import {fromLauncher} from "../launcher/state/launcher.selectors";
import {Store} from "@ngrx/store";
import {AppState} from "../core/state/app.state";


@Component({
  selector: 'app-map',
  providers: [],
  imports: [
    NgxMapLibreGLModule,
    MatDialogModule
  ],
  standalone: true,
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class MapComponent implements OnInit, OnDestroy {
  @Input('sessionId') sessionId: string | null = null;
  private subscriptions: Subscription[] = [];
  style = style as StyleSpecification;
  map: Map;
  popup = new Popup({closeButton: false});
  canvas: HTMLElement;
  start: any;
  current: any
  box: any;

  private sessionState$: Observable<Session> = this.store.select(fromLauncher.selectLauncherSession);

  constructor(private httpClient: HttpClient,private store: Store<AppState>) {
    this.sessionState$.subscribe(this.sessionStateChange.bind(this));
  }

  ngOnInit(): void {

  }

  initializeMap(map: Map) {
    this.map = map;
    if(this.sessionId == null) {
      this.loadBasedLayer();
    } else {
      this.loadResultLayer(this.sessionId);
    }
  }

  loadBasedLayer() {
    this.map.boxZoom.disable();
    if (!this.map) return;
    const sub = this.httpClient.get<VectorTileLayer>(`http://localhost:8080/api/v1/tiles/base`).subscribe((layer: VectorTileLayer) => {
      this.map.addLayer(layer as LayerSpecification);
      this.map.addLayer(
        {
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
        } as LayerSpecification
      );
      this.map.fitBounds(layer.bounds as LngLatBoundsLike, {padding: 20});
      this.initBoundingBoxDrawing();
    });
    this.subscriptions.push(sub);
  }

  async loadResultLayer(sessionId?: string) {
    if (!this.map) return;

    // Function to loadImage and addImage to map as a promise
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
      // Load and add image synchronously
      await loadImageAndAdd();

      const sub = this.httpClient.get<VectorTileLayer>(`http://localhost:8080/api/v1/tiles/allocation/${sessionId}`)
      .subscribe((layer: VectorTileLayer) => {
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
          this.map.fitBounds(layer[0].bounds as LngLatBoundsLike, { padding: 20 });
        } else {
          this.map.addLayer(layer as LayerSpecification);
          this.map.fitBounds(layer.bounds as LngLatBoundsLike, { padding: 20 });
        }
      });

      this.subscriptions.push(sub);
    } catch (error) {
      console.error('Error while loading or adding image:', error);
    }
  }


  initBoundingBoxDrawing() {

    this.canvas = this.map.getCanvasContainer();

    this.canvas.addEventListener('mousedown', this.mouseDown.bind(this), true);

    this.map.on('mousemove', (e) => {

      const features = this.map.queryRenderedFeatures(e.point, {
        layers: ['region-highlighted']
      });

      this.map.getCanvas().style.cursor = features.length ? 'pointer' : '';

      if (!features.length) {
        this.popup.remove();
        return;
      }

      this.popup
      .setLngLat(e.lngLat)
      .setText(features[0].properties['region_code'])
      .addTo(this.map);
    });
  }

  sessionStateChange(session: Session) {
    this.loadResultLayer(session.id);
  }

  mousePos(e: { shiftKey?: any; button?: number; clientX?: any; clientY?: any; }) {
    const rect = this.canvas.getBoundingClientRect();
    return [
      e.clientX - rect.left - this.canvas.clientLeft,
      e.clientY - rect.top - this.canvas.clientTop
    ] as PointLike;
  }

  mouseDown(e: { shiftKey: any; button: number; }) {
    if (!(e.shiftKey && e.button === 0)) return;
    this.map.dragPan.disable();
    document.addEventListener('mousemove', this.onMouseMove.bind(this));
    document.addEventListener('mouseup', this.onMouseUp.bind(this));
    document.addEventListener('keydown', this.onKeyDown.bind(this));
    this.start = this.mousePos(e);
  }

  onMouseMove(e: { shiftKey?: any; button?: number; clientX?: any; clientY?: any; }) {
    this.current = this.mousePos(e);
    if (!this.box) {
      this.box = document.createElement('div');
      this.box.style.cssText = 'background: rgba(56, 135, 190, 0.1); border: 2px solid #3887be; position: absolute; top: 0; left: 0; width: 0; height: 0; z-index:10000;';
      this.canvas.appendChild(this.box);
    }

    const minX = Math.min(this.start.x, this.current.x),
      maxX = Math.max(this.start.x, this.current.x),
      minY = Math.min(this.start.y, this.current.y),
      maxY = Math.max(this.start.y, this.current.y);

    this.box.style.transform = `translate(${minX}px, ${minY}px)`;
    this.box.style.width = maxX - minX + 'px';
    this.box.style.height = maxY - minY + 'px';
  }

  onMouseUp(e: { shiftKey?: any; button?: number; clientX?: any; clientY?: any; }) {
    this.finish([this.start, this.mousePos(e)]);
  }

  onKeyDown(e: { keyCode: number; }) {
    if (e.keyCode === 27) this.finish();
  }

  finish(bbox?: any) {
    document.removeEventListener('mousemove', this.onMouseMove);
    document.removeEventListener('keydown', this.onKeyDown);
    document.removeEventListener('mouseup', this.onMouseUp);

    if (this.box) {
      this.box.parentNode.removeChild(this.box);
      this.box = null;
    }

    if (bbox) {
      const features = this.map.queryRenderedFeatures(bbox, {
        layers: ['region']
      });

      if (features.length >= 1000) {
        return window.alert('Select a smaller number of features');
      }

      const fips = features.map((feature) => feature.properties['region_code']);
      this.map.setFilter('region-highlighted', ['in', 'region_code', ...fips]);
    }

    this.map.dragPan.enable();
  }

  ngOnDestroy() {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

}
