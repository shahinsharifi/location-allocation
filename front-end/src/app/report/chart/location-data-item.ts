export class LocationDataItem {
  public constructor(init: Partial<LocationDataItem>) {
    Object.assign(this, init);
  }
  fitness?: Array<any> | undefined;
}
