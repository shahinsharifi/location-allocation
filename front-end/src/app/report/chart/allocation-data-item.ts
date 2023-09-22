export class AllocationDataItem {

  public constructor(init: Partial<AllocationDataItem>) {
    Object.assign(this, init);
  }

  fitness?: Array<any> | undefined;
  travelCostDistribution?: Array<any> | undefined;

}
