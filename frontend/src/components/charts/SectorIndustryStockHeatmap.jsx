import React, { useState, useMemo } from "react";
import { ResponsiveCalendar } from "@nivo/calendar";
import { format, parseISO, isAfter, isBefore } from "date-fns";

const SECTORS = ["Banking", "IT", "Pharma"];
const INDUSTRIES = ["Large Cap", "Mid Cap", "Small Cap"];
const STOCKS = ["HDFC Bank", "TCS", "Dr. Reddy", "Infosys", "SBI"];

const allData = [...Array(300)].map((_, idx) => {
  const date = new Date(2023, 0, 1);
  date.setDate(date.getDate() + idx);

  return {
    day: format(date, "yyyy-MM-dd"),
    value: Math.floor(Math.random() * 100),
    sector: SECTORS[Math.floor(Math.random() * SECTORS.length)],
    industry: INDUSTRIES[Math.floor(Math.random() * INDUSTRIES.length)],
    stock: STOCKS[Math.floor(Math.random() * STOCKS.length)],
  };
});

const SectorIndustryStockHeatmap = () => {
  const [selectedSector, setSelectedSector] = useState("");
  const [selectedIndustry, setSelectedIndustry] = useState("");
  const [selectedStock, setSelectedStock] = useState("");
  const [fromDate, setFromDate] = useState("2023-01-01");
  const [toDate, setToDate] = useState("2025-01-01");

  const filtered = useMemo(() => {
    return allData.filter((d) => {
      const date = parseISO(d.day);
      const from = parseISO(fromDate);
      const to = parseISO(toDate);

      return (
        (!selectedSector || d.sector === selectedSector) &&
        (!selectedIndustry || d.industry === selectedIndustry) &&
        (!selectedStock || d.stock === selectedStock) &&
        isAfter(date, from) &&
        isBefore(date, to)
      );
    });
  }, [selectedSector, selectedIndustry, selectedStock, fromDate, toDate]);

  return (
    <div className="space-y-4">
      {/* Filters */}
      <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
        <select
          className="p-2 border rounded"
          value={selectedSector}
          onChange={(e) => setSelectedSector(e.target.value)}
        >
          <option value="">All Sectors</option>
          {SECTORS.map((s) => (
            <option key={s}>{s}</option>
          ))}
        </select>
        <select
          className="p-2 border rounded"
          value={selectedIndustry}
          onChange={(e) => setSelectedIndustry(e.target.value)}
        >
          <option value="">All Industries</option>
          {INDUSTRIES.map((i) => (
            <option key={i}>{i}</option>
          ))}
        </select>
        <select
          className="p-2 border rounded"
          value={selectedStock}
          onChange={(e) => setSelectedStock(e.target.value)}
        >
          <option value="">All Stocks</option>
          {STOCKS.map((s) => (
            <option key={s}>{s}</option>
          ))}
        </select>
        <input
          type="month"
          className="p-2 border rounded"
          value={fromDate.slice(0, 7)}
          onChange={(e) => setFromDate(`${e.target.value}-01`)}
        />
        <input
          type="month"
          className="p-2 border rounded"
          value={toDate.slice(0, 7)}
          onChange={(e) => {
            const lastDay = new Date(e.target.value + "-01");
            lastDay.setMonth(lastDay.getMonth() + 1);
            lastDay.setDate(0); // last date of month
            setToDate(format(lastDay, "yyyy-MM-dd"));
          }}
        />
      </div>

      {/* Heatmap */}
      <div className="h-[250px]">
        <ResponsiveCalendar
          data={filtered}
          from={fromDate}
          to={toDate}
          emptyColor="#eeeeee"
          colors={["#c6e48b", "#7bc96f", "#239a3b", "#196127"]}
          margin={{ top: 30, right: 40, bottom: 30, left: 40 }}
          yearSpacing={30}
          monthBorderColor="#ffffff"
          dayBorderWidth={2}
          dayBorderColor="#ffffff"
          tooltip={({ day, value }) => `${day}: ${value} units`}
        />
      </div>
    </div>
  );
}

export default SectorIndustryStockHeatmap;
