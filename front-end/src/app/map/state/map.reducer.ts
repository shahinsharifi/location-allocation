import {createReducer, on} from '@ngrx/store';
import {MapState} from "./map.state";
import {mapActions} from "./map.actions";

export const initialState: MapState = {
  drawing: false,
  regionSelection: {
    selectionRegions: 0,
    wkt: ''
  },
  visibility: {
    region: true,
    regionSelection: false,
    location: false,
    allocation: false
  }
};


export const mapReducer = createReducer<MapState>(
  initialState,
  on(mapActions.enableDrawing, state => ({
    ...state,
    drawing: true
  })),
  on(mapActions.disableDrawing, state => ({
    ...state,
    drawing: false
  })),
  on(mapActions.regionsSelected, (state, regionSelection) => ({
    ...state,
    regionSelection: {
      ...state.regionSelection,
      ...regionSelection.regionSelection
    }
  })),
  on(mapActions.clearSelection, state => ({
    ...state,
    regionSelection: {
      selectionRegions: 0,
      wkt: ''
    }
  })),
  on(mapActions.toggleRegionLayer, state => ({
      ...state,
      visibility: {
        ...state.visibility,
        baseLayer: !state.visibility.region
      }
    }
  )),
  on(mapActions.toggleRegionSelectionLayer, state => ({
      ...state,
      visibility: {
        ...state.visibility,
        selectedRegionsLayer: !state.visibility.regionSelection
      }
    }
  )),
  on(mapActions.toggleLocationLayer, state => ({
      ...state,
      visibility: {
        ...state.visibility,
        facilityLayer: !state.visibility.location
      }
    }
  )),
  on(mapActions.toggleAllocationLayer, state => ({
      ...state,
      visibility: {
        ...state.visibility,
        allocationLayer: !state.visibility.allocation
      }
    }
  )),
  on(mapActions.resetMap, () => initialState)
);
