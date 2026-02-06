// src/pages/HoldingChangeView.jsx
import React, { useEffect, useState } from "react";
import Header from "../components/Header";
import FilterPanel from "./FilterPanel";
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
} from "recharts";

const COLORS = [
  "#0088FE", "#00C49F", "#FFBB28", "#FF8042",
  "#A28EFF", "#FF6699", "#33CC99", "#FF6666",
];

const HoldingChangeView = () => {
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [data, setData] = useState(null);
  const [filteredDate, setFilteredDate] = useState(null);

  // new filter states (store option objects)
  const [selectedSector, setSelectedSector] = useState(null);
  const [selectedIndustry, setSelectedIndustry] = useState(null);
  const [selectedMktCategory, setSelectedMktCategory] = useState(null);

  useEffect(() => {
    // build query params only for non-null values
    const params = new URLSearchParams();
    if (filteredDate) params.append("date", filteredDate);
    if (selectedSector) params.append("sectorId", selectedSector.value);
    if (selectedIndustry) params.append("industryId", selectedIndustry.value);
    if (selectedMktCategory) params.append("marketCapCategory", selectedMktCategory.value);

    const query = params.toString();
    const url = query
      ? `http://localhost:8080/api/stock-holdings/metrics?${query}`
      : `http://localhost:8080/api/stock-holdings/metrics`;

    fetch(url)
      .then((res) => res.json())
      .then(setData)
      .catch(console.error);
  }, [filteredDate, selectedSector, selectedIndustry, selectedMktCategory]);

  if (!data || !data.stockPerformance) {
    return <div className="p-6">Loading...</div>;
  }

  const { stockPerformance } = data;

  const handleFilterChange = (key, value) => {
    // if a full apply payload is sent, extract values (value may be option objects or raw strings)
    if (key === "apply") {
      const dateVal = value?.dates?.value ?? value?.dates ?? null;
      const sectorVal = value?.sector ?? null; // already option object or null
      const industryVal = value?.industry ?? null;
      const mktCatVal = value?.mktCategory ?? null;

      setFilteredDate(dateVal);
      setSelectedSector(sectorVal);
      setSelectedIndustry(industryVal);
      setSelectedMktCategory(mktCatVal);

    } else if (key === "dates") {
      // single-date change from FilterPanel
      const dateVal = value?.value ?? value ?? null;
      setFilteredDate(dateVal);
    } else if (key === "sector") {
      const sectorVal = value ?? null;
      setSelectedSector(sectorVal);
    } else if (key === "industry") {
      const industryVal = value ?? null;
      setSelectedIndustry(industryVal);
    } else if (key === "mktCategory") {
      const mktCatVal = value ?? null;
      setSelectedMktCategory(mktCatVal);
    } else if (key === "clear") {
      setFilteredDate(null);
      setSelectedSector(null);
      setSelectedIndustry(null);
      setSelectedMktCategory(null);
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
            Holding Change Insights
          </h1>
          <div className="text-sm text-gray-400">
            {filteredDate && <span>Showing for {filteredDate} </span>}
            {selectedSector && <span> • Sector: {selectedSector.label} </span>}
            {selectedIndustry && <span> • Industry: {selectedIndustry.label} </span>}
            {selectedMktCategory && <span> • Market Cap: {selectedMktCategory.label} </span>}
          </div>
        </div>

{/* Metrics (Best | Growth | Avoid) */}
<section className="grid grid-cols-1 sm:grid-cols-3 gap-4">
  {/* Best Stocks */}
  <div className="bg-white shadow rounded-xl p-4 border-l-4 border-green-500">
    <h2 className="text-sm font-medium text-gray-500">🚀 Best Stocks</h2>
    <ul className="mt-2 space-y-1 text-green-600 text-sm font-semibold">
      {stockPerformance.top10Overall.slice(0, 5).map((s) => (
        <li key={s.id}>
          {s.name} <span className="text-gray-500">({s.symbol})</span>
        </li>
      ))}
    </ul>
  </div>

  {/* Growth Stocks */}
  <div className="bg-white shadow rounded-xl p-4 border-l-4 border-blue-500">
    <h2 className="text-sm font-medium text-gray-500">📈 Growth Stocks</h2>
    <ul className="mt-2 space-y-1 text-blue-600 text-sm font-semibold">
      {stockPerformance.top10ByRelativePct.slice(0, 5).map((s) => (
        <li key={s.id}>
          {s.name} <span className="text-gray-500">({s.symbol})</span>
        </li>
      ))}
    </ul>
  </div>

  {/* Avoid Stocks */}
  <div className="bg-white shadow rounded-xl p-4 border-l-4 border-red-500">
    <h2 className="text-sm font-medium text-gray-500">⚠️ Avoid Stocks</h2>
    <ul className="mt-2 space-y-1 text-red-600 text-sm font-semibold">
      {stockPerformance.top10ByExposure
        .filter((s) => s.exposureChange < 0 || s.score < 0)
        .slice(0, 5)
        .map((s) => (
          <li key={s.id}>
            {s.name} <span className="text-gray-500">({s.symbol})</span>
          </li>
        ))}
    </ul>
  </div>
</section>

        {/* Charts */}
        <section className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Top Exposure */}
          <div className="bg-white shadow rounded-xl p-4">
            <h2 className="text-lg font-semibold text-gray-700 mb-2">
              Top 10 by Exposure Change
            </h2>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart
                data={stockPerformance.top10ByExposure}
                layout="vertical"
              >
                <XAxis type="number" />
                <YAxis dataKey="symbol" type="category" />
                <Tooltip />
                <Bar dataKey="exposureChange" fill="#00BFFF" />
              </BarChart>
            </ResponsiveContainer>
          </div>

          {/* Top Fund Count */}
          <div className="bg-white shadow rounded-xl p-4">
            <h2 className="text-lg font-semibold text-gray-700 mb-2">
              Top 10 by Fund Count Change
            </h2>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart
                data={stockPerformance.top10ByFundCount}
                layout="vertical"
              >
                <XAxis type="number" />
                <YAxis dataKey="symbol" type="category" />
                <Tooltip />
                <Bar dataKey="fundCountChange" fill="#FF8042" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </section>

        {/* Relative % Change */}
        <div className="bg-white shadow rounded-xl p-4">
          <h2 className="text-lg font-semibold text-gray-700 mb-2">
            Top 10 by Relative % Change
          </h2>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={stockPerformance.top10ByRelativePct}>
              <XAxis dataKey="symbol" />
              <YAxis />
              <Tooltip />
              <Bar dataKey="relativeChangePct" fill="#4CAF50" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Overall Top 10 */}
        <div className="bg-white shadow rounded-xl p-4">
          <h2 className="text-lg font-semibold text-gray-700 mb-2">
            Top 10 Overall Score
          </h2>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={stockPerformance.top10Overall}
                dataKey="score"
                nameKey="symbol"
                outerRadius={120}
                label
              >
                {stockPerformance.top10Overall.map((_, i) => (
                  <Cell key={i} fill={COLORS[i % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </main>
    </div>
  );
};

export default HoldingChangeView;
