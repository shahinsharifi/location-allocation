import {createActionGroup, emptyProps, props} from '@ngrx/store';
import {Session, SessionStatus} from "../session";

export const sessionActions = createActionGroup({
  source: 'Session',
  events: {
    'Create Session': props<{ activeSession: Session }>(),
    'Update Session STATUS': props<{ status: SessionStatus }>(),
    'Delete Session': props<{ id: string }>(),
    'Activate Session': props<{ id: string }>(),
    'Reset Active Session': emptyProps(),
    'Load Stored Sessions': props<{ sessions: Session[] }>(),
  },
});

