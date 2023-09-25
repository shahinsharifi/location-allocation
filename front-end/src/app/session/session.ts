export enum SessionStatus {
  INIT = 'INIT',
  PENDING = 'PENDING',
  STARTING = 'STARTING',
  RUNNING = 'RUNNING',
  INTERRUPTED = 'INTERRUPTED',
  COMPLETED = 'COMPLETED'
}

export interface Session {
  id?: string;
  status?: SessionStatus | SessionStatus.INIT;
  createdAt?: string;
  numberOfFacilities?: number;
  maxTravelTimeInMinutes?: number;
  maxTravelDistanceInMeters?: number;
  maxTravelDistanceInKilometers?: number;
  wkt?: string;
  maxRunningTimeInMinutes?: number;
}
