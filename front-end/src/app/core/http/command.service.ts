import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from "../../../environments/environment";


@Injectable({
  providedIn: 'root'
})

export class CommandService {
  baseUrl = environment.baseURL;
  appContext = environment.context;
  constructor(private http: HttpClient) {}

  public execute(cmd: string, method: string, data: any) {
    const url = this.baseUrl + '/' + this.appContext + '/' + cmd;
    return (method.toLocaleLowerCase() === 'post') ? this.http.post<any>(url, data) : this.http.get<any>(url);
  }

}
