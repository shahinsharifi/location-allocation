import {createActionGroup, props} from '@ngrx/store';

export const reportActions = createActionGroup({
  source: 'Report',
  events: {
    'Update Logs': props<{ log: string }>(),
    'Update Location Fitness': props<{ locationFitness: any }>(),
    'Update Allocation Fitness': props<{ allocationFitness: any }>(),
    'Update Allocation Travel Cost Distribution': props<{ travelCostDistribution: Array<any> }>()
  },
});

