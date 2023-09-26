import {createActionGroup, emptyProps, props} from '@ngrx/store';
import {Session} from "../../session/session";

export const launcherActions = createActionGroup({
  source: 'Launcher',
  events: {
    'Start Process': props<Session>(),
    'Stop Process': props<String>(),
    'Resume Process': props<String>(),
    'Change Step': props<{ stepIndex: number }>(),
    'Toggle Selection': props<{ active: boolean }>(),
    'Clear Selection': emptyProps(),
    'Reset Session': emptyProps()
  },
});
