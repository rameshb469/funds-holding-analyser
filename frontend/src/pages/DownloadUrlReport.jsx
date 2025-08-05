
import React, { useState } from 'react';
import FilterPanel from './FilterPanel';
import MetricCard from './MetricCard'
import MFDownloadsTable from './MFDownloadsTable';
import Header from '../components/Header';

const DownloadUrlReport = () => {
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
          <MetricCard title="Stock Quantity" value="104M" />
          <MetricCard title="Q Change" value="5.97" />
          <MetricCard title="Avg Price Change" value="2.08k" />
          <MetricCard title="Mutual Funds" value="10" />
        </section>

        <section className="w-full bg-white shadow rounded-xl p-4">
          <MFDownloadsTable />
        </section>
      </main>
    </div>
  );
};

export default DownloadUrlReport
