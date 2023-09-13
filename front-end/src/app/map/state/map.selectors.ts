import { createSelector } from '@ngrx/store';
import {MapState} from "./map.state";
import {AppState} from "../../core/state/app.state";

export const selectMapState = (state: AppState) => state.map;

export const selectSelectionActive = createSelector(
  selectMapState,
  (mapState: MapState) => mapState.selectionActive
);

export const selectSpatialQuery = createSelector(
  selectMapState,
  (mapState: MapState) => mapState.spatialQuery
);

export const selectNumSelectedRegions = createSelector(
  selectMapState,
  (mapState: MapState) => mapState.numSelectedRegions
);

export const fromMapSelectors = {
  selectMapState,
  selectSelectionActive,
  selectSpatialQuery,
  selectNumSelectedRegions
};
