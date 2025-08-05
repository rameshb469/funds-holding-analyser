import React from 'react';
import {
    PieChart, Pie, Cell,Tooltip, ResponsiveContainer
  } from 'recharts'

const PieChartComponent = ({pieData, dataKey, nameKey}) => {
    const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#A020F0']
    return (
        <>
            <ResponsiveContainer width="100%" height={200}>
                <PieChart>
                    <Pie
                        data={pieData}
                        dataKey={dataKey}
                        nameKey={nameKey}
                        outerRadius={80}
                        label
                    >
                        {pieData.map((entry, index) => (
                        <Cell key={index} fill={COLORS[index % COLORS.length]} />
                        ))}
                    </Pie>
                <Tooltip />
                </PieChart>
            </ResponsiveContainer> 
        </>
    )
}

export default PieChartComponent;