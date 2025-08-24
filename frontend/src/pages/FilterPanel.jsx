import React, { useState, useEffect } from 'react';
import Select from 'react-select';
import { X } from 'lucide-react';

const FilterSidebar = ({ isOpen, onClose, onFilterChange }) => {
  const [filters, setFilters] = useState({
    sector: [],
    industry: [],
    fundType: [],
    mfName: [],
    stockInfo: []
  });

  const [selectedSector, setSelectedSector] = useState(null);
  const [selectedIndustry, setSelectedIndustry] = useState(null);
  const [selectedFundType, setSelectedFundType] = useState(null);
  const [selectedMfName, setSelectedMfName] = useState(null);
  const [selectedStock, setSelectedStock] = useState(null);

  const [industryOptions, setIndustryOptions] = useState([]);
  const [fundNameOptions, setFundNameOptions] = useState([]);

  // Fetch filters from API
  useEffect(() => {
    const fetchFilters = async () => {
      try {
        const res = await fetch("http://localhost:8080/api/mf-filters");
        const data = await res.json();

        setFilters({
          sector: data.sectors?.map(s => ({ value: s.id, label: s.description })) || [],
          industry: data.industry?.map(i => ({ value: i.id, label: i.description, metaInfo: i.metaInfo })) || [],
          fundType: data.fundTypes?.map(f => ({ value: f.id, label: f.description })) || [],
          mfName: data.fundNames?.map(m => ({ value: m.id, label: m.name, metaInfo: m.metaInfo })) || [],
          stockInfo: data.stockInfo?.map(st => ({
            value: st.id,
            label: `${st.name} (${st.description})`, // name + symbol
            name: st.name,
            symbol: st.description
          })) || []
        });
      } catch (err) {
        console.error("Failed to fetch filters", err);
      }
    };

    fetchFilters();
  }, []);

  // Update industries when sector changes
  useEffect(() => {
    if (selectedSector) {
      setIndustryOptions(
        filters.industry.filter(i => i.metaInfo?.sector === selectedSector.value)
      );
    } else {
      setIndustryOptions([]);
    }
    setSelectedIndustry(null); // reset industry when sector changes
  }, [selectedSector, filters.industry]);

  // Update fund names when fundType changes
  useEffect(() => {
    if (selectedFundType) {
      setFundNameOptions(
        filters.mfName.filter(f => f.metaInfo?.type === selectedFundType.value)
      );
    } else {
      setFundNameOptions([]);
    }
    setSelectedMfName(null); // reset mfName when fundType changes
  }, [selectedFundType, filters.mfName]);

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
        {/* Sector */}
        <div>
          <label className="block font-medium text-gray-700 mb-1">Sector</label>
          <Select
            options={filters.sector}
            value={selectedSector}
            onChange={(v) => {
              setSelectedSector(v);
              onFilterChange('sector', v);
            }}
            className="text-sm"
          />
        </div>

        {/* Industry (depends on Sector) */}
        <div>
          <label className="block font-medium text-gray-700 mb-1">Industry</label>
          <Select
            options={industryOptions}
            value={selectedIndustry}
            onChange={(v) => {
              setSelectedIndustry(v);
              onFilterChange('industry', v);
            }}
            className="text-sm"
            isDisabled={!selectedSector}
          />
        </div>

        {/* Fund Type */}
        <div>
          <label className="block font-medium text-gray-700 mb-1">Fund Type</label>
          <Select
            options={filters.fundType}
            value={selectedFundType}
            onChange={(v) => {
              setSelectedFundType(v);
              onFilterChange('fundType', v);
            }}
            className="text-sm"
          />
        </div>

        {/* Mutual Fund Name (depends on Fund Type) */}
        <div>
          <label className="block font-medium text-gray-700 mb-1">Mutual Fund Name</label>
          <Select
            options={fundNameOptions}
            value={selectedMfName}
            onChange={(v) => {
              setSelectedMfName(v);
              onFilterChange('mfName', v);
            }}
            className="text-sm"
            isDisabled={!selectedFundType}
          />
        </div>

        {/* Stock Info */}
        <div>
          <label className="block font-medium text-gray-700 mb-1">Stock</label>
          <Select
            options={filters.stockInfo}
            value={selectedStock}
            onChange={(v) => {
              setSelectedStock(v);
              onFilterChange('stockInfo', v);
            }}
            className="text-sm"
          />
        </div>

        {/* Buttons */}
        <div className="flex justify-between pt-4 border-t mt-2">
          <button
            onClick={() => onFilterChange('apply')}
            className="bg-blue-600 text-white text-sm px-4 py-2 rounded-lg hover:bg-blue-700 transition"
          >
            Apply
          </button>
          <button
            onClick={() => {
              setSelectedSector(null);
              setSelectedIndustry(null);
              setSelectedFundType(null);
              setSelectedMfName(null);
              setSelectedStock(null);
              setIndustryOptions([]);
              setFundNameOptions([]);
              onFilterChange('clear');
            }}
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
