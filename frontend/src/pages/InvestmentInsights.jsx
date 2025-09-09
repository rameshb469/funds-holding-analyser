import React, { useEffect, useState } from "react";
import Header from '../components/Header';
import FilterPanel from "./FilterPanel"; // ✅ Import FilterPanel
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

const CATEGORY_COLORS = {
  stable: "#4CAF50",
  growth: "#2196F3",
  avoid: "#F44336",
  new: "#9C27B0",
};

const CAP_COLORS = {
  LargeCap: "#0088FE",
  MidCap: "#00C49F",
  SmallCap: "#FFBB28",
};

// ✅ Helper to combine cap + category
const getCapCategoryKey = (cap, type) => `${cap}-${type}`;

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

  const { stable, growth, avoid, newlyAdded } = data;

  // ✅ Gather all stocks grouped by Cap + Category
  const allStocks = [
    ...Object.entries(stable).flatMap(([cap, items]) =>
      items.map((s) => ({ ...s, cap, type: "stable" }))
    ),
    ...Object.entries(growth).flatMap(([cap, items]) =>
      items.map((s) => ({ ...s, cap, type: "growth" }))
    ),
    ...Object.entries(avoid).flatMap(([cap, items]) =>
      items.map((s) => ({ ...s, cap, type: "avoid" }))
    ),
    ...Object.entries(newlyAdded).flatMap(([cap, items]) =>
      items.map((s) => ({ ...s, cap, type: "new" }))
    ),
  ];

  const capCategoryDistribution = allStocks.reduce((acc, stock) => {
    const key = getCapCategoryKey(stock.cap, stock.type);
    acc[key] = (acc[key] || 0) + 1;
    return acc;
  }, {});

  const capCategoryData = Object.entries(capCategoryDistribution).map(
    ([name, value]) => ({
      name,
      value,
    })
  );

  const renderStocks = (stocks, type) => (
    <div className="space-y-1">
      {Object.entries(stocks).map(([cap, items]) => (
        <div key={cap} className="mb-4">
          <h4 className="text-sm font-medium text-gray-500">{cap}</h4>
          <ul className="list-disc list-inside">
            {items.map((s, i) => (
              <li key={i} className="font-semibold">
                <span style={{ color: CATEGORY_COLORS[type] }}>
                  {s.symbol}
                </span>{" "}
                - {s.company}
              </li>
            ))}
          </ul>

          {/* Chart for each cap group */}
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={items}>
              <XAxis dataKey="symbol" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Bar
                dataKey="valueChangePct"
                fill={CATEGORY_COLORS[type]}
                name="Exposure % Change"
              />
            </BarChart>
          </ResponsiveContainer>
        </div>
      ))}
    </div>
  );

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

      {/* Sidebar Filter */}
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
        <h1 className="text-2xl font-bold text-gray-800">
          Investment Insights
        </h1>
        <p className="text-sm text-gray-500">
          Current: {data.currDate} | Previous: {data.prevDate}
          {filteredDate && (
            <span className="ml-2 text-gray-400">Filtered: {filteredDate}</span>
          )}
        </p>

        {/* 🔥 Market Cap + Category Distribution */}
        <div className="bg-white shadow rounded-xl p-4">
          <h2 className="text-lg font-semibold text-gray-700 mb-2">
            Market Cap & Category Distribution
          </h2>
          <ResponsiveContainer width="100%" height={350}>
            <PieChart>
              <Pie
                data={capCategoryData}
                dataKey="value"
                nameKey="name"
                outerRadius={140}
                label={({ name, value }) => `${name} (${value})`}
              >
                {capCategoryData.map((entry, i) => {
                  const [cap, type] = entry.name.split("-");
                  const typeColor = CATEGORY_COLORS[type] || "#ccc";
                  return <Cell key={i} fill={typeColor} />;
                })}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>

        {/* Detailed Cards */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="bg-white shadow rounded-xl p-4 border-l-4 border-green-500">
            <h2 className="text-lg font-semibold mb-2">Stable</h2>
            {renderStocks(stable, "stable")}
          </div>

          <div className="bg-white shadow rounded-xl p-4 border-l-4 border-blue-500">
            <h2 className="text-lg font-semibold mb-2">Growth</h2>
            {renderStocks(growth, "growth")}
          </div>

          <div className="bg-white shadow rounded-xl p-4 border-l-4 border-red-500">
            <h2 className="text-lg font-semibold mb-2">Avoid</h2>
            {renderStocks(avoid, "avoid")}
          </div>

          <div className="bg-white shadow rounded-xl p-4 border-l-4 border-purple-500">
            <h2 className="text-lg font-semibold mb-2">Newly Added</h2>
            {renderStocks(newlyAdded, "new")}
          </div>
        </div>
      </main>
    </div>
  );
};

export default InvestmentInsights;
