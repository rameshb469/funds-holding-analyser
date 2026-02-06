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
  Legend,
} from "recharts";

const CATEGORY_COLORS = {
  "Strong Exposure": "#4CAF50",
  Accumulation: "#2196F3",
  "Valuation Growth (with Accumulation)": "#00C49F",
  "Consensus Buy": "#FFC107",
  "Newly Added": "#9C27B0",
  "Trimmed Holding": "#F44336",
  "Exit / Zero Holding": "#795548",
};

const InvestmentInsights = () => {
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [data, setData] = useState(null);
  const [filteredDate, setFilteredDate] = useState(null);

  // new filter selections to be included in query params
  const [selectedSector, setSelectedSector] = useState(null);
  const [selectedIndustry, setSelectedIndustry] = useState(null);
  const [selectedMktCategory, setSelectedMktCategory] = useState(null);

  useEffect(() => {
    const params = new URLSearchParams();
    if (filteredDate) params.append("date", filteredDate);
    if (selectedSector) params.append("sectorId", selectedSector.value);
    if (selectedIndustry) params.append("industryId", selectedIndustry.value);
    if (selectedMktCategory) params.append("marketCapCategory", selectedMktCategory.value);

    const query = params.toString();
    const url = query
      ? `http://localhost:8080/api/investment-insights?${query}`
      : "http://localhost:8080/api/investment-insights";

    fetch(url)
      .then((res) => res.json())
      .then(setData)
      .catch(console.error);
  }, [filteredDate, selectedSector, selectedIndustry, selectedMktCategory]);

  if (!data) return <div className="p-6">Loading...</div>;

  const renderSignals = (signals) => (
    <ul className="flex flex-wrap gap-2">
      {signals.map((s, i) => (
        <li
          key={i}
          className="px-2 py-1 rounded text-white text-xs font-semibold"
          style={{ backgroundColor: CATEGORY_COLORS[s] || "#999" }}
        >
          {s}
        </li>
      ))}
    </ul>
  );

  // Handle filter changes coming from FilterPanel
  const handleFilterChange = (key, value) => {
    if (key === "apply") {
      // value is the full apply payload
      const dateVal = value?.dates?.value ?? value?.dates ?? null;
      const sectorVal = value?.sector ?? null; // option object or null
      const industryVal = value?.industry ?? null;
      const mktCatVal = value?.mktCategory ?? null;

      setFilteredDate(dateVal);
      setSelectedSector(sectorVal);
      setSelectedIndustry(industryVal);
      setSelectedMktCategory(mktCatVal);
    } else if (key === "dates") {
      setFilteredDate(value?.value ?? value ?? null);
    } else if (key === "sector") {
      setSelectedSector(value ?? null);
    } else if (key === "industry") {
      setSelectedIndustry(value ?? null);
    } else if (key === "mktCategory") {
      setSelectedMktCategory(value ?? null);
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

      <button
        onClick={() => setIsFilterOpen(!isFilterOpen)}
        className="fixed top-4 left-4 z-50 bg-blue-600 text-white px-3 py-2 rounded hover:bg-blue-700 shadow"
      >
        {isFilterOpen ? "Hide Filters" : "Show Filters"}
      </button>

      <FilterPanel
        isOpen={isFilterOpen}
        onClose={() => setIsFilterOpen(false)}
        onFilterChange={handleFilterChange}
      />

      <main className={`transition-all duration-300 ${isFilterOpen ? "ml-80" : "ml-0"} p-6`}>
        <h1 className="text-2xl font-bold text-gray-800">Investment Insights</h1>
        <p className="text-sm text-gray-500">
          Current: {data.currDate} | Previous: {data.prevDate}
          {filteredDate && <span className="ml-2 text-gray-400">Filtered: {filteredDate}</span>}
          {selectedSector && <span className="ml-2 text-gray-400"> • Sector: {selectedSector.label}</span>}
          {selectedIndustry && <span className="ml-2 text-gray-400"> • Industry: {selectedIndustry.label}</span>}
          {selectedMktCategory && <span className="ml-2 text-gray-400"> • Market Cap: {selectedMktCategory.label}</span>}
        </p>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-6">
          {data.stocks.map((stock) => (
            <div key={stock.stockId} className="bg-white shadow rounded-xl p-4 border-l-4 border-blue-500">
              <div className="flex justify-between items-center mb-2">
                <h2 className="text-lg font-semibold">
                  {stock.symbol} - {stock.company}
                </h2>
                <span className="text-sm font-bold">Score: {stock.score}</span>
              </div>

              <div className="text-sm text-gray-600 mb-2">
                <p>Market Cap Category: {stock.marketCapCategory}</p>
                <p>
                  Quantity: {stock.quantityPrev} → {stock.quantityCurr} ({stock.quantityChangePct?.toFixed(1)}%)
                </p>
                <p>
                  Valuation: {stock.valuePrev?.toFixed(2)} → {stock.valueCurr?.toFixed(2)} ({stock.valueChangePct?.toFixed(1)}%)
                </p>
                <p>
                  Net Asset %: {stock.netAssetPctPrev?.toFixed(2)} → {stock.netAssetPctCurr?.toFixed(2)} ({stock.weightChangePct?.toFixed(2)}%)
                </p>
              </div>

              {renderSignals(stock.signals)}

              {/* Bar chart for Value Change % */}
              <ResponsiveContainer width="100%" height={150}>
                <BarChart data={[stock]}>
                  <XAxis dataKey="symbol" hide />
                  <YAxis />
                  <Tooltip />
                  <Bar dataKey="valueChangePct" fill="#2196F3" name="Value % Change" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          ))}
        </div>
      </main>
    </div>
  );
};

export default InvestmentInsights;
