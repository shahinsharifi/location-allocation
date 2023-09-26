import {SessionStatus} from "../../session/session";

export interface LauncherState {
  sessionId?: string;
  stepIndex: number;
  status?: SessionStatus;
  selection?: {
    active: boolean;
    wkt: string;
    selectedRegions: number;
  };
  numberOfFacilities?: number;
  maxTravelTimeInMinutes?: number;
  maxRunningTimeInMinutes?: number;
  buttons: {
    next: boolean;
    previous: boolean;
    start: boolean;
    stop: boolean;
    reset: boolean;
    resume: boolean;
    clear: boolean;
  };
}
