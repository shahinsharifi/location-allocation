import {createActionGroup, props} from '@ngrx/store';

export const reportActions = createActionGroup({
  source: 'Report',
  events: {
    'Update Logs': props<{ log: string }>(),
    'Update Location Fitness': props<{ fitness: any }>(),
    'Update Allocation Fitness': props<{ fitness: any }>(),
    'Update Allocation Travel Cost Distribution': props<{ travelCostDistribution: Array<any> }>()
  },
});

