import BarChartComponent from '../components/charts/BarChartComponent';
import PieChartComponent from '../components/charts/PieChartComponent';
import LineChartComponent from '../components/charts/LineChartComponent';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#A020F0', '#FF4444', '#33B5E5', '#AA66CC'];

const Charts = ({ quantityChangeByDates, mfDistribution, valuationChangeByDates }) => {
  // ✅ Transform for stacked bar: one row per date, each mfName is a key
  const barData = quantityChangeByDates.map(entry => {
    const date = Object.keys(entry)[0];
    const values = entry[date];
    const obj = { date };
    values.forEach(v => {
      obj[v.mfName] = v.quantity;
    });
    return obj;
  });

  // List of unique mfNames
  const allMfNames = [
    ...new Set(
      quantityChangeByDates.flatMap(entry => {
        const date = Object.keys(entry)[0];
        return entry[date].map(v => v.mfName);
      })
    )
  ];

  // Build bars config
  const bars = allMfNames.map((mf, i) => ({
    dataKey: mf,
    fill: COLORS[i % COLORS.length],
  }));

  // Fund Distribution
  const firstDistributionDate = Object.keys(mfDistribution)[0];
  const pieData = (mfDistribution[firstDistributionDate] || []).map(d => ({
    name: d.mfName,
    value: d.quantity,
  }));

  // Valuation data
  const valuationData = valuationChangeByDates.map(entry => {
    const date = Object.keys(entry)[0];
    return { date, value: entry[date] };
  });

  return (
    <div className="space-y-6">
      {/* Quantity Holding per Month */}
      <section className="w-full bg-white shadow rounded-xl p-4">
        <h2 className="text-lg font-semibold mb-2">Quantity Holding per Month</h2>
        <BarChartComponent
          data={barData}
          xAxisDataKey="date"
          bars={bars}
        />
      </section>

      {/* Pie Chart + Valuation */}
      <section className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="bg-white shadow rounded-xl p-4">
          <h2 className="text-lg font-semibold mb-2">Fund Distribution ({firstDistributionDate})</h2>
          <PieChartComponent pieData={pieData} dataKey="value" nameKey="name" />
        </div>

        <div className="bg-white shadow rounded-xl p-4">
          <h2 className="text-lg font-semibold mb-2">Overall MF Valuation</h2>
          <LineChartComponent data={valuationData} xAxisDataKey="date" yAxisDataKey="value" />
        </div>
      </section>
    </div>
  );
};

export default Charts;
