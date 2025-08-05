import React, { useState } from 'react';
import FilterPanel from './FilterPanel';
import MetricCard from './MetricCard'
import PieChartComponent from './../components/charts/PieChartComponent';
import LineChartComponent from './../components/charts/LineChartComponent';
import SectorIndustryStockHeatmap from './../components/charts/SectorIndustryStockHeatmap'
import Header from '../components/Header';

const Dashboard = () => {
  const [isFilterOpen, setIsFilterOpen] = useState(false);

  const filters = {
    sector: [],
    industry: [],
    fundType: [],
    stockName: [],
    symbol: [],
    mfName: [],
  };

  const handleFilterChange = (key, value) => {
    console.log(key, value);
  };

  const pieData = [
    { name: 'SBI Bluechip Fund', value: 30 },
    { name: 'Mirae Largecap Fund', value: 25 },
    { name: 'Axis Bluechip Fund', value: 20 },
    { name: 'Canara Robeco', value: 15 },
    { name: 'Nippon India', value: 10 },
    { name: 'HDFC Equity Fund', value: 28 },
    { name: 'ICICI Prudential Fund', value: 24 },
    { name: 'Kotak Standard Multicap', value: 22 },
    { name: 'Franklin India Flexicap', value: 18 },
    { name: 'Tata Large Cap Fund', value: 16 },
    { name: 'UTI Nifty Index Fund', value: 14 },
    { name: 'Edelweiss Midcap Fund', value: 12 },
    { name: 'DSP BlackRock Equity', value: 11 },
    { name: 'Aditya Birla Sun Life', value: 10 },
    { name: 'Sundaram Select Focus', value: 9 },
    { name: 'Motilal Oswal Nasdaq', value: 8 },
    { name: 'LIC MF Large Cap', value: 7 },
    { name: 'Baroda BNP Paribas', value: 6 },
    { name: 'PGIM India Flexi Cap', value: 5 },
    { name: 'Quant Active Fund', value: 4 },
  ];
  
  const valuationData = [
    { date: '2023-01-31', value: 600000 },
    { date: '2023-04-30', value: 800000 },
    { date: '2023-07-31', value: 1100000 },
    { date: '2023-10-31', value: 1300000 },
    { date: '2024-01-31', value: 1600000 },
    { date: '2024-04-30', value: 1500000 },
    { date: '2024-07-31', value: 1700000 },
    { date: '2024-10-31', value: 1900000 },
    { date: '2025-03-31', value: 2000000 },
    { date: '2025-06-30', value: 2200000 },
  ];
  
  return (
    <div className="relative min-h-screen bg-gray-50">
      <Header />
      {/* Toggle Filter Button */}
      <button
        onClick={() => setIsFilterOpen(!isFilterOpen)}
        className="fixed top-4 left-4 z-50 bg-blue-600 text-white px-3 py-2 rounded hover:bg-blue-700 shadow"
      >
        {isFilterOpen ? 'Hide Filters' : 'Show Filters'}
      </button>

      {/* Sidebar (FilterPanel) */}
      <FilterPanel
        isOpen={isFilterOpen}
        onClose={() => setIsFilterOpen(false)}
        filters={filters}
        onFilterChange={handleFilterChange}
      />

      {/* Main Content */}
      <main
        className={`transition-all duration-300 ${
          isFilterOpen ? 'ml-80' : 'ml-0'
        } p-6 space-y-6`}
      >
        <section className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
          <MetricCard title="No.Of Stock" value="633" />
          <MetricCard title="Stock Quantity" value="5.97M" />
          <MetricCard title="No.Of MF's" value="432" />
          <MetricCard title="Sector Valuations" value="10B" />
        </section>

          {/* Pie Chart + Overall MF Valuation - Two Columns */}
        <section className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="bg-white shadow rounded-xl p-4">
            <h2 className="text-lg font-semibold mb-2">Fund Distribution</h2>
            {/* Replace with your actual pie chart */}
            <div className="h-64 bg-gray-100 rounded-md flex items-center justify-center">
            <PieChartComponent pieData={pieData} dataKey="value" nameKey="name" />
            </div>
            </div>

            <div className="bg-white shadow rounded-xl p-4">
            <h2 className="text-lg font-semibold mb-2">Overall MF Valuation</h2>
            {/* Replace with your actual metrics or chart */}
            <div className="h-64 bg-gray-100 rounded-md flex items-center justify-center">
            <LineChartComponent data={valuationData} xAxisDataKey="date" yAxisDataKey="value"/>
            </div>
            </div>
        </section>

        <section className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="bg-white shadow rounded-xl p-4">
            <h2 className="text-xl font-semibold">Performance Heatmap (Sector/Stock-wise)</h2>
            <SectorIndustryStockHeatmap />
            </div>
        </section>

      </main>
    </div>
  );
};

export default Dashboard;
