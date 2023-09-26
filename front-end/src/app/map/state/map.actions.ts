import {createActionGroup, emptyProps, props} from "@ngrx/store";
import {RegionSelection} from "../region-selection";
import {LayerVisibility} from "../layer-visibility";

export const mapActions = createActionGroup({
  source: 'Map',
  events: {
    'Enable Selection': emptyProps(),
    'Disable Selection': emptyProps(),
    'Regions Selected': props<RegionSelection>(),
    'Clear Selection': props<RegionSelection>(),
    'Change Layer Visibility': props<LayerVisibility>(),
    'Reset Map': emptyProps()
  }
});


