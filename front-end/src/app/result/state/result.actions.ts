import {createActionGroup, props} from '@ngrx/store';

export const resultActions = createActionGroup({
  source: 'Result',
  events: {
    'Update Logs': props<{ log: string }>(),
    'Update Progress': props<{ progress: Array<any> }>()
  },
});

