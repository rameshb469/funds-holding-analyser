import React, { useEffect, useMemo, useState } from "react";

const MutualFundConfigTable = () => {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);

  // filters
  const [houseFilter, setHouseFilter] = useState("ALL");
  const [fundFilter, setFundFilter] = useState("");
  const [typeFilter, setTypeFilter] = useState("ALL");

  // pagination
  const [pageSize, setPageSize] = useState(10);
  const [currentPage, setCurrentPage] = useState(1);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await fetch("http://localhost:8080/api/mf-configs");
        const data = await res.json();
        const mapped = Array.isArray(data) ? data.map(item => ({
          id: item.id,
          mfHouseName: item.mfHouseName ?? "-",
          mutualFundName: item.mutualFundName ?? "-",
          mfTypeName: item.mfTypeName ?? "-",
          configUrl: item.configUrl,
          sheetName: item.sheetName ?? "",
          dateMapper1: item.dateMapper1,
          dateMapper2: item.dateMapper2,
          dateMapper3: item.dateMapper3,
          dateMapper4: item.dateMapper4,
          successfulCount: item.successfulCount,
          failedCount: item.failedCount
        })) : [];
        setRows(mapped);
      } catch (err) {
        console.error("Error fetching configs:", err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  // unique filter options
  const houseOptions = useMemo(() => ["ALL", ...new Set(rows.map(r => r.mfHouseName).filter(Boolean))], [rows]);
  const typeOptions = useMemo(() => ["ALL", ...new Set(rows.map(r => r.mfTypeName).filter(Boolean))], [rows]);

  // filtering
  const filteredRows = useMemo(() => {
    return rows.filter(r => {
      const byHouse = houseFilter === "ALL" || r.mfHouseName === houseFilter;
      const byFund = !fundFilter || r.mutualFundName.toLowerCase().includes(fundFilter.toLowerCase());
      const byType = typeFilter === "ALL" || r.mfTypeName === typeFilter;
      return byHouse && byFund && byType;
    });
  }, [rows, houseFilter, fundFilter, typeFilter]);

  // pagination
  const totalPages = Math.ceil(filteredRows.length / pageSize) || 1;
  const pagedRows = useMemo(() => {
    const start = (currentPage - 1) * pageSize;
    return filteredRows.slice(start, start + pageSize);
  }, [filteredRows, currentPage, pageSize]);

  useEffect(() => { setCurrentPage(1); }, [pageSize, houseFilter, fundFilter, typeFilter]);

  if (loading) {
    return <div className="p-6 text-gray-500">Loading...</div>;
  }

  return (
    <div className="relative p-4 space-y-4">
      {/* Filters */}
      <div className="flex flex-wrap gap-3 items-end">
        <div>
          <label className="block text-xs text-gray-600 mb-1">Fund House</label>
          <select
            value={houseFilter}
            onChange={(e) => setHouseFilter(e.target.value)}
            className="border rounded px-3 py-2 text-sm"
          >
            {houseOptions.map(opt => <option key={opt} value={opt}>{opt}</option>)}
          </select>
        </div>

        <div>
          <label className="block text-xs text-gray-600 mb-1">Fund Name</label>
          <input
            type="text"
            value={fundFilter}
            onChange={(e) => setFundFilter(e.target.value)}
            placeholder="Search Fund Name"
            className="border rounded px-3 py-2 text-sm"
          />
        </div>

        <div>
          <label className="block text-xs text-gray-600 mb-1">Type</label>
          <select
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
            className="border rounded px-3 py-2 text-sm"
          >
            {typeOptions.map(opt => <option key={opt} value={opt}>{opt}</option>)}
          </select>
        </div>
      </div>

      {/* Table */}
      <div className="overflow-x-auto bg-white rounded shadow border">
        <table className="min-w-full text-sm">
          <thead className="bg-gray-100">
            <tr>
              <th className="p-2 border">Mutual Fund House</th>
              <th className="p-2 border">Mutual Fund Name</th>
              <th className="p-2 border">MF Type</th>
              <th className="p-2 border">Config URL</th>
              <th className="p-2 border">Sheet Name</th>
              <th className="p-2 border">Date Mapper 1</th>
              <th className="p-2 border">Date Mapper 2</th>
              <th className="p-2 border">Date Mapper 3</th>
              <th className="p-2 border">Date Mapper 4</th>
              <th className="p-2 border">Success Count</th>
              <th className="p-2 border">Failed Count</th>
            </tr>
          </thead>
          <tbody>
            {pagedRows.map(r => (
              <tr key={r.id} className="hover:bg-gray-50">
                <td className="p-2 border">{r.mfHouseName}</td>
                <td className="p-2 border">{r.mutualFundName}</td>
                <td className="p-2 border">{r.mfTypeName}</td>
                <td className="p-2 border">{r.configUrl} </td>
                <td className="p-2 border">{r.sheetName}</td>
                <td className="p-2 border">{r.dateMapper1}</td>
                <td className="p-2 border">{r.dateMapper2}</td>
                <td className="p-2 border">{r.dateMapper3}</td>
                <td className="p-2 border">{r.dateMapper4}</td>
                <td className="p-2 border text-green-600">{r.successfulCount}</td>
                <td className="p-2 border text-red-500">{r.failedCount}</td>
              </tr>
            ))}
            {pagedRows.length === 0 && (
              <tr>
                <td className="p-4 text-center text-gray-500 italic" colSpan={11}>
                  No results with current filters.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      <div className="flex justify-between items-center mt-3">
        <div className="flex items-center gap-2 text-sm">
          <span>Rows per page:</span>
          <select
            className="border rounded px-2 py-1 text-sm"
            value={pageSize}
            onChange={(e) => setPageSize(Number(e.target.value))}
          >
            {[5, 10, 20, 50].map(size => (
              <option key={size} value={size}>{size}</option>
            ))}
          </select>
        </div>
        <div className="flex items-center gap-2 text-sm">
          <button
            className="px-2 py-1 border rounded disabled:opacity-50"
            disabled={currentPage === 1}
            onClick={() => setCurrentPage(p => Math.max(p - 1, 1))}
          >
            Prev
          </button>
          <span>Page {currentPage} of {totalPages}</span>
          <button
            className="px-2 py-1 border rounded disabled:opacity-50"
            disabled={currentPage === totalPages}
            onClick={() => setCurrentPage(p => Math.min(p + 1, totalPages))}
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
};

export default MutualFundConfigTable;
