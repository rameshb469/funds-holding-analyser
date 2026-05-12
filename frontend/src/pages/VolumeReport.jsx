import React, { useEffect, useState } from 'react';
import Header from '../components/Header';

const fmtNumber = (v, decimals = 0) => {
  if (v === null || v === undefined) return '-';
  const n = Number(v);
  if (!isFinite(n)) return '-';
  return decimals === 0 ? n.toLocaleString() : n.toFixed(decimals);
};

const fmtBigDecimal = (v) => {
  if (v === null || v === undefined) return '-';
  // backend may serialize BigDecimal as string or number
  const n = Number(v);
  if (!isFinite(n)) return String(v);
  return n.toLocaleString();
};

const VolumeReport = () => {
  const [data, setData] = useState([]); // aggregated picks
  const [loading, setLoading] = useState(false);
  const [running, setRunning] = useState(false);
  const [runDbExtract, setRunDbExtract] = useState(false);

  useEffect(() => {
    // fetch latest backtest results on mount
    fetchLatest();
  }, []);

  const aggregatePicks = (windows) => {
    // windows: array of window results each with 'picks' array
    const map = new Map();
    (windows || []).forEach((w) => {
      const picks = w.picks || [];
      picks.forEach((p) => {
        const sid = p.stock_id ?? p.stockId ?? p.id ?? p.symbol;
        const key = sid != null ? String(sid) : String(p.symbol || p.stockSymbol || p.ticker);
        const existing = map.get(key) || { key, symbol: p.symbol || p.stockSymbol || key, sumScore: 0, sumNet: 0, count: 0, MKT_CAP: p.MKT_CAP ?? p.marketCap ?? null, red_flags: new Set() };
        if (p.score != null && !isNaN(Number(p.score))) existing.sumScore += Number(p.score);
        if (p.net_return_after_tc != null && !isNaN(Number(p.net_return_after_tc))) existing.sumNet += Number(p.net_return_after_tc);
        existing.count += 1;
        // prefer non-null market cap
        if ((existing.MKT_CAP === null || existing.MKT_CAP === undefined) && (p.MKT_CAP ?? p.marketCap) != null) {
          existing.MKT_CAP = p.MKT_CAP ?? p.marketCap;
        }
        // collect red flags
        if (Array.isArray(p.red_flags)) p.red_flags.forEach(f => existing.red_flags.add(f));
        map.set(key, existing);
      });
    });

    // convert to array and compute averages
    const arr = Array.from(map.values()).map((v) => ({
      key: v.key,
      symbol: v.symbol,
      avgScore: v.count > 0 ? v.sumScore / v.count : null,
      avgNetReturn: v.count > 0 ? v.sumNet / v.count : null,
      occurrences: v.count,
      MKT_CAP: v.MKT_CAP,
      red_flags: Array.from(v.red_flags),
    }));

    // sort by occurrences desc then avgScore desc
    arr.sort((a, b) => (b.occurrences - a.occurrences) || ((b.avgScore || 0) - (a.avgScore || 0)));
    return arr;
  };

  const fetchLatest = async () => {
    setLoading(true);
    try {
      const resp = await fetch('http://localhost:8080/api/reports/backtest/latest');
      if (!resp.ok) {
        // no latest, clear data
        setData([]);
        return;
      }
      const json = await resp.json();
      // backtest latest returns array of window objects
      const aggregated = aggregatePicks(json);
      setData(aggregated);
    } catch (err) {
      console.error('Failed to fetch latest backtest:', err);
      setData([]);
    } finally {
      setLoading(false);
    }
  };

  const runBacktest = async () => {
    setRunning(true);
    try {
      const params = new URLSearchParams();
      // prefer defaults: nStocks=500, nPicks=10; expose runDbExtract flag
      params.append('nStocks', '500');
      params.append('nPicks', '10');
      params.append('runDbExtract', String(runDbExtract));
      // POST with query params
      const resp = await fetch('http://localhost:8080/api/reports/backtest/run?' + params.toString(), { method: 'POST' });
      if (!resp.ok) {
        const text = await resp.text();
        throw new Error('Backtest run failed: ' + resp.status + ' ' + text);
      }
      const json = await resp.json();
      // endpoint returns results (array) on success
      const windows = Array.isArray(json) ? json : (json.results || json);
      const aggregated = aggregatePicks(windows);
      setData(aggregated);
    } catch (err) {
      console.error(err);
      alert('Failed to run backtest: ' + err.message);
    } finally {
      setRunning(false);
    }
  };

  return (
    <div className="relative min-h-screen bg-gray-50">
      <Header />
      <main className="p-6">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-2xl font-bold">Volume / Backtest Picks (ML)</h1>
          <div className="flex items-center gap-2">
            <label className="flex items-center gap-2 text-sm">
              <input type="checkbox" checked={runDbExtract} onChange={e => setRunDbExtract(e.target.checked)} />
              <span>Run DB Extract</span>
            </label>
            <button onClick={runBacktest} disabled={running} className="bg-blue-600 text-white px-3 py-2 rounded hover:bg-blue-700">
              {running ? 'Running...' : 'Run ML Backtest'}
            </button>
            <button onClick={fetchLatest} disabled={loading} className="bg-gray-100 px-3 py-2 rounded hover:bg-gray-200">
              {loading ? 'Loading...' : 'Fetch Latest'}
            </button>
          </div>
        </div>

        <div className="overflow-auto bg-white shadow rounded p-4">
          <table className="min-w-full table-auto">
            <thead>
              <tr>
                <th className="px-4 py-2">Ticker</th>
                <th className="px-4 py-2">Market Cap</th>
                <th className="px-4 py-2">Avg Score</th>
                <th className="px-4 py-2">Avg Net Return</th>
                <th className="px-4 py-2">Occurrences</th>
                <th className="px-4 py-2">Red Flags</th>
              </tr>
            </thead>
            <tbody>
              {data.map((row) => (
                <tr key={row.key}>
                  <td className="border px-4 py-2">{row.symbol || row.key}</td>
                  <td className="border px-4 py-2">{fmtBigDecimal(row.MKT_CAP)}</td>
                  <td className="border px-4 py-2">{row.avgScore != null ? row.avgScore.toFixed(4) : '-'}</td>
                  <td className="border px-4 py-2">{row.avgNetReturn != null ? (row.avgNetReturn * 100).toFixed(2) + '%' : '-'}</td>
                  <td className="border px-4 py-2">{row.occurrences}</td>
                  <td className="border px-4 py-2">{(row.red_flags || []).join(', ') || '-'}</td>
                </tr>
              ))}
              {data.length === 0 && (
                <tr>
                  <td colSpan={6} className="text-center p-4">No data (run ML backtest or fetch latest)</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  );
};

export default VolumeReport;

