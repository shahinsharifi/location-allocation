import {createActionGroup, props} from '@ngrx/store';

export const reportActions = createActionGroup({
  source: 'Report',
  events: {
    'Update Logs': props<{ log: string }>(),
    'Update Location Fitness': props<{ fitness: Object }>(),
    'Update Allocation Fitness': props<{ fitness: Object }>(),
    'Update Allocation Travel Cost Distribution': props<{ travelCostDistribution: Object }>()
  },
});

