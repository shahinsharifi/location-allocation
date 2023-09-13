import {createActionGroup, props} from '@ngrx/store';
import {Session} from "../session";

export const sessionActions = createActionGroup({
  source: 'Session',
  events: {
    'Create Session': props<{ activeSession: Session }>(),
    'Update Session': props<{ activeSession: Session }>(),
    'Delete Session': props<{ activeSession: Session }>(),
    'Activate Session': props<{ activeSession: Session }>()
  },
});

