import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {environment} from "../../../environments/environment";


@Injectable({
  providedIn: 'root'
})

export class CommandService {
  baseUrl = environment.baseURL;
  appContext = environment.context;
  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      'cache-control': 'no-cache',
      'Accept': 'application/json'
    })
  };

  constructor(private http: HttpClient) {
    console.log('Hi');
  }


  public execute(cmd: string, method: string, responseType: string, data: any, isRemoteServer: boolean) {
    console.log(method + responseType);
    let url = '';
    if (isRemoteServer)
      url = this.baseUrl + '/' + this.appContext + '/' + cmd;
    else
      url = cmd;
    return (method.toLocaleLowerCase() === 'post') ? this.http.post<any>(url, data) : this.http.get<any>(url);
  }

}
