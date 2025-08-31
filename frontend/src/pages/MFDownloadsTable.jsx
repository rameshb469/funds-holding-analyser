import React, { useMemo, useState, useEffect } from "react";

function formatDMY(s) { if (!s) return ""; const [y,m,d]=s.split("-"); return `${d}-${m}-${y}`; }
function isValidUrl(s){ try{const u=new URL(s); return u.protocol==="http:"||u.protocol==="https:";}catch{return false;} }
function cn(...c){ return c.filter(Boolean).join(" "); }

const MFDownloadsTable = () => {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedIds, setSelectedIds] = useState([]);
  const [accordionOpen, setAccordionOpen] = useState(false);
  const [editMap, setEditMap] = useState({});
  const [applyAllValue, setApplyAllValue] = useState("");
  const [sortConfig, setSortConfig] = useState({ key: "date", direction: "desc" });

  // pagination
  const [pageSize, setPageSize] = useState(10);
  const [currentPage, setCurrentPage] = useState(1);

  // filters
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [fundFilter, setFundFilter] = useState("");

  // Fetch API
  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await fetch("http://localhost:8080/api/mf-extractor-info");
        const data = await res.json();
        const mapped = data.map(item => ({
          id: item.id,
          fund: item.mutualFundName,
          mutualFundId: item.mutualFundId,
          url: item.url,
          date: item.atDate,
          status: item.status,
          error: item.error,
          count: item.recordCount ?? 0,
        }));
        setRows(mapped);
      } catch (e) {
        console.error("Failed to fetch:", e);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  // Summary stats
  const summary = useMemo(() => {
    const uniqueFunds = new Set(rows.map(r => r.mutualFundId)).size;
    const totalStocks = rows.length;
    const successCount = rows.filter(r => r.status === "SUCCESSFUL").length;
    const failedCount = rows.filter(r => r.status === "FAILED").length;
    return { uniqueFunds, totalStocks, successCount, failedCount };
  }, [rows]);

  const statusOptions = useMemo(() => {
    const unique = Array.from(new Set(rows.map(r => r.status))).filter(Boolean).sort();
    return ["ALL", ...unique];
  }, [rows]);

  // Filtering
  const filteredRows = useMemo(() => {
    return rows.filter((r) => {
      const byStatus = statusFilter === "ALL" || r.status === statusFilter;
      const byFund = !fundFilter || r.fund.toLowerCase().includes(fundFilter.trim().toLowerCase());
      return byStatus && byFund;
    });
  }, [rows, statusFilter, fundFilter]);

  // Sorting
  const sortedRows = useMemo(() => {
    if (!sortConfig.key) return filteredRows;
    const sorted = [...filteredRows].sort((a, b) => {
      const valA = a[sortConfig.key];
      const valB = b[sortConfig.key];
      if (valA < valB) return sortConfig.direction === "asc" ? -1 : 1;
      if (valA > valB) return sortConfig.direction === "asc" ? 1 : -1;
      return 0;
    });
    return sorted;
  }, [filteredRows, sortConfig]);

  const requestSort = (key) => {
    setSortConfig((prev) => {
      if (prev.key === key) {
        return { key, direction: prev.direction === "asc" ? "desc" : "asc" };
      }
      return { key, direction: "asc" };
    });
  };

  // Pagination logic
  const totalPages = Math.ceil(sortedRows.length / pageSize) || 1;
  const pagedRows = useMemo(() => {
    const start = (currentPage - 1) * pageSize;
    return sortedRows.slice(start, start + pageSize);
  }, [sortedRows, currentPage, pageSize]);

  useEffect(() => { setCurrentPage(1); }, [pageSize, statusFilter, fundFilter]);

  // Selection logic
  const selectableVisible = pagedRows.filter(r => r.status !== "SUCCESSFUL");
  const allVisibleSelected =
    selectableVisible.length > 0 &&
    selectableVisible.every((r) => selectedIds.includes(r.id));

  const toggleSelectOne = (id) => {
    const row = rows.find(r => r.id === id);
    if (!row || row.status === "SUCCESSFUL") return;
    setSelectedIds(prev => prev.includes(id) ? prev.filter(x=>x!==id) : [...prev,id]);
  };

  const toggleSelectAllVisible = () => {
    if (allVisibleSelected) {
      const visibleIds = new Set(selectableVisible.map(r=>r.id));
      setSelectedIds(prev => prev.filter(id => !visibleIds.has(id)));
    } else {
      const toAdd = selectableVisible.map(r=>r.id).filter(id => !selectedIds.includes(id));
      setSelectedIds(prev => [...prev, ...toAdd]);
    }
  };

  const clearSelection = () => setSelectedIds([]);

  useEffect(() => {
    if (selectedIds.length > 0) {
      setAccordionOpen(true);
      setEditMap(prev => {
        const copy = { ...prev };
        selectedIds.forEach(id => {
          if (!copy[id]) {
            const row = rows.find(r=>r.id===id);
            if (row) copy[id] = { url: row.url, error: null };
          }
        });
        Object.keys(copy).forEach(k => { if (!selectedIds.includes(Number(k))) delete copy[k]; });
        return copy;
      });
    } else {
      setAccordionOpen(false);
      setEditMap({});
      setApplyAllValue("");
    }
  }, [selectedIds, rows]);

  const applyAllToSelected = () => {
    if (!applyAllValue) return;
    const ok = isValidUrl(applyAllValue);
    setEditMap(prev => {
      const m = { ...prev };
      selectedIds.forEach(id => { m[id] = { url: applyAllValue, error: ok?null:"Invalid URL" }; });
      return m;
    });
  };

  const handleSingleUrlChange = (id, value) => {
    setEditMap(prev => ({
      ...prev,
      [id]: { url: value, error: value && !isValidUrl(value) ? "Invalid URL" : null },
    }));
  };

 const handleSave = async () => {
   for (const id of selectedIds) {
     const e = editMap[id];
     if (!e || !e.url || e.error) {
       return alert("Please fix invalid or empty URLs before saving.");
     }
   }

   // Build request body
   const updatedLinks = {};
   selectedIds.forEach(id => {
     updatedLinks[id] = editMap[id].url;
   });

   try {
     const res = await fetch("http://localhost:8080/api/mf-extractor-info", {
       method: "POST",
       headers: { "Content-Type": "application/json" },
       body: JSON.stringify({ updatedLinks }),
     });

     if (!res.ok) {
       throw new Error("Failed to update links");
     }

     // Optionally re-fetch rows after update
     const updated = await res.json();
     console.log("Update response:", updated);

     setRows(prev =>
       prev.map(r =>
         selectedIds.includes(r.id) ? { ...r, url: editMap[r.id].url } : r
       )
     );

     setAccordionOpen(false);
     setEditMap({});
     setApplyAllValue("");
     clearSelection();
     alert("Links updated successfully!");
   } catch (err) {
     console.error(err);
     alert("Error updating links: " + err.message);
   }
 };

  const handleCancel = () => {
    setAccordionOpen(false);
    setEditMap({});
    setApplyAllValue("");
    clearSelection();
  };

  if (loading) {
    return <div className="p-6 text-gray-500">Loading...</div>;
  }

  return (
    <div className="space-y-4">
      {/* Summary cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="p-4 bg-white rounded-lg shadow border text-center">
          <p className="text-xs text-gray-500 uppercase">No. of Mutual Funds</p>
          <p className="mt-1 text-2xl font-semibold">{summary.uniqueFunds}</p>
        </div>
        <div className="p-4 bg-white rounded-lg shadow border text-center">
          <p className="text-xs text-gray-500 uppercase">No. of Stocks</p>
          <p className="mt-1 text-2xl font-semibold">{summary.totalStocks}</p>
        </div>
        <div className="p-4 bg-white rounded-lg shadow border text-center">
          <p className="text-xs text-gray-500 uppercase">Successful Count</p>
          <p className="mt-1 text-2xl font-semibold text-green-600">{summary.successCount}</p>
        </div>
        <div className="p-4 bg-white rounded-lg shadow border text-center">
          <p className="text-xs text-gray-500 uppercase">Failed Count</p>
          <p className="mt-1 text-2xl font-semibold text-red-600">{summary.failedCount}</p>
        </div>
      </div>

      {/* Filters */}
      <div className="flex flex-col md:flex-row gap-3 md:items-end justify-between">
        <div className="flex flex-wrap gap-3">
          <div>
            <label className="block text-xs text-gray-600 mb-1">Status</label>
            <select
              className="border rounded px-3 py-2 text-sm"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              {statusOptions.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>

          <div>
            <label className="block text-xs text-gray-600 mb-1">Mutual Fund Name</label>
            <input
              list="fundList"
              className="border rounded px-3 py-2 text-sm w-64"
              placeholder="Search or choose fund…"
              value={fundFilter}
              onChange={(e) => setFundFilter(e.target.value)}
            />
            <datalist id="fundList">
              {Array.from(new Set(rows.map(r => r.fund))).sort().map((f) => <option key={f} value={f} />)}
            </datalist>
          </div>
        </div>

        <div className="flex gap-2">
          <button
            className="text-sm px-3 py-2 bg-gray-100 rounded hover:bg-gray-200"
            onClick={() => { setStatusFilter("ALL"); setFundFilter(""); }}
          >
            Clear Filters
          </button>
          <button
            className="text-sm px-3 py-2 bg-gray-100 rounded hover:bg-gray-200"
            onClick={clearSelection}
          >
            Clear Selection
          </button>
        </div>
      </div>

      {/* Table */}
      <div className="overflow-x-auto bg-white rounded-lg shadow border border-gray-200">
        <table className="min-w-full text-sm">
          <thead className="bg-gray-100 text-gray-700">
            <tr>
              <th className="p-3 w-10">
                <input
                  type="checkbox"
                  checked={selectableVisible.length>0 && allVisibleSelected}
                  onChange={toggleSelectAllVisible}
                />
              </th>
              {["fund","url","date","status","error","count"].map((col) => (
                <th
                  key={col}
                  className="p-3 text-left cursor-pointer select-none"
                  onClick={() => requestSort(col)}
                >
                  {col === "fund" ? "Mutual Fund Name" :
                   col === "url" ? "Download URL" :
                   col === "date" ? "Date (dd-MM-YYYY)" :
                   col === "status" ? "Status" :
                   col === "error" ? "Error" : "Record Count"}
                  {sortConfig.key === col ? (sortConfig.direction === "asc" ? " ▲" : " ▼") : ""}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {pagedRows.map((r) => {
              const selected = selectedIds.includes(r.id);
              const unselectable = r.status === "SUCCESSFUL";
              return (
                <tr key={r.id} className={selected ? "bg-blue-50" : "hover:bg-gray-50"}>
                  <td className="p-3">
                    <input
                      type="checkbox"
                      checked={selected}
                      disabled={unselectable}
                      title={unselectable ? "SUCCESSFUL rows cannot be edited" : ""}
                      onChange={() => toggleSelectOne(r.id)}
                    />
                  </td>
                  <td className="p-3">{r.fund}</td>
                  <td className="p-3 truncate max-w-[360px]">
                    <a href={r.url} target="_blank" rel="noopener noreferrer" className="text-blue-600 underline">
                      {r.url}
                    </a>
                  </td>
                  <td className="p-3">{formatDMY(r.date)}</td>
                  <td className="p-3">
                    <span
                      className={
                        r.status === "SUCCESSFUL"
                          ? "text-green-700"
                          : r.status === "FAILED"
                          ? "text-red-600"
                          : "text-amber-600"
                      }
                    >
                      {r.status}
                    </span>
                  </td>
                  <td className="p-3 text-gray-600">{r.error || <span className="text-gray-400">—</span>}</td>
                  <td className="p-3 text-right">{r.count.toLocaleString()}</td>
                </tr>
              );
            })}

            {pagedRows.length === 0 && (
              <tr>
                <td className="p-6 text-center text-gray-500 italic" colSpan={7}>
                  No results with current filters.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination controls */}
      <div className="flex justify-between items-center mt-3">
        <div className="flex items-center gap-2 text-sm">
          <span>Rows per page:</span>
          <select
            className="border rounded px-2 py-1 text-sm"
            value={pageSize}
            onChange={(e) => setPageSize(Number(e.target.value))}
          >
            {[5,10,20,50].map(size => (
              <option key={size} value={size}>{size}</option>
            ))}
          </select>
        </div>

        <div className="flex items-center gap-2 text-sm">
          <button
            className="px-2 py-1 border rounded disabled:opacity-50"
            disabled={currentPage === 1}
            onClick={() => setCurrentPage(p => Math.max(p-1, 1))}
          >
            Prev
          </button>
          <span>
            Page {currentPage} of {totalPages}
          </span>
          <button
            className="px-2 py-1 border rounded disabled:opacity-50"
            disabled={currentPage === totalPages}
            onClick={() => setCurrentPage(p => Math.min(p+1, totalPages))}
          >
            Next
          </button>
        </div>
      </div>

      {/* Accordion */}
      <div className="mt-2">
        <button
          disabled={selectedIds.length === 0}
          onClick={() => setAccordionOpen((s) => !s)}
          className={accordionBtnClass(selectedIds.length > 0, accordionOpen)}
        >
          <span className="font-medium">Bulk Edit Download URL ({selectedIds.length} selected)</span>
          <span>{accordionOpen ? "▲" : "▼"}</span>
        </button>

        {accordionOpen && selectedIds.length > 0 && (
          <div className="border rounded-b-md border-t-0 p-4 bg-white shadow-sm">
            <div className="flex flex-col md:flex-row gap-3 items-start md:items-end mb-4">
              <div className="flex-1">
                <label className="block text-xs text-gray-600 mb-1">Set one URL for all selected</label>
                <input
                  className="w-full border rounded px-3 py-2 text-sm"
                  placeholder="https://example.com/file.xlsx"
                  value={applyAllValue}
                  onChange={(e) => setApplyAllValue(e.target.value)}
                />
              </div>
              <button onClick={applyAllToSelected} className="px-4 py-2 bg-gray-100 rounded hover:bg-gray-200 text-sm">
                Apply to all
              </button>
            </div>

            <div className="max-h-72 overflow-auto border rounded">
              <table className="w-full text-sm">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="p-2 text-left w-48">Fund</th>
                    <th className="p-2 text-left">New Download URL</th>
                    <th className="p-2 text-left w-40">Date (dd-MM-YYYY)</th>
                  </tr>
                </thead>
                <tbody>
                  {selectedIds.map((id) => {
                    const row = rows.find((r) => r.id === id);
                    const edit = editMap[id] || { url: row?.url || "", error: null };
                    return (
                      <tr key={id} className="border-t">
                        <td className="p-2">{row?.fund}</td>
                        <td className="p-2">
                          <input
                            className={inputClass(edit.error)}
                            value={edit.url}
                            onChange={(e) => handleSingleUrlChange(id, e.target.value)}
                            placeholder="https://..."
                          />
                          {edit.error && <p className="text-xs text-red-600 mt-1">{edit.error}</p>}
                        </td>
                        <td className="p-2">{formatDMY(row?.date)}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            <div className="flex justify-end gap-3 mt-4">
              <button onClick={handleCancel} className="px-4 py-2 bg-gray-100 rounded hover:bg-gray-200 text-sm">
                Cancel
              </button>
              <button onClick={handleSave} className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 text-sm">
                Save URLs
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

/* helpers */
function accordionBtnClass(enabled, open) {
  return cn(
    "w-full flex items-center justify-between px-4 py-2 rounded-md border text-sm",
    enabled ? "bg-white hover:bg-gray-50 cursor-pointer" : "bg-gray-100 cursor-not-allowed",
    open ? "border-gray-300" : "border-gray-200"
  );
}
function inputClass(hasError) {
  return cn(
    "w-full border rounded px-3 py-2 text-sm",
    hasError ? "border-red-500 focus:outline-red-500" : "border-gray-300 focus:outline-blue-500"
  );
}

export default MFDownloadsTable;
