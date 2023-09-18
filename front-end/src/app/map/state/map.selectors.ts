import {createFeatureSelector, createSelector} from "@ngrx/store";
import {MapState} from "./map.state";

export const selectMapState = createFeatureSelector<MapState>('map');

export const selectDrawing = createSelector(
  selectMapState,
  (state: MapState) => state.drawing
);

export const selectRegionSelection = createSelector(
  selectMapState,
  (state: MapState) => state.regionSelection
);

export const selectVisibility = createSelector(
  selectMapState,
  (state: MapState) => state.visibility
);


export const fromMapSelectors = {
  selectMapState,
  selectDrawing,
  selectRegionSelection,
  selectVisibility
};
