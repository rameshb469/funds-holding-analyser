// src/pages/SectorIndustryInsights.jsx
import React, { useEffect, useState } from "react";
import Header from '../components/Header';
import FilterPanel from './FilterPanel';
import SectorIndustryHeatmap from './SectorIndustryHeatmap'
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  PieChart,
  Pie,
  Cell,
} from "recharts";

const COLORS = [
  "#0088FE", "#00C49F", "#FFBB28", "#FF8042",
  "#A28EFF", "#FF6699", "#33CC99", "#FF6666",
];

const SectorIndustryInsights = () => {
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [data, setData] = useState([]);
  const [filteredDate, setFilteredDate] = useState(null);

  useEffect(() => {
    const url = filteredDate
      ? `http://localhost:8080/api/sector-insights?date=${filteredDate}`
      : `http://localhost:8080/api/sector-insights`;

    fetch(url)
      .then((res) => res.json())
      .then(setData)
      .catch(console.error);
  }, [filteredDate]);

  if (!data || data.length === 0) {
    return <div className="p-6">Loading...</div>;
  }

  // Aggregate metrics
  const topGainer = [...data].sort((a, b) => b.changePct - a.changePct)[0];
  const topLoser = [...data].sort((a, b) => a.changePct - b.changePct)[0];

  const sectorAgg = data.reduce((acc, d) => {
    acc[d.sectorName] = (acc[d.sectorName] || 0) + d.currentValue;
    return acc;
  }, {});
  const sectorData = Object.entries(sectorAgg).map(([name, value]) => ({
    name,
    value,
  }));
  const strongestSector = sectorData.sort((a, b) => b.value - a.value)[0];

  const handleFilterChange = (key, value) => {
    if (key === "apply" && value?.dates) {
      setFilteredDate(value?.dates.value || null);
    } else if (key === "clear") {
      setFilteredDate(null);
    }
  };

  return (
    <div className="relative min-h-screen bg-gray-50">
      <Header />

      {/* Toggle Filter Button */}
      <button
        onClick={() => setIsFilterOpen(!isFilterOpen)}
        className="fixed top-4 left-4 z-50 bg-blue-600 text-white px-3 py-2 rounded hover:bg-blue-700 shadow"
      >
        {isFilterOpen ? "Hide Filters" : "Show Filters"}
      </button>

      {/* Sidebar */}
      <FilterPanel
        isOpen={isFilterOpen}
        onClose={() => setIsFilterOpen(false)}
        onFilterChange={handleFilterChange}
      />

      <main
        className={`transition-all duration-300 ${
          isFilterOpen ? "ml-80" : "ml-0"
        } p-6 space-y-6`}
      >
        {/* Header */}
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-bold text-gray-800">
            Sector & Industry Insights
          </h1>
          {filteredDate && (
            <span className="text-sm text-gray-400">
              Showing for {filteredDate}
            </span>
          )}
        </div>

        {/* Metrics */}
        <section className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="bg-white shadow rounded-xl p-4 border-l-4 border-green-500">
            <h2 className="text-sm font-medium text-gray-500">Top Gainer</h2>
            <p className="text-lg font-bold text-green-600">
              {topGainer.stockName} ({topGainer.changePct.toFixed(2)}%)
            </p>
          </div>
          <div className="bg-white shadow rounded-xl p-4 border-l-4 border-red-500">
            <h2 className="text-sm font-medium text-gray-500">Top Loser</h2>
            <p className="text-lg font-bold text-red-600">
              {topLoser.stockName} ({topLoser.changePct.toFixed(2)}%)
            </p>
          </div>
          <div className="bg-white shadow rounded-xl p-4 border-l-4 border-blue-500">
            <h2 className="text-sm font-medium text-gray-500">Strongest Sector</h2>
            <p className="text-lg font-bold text-blue-600">
              {strongestSector.name} (₹
              {strongestSector.value.toLocaleString()})
            </p>
          </div>
        </section>

        {/* Charts */}
        <section className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Pie: Sector Contribution */}
          <div className="bg-white shadow rounded-xl p-4">
            <h2 className="text-lg font-semibold text-gray-700 mb-2">
              Sector Contribution
            </h2>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={sectorData}
                  dataKey="value"
                  nameKey="name"
                  outerRadius={120}
                  label
                >
                  {sectorData.map((_, i) => (
                    <Cell key={i} fill={COLORS[i % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>

          {/* Stock Change % */}
          <div className="bg-white shadow rounded-xl p-4">
            <h2 className="text-lg font-semibold text-gray-700 mb-2">
              Stock-Level % Change
            </h2>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={data}>
                <XAxis dataKey="stockSymbol" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="changePct" fill="#00BFFF" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </section>

        {/* Top Gainers/Losers */}
        <section className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="bg-white shadow rounded-xl p-4">
            <h2 className="text-lg font-semibold text-gray-700 mb-2">
              Top 10 Gainers
            </h2>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart
                data={[...data].sort((a, b) => b.changePct - a.changePct).slice(0, 10)}
                layout="vertical"
              >
                <XAxis type="number" />
                <YAxis dataKey="stockSymbol" type="category" />
                <Tooltip />
                <Bar dataKey="changePct" fill="#4CAF50" />
              </BarChart>
            </ResponsiveContainer>
          </div>

          <div className="bg-white shadow rounded-xl p-4">
            <h2 className="text-lg font-semibold text-gray-700 mb-2">
              Top 10 Losers
            </h2>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart
                data={[...data].sort((a, b) => a.changePct - b.changePct).slice(0, 10)}
                layout="vertical"
              >
                <XAxis type="number" />
                <YAxis dataKey="stockSymbol" type="category" />
                <Tooltip />
                <Bar dataKey="changePct" fill="#F44336" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </section>



        {/* Table */}
        <div className="bg-white shadow rounded-xl p-4">
          <h2 className="text-lg font-semibold text-gray-700 mb-2">
            Detailed Stock Changes
          </h2>
          <div className="overflow-x-auto">
           {/* ✅ Replace Detailed Table with Heatmap */}
                   {data.length > 0 ? (
                     <SectorIndustryHeatmap data={data} />
                   ) : (
                     <p className="text-gray-500">No data available</p>
            )}
            <table className="w-full text-sm text-left border">
              <thead className="bg-gray-100">
                <tr>
                  <th className="px-2 py-1">Sector</th>
                  <th className="px-2 py-1">Industry</th>
                  <th className="px-2 py-1">Stock</th>
                  <th className="px-2 py-1">Symbol</th>
                  <th className="px-2 py-1">Current Value</th>
                  <th className="px-2 py-1">Prev Value</th>
                  <th className="px-2 py-1">Change %</th>
                </tr>
              </thead>
              <tbody>
                {data.map((d) => (
                  <tr key={d.stockId} className="border-t">
                    <td className="px-2 py-1">{d.sectorName}</td>
                    <td className="px-2 py-1">{d.industryName}</td>
                    <td className="px-2 py-1">{d.stockName}</td>
                    <td className="px-2 py-1">{d.stockSymbol}</td>
                    <td className="px-2 py-1">{d.currentValue.toLocaleString()}</td>
                    <td className="px-2 py-1">{d.prevValue.toLocaleString()}</td>
                    <td
                      className={`px-2 py-1 font-bold ${
                        d.changePct >= 0 ? "text-green-600" : "text-red-600"
                      }`}
                    >
                      {d.changePct.toFixed(2)}%
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  );
};

export default SectorIndustryInsights;
