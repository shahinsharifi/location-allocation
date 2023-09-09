import { Injectable } from '@angular/core';
import {CommandService} from "../core/http/command.service";
import {Observable} from "rxjs";
import {Session} from "../session/session";

@Injectable({
  providedIn: 'root'
})
export class LauncherService {

  constructor(private commandService: CommandService) { }

  start(payload: Session): Observable<Session> {
    console.log("hello!")
    return this.commandService.execute(
      `start`, 'POST', 'json', payload, true
    );
  }

  stop(payload: Session): Observable<Session> {
    return this.commandService.execute(
      `stop`, 'POST', 'json', payload, true
    );
  }
}
