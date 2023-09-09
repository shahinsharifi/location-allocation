import { Injectable } from '@angular/core';
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

  constructor(private http: HttpClient) {console.log('Hi');}


  public execute(cmd: string, method: string, responseType: string, data: any, isRemoteServer: boolean) {
    console.log(method + responseType);
    let url = '';
    if (isRemoteServer)
      url = this.baseUrl + '/' + this.appContext + '/' + cmd;
    else
      url = cmd;
    return this.http.post<any>(url,data);
    // if (method.toLocaleLowerCase() === 'post') {
    //   if (responseType.toLocaleLowerCase() === 'blob')
    //     return this.doBinaryDownload(url, data);
    //   else if(data instanceof  FormData && data.has('file'))
    //     return this.doFileUpload(url, data);
    //   else
    //     return this.doPostCall(url, data)
    // } else
    //   return this.doGetCall(url);
  }


  // private doGetCall(url: string) {
  //   return this.http.get(url);
  // }
  //
  //
  // private doPostCall(url: string, data: any) {
  //   return this.http.post<any>(url, data, this.httpOptions);
  // }
  //
  // private doFileUpload(url: string, data: any) {
  //   return this.http.post<any>(url, data);
  // }
  //
  // private doBinaryDownload(url: string, data: any): Observable<Blob> {
  //   return this.http.post(url, data, {headers: this.httpOptions.headers, responseType: 'blob'});
  // }
  //
  // error(message) {
  //   return throwError({ error: { message } });
  // }
}
