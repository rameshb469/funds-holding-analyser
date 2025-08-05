import React from 'react';
import Select from 'react-select';
import { X } from 'lucide-react';

const FilterSidebar = ({ isOpen, onClose, filters, onFilterChange }) => {
  return (
    <div
      className={`fixed top-0 left-0 h-full w-80 bg-white shadow-lg z-50 transform transition-transform duration-300 ${
        isOpen ? 'translate-x-0' : '-translate-x-full'
      }`}
    >
      {/* Header */}
      <div className="flex justify-between items-center p-4 border-b">
        <h2 className="text-lg font-semibold text-gray-800">🔍 Filter Mutual Funds</h2>
        <button onClick={onClose} className="text-gray-600 hover:text-red-500">
          <X size={20} />
        </button>
      </div>

      {/* Filters */}
      <div className="p-4 space-y-4 text-sm overflow-y-auto h-[calc(100%-60px)]">
        {[
          ['Sector', 'sector'],
          ['Industry', 'industry'],
          ['Fund Type', 'fundType'],
          ['Stock Name', 'stockName'],
          ['Symbol', 'symbol'],
          ['Mutual Fund Name', 'mfName'],
        ].map(([label, key]) => (
          <div key={key}>
            <label className="block font-medium text-gray-700 mb-1">{label}</label>
            <Select
              options={filters[key]}
              onChange={(v) => onFilterChange(key, v)}
              className="text-sm"
            />
          </div>
        ))}

        <div className="flex justify-between pt-4 border-t mt-2">
          <button
            onClick={() => onFilterChange('apply')}
            className="bg-blue-600 text-white text-sm px-4 py-2 rounded-lg hover:bg-blue-700 transition"
          >
            Apply
          </button>
          <button
            onClick={() => onFilterChange('clear')}
            className="text-sm text-gray-600 hover:text-red-500 transition"
          >
            Clear All
          </button>
        </div>
      </div>
    </div>
  );
};

export default FilterSidebar;
