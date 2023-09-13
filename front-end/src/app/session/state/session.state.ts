import {Session} from "../session";

export interface SessionState {
  activeSession: Session | undefined;
  sessions: Session[] | undefined;
}


