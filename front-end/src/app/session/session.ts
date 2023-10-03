export enum SessionStatus {
  INIT = 'INIT',
  PENDING= 'PENDING',
  START = 'START',
  STARTING= 'STARTING',
  RUNNING= 'RUNNING',
  ABORT = 'ABORT',
  ABORTING= 'ABORTING',
  ABORTED= 'ABORTED',
  FAILED= 'FAILED',
  COMPLETED= 'COMPLETED',
  RESET= 'RESET'
}

export interface Session {
  id?: string | null;
  status?: SessionStatus;
  createdAt?: string;
  numberOfFacilities?: number;
  maxTravelTimeInMinutes?: number;
  maxTravelDistanceInMeters?: number;
  maxTravelDistanceInKilometers?: number;
  wkt?: string | null;
  maxRunningTimeInMinutes?: number;
}
