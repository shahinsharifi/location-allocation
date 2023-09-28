import {ReportState} from "./report.state";
import {createReducer, on} from "@ngrx/store";
import {reportActions} from "./report.actions";


const initialState: ReportState = {
	logs: [],
	locationFitnessChart: {
		data: [],
		metadata: {}
	},
	allocationFitnessChart: {
		data: [],
		metadata: {}
	},
	costDistributionChart: {
		data: [],
		metadata: {}
	}
}

export const reportReducer = createReducer<ReportState>(
		initialState,
		on(reportActions.updateLogs, (state, {log}) => {
			const logs = state.logs ? [...state.logs, log] : [log];
			return {...state, logs};
		}),
		on(reportActions.updateAllocationFitnessChart, (state, chartData) => {
			const chart = state.locationFitnessChart;
			const data = chart.data ? [...chart.data, chartData.data] : [chartData.data];
			const metadata = chartData.metadata;
			return {...state, locationFitnessChart: {...chart, data, metadata}};
		}),
		on(reportActions.updateAllocationFitnessChart, (state, chartData) => {
			const chart = state.allocationFitnessChart;
			const data = chart.data ? [...chart.data, chartData.data] : [chartData.data];
			const metadata = chartData.metadata;
			return {...state, allocationFitnessChart: {...chart, data, metadata}};
		}),
		on(reportActions.updateCostDistributionChart, (state, chartData) => {
			const chart = state.costDistributionChart;
			const data = chart.data ? [...chart.data, chartData.data] : [chartData.data];
			const metadata = chartData.metadata;
			return {...state, costDistributionChart: {...chart, data, metadata}};
		})
);
