import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
  ViewEncapsulation
} from '@angular/core';
import {IgxCategoryChartModule, IgxLegendModule} from 'igniteui-angular-charts';
import {CommonModule} from "@angular/common";
import {CountryRenewableElectricity} from './chart-data';
import {FlexModule} from "@angular/flex-layout";
import {MatCardModule} from "@angular/material/card";

@Component({
  selector: 'app-chart',
  standalone: true,
  imports: [CommonModule, IgxCategoryChartModule, IgxLegendModule, FlexModule, MatCardModule],
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None
})
export class ChartComponent implements OnInit, OnDestroy {

  public constructor() {

  }

  private _countryRenewableElectricity: CountryRenewableElectricity = null;
  public get countryRenewableElectricity(): CountryRenewableElectricity {
    if (this._countryRenewableElectricity == null)
    {
      this._countryRenewableElectricity = new CountryRenewableElectricity();
    }
    return this._countryRenewableElectricity;
  }

  ngOnDestroy(): void {
  }

  ngOnInit(): void {

  }


}
