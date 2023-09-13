import {createActionGroup, emptyProps, props} from "@ngrx/store";

export const mapActions = createActionGroup({
  source: 'Map',
  events: {
    'Activate Polygon Drawing': emptyProps(),
    'Deactivate Polygon Drawing': emptyProps(),
    'Clear Selection': emptyProps(),
    'Regions Selected': props<{ spatialQuery: string, numSelectedRegions: number }>(),
  },
});
