import React, { useMemo, useState } from "react";

// ---- dummy data (replace with API data) ----
const initialRows = [
  {
    id: 1,
    fund: "SBI Bluechip Fund",
    url: "https://example.com/sbi-bluechip-2025-06.xlsx",
    date: "2025-06-30", // string
    status: "SUCCESS",
    error: "",
    count: 1200,
  },
  {
    id: 2,
    fund: "Mirae Largecap Fund",
    url: "https://example.com/mirae-largecap-2025-06.xlsx",
    date: "2025-06-30",
    status: "FAILED",
    error: "File not found",
    count: 0,
  },
  {
    id: 3,
    fund: "Axis Bluechip Fund",
    url: "https://example.com/axis-bluechip-2025-05.xlsx",
    date: "2025-05-31",
    status: "SUCCESS",
    error: "",
    count: 980,
  },
  {
    id: 4,
    fund: "Canara Robeco Flexi Cap",
    url: "https://example.com/canara-flexi-2025-06.xlsx",
    date: "2025-06-30",
    status: "PARTIAL",
    error: "Some rows skipped",
    count: 450,
  },
];

// format yyyy-MM-dd -> dd-MM-YYYY without Date()
function formatDMY(dateStr) {
  if (!dateStr) return "";
  const [yyyy, mm, dd] = dateStr.split("-");
  return `${dd}-${mm}-${yyyy}`;
}

// simple url validator
function isValidUrl(s) {
  try {
    // Accept http(s) only; tweak if you allow others
    const u = new URL(s);
    return u.protocol === "http:" || u.protocol === "https:";
  } catch {
    return false;
  }
}

const MFDownloadsTable = () => {
  const [rows, setRows] = useState(initialRows);
  const [selectedIds, setSelectedIds] = useState([]);
  const [accordionOpen, setAccordionOpen] = useState(false);

  // local edit map: { id -> { url: string, error: string|null } }
  const [editMap, setEditMap] = useState({});
  const [applyAllValue, setApplyAllValue] = useState("");

  // Filters (optional; reuse from your earlier version if needed)
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [fundFilter, setFundFilter] = useState("");

  const fundOptions = useMemo(
    () => Array.from(new Set(rows.map((r) => r.fund))).sort(),
    [rows]
  );

  const filteredRows = useMemo(() => {
    return rows.filter((r) => {
      const byStatus = statusFilter === "ALL" || r.status === statusFilter;
      const byFund =
        !fundFilter ||
        r.fund.toLowerCase().includes(fundFilter.trim().toLowerCase());
      return byStatus && byFund;
    });
  }, [rows, statusFilter, fundFilter]);

  const allVisibleSelected =
    filteredRows.length > 0 &&
    filteredRows.every((r) => selectedIds.includes(r.id));

  // selection handlers
  const toggleSelectOne = (id) => {
    setSelectedIds((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]
    );
  };

  const toggleSelectAllVisible = () => {
    if (allVisibleSelected) {
      // unselect all visible
      const visibleIds = new Set(filteredRows.map((r) => r.id));
      setSelectedIds((prev) => prev.filter((id) => !visibleIds.has(id)));
    } else {
      // add all visible
      const toAdd = filteredRows
        .map((r) => r.id)
        .filter((id) => !selectedIds.includes(id));
      setSelectedIds((prev) => [...prev, ...toAdd]);
    }
  };

  const clearSelection = () => setSelectedIds([]);

  // Open accordion when selection changes (optional behavior)
  React.useEffect(() => {
    if (selectedIds.length > 0) {
      setAccordionOpen(true);
      // initialize editMap for new selections
      setEditMap((prev) => {
        const copy = { ...prev };
        selectedIds.forEach((id) => {
          if (!copy[id]) {
            const row = rows.find((r) => r.id === id);
            if (row) copy[id] = { url: row.url, error: null };
          }
        });
        // prune removed selections
        Object.keys(copy).forEach((k) => {
          if (!selectedIds.includes(Number(k))) delete copy[k];
        });
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
    const isUrl = isValidUrl(applyAllValue);
    setEditMap((prev) => {
      const m = { ...prev };
      selectedIds.forEach((id) => {
        m[id] = {
          url: applyAllValue,
          error: isUrl ? null : "Invalid URL",
        };
      });
      return m;
    });
  };

  const handleSingleUrlChange = (id, value) => {
    setEditMap((prev) => ({
      ...prev,
      [id]: { url: value, error: value && !isValidUrl(value) ? "Invalid URL" : null },
    }));
  };

  const handleSave = () => {
    // validate
    for (const id of selectedIds) {
      const item = editMap[id];
      if (!item || !item.url || item.error) {
        return alert("Please fix invalid or empty URLs before saving.");
      }
    }
    // commit
    setRows((prev) =>
      prev.map((r) =>
        selectedIds.includes(r.id) ? { ...r, url: editMap[r.id].url } : r
      )
    );
    setAccordionOpen(false);
    setEditMap({});
    setApplyAllValue("");
    clearSelection();
  };

  const handleCancel = () => {
    setAccordionOpen(false);
    setEditMap({});
    setApplyAllValue("");
    clearSelection();
  };

  return (
    <div className="space-y-3">
      {/* Filters (optional) */}
      <div className="flex flex-col md:flex-row gap-3 md:items-end justify-between">
        <div className="flex flex-wrap gap-3">
          <div>
            <label className="block text-xs text-gray-600 mb-1">Status</label>
            <select
              className="border rounded px-3 py-2 text-sm"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="ALL">All</option>
              <option value="SUCCESS">Success</option>
              <option value="FAILED">Failed</option>
              <option value="PARTIAL">Partial</option>
            </select>
          </div>

          <div>
            <label className="block text-xs text-gray-600 mb-1">
              Mutual Fund Name
            </label>
            <input
              list="fundList"
              className="border rounded px-3 py-2 text-sm w-64"
              placeholder="Search or choose fund…"
              value={fundFilter}
              onChange={(e) => setFundFilter(e.target.value)}
            />
            <datalist id="fundList">
              {fundOptions.map((f) => (
                <option key={f} value={f} />
              ))}
            </datalist>
          </div>
        </div>

        <div className="flex gap-2">
          <button
            className="text-sm px-3 py-2 bg-gray-100 rounded hover:bg-gray-200"
            onClick={() => {
              setStatusFilter("ALL");
              setFundFilter("");
            }}
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
                  checked={filteredRows.length > 0 && allVisibleSelected}
                  onChange={toggleSelectAllVisible}
                />
              </th>
              <th className="p-3 text-left">Mutual Fund Name</th>
              <th className="p-3 text-left">Download URL</th>
              <th className="p-3 text-left">Date (dd-MM-YYYY)</th>
              <th className="p-3 text-left">Status</th>
              <th className="p-3 text-left">Error</th>
              <th className="p-3 text-right">Record Count</th>
            </tr>
          </thead>
          <tbody>
            {filteredRows.map((r) => {
              const selected = selectedIds.includes(r.id);
              return (
                <tr key={r.id} className={selected ? "bg-blue-50" : "hover:bg-gray-50"}>
                  <td className="p-3">
                    <input
                      type="checkbox"
                      checked={selected}
                      onChange={() => toggleSelectOne(r.id)}
                    />
                  </td>
                  <td className="p-3">{r.fund}</td>
                  <td className="p-3 truncate max-w-[360px]">
                    <a
                      href={r.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-blue-600 underline"
                    >
                      {r.url}
                    </a>
                  </td>
                  <td className="p-3">{formatDMY(r.date)}</td>
                  <td className="p-3">
                    <span
                      className={
                        r.status === "SUCCESS"
                          ? "text-green-700"
                          : r.status === "FAILED"
                          ? "text-red-600"
                          : "text-amber-600"
                      }
                    >
                      {r.status}
                    </span>
                  </td>
                  <td className="p-3 text-gray-600">
                    {r.error || <span className="text-gray-400">—</span>}
                  </td>
                  <td className="p-3 text-right">{r.count.toLocaleString()}</td>
                </tr>
              );
            })}

            {filteredRows.length === 0 && (
              <tr>
                <td className="p-6 text-center text-gray-500 italic" colSpan={7}>
                  No results with current filters.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Accordion (shows only when selection exists) */}
      <div className="mt-2">
        <button
          disabled={selectedIds.length === 0}
          onClick={() => setAccordionOpen((s) => !s)}
          className={classNamesAccordionButton(selectedIds.length > 0, accordionOpen)}
        >
          <span className="font-medium">
            Bulk Edit Download URL ({selectedIds.length} selected)
          </span>
          <span>{accordionOpen ? "▲" : "▼"}</span>
        </button>

        {accordionOpen && selectedIds.length > 0 && (
          <div className="border rounded-b-md border-t-0 p-4 bg-white shadow-sm">
            {/* Apply to all */}
            <div className="flex flex-col md:flex-row gap-3 items-start md:items-end mb-4">
              <div className="flex-1">
                <label className="block text-xs text-gray-600 mb-1">
                  Set one URL for all selected
                </label>
                <input
                  className="w-full border rounded px-3 py-2 text-sm"
                  placeholder="https://example.com/file.xlsx"
                  value={applyAllValue}
                  onChange={(e) => setApplyAllValue(e.target.value)}
                />
              </div>
              <button
                onClick={applyAllToSelected}
                className="px-4 py-2 bg-gray-100 rounded hover:bg-gray-200 text-sm"
              >
                Apply to all
              </button>
            </div>

            {/* Per-row editors */}
            <div className="max-h-72 overflow-auto border rounded">
              <table className="w-full text-sm">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="p-2 text-left w-48">Fund</th>
                    <th className="p-2 text-left">New Download URL</th>
                    <th className="p-2 text-left w-24">Status</th>
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
                            className={classNamesInput(edit.error)}
                            value={edit.url}
                            onChange={(e) => handleSingleUrlChange(id, e.target.value)}
                            placeholder="https://..."
                          />
                          {edit.error && (
                            <p className="text-xs text-red-600 mt-1">{edit.error}</p>
                          )}
                        </td>
                        <td className="p-2">
                          {edit.error ? (
                            <span className="text-red-600">Invalid</span>
                          ) : (
                            <span className="text-green-700">OK</span>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            <div className="flex justify-end gap-3 mt-4">
              <button
                onClick={handleCancel}
                className="px-4 py-2 bg-gray-100 rounded hover:bg-gray-200 text-sm"
              >
                Cancel
              </button>
              <button
                onClick={handleSave}
                className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 text-sm"
              >
                Save URLs
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

/* ------- small helpers for classNames (no external deps) ------- */
function classNames(...c) {
  return c.filter(Boolean).join(" ");
}
function classNamesAccordionButton(enabled, open) {
  return classNames(
    "w-full flex items-center justify-between px-4 py-2 rounded-md border text-sm",
    enabled ? "bg-white hover:bg-gray-50 cursor-pointer" : "bg-gray-100 cursor-not-allowed",
    open ? "border-gray-300" : "border-gray-200"
  );
}
function classNamesInput(hasError) {
  return classNames(
    "w-full border rounded px-3 py-2 text-sm",
    hasError ? "border-red-500 focus:outline-red-500" : "border-gray-300 focus:outline-blue-500"
  );
}


export default MFDownloadsTable;
