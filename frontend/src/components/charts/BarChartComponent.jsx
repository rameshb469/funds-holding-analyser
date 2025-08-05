import React from 'react';
import {
    BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer
  } from 'recharts'

const BarChartComponent = ({data, xAxisDataKey, quantity}) => {

    return (
        <>
            <ResponsiveContainer width="100%" height={200}>
                <BarChart data={data}>
                    <XAxis dataKey={xAxisDataKey} />
                    <YAxis />
                    <Tooltip />
                    <Bar dataKey={quantity} fill="#00BFFF"/>
                </BarChart>
            </ResponsiveContainer>  
        </> 
    )
}

export default BarChartComponent