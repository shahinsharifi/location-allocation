import {createActionGroup, props} from '@ngrx/store';
import {Session} from "../session";

export const sessionActions = createActionGroup({
  source: 'Session',
  events: {
    'Select Session': props<Session>(),
    'Session Selected': props<Session>()
  },
});

