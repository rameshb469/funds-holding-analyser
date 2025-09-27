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

  useEffect(() => {
    const url = filteredDate
      ? `http://localhost:8080/api/investment-insights?date=${filteredDate}`
      : "http://localhost:8080/api/investment-insights";

    fetch(url)
      .then((res) => res.json())
      .then(setData)
      .catch(console.error);
  }, [filteredDate]);

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
        onFilterChange={(key, value) =>
          key === "apply" && value?.dates
            ? setFilteredDate(value?.dates.value || null)
            : setFilteredDate(null)
        }
      />

      <main className={`transition-all duration-300 ${isFilterOpen ? "ml-80" : "ml-0"} p-6`}>
        <h1 className="text-2xl font-bold text-gray-800">Investment Insights</h1>
        <p className="text-sm text-gray-500">
          Current: {data.currDate} | Previous: {data.prevDate}
          {filteredDate && <span className="ml-2 text-gray-400">Filtered: {filteredDate}</span>}
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
