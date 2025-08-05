import BarChartComponent from '../components/charts/BarChartComponent'
import PieChartComponent from '../components/charts/PieChartComponent'
import LineChartComponent from '../components/charts/LineChartComponent'

const dataMonthly = [
  { date: '2023-04-30', quantity: 40000000 },
  { date: '2023-08-31', quantity: 10000000 },
  { date: '2023-10-31', quantity: 95000000 },
  { date: '2024-04-30', quantity: 105000000 },
  { date: '2025-06-30', quantity: 98000000 }
]

const pieData = [
  { name: 'SBI Bluechip Fund', value: 30 },
  { name: 'Mirae Largecap Fund', value: 25 },
  { name: 'Axis Bluechip Fund', value: 20 },
  { name: 'Canara Robeco', value: 15 },
  { name: 'Nippon India', value: 10 },
]

const valuationData = [
  { date: '2023-04-30', value: 800000 },
  { date: '2023-08-31', value: 1200000 },
  { date: '2023-10-31', value: 1800000 },
  { date: '2024-04-30', value: 1500000 },
  { date: '2025-06-30', value: 2200000 }
]

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#A020F0']

const Charts = () => (
  <div className="space-y-6">
  {/* Quantity Holding per Month - Full Row */}
  <section className="w-full bg-white shadow rounded-xl p-4">
    <h2 className="text-lg font-semibold mb-2">Quantity Holding per Month</h2>
    {/* Replace with your actual bar/line chart */}
    <div className="h-64 bg-gray-100 rounded-md flex items-center justify-center">
    {/* <ResponsiveContainer width="100%" height={200}>
        <BarChart data={dataMonthly}>
          <XAxis dataKey="date" />
          <YAxis />
          <Tooltip />
          <Bar dataKey="quantity" fill="#00BFFF" />
        </BarChart>
      </ResponsiveContainer>     */}
      <BarChartComponent data = {dataMonthly} xAxisDataKey = "date" yAxisDataKey = "quantity" fillColor = "#00BFFF"/>
      </div>
  </section>

  {/* Pie Chart + Overall MF Valuation - Two Columns */}
  <section className="grid grid-cols-1 md:grid-cols-2 gap-4">
    <div className="bg-white shadow rounded-xl p-4">
      <h2 className="text-lg font-semibold mb-2">Fund Distribution</h2>
      {/* Replace with your actual pie chart */}
      <div className="h-64 bg-gray-100 rounded-md flex items-center justify-center">
      {/* <ResponsiveContainer width="100%" height={200}>
        <PieChart>
          <Pie
            data={pieData}
            dataKey="value"
            nameKey="name"
            outerRadius={80}
            label
          >
            {pieData.map((entry, index) => (
              <Cell key={index} fill={COLORS[index % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip />
        </PieChart>
      </ResponsiveContainer>       */}
      <PieChartComponent pieData={pieData} dataKey="value" nameKey="name" />
      </div>
    </div>

    <div className="bg-white shadow rounded-xl p-4">
      <h2 className="text-lg font-semibold mb-2">Overall MF Valuation</h2>
      {/* Replace with your actual metrics or chart */}
      <div className="h-64 bg-gray-100 rounded-md flex items-center justify-center">
      {/* <ResponsiveContainer width="100%" height={200}>
        <LineChart data={valuationData}>
          <XAxis dataKey="date" />
          <YAxis />
          <Tooltip />
          <Line type="monotone" dataKey="value" stroke="#0070f3" />
        </LineChart>
      </ResponsiveContainer>       */}
      <LineChartComponent data={valuationData} xAxisDataKey="date" yAxisDataKey="value"/>
      </div>
    </div>
  </section>
</div>

)

export default Charts
