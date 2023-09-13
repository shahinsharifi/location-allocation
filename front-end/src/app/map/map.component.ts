import {Component, OnDestroy, OnInit, ViewEncapsulation,} from '@angular/core';
import {NgxMapLibreGLModule} from "@maplibre/ngx-maplibre-gl";
import {
  IControl,
  LayerSpecification,
  LngLatBoundsLike,
  Map,
  NavigationControl,
  StyleSpecification
} from "maplibre-gl";
import {Observable, Subscription} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {VectorTileLayer} from "./vector-tile-layer";
import {MatDialogModule} from "@angular/material/dialog";
import style from '../../assets/style-de-at.json';
import {Session, SessionStatus} from "../session/session";
import {select, Store} from "@ngrx/store";
import {AppState} from "../core/state/app.state";
import MapboxDraw from "@mapbox/mapbox-gl-draw";
import booleanIntersects from "@turf/boolean-intersects";
import {mapActions} from "./state/map.actions";
import {MapUtils} from "./map-utils";


@Component({
  selector: 'app-map',
  providers: [],
  imports: [
    NgxMapLibreGLModule,
    MatDialogModule,
  ],
  standalone: true,
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class MapComponent implements OnInit, OnDestroy {
  mapUtils = new MapUtils();
  private subscriptions: Subscription[] = [];
  style = style as StyleSpecification;
  map: Map;
  start: any;
  draw: MapboxDraw;
  session: Session | null = null;
  sessionState$: Observable<Session>
  selectionActive$: Observable<boolean>;
  spatialQuery$: Observable<string>;
  numberOfSelectedRegions$: Observable<number>;

  constructor(private httpClient: HttpClient, private store: Store<AppState>) {
    this.sessionState$ = this.store.pipe(select(state => state.session.activeSession));
    this.selectionActive$ = this.store.pipe(select(state => state.map.selectionActive));
    this.spatialQuery$ = this.store.pipe(select(state => state.map.spatialQuery));
    this.numberOfSelectedRegions$ = this.store.pipe(select(state => state.map.numSelectedRegions));
  }

  ngOnInit(): void {
    this.sessionState$.subscribe(this.sessionStateChange.bind(this));
    this.selectionActive$.subscribe(active => console.log("Selection active: " + active));
  }

  initializeMap(map: Map) {
    this.map = map;
    this.map.addControl(new NavigationControl());
    if (this.session == null || this.session.id == null) {
      this.loadBasedLayer();
    } else if (
        this.session.status == SessionStatus.RUNNING ||
        this.session.status == SessionStatus.COMPLETED ||
        this.session.status == SessionStatus.INTERRUPTED
    ) {
      this.loadResultLayer(this.session.id);
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
      this.initDrawing();
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
          this.map.fitBounds(layer[0].bounds as LngLatBoundsLike, {padding: 20});
        } else {
          this.map.addLayer(layer as LayerSpecification);
          this.map.fitBounds(layer.bounds as LngLatBoundsLike, {padding: 20});
        }
      });

      this.subscriptions.push(sub);
    } catch (error) {
      console.error('Error while loading or adding image:', error);
    }
  }

  sessionStateChange(session: Session) {
    if (session == null) return;
    console.log("The status is: " + SessionStatus[session.status]);
    if (session.id != null && session.status == SessionStatus.RUNNING) {
      this.loadResultLayer(session.id).then(() => console.log(" Loaded result layer"));
    }
  }

  initDrawing() {
    this.draw = new MapboxDraw({
      displayControlsDefault: false,
      controls: {
        polygon: false,
        trash: false
      }
    });
    this.map.addControl(this.draw as unknown as IControl);
    this.map.on('draw.create', this.selectFeatures.bind(this));
    this.map.on('draw.delete', this.clearSelection.bind(this));
    this.map.on('draw.update', this.selectFeatures.bind(this));

    this.selectionActive$.subscribe(active => {

      if (!this.map) return;
      const mapContainer = this.map.getCanvasContainer(); //get the map container

      if (active) {
        console.log("Selection active");
        this.draw.changeMode(MapboxDraw.constants.modes.DRAW_POLYGON);
        mapContainer.style.cursor = 'crosshair'; // set cursor to crosshair
      } else {
        console.log("Selection inactive");
        this.clearSelection();
        mapContainer.style.cursor = ''; // reset cursor
      }
    });
  }

  selectFeatures() {
    let polygon = this.draw.getAll(); // Get all drawn polygons

    if (polygon.features.length > 0) {
      let drawnPolygon = polygon.features[0];
      let features = this.map.queryRenderedFeatures({layers: ['region']});
      let intersectedFeatures = [];

      features.forEach(feature => {
        if (booleanIntersects(drawnPolygon, feature)) {
          intersectedFeatures.push(feature);
        }
      });

      let region_codes = intersectedFeatures.map((feature) => feature.properties['region_code']);
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

  clearSelection() {
    this.draw.deleteAll();
    this.map.setFilter('region-highlighted', ['in', 'region_code', '']);
    this.store.dispatch(mapActions.clearSelection());
  }


  ngOnDestroy() {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

}
