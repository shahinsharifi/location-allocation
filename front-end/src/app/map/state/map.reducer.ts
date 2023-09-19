import {MapState} from "./map.state";
import {mapActions} from "./map.actions";
import {createReducer, on} from "@ngrx/store";

export const initialState: MapState = {
  selection: {
    active: false,
    wkt: null,
    selectedRegions: 0
  },
  visibility: {
    region: true,  // Assuming regions are visible by default.
    location: false,
    allocation: false
  }
};

export const mapReducer = createReducer<MapState>(
  initialState,
  on(mapActions.enableSelection, state => ({
    ...state,
    selection: {
      ...state.selection,
      active: true
    }
  })),
  on(mapActions.disableSelection, state => ({
      ...state,
      selection: {
        ...state.selection,
        active: false
      }
    }
  )),
  on(mapActions.regionsSelected, (state, selection) => ({
      ...state,
      selection: selection
    }
  )),
  on(mapActions.changeLayerVisibility, (state, visibility) => ({
      ...state,
      visibility: visibility
    }
  )),
);

