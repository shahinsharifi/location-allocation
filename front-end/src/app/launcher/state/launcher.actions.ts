import {createActionGroup, props} from '@ngrx/store';
import {Session} from "../../session/session";

export const launcherActions = createActionGroup({
  source: 'Launcher',
  events: {
    'Run': props<Session>(),
    'Stop': props<Session>()
  },
});
