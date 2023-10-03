import {createActionGroup, emptyProps, props} from '@ngrx/store';

export const reportActions = createActionGroup({
  source: 'Report',
  events: {
    'Update Logs': props<{ log: string }>(),
    'Update Location Fitness Chart': props<{ metadata: any, data: Array<any> }>(),
    'Update Allocation Fitness Chart': props<{ metadata: any, data: Array<any> }>(),
    'Update Cost Distribution Chart': props<{ metadata: any, data: Array<any> }>(),
    'Reset Charts': emptyProps()
  },
});

