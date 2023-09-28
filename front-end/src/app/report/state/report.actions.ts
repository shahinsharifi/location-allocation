import {createActionGroup, props} from '@ngrx/store';

export const reportActions = createActionGroup({
  source: 'Report',
  events: {
    'Update Logs': props<{ log: string }>(),
    'Update Location Fitness Chart': props<{ metadata: any, data: any }>(),
    'Update Allocation Fitness Chart': props<{ metadata: any, data: any }>(),
    'Update Cost Distribution Chart': props<{ metadata: any, data: Array<any> }>()
  },
});

