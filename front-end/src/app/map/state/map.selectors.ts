import { createSelector } from '@ngrx/store';
import {MapState} from "./map.state";
import {AppState} from "../../core/state/app.state"; // assuming AppState holds your global application state

// Select feature
export const selectMap = (state: AppState) => state.map;

// Selectors
export const selectRegionsOfInterest = createSelector(
  selectMap,
  (mapState: MapState) => mapState.numSelectedRegions
);

export const selectDrawingActivationMode = createSelector(
  selectMap,
  (mapState: MapState) => mapState.selectionActive
);

