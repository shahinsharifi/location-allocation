import {SessionStatus} from "../../session/session";
import {RegionSelection} from "../../map/region-selection";

export interface LauncherState {
  sessionId?: string;
  stepIndex: number;
  status?: SessionStatus;
  selection?: RegionSelection;
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
