export interface Session {
  id?: string;
  running?: boolean;
  createdAt?: string;
  numberOfFacilities?: number;
  maxTravelTimeInMinutes?: number;
  maxTravelDistanceInMeters?: number;
  maxTravelDistanceInKilometers?: number;
}
