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
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const end = new Date().toISOString().slice(0, 10);
      const params = new URLSearchParams({ end, months: '3' });
      const resp = await fetch(`http://localhost:8080/api/reports/volume/last-months?` + params.toString());
      if (!resp.ok) throw new Error('Network response was not ok');
      const json = await resp.json();
      setData(json || []);
    } catch (err) {
      console.error(err);
      setData([]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="relative min-h-screen bg-gray-50">
      <Header />
      <main className="p-6">
        <h1 className="text-2xl font-bold mb-4">Volume Report (Last 3 months)</h1>
        {loading ? (
          <div>Loading...</div>
        ) : (
          <div className="overflow-auto bg-white shadow rounded p-4">
            <table className="min-w-full table-auto">
              <thead>
                <tr>
                  <th className="px-4 py-2">Ticker</th>
                  <th className="px-4 py-2">Avg Volume</th>
                  <th className="px-4 py-2">Std Dev</th>
                  <th className="px-4 py-2">Trading Days</th>
                  <th className="px-4 py-2">Avg Turnover</th>
                  <th className="px-4 py-2">Last Close</th>
                  <th className="px-4 py-2">Market Cap</th>
                </tr>
              </thead>
              <tbody>
                {data.map((row) => (
                  <tr key={row.stockId}>
                    <td className="border px-4 py-2">{row.ticker || 'UNKNOWN'}</td>
                    <td className="border px-4 py-2">{fmtNumber(row.avgVolume, 0)}</td>
                    <td className="border px-4 py-2">{fmtNumber(row.volumeStdDev, 0)}</td>
                    <td className="border px-4 py-2">{row.tradingDays ?? '-'}</td>
                    <td className="border px-4 py-2">{fmtBigDecimal(row.avgTurnover)}</td>
                    <td className="border px-4 py-2">{fmtBigDecimal(row.lastClosePrice)}</td>
                    <td className="border px-4 py-2">{fmtBigDecimal(row.marketCap)}</td>
                  </tr>
                ))}
                {data.length === 0 && (
                  <tr>
                    <td colSpan={7} className="text-center p-4">No data</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </main>
    </div>
  );
};

export default VolumeReport;

