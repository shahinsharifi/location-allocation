import {createFeature, createReducer, on} from '@ngrx/store';
import {mapActions} from './map.actions';
import {MapState} from "./map.state";
export const initialState: MapState = {
  selectionActive: false,
  spatialQuery: null,
  numSelectedRegions: 0
}

export const mapFeature = createFeature({
  name: 'map',
  reducer: createReducer<MapState>(
    initialState,
    on(mapActions.regionsSelected, (state, {numSelectedRegions, spatialQuery}) => ({
      ...state,
      spatialQuery: spatialQuery,
      numSelectedRegions: numSelectedRegions
    })),
    on(mapActions.activatePolygonDrawing, state => ({
      ...state,
      selectionActive: true
    })),
    on(mapActions.clearSelection, state => ({
      ...state,
      spatialQuery: null,
      numSelectedRegions: 0,
      selectionActive: false
    }))
  ),
});

