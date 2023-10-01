import {MapState} from "./map.state";
import {mapActions} from "./map.actions";
import {createReducer, on} from "@ngrx/store";

export const initialState: MapState = {
  selection: {
    activeDrawing: false,
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
      activeDrawing: true
    }
  })),
  on(mapActions.disableSelection, state => ({
      ...state,
      selection: {
        ...state.selection,
        activeDrawing: false
      }
    }
  )),
  on(mapActions.regionsSelected, (state, selection) => ({
      ...state,
      selection: {
        ...state.selection,
        activeDrawing: true,
        wkt: selection.wkt,
        selectedRegions: selection.selectedRegions
      }
    }
  )),
  on(mapActions.changeLayerVisibility, (state, visibility) => ({
      ...state,
      visibility: visibility
    }
  )),
  on(mapActions.clearSelection, state => ({
      ...state,
      selection: {
        ...state.selection,
        wkt: null,
        selectedRegions: 0
      }
    }
  ))
);

