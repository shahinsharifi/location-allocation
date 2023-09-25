import {Injectable} from '@angular/core';
import {Session} from "./session";


@Injectable({
  providedIn: 'root'
})
export class SessionService {

  sessions: Session[];
  activeSession: Session;
  constructor() {

  }


}
