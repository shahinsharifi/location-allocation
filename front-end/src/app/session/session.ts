export enum SessionStatus {
  PENDING = 'PENDING',
  STARTING = 'STARTING',
  RUNNING = 'RUNNING',
  INTERRUPTED = 'INTERRUPTED',
  COMPLETED = 'COMPLETED'
}

export interface Session {
  id?: string;
  status?: SessionStatus; // Replace 'running' with 'status'
  createdAt?: string;
  numberOfFacilities?: number;
  maxTravelTimeInMinutes?: number;
  maxTravelDistanceInMeters?: number;
  maxTravelDistanceInKilometers?: number;
  wkt?: string;
  maxRunningTimeInMinutes?: number;
}
