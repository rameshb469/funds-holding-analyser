import React, { useEffect, useState } from 'react';
import Header from '../components/Header';

const API_BASE = 'http://localhost:8080/api/broker';

const EMPTY_ORDER = {
  stockId: null,
  tradingsymbol: '',
  exchange: 'NSE',
  transactionType: 'BUY',
  orderType: 'MARKET',
  product: 'CNC',
  quantity: 1,
  price: '',
  triggerPrice: '',
  tag: '',
  validity: 'DAY',
};

const ORDER_TYPE_OPTIONS = ['MARKET', 'LIMIT', 'SL', 'SL-M'];
const PRODUCT_OPTIONS = ['CNC', 'MIS', 'NRML'];
const EXCHANGE_OPTIONS = ['NSE', 'BSE', 'NFO', 'MCX', 'CDS'];
const VALIDITY_OPTIONS = ['DAY', 'IOC'];

function StatusPill({ connected, sandbox }) {
  if (!connected) {
    return <span className="px-2 py-1 text-xs rounded bg-red-100 text-red-700">Disconnected</span>;
  }
  return (
    <span className="px-2 py-1 text-xs rounded bg-green-100 text-green-700">
      Connected{sandbox ? ' · sandbox' : ''}
    </span>
  );
}

function Broker() {
  const [session, setSession] = useState(null);
  const [loginUrl, setLoginUrl] = useState(null);
  const [orderDraft, setOrderDraft] = useState(EMPTY_ORDER);
  const [orders, setOrders] = useState([]);
  const [audits, setAudits] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [feedback, setFeedback] = useState(null);
  const [error, setError] = useState(null);
  const [stockQuery, setStockQuery] = useState('');
  const [stockOptions, setStockOptions] = useState([]);
  const [stockOpen, setStockOpen] = useState(false);
  const [stocksLoading, setStocksLoading] = useState(false);
  const [selectedStock, setSelectedStock] = useState(null);
  const [quote, setQuote] = useState(null);
  const [quoteLoading, setQuoteLoading] = useState(false);
  const [quoteError, setQuoteError] = useState(null);

  const refresh = async () => {
    try {
      const [sessRes, ordersRes, auditRes] = await Promise.all([
        fetch(`${API_BASE}/session`),
        fetch(`${API_BASE}/orders`),
        fetch(`${API_BASE}/orders/audit`),
      ]);
      if (sessRes.ok) setSession(await sessRes.json());
      if (ordersRes.ok) setOrders(await ordersRes.json());
      if (auditRes.ok) setAudits(await auditRes.json());
    } catch (e) {
      setError(`Failed to load broker state: ${e.message}`);
    }
  };

  useEffect(() => {
    refresh();
  }, []);

  useEffect(() => {
    if (!stockOpen) return undefined;
    const handle = setTimeout(async () => {
      setStocksLoading(true);
      try {
        const url = stockQuery.trim()
          ? `${API_BASE}/stocks?q=${encodeURIComponent(stockQuery.trim())}`
          : `${API_BASE}/stocks`;
        const res = await fetch(url);
        if (res.ok) {
          setStockOptions(await res.json());
        } else {
          setStockOptions([]);
        }
      } catch {
        setStockOptions([]);
      } finally {
        setStocksLoading(false);
      }
    }, 200);
    return () => clearTimeout(handle);
  }, [stockQuery, stockOpen]);

  useEffect(() => {
    if (!selectedStock) {
      setQuote(null);
      setQuoteError(null);
      return undefined;
    }
    let cancelled = false;
    const fetchQuote = async () => {
      setQuoteLoading(true);
      try {
        const exchange = selectedStock.exchange || 'NSE';
        const symbol = selectedStock.symbol;
        const res = await fetch(
          `${API_BASE}/quote?exchange=${encodeURIComponent(exchange)}&symbol=${encodeURIComponent(symbol)}`
        );
        if (cancelled) return;
        if (!res.ok) {
          const body = await res.json().catch(() => ({}));
          setQuote(null);
          setQuoteError(body.message || `Quote fetch failed (status ${res.status})`);
        } else {
          const data = await res.json();
          setQuote(data);
          setQuoteError(null);
        }
      } catch (e) {
        if (!cancelled) {
          setQuote(null);
          setQuoteError(e.message);
        }
      } finally {
        if (!cancelled) setQuoteLoading(false);
      }
    };
    fetchQuote();
    const handle = setInterval(fetchQuote, 3000);
    return () => {
      cancelled = true;
      clearInterval(handle);
    };
  }, [selectedStock]);

  const handleSelectStock = (stock) => {
    setSelectedStock(stock);
    setOrderDraft((prev) => ({
      ...prev,
      stockId: stock.id,
      symbol: stock.symbol,
      exchange: stock.exchange || prev.exchange || 'NSE',
    }));
    setStockOpen(false);
  };

  const handleClearStock = () => {
    setSelectedStock(null);
    setOrderDraft((prev) => ({ ...prev, stockId: null, symbol: '' }));
  };

  const handleConnect = async () => {
    setError(null);
    setFeedback(null);
    try {
      const res = await fetch(`${API_BASE}/login-url`);
      if (!res.ok) throw new Error(`status ${res.status}`);
      const data = await res.json();
      setLoginUrl(data.url);
      window.open(data.url, '_blank', 'noopener,noreferrer');
    } catch (e) {
      setError(`Could not build login URL: ${e.message}`);
    }
  };

  const handleField = (field) => (ev) => {
    const value = ev.target.type === 'number' && ev.target.value !== ''
      ? Number(ev.target.value)
      : ev.target.value;
    setOrderDraft((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmitOrder = async (ev) => {
    ev.preventDefault();
    if (!orderDraft.stockId || !orderDraft.symbol) {
      setError('Pick a stock from the list before placing the order.');
      return;
    }
    setSubmitting(true);
    setError(null);
    setFeedback(null);
    try {
      const payload = { ...orderDraft };
      if (payload.orderType === 'MARKET') {
        payload.price = null;
        payload.triggerPrice = null;
      }
      if (payload.orderType === 'LIMIT') {
        payload.triggerPrice = null;
      }
      const res = await fetch(`${API_BASE}/orders`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      const body = await res.json().catch(() => ({}));
      if (!res.ok) {
        throw new Error(body.message || `Order failed (status ${res.status})`);
      }
      setFeedback(
        `Order accepted: ${body.kiteOrderId}${body.sandbox ? ' (sandbox)' : ''}`
      );
      setOrderDraft(EMPTY_ORDER);
      setSelectedStock(null);
      setStockQuery('');
      await refresh();
    } catch (e) {
      setError(e.message);
    } finally {
      setSubmitting(false);
    }
  };

  const isConnected = session?.connected;

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <div className="p-6 max-w-6xl mx-auto space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold">Broker · Zerodha Kite</h1>
            <p className="text-sm text-gray-500">
              Place BUY / SELL orders and inspect the audit log.
            </p>
          </div>
          <div className="flex items-center gap-3">
            <StatusPill connected={isConnected} sandbox={session?.sandbox} />
            {isConnected ? (
              <span className="text-sm text-gray-600">
                {session?.userName || session?.userId}
              </span>
            ) : (
              <button
                onClick={handleConnect}
                className="px-4 py-2 text-sm rounded bg-blue-600 text-white hover:bg-blue-700"
              >
                Connect to Zerodha
              </button>
            )}
          </div>
        </div>

        {loginUrl && !isConnected && (
          <div className="p-3 rounded border border-amber-300 bg-amber-50 text-sm">
            Kite login opened in a new tab. After approving, you'll be redirected back
            to <code className="font-mono">/api/broker/callback</code> and the session
            will activate here. <span className="text-gray-500">(URL: {loginUrl})</span>
          </div>
        )}

        {feedback && (
          <div className="p-3 rounded border border-green-300 bg-green-50 text-sm text-green-800">
            {feedback}
          </div>
        )}
        {error && (
          <div className="p-3 rounded border border-red-300 bg-red-50 text-sm text-red-800">
            {error}
          </div>
        )}

        <section className="bg-white rounded shadow p-4">
          <h2 className="text-lg font-semibold mb-3">Place order</h2>
          <form
            onSubmit={handleSubmitOrder}
            onKeyDown={(e) => {
              // Belt-and-braces: swallow Enter on any input/select so the browser
              // never does a native form submission (which posts as
              // application/x-www-form-urlencoded and breaks the JSON DTO binding).
              if (e.key === 'Enter' && e.target.tagName !== 'TEXTAREA') {
                e.preventDefault();
              }
            }}
            className="grid grid-cols-2 md:grid-cols-4 gap-3"
          >
            <label className="text-sm col-span-2 md:col-span-2">
              <span className="block text-gray-600">Symbol (from stock_details)</span>
              {selectedStock ? (
                <div className="mt-1 flex items-center justify-between w-full border rounded px-2 py-1 bg-gray-50">
                  <div>
                    <span className="font-medium">{selectedStock.symbol}</span>
                    <span className="text-gray-500 text-xs ml-2">
                      {selectedStock.company}
                      {selectedStock.isinNumber ? ` · ${selectedStock.isinNumber}` : ''}
                    </span>
                  </div>
                  <button
                    type="button"
                    onClick={handleClearStock}
                    className="text-xs text-blue-600 hover:underline"
                  >
                    Change
                  </button>
                </div>
              ) : (
                <div className="relative mt-1">
                  <input
                    type="search"
                    value={stockQuery}
                    onChange={(e) => {
                      setStockQuery(e.target.value);
                      setStockOpen(true);
                    }}
                    onFocus={() => setStockOpen(true)}
                    onBlur={() => setTimeout(() => setStockOpen(false), 150)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') e.preventDefault();
                    }}
                    placeholder="Type to search (e.g. INFY, RELIANCE)"
                    className="w-full border rounded px-2 py-1"
                  />
                  {stockOpen && (
                    <div className="absolute z-20 mt-1 w-full max-h-60 overflow-y-auto bg-white border rounded shadow">
                      {stocksLoading && (
                        <div className="px-2 py-1 text-xs text-gray-500">Loading…</div>
                      )}
                      {!stocksLoading && stockOptions.length === 0 && (
                        <div className="px-2 py-1 text-xs text-gray-500">No matches</div>
                      )}
                      {stockOptions.map((s) => (
                        <button
                          type="button"
                          key={s.id}
                          onMouseDown={(ev) => {
                            ev.preventDefault();
                            handleSelectStock(s);
                          }}
                          className="w-full text-left px-2 py-1 text-sm hover:bg-blue-50"
                        >
                          <span className="font-medium">{s.symbol}</span>
                          <span className="text-gray-500 text-xs ml-2">{s.company}</span>
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </label>
            <label className="text-sm">
              <span className="block text-gray-600">Exchange</span>
              <select
                value={orderDraft.exchange}
                onChange={handleField('exchange')}
                className="mt-1 w-full border rounded px-2 py-1"
              >
                {EXCHANGE_OPTIONS.map((o) => (
                  <option key={o} value={o}>{o}</option>
                ))}
              </select>
            </label>
            <label className="text-sm">
              <span className="block text-gray-600">Side</span>
              <select
                value={orderDraft.transactionType}
                onChange={handleField('transactionType')}
                className="mt-1 w-full border rounded px-2 py-1"
              >
                <option value="BUY">BUY</option>
                <option value="SELL">SELL</option>
              </select>
            </label>
            <label className="text-sm">
              <span className="block text-gray-600">Order type</span>
              <select
                value={orderDraft.orderType}
                onChange={handleField('orderType')}
                className="mt-1 w-full border rounded px-2 py-1"
              >
                {ORDER_TYPE_OPTIONS.map((o) => (
                  <option key={o} value={o}>{o}</option>
                ))}
              </select>
            </label>
            <label className="text-sm">
              <span className="block text-gray-600">Product</span>
              <select
                value={orderDraft.product}
                onChange={handleField('product')}
                className="mt-1 w-full border rounded px-2 py-1"
              >
                {PRODUCT_OPTIONS.map((o) => (
                  <option key={o} value={o}>{o}</option>
                ))}
              </select>
            </label>
            <label className="text-sm">
              <span className="block text-gray-600">Quantity</span>
              <input
                type="number"
                min="1"
                value={orderDraft.quantity}
                onChange={handleField('quantity')}
                className="mt-1 w-full border rounded px-2 py-1"
              />
            </label>
            <label className="text-sm">
              <span className="block text-gray-600">Price</span>
              <input
                type="number"
                step="0.05"
                value={orderDraft.price}
                onChange={handleField('price')}
                disabled={orderDraft.orderType === 'MARKET' || orderDraft.orderType === 'SL-M'}
                className="mt-1 w-full border rounded px-2 py-1 disabled:bg-gray-100"
              />
            </label>
            <label className="text-sm">
              <span className="block text-gray-600">Trigger</span>
              <input
                type="number"
                step="0.05"
                value={orderDraft.triggerPrice}
                onChange={handleField('triggerPrice')}
                disabled={orderDraft.orderType === 'MARKET' || orderDraft.orderType === 'LIMIT'}
                className="mt-1 w-full border rounded px-2 py-1 disabled:bg-gray-100"
              />
            </label>
            <label className="text-sm">
              <span className="block text-gray-600">Validity</span>
              <select
                value={orderDraft.validity}
                onChange={handleField('validity')}
                className="mt-1 w-full border rounded px-2 py-1"
              >
                {VALIDITY_OPTIONS.map((o) => (
                  <option key={o} value={o}>{o}</option>
                ))}
              </select>
            </label>
            <label className="text-sm col-span-2">
              <span className="block text-gray-600">Tag (optional)</span>
              <input
                value={orderDraft.tag}
                onChange={handleField('tag')}
                placeholder="e.g. portfolio-rebalance"
                className="mt-1 w-full border rounded px-2 py-1"
              />
            </label>
            <div className="flex items-end col-span-2 md:col-span-4">
              <button
                type="submit"
                disabled={submitting || !isConnected || !selectedStock}
                className="px-5 py-2 rounded bg-blue-600 text-white text-sm font-medium hover:bg-blue-700 disabled:bg-gray-300"
                title={
                  !isConnected
                    ? 'Connect to Zerodha first'
                    : !selectedStock
                      ? 'Pick a stock from the list first'
                      : ''
                }
              >
                {submitting
                  ? 'Submitting…'
                  : `${orderDraft.transactionType} ${orderDraft.symbol || ''}`.trim()}
              </button>
              {session?.sandbox && (
                <span className="ml-3 text-xs text-amber-700">
                  Sandbox mode: orders are simulated, not sent to Kite.
                </span>
              )}
            </div>
          </form>

          {selectedStock && (
            <div className="mt-4 border-t pt-4">
              <div className="flex items-center justify-between mb-2">
                <h3 className="text-sm font-semibold text-gray-700">
                  Live quote · {selectedStock.symbol} ({selectedStock.exchange || 'NSE'})
                </h3>
                <span className="text-xs text-gray-500">
                  {quoteLoading ? 'Refreshing…' : 'auto-refresh 3s'}
                </span>
              </div>
              {quoteError && !quote && (
                <div className="text-xs text-red-700 bg-red-50 border border-red-200 rounded px-2 py-1">
                  {quoteError}
                </div>
              )}
              {quote && (
                <div className="grid grid-cols-2 md:grid-cols-4 gap-3 text-sm">
                  <div>
                    <div className="text-gray-500 text-xs">Last price</div>
                    <div className="text-lg font-semibold">
                      {quote.lastPrice != null ? `₹${Number(quote.lastPrice).toFixed(2)}` : '—'}
                    </div>
                  </div>
                  <div>
                    <div className="text-gray-500 text-xs">Change</div>
                    <div
                      className={`text-lg font-semibold ${
                        quote.change == null
                          ? 'text-gray-500'
                          : quote.change >= 0
                            ? 'text-green-700'
                            : 'text-red-700'
                      }`}
                    >
                      {quote.change != null
                        ? `${quote.change >= 0 ? '+' : ''}${Number(quote.change).toFixed(2)}`
                        : '—'}
                    </div>
                  </div>
                  <div>
                    <div className="text-gray-500 text-xs">Open / High / Low</div>
                    <div className="text-sm">
                      {quote.ohlc?.open != null ? `₹${Number(quote.ohlc.open).toFixed(2)}` : '—'}
                      {' / '}
                      {quote.ohlc?.high != null ? `₹${Number(quote.ohlc.high).toFixed(2)}` : '—'}
                      {' / '}
                      {quote.ohlc?.low != null ? `₹${Number(quote.ohlc.low).toFixed(2)}` : '—'}
                    </div>
                  </div>
                  <div>
                    <div className="text-gray-500 text-xs">Prev close</div>
                    <div className="text-sm">
                      {quote.ohlc?.close != null
                        ? `₹${Number(quote.ohlc.close).toFixed(2)}`
                        : '—'}
                    </div>
                  </div>
                </div>
              )}
              {quote?.paidDataRequired && (
                <div className="mt-3 p-2 rounded border border-amber-300 bg-amber-50 text-xs text-amber-800">
                  Real-time market data (including the 5-level depth below) requires a
                  paid <strong>Kite Connect</strong> plan (Connect ₹2,000/mo or Connect Plus
                  ₹5,000/mo). The free Kite API tier does not include market depth or
                  live LTP — this response was returned empty/null by the upstream.
                  {quote.errorType && (
                    <span className="block text-amber-700 mt-1">
                      Upstream: {quote.errorType}
                      {quote.message ? ` — ${quote.message}` : ''}
                    </span>
                  )}
                </div>
              )}
              {quote?.depth && (quote.depth.buy?.length > 0 || quote.depth.sell?.length > 0) && (
                <div className="mt-3 grid grid-cols-2 gap-3 text-xs">
                  <div>
                    <div className="text-gray-500 mb-1">Bids (buy)</div>
                    <table className="w-full">
                      <thead>
                        <tr className="text-gray-500 text-left">
                          <th className="py-0.5 pr-2">Price</th>
                          <th className="py-0.5 pr-2">Qty</th>
                          <th className="py-0.5">Orders</th>
                        </tr>
                      </thead>
                      <tbody>
                        {(quote.depth.buy || []).slice(0, 5).map((l, i) => (
                          <tr key={`b-${i}`} className="text-green-800">
                            <td className="py-0.5 pr-2">
                              {l.price != null ? `₹${Number(l.price).toFixed(2)}` : '—'}
                            </td>
                            <td className="py-0.5 pr-2">{l.quantity ?? '—'}</td>
                            <td className="py-0.5">{l.orders ?? '—'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                  <div>
                    <div className="text-gray-500 mb-1">Asks (sell)</div>
                    <table className="w-full">
                      <thead>
                        <tr className="text-gray-500 text-left">
                          <th className="py-0.5 pr-2">Price</th>
                          <th className="py-0.5 pr-2">Qty</th>
                          <th className="py-0.5">Orders</th>
                        </tr>
                      </thead>
                      <tbody>
                        {(quote.depth.sell || []).slice(0, 5).map((l, i) => (
                          <tr key={`s-${i}`} className="text-red-800">
                            <td className="py-0.5 pr-2">
                              {l.price != null ? `₹${Number(l.price).toFixed(2)}` : '—'}
                            </td>
                            <td className="py-0.5 pr-2">{l.quantity ?? '—'}</td>
                            <td className="py-0.5">{l.orders ?? '—'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </div>
          )}
        </section>

        <section className="bg-white rounded shadow p-4">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-lg font-semibold">Order book</h2>
            <button
              onClick={refresh}
              className="text-sm px-3 py-1 rounded bg-gray-100 hover:bg-gray-200"
            >
              Refresh
            </button>
          </div>
          {orders.length === 0 ? (
            <p className="text-sm text-gray-500">
              {isConnected ? 'No orders for today.' : 'Connect to Zerodha to see today\u2019s order book.'}
            </p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-left text-gray-500 border-b">
                    <th className="py-2 pr-3">Time</th>
                    <th className="py-2 pr-3">Symbol</th>
                    <th className="py-2 pr-3">Side</th>
                    <th className="py-2 pr-3">Type</th>
                    <th className="py-2 pr-3">Product</th>
                    <th className="py-2 pr-3">Qty</th>
                    <th className="py-2 pr-3">Price</th>
                    <th className="py-2 pr-3">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map((o) => (
                    <tr key={o.orderId} className="border-b last:border-0">
                      <td className="py-2 pr-3 text-gray-600">{o.orderTimestamp}</td>
                      <td className="py-2 pr-3 font-medium">{o.tradingSymbol}</td>
                      <td className={`py-2 pr-3 ${o.transactionType === 'BUY' ? 'text-green-700' : 'text-red-700'}`}>
                        {o.transactionType}
                      </td>
                      <td className="py-2 pr-3">{o.orderType}</td>
                      <td className="py-2 pr-3">{o.product}</td>
                      <td className="py-2 pr-3">{o.quantity}</td>
                      <td className="py-2 pr-3">{o.price}</td>
                      <td className="py-2 pr-3">{o.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        <section className="bg-white rounded shadow p-4">
          <h2 className="text-lg font-semibold mb-3">Recent order attempts (audit)</h2>
          {audits.length === 0 ? (
            <p className="text-sm text-gray-500">No audit rows yet.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-left text-gray-500 border-b">
                    <th className="py-2 pr-3">Time</th>
                    <th className="py-2 pr-3">Symbol</th>
                    <th className="py-2 pr-3">Side</th>
                    <th className="py-2 pr-3">Type</th>
                    <th className="py-2 pr-3">Qty</th>
                    <th className="py-2 pr-3">Stock id</th>
                    <th className="py-2 pr-3">Kite order</th>
                    <th className="py-2 pr-3">Sandbox</th>
                    <th className="py-2 pr-3">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {audits.map((a) => (
                    <tr key={a.id} className="border-b last:border-0">
                      <td className="py-2 pr-3 text-gray-600">{a.createdAt}</td>
                      <td className="py-2 pr-3 font-medium">{a.symbol}</td>
                      <td className={`py-2 pr-3 ${a.transactionType === 'BUY' ? 'text-green-700' : 'text-red-700'}`}>
                        {a.transactionType}
                      </td>
                      <td className="py-2 pr-3">{a.orderType}</td>
                      <td className="py-2 pr-3">{a.quantity}</td>
                      <td className="py-2 pr-3 text-gray-600">{a.stockId ?? '—'}</td>
                      <td className="py-2 pr-3 font-mono text-xs">{a.kiteOrderId}</td>
                      <td className="py-2 pr-3">{a.sandbox ? 'yes' : 'no'}</td>
                      <td className="py-2 pr-3 truncate max-w-xs" title={a.status}>{a.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </div>
  );
}

export default Broker;
