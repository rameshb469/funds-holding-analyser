import React, { useState, useEffect } from 'react';
import Select from 'react-select';
import { X } from 'lucide-react';

const FilterPanel = ({ isOpen, onClose, onFilterChange }) => {
  const [filters, setFilters] = useState({
    sector: [],
    industry: [],
    fundType: [],
    mfName: [],
    stockInfo: [],
    dates: [],
    mktCategory: []
  });

  const [selectedSector, setSelectedSector] = useState(null);
  const [selectedIndustry, setSelectedIndustry] = useState(null);
  const [selectedFundType, setSelectedFundType] = useState(null);
  const [selectedMfName, setSelectedMfName] = useState(null);
  const [selectedStock, setSelectedStock] = useState(null);
  const [selectedDate, setSelectedDate] = useState(null); // local state only
  const [selectedMktCategory, setSelectedMktCategory] = useState(null); // local state for market cap

  const [industryOptions, setIndustryOptions] = useState([]);
  const [fundNameOptions, setFundNameOptions] = useState([]);
  const [stockOptions, setStockOptions] = useState([]);
  const [dateOptions, setDateOptions] = useState([]);
  const [mktCategoryOptions, setMktCategoryOptions] = useState([]);

  // Fetch filters from API
  useEffect(() => {
    const fetchFilters = async () => {
      try {
        const res = await fetch("http://localhost:8080/api/mf-filters");
        const data = await res.json();

        const stocks = data.stockInfo?.map(st => ({
          value: st.id,
          label: `${st.name} (${st.description})`,
          name: st.name,
          symbol: st.description,
          metaInfo: st.metaInfo
        })) || [];

        const dates = data.dates?.map(d => ({ value: d, label: d })) || [];

        // map market cap categories from API
        const mktCats = data.marketCapCategories?.map(c => ({
          value: c.id,
          label: c.name,
          metaInfo: c.metaInfo
        })) || [];

        setFilters({
          sector: data.sectors?.map(s => ({ value: s.id, label: s.description })) || [],
          industry: data.industry?.map(i => ({ value: i.id, label: i.description, metaInfo: i.metaInfo })) || [],
          fundType: data.fundTypes?.map(f => ({ value: f.id, label: f.description })) || [],
          mfName: data.fundNames?.map(m => ({ value: m.id, label: m.name, metaInfo: m.metaInfo })) || [],
          stockInfo: stocks,
          dates: dates,
          mktCategory: mktCats
        });

        setStockOptions(stocks); // default all stocks
        setDateOptions(dates);
        setMktCategoryOptions(mktCats);

        // Default first date selection (local only)
        if (dates.length > 0) {
          setSelectedDate(dates[0]);
          // do not propagate to parent now; Apply will send the selection
        }
      } catch (err) {
        console.error("Failed to fetch filters", err);
      }
    };

    fetchFilters();
  }, []);

  // Update industries & stocks when sector changes (local)
  useEffect(() => {
    if (selectedSector) {
      setIndustryOptions(
        filters.industry.filter(i => String(i.metaInfo?.sector) === String(selectedSector.value))
      );
      setStockOptions(
        filters.stockInfo.filter(st => String(st.metaInfo?.sector) === String(selectedSector.value))
      );
    } else {
      setIndustryOptions([]);
      setStockOptions(filters.stockInfo); // fallback: all stocks
    }
    setSelectedIndustry(null);
    setSelectedStock(null);
  }, [selectedSector, filters.industry, filters.stockInfo]);

  // Update stocks when industry changes (local)
  useEffect(() => {
    if (selectedIndustry) {
      setStockOptions(
        filters.stockInfo.filter(st => String(st.metaInfo?.industry) === String(selectedIndustry.value))
      );
    } else if (selectedSector) {
      setStockOptions(
        filters.stockInfo.filter(st => String(st.metaInfo?.sector) === String(selectedSector.value))
      );
    } else {
      setStockOptions(filters.stockInfo); // fallback: all stocks
    }
    setSelectedStock(null);
  }, [selectedIndustry, selectedSector, filters.stockInfo]);

  // Update fund names when fundType changes (local)
  useEffect(() => {
    if (selectedFundType) {
      setFundNameOptions(
        filters.mfName.filter(f => String(f.metaInfo?.type) === String(selectedFundType.value))
      );
    } else {
      setFundNameOptions([]);
    }
    setSelectedMfName(null);
  }, [selectedFundType, filters.mfName]);

  // Only propagate when user clicks Apply
  const handleApply = () => {
    onFilterChange('apply', {
      dates: selectedDate,
      sector: selectedSector,
      industry: selectedIndustry,
      fundType: selectedFundType,
      mfName: selectedMfName,
      stockInfo: selectedStock,
      mktCategory: selectedMktCategory
    });
  };

  // Clear only local selections; parent will be updated when user clicks Apply
  const handleClearAll = () => {
    setSelectedDate(dateOptions.length > 0 ? dateOptions[0] : null);
    setSelectedSector(null);
    setSelectedIndustry(null);
    setSelectedFundType(null);
    setSelectedMfName(null);
    setSelectedStock(null);
    setSelectedMktCategory(null);
    setIndustryOptions([]);
    setFundNameOptions([]);
    setStockOptions(filters.stockInfo);
  };

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

        {/* Date Dropdown (local) */}
        <div>
          <label className="block font-medium text-gray-700 mb-1">Date</label>
         <Select
           options={dateOptions}
           value={selectedDate}
           onChange={(v) => {
             setSelectedDate(v);
           }}
           className="text-sm"
         />
        </div>

        {/* Sector */}
        <div>
          <label className="block font-medium text-gray-700 mb-1">Sector</label>
          <Select
            options={filters.sector}
            value={selectedSector}
            onChange={(v) => {
              setSelectedSector(v);
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
            }}
            className="text-sm"
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
            }}
            className="text-sm"
          />
        </div>

        {/* Market Cap Category (local) */}
        <div>
          <label className="block font-medium text-gray-700 mb-1">Market Cap Category</label>
          <Select
            options={mktCategoryOptions}
            value={selectedMktCategory}
            onChange={(v) => {
              setSelectedMktCategory(v);
            }}
            className="text-sm"
          />
        </div>

        {/* Mutual Fund Name */}
        <div>
          <label className="block font-medium text-gray-700 mb-1">Mutual Fund Name</label>
          <Select
            options={fundNameOptions}
            value={selectedMfName}
            onChange={(v) => {
              setSelectedMfName(v);
            }}
            className="text-sm"
          />
        </div>

        {/* Stock Info */}
        <div>
          <label className="block font-medium text-gray-700 mb-1">Stock</label>
          <Select
            options={stockOptions}
            value={selectedStock}
            onChange={(v) => {
              setSelectedStock(v);
            }}
            className="text-sm"
          />
        </div>

        {/* Buttons */}
        <div className="flex justify-between pt-4 border-t mt-2">
          <button
            onClick={handleApply}
            className="bg-blue-600 text-white text-sm px-4 py-2 rounded-lg hover:bg-blue-700 transition"
          >
            Apply
          </button>
          <button
            onClick={handleClearAll}
            className="text-sm text-gray-600 hover:text-red-500 transition"
          >
            Clear All
          </button>
        </div>
      </div>
    </div>
  );
};

export default FilterPanel;
