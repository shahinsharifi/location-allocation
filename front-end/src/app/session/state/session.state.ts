import {Session} from "../session";


export interface SessionState {
  error: string | undefined;
  loaded: boolean;
  selected: Session | undefined;
  sessions: Session[] | undefined;
}


