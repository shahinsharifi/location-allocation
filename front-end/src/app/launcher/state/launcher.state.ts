import {Session} from "../../session/session";

export interface LauncherState {
  error: string | undefined;
  currentStep: number;
  session: Session | undefined;
}
