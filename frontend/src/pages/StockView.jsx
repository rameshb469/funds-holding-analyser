import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import FilterPanel from './FilterPanel';
import MetricCard from './MetricCard';
import Charts from './Charts';
import Header from '../components/Header';

const StockView = () => {
  const { stockId = "1" } = useParams();
  const navigate = useNavigate();
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [data, setData] = useState(null);

  useEffect(() => {
    fetch(`http://localhost:8080/api/stock-holdings/${stockId}/metrics`)
      .then(res => res.json())
      .then(setData)
      .catch(console.error);
  }, [stockId]);

  if (!data) return <div className="p-6">Loading...</div>;

  const filters = {
    sector: [],
    industry: [],
    fundType: [],
    stockName: [],
    symbol: [],
    mfName: [],
  };

const handleFilterChange = (key, value) => {
  if (key === "apply" && value?.stockInfo) {
    // navigate to new stock page
    navigate(`/stocks/${value.stockInfo.value}`);
  } else if (key === "clear") {
    console.log("Filters cleared");
  } else {
    console.log("Filter changed:", key, value);
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
        {isFilterOpen ? 'Hide Filters' : 'Show Filters'}
      </button>

      {/* Sidebar */}
      <FilterPanel
        isOpen={isFilterOpen}
        onClose={() => setIsFilterOpen(false)}
        filters={filters}
        onFilterChange={handleFilterChange}
      />

      {/* Main Content */}
      <main className={`transition-all duration-300 ${isFilterOpen ? 'ml-80' : 'ml-0'} p-6 space-y-6`}>

        {/* Stock Header */}
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-bold text-gray-800">
            {data.stockName} <span className="text-gray-500 text-lg">({data.symbol})</span>
          </h1>
          <span className="text-sm text-gray-400">Stock ID: {stockId}</span>
        </div>

        {/* Metrics */}
        <section className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
          <MetricCard title="Stock Quantity" value={data.totalQuantity.toLocaleString()} change={data.quantityChange}/>
          <MetricCard title="Avg Price" value={data.avgPrice.toFixed(2)} change={data.avgPriceChange}/>
          <MetricCard title="Mutual Funds" value={data.totalMfCount} change={data.totalMfCountChange}/>
          <MetricCard title="Valuation" value={data.valuation.toFixed(2)} change={data.valuationChange}/>
        </section>

        {/* Charts */}
        <Charts
          quantityChangeByDates={data.quantityChangeByDates}
          mfDistribution={data.mfDistribution}
          valuationChangeByDates={data.valuationChangeByDates}
        />
      </main>

    </div>
  );
};

export default StockView;
