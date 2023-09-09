import {createActionGroup, props} from "@ngrx/store";


export const mapActions = createActionGroup({
  source: 'Map',
  events: {
    'Select Bbox': props<{ bbox: Array<number>[4] }>(),
  },
});
