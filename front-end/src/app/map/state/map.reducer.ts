import { createReducer, on } from '@ngrx/store';
import { MapState } from "./map.state";
import {mapActions} from "./map.actions";

export const initialState: MapState = {
  selectionActive: false,
  spatialQuery: null,
  numSelectedRegions: 0
}

export const mapReducer = createReducer<MapState>(
  initialState,
  on(mapActions.regionsSelected, (state, {numSelectedRegions, spatialQuery}) => ({
    ...state,
    spatialQuery,
    numSelectedRegions
  })),
  on(mapActions.activatePolygonDrawing, state => ({
    ...state,
    selectionActive: true
  })),
  on(mapActions.deactivatePolygonDrawing, state => ({
    ...state,
    selectionActive: false
  })),
  on(mapActions.clearSelection, () => initialState)
);
