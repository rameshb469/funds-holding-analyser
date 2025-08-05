import React from 'react';
import {
    LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer  
  } from 'recharts'

const LineChartComponent = ({data, xAxisDataKey, yAxisDataKey}) => {
    return (
        <>
            <ResponsiveContainer>
                <LineChart data={data}>
                    <XAxis dataKey={xAxisDataKey} />
                    <YAxis />
                    <Tooltip />
                    <Line type="monotone" dataKey={yAxisDataKey} stroke="#0070f3" />
                </LineChart>
            </ResponsiveContainer> 
        </>
    )
}

export default LineChartComponent;