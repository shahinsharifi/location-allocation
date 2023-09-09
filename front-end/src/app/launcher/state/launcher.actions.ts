import {createActionGroup, props} from '@ngrx/store';
import {Session} from "../../session/session";

export const launcherActions = createActionGroup({
  source: 'Launcher',
  events: {
    'Run': props<Session>(),
    'Run Success': props<{ session: Session }>(),
    'Stop': props<Session>(),
    'Stop Success': props<{ session: Session }>()
  },
});
