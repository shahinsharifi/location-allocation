import {createActionGroup, props} from '@ngrx/store';

export const reportActions = createActionGroup({
  source: 'Report',
  events: {
    'Update Logs': props<{ log: string }>(),
    'Update Progress': props<{ progress: Array<any> }>()
  },
});

