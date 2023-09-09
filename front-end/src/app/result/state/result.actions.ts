import { createAction, props } from '@ngrx/store';



// Result actions
export const updateProgress = createAction('[Result] Update Progress', props<{ progress: number }>());
