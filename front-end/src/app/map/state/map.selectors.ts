import {createFeatureSelector, createSelector} from "@ngrx/store";
import {MapState} from "./map.state";

export const selectMapState = createFeatureSelector<MapState>('map');

export const selectMapSelection = createSelector(
  selectMapState,
  (state: MapState) => state.selection
);

export const selectActiveSelection = createSelector(
  selectMapSelection,
  (selection) => selection.active
);

export const selectRegionSelection = createSelector(
  selectMapSelection,
  (selection) => selection.wkt
);

export const selectRegionSelectedCount = createSelector(
  selectMapSelection,
  (selection) => selection.selectedRegions
);

export const selectLayerVisibility = createSelector(
  selectMapState,
  (state) => state.visibility
);


export const fromMapSelectors = {
  selectMapState,
  selectMapSelection,
  selectActiveSelection,
  selectRegionSelection,
  selectRegionSelectedCount,
  selectLayerVisibility
};
