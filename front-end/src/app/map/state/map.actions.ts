import {createActionGroup, emptyProps, props} from "@ngrx/store";

export const mapActions = createActionGroup({
  source: 'Map',
  events: {
    'Enable Drawing': emptyProps(),
    'Disable Drawing': emptyProps(),
    'Clear Selection': emptyProps(),
    'Regions Selected': props<{ regionSelection: object }>(),
    'Toggle Region Layer': emptyProps(),
    'Toggle Region Selection Layer': emptyProps(),
    'Toggle Location Layer': emptyProps(),
    'Toggle Allocation Layer': emptyProps(),
    'Reset Map': emptyProps()
  }
});
