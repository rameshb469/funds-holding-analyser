import React from 'react';
import { ArrowUpRight, ArrowDownRight, Minus } from 'lucide-react';

const MetricCard = ({ title, value, change }) => {
  let changeColor = 'text-gray-600';
  let ChangeIcon = Minus;

  if (change > 0) {
    changeColor = 'text-green-600';
    ChangeIcon = ArrowUpRight;
  } else if (change < 0) {
    changeColor = 'text-red-600';
    ChangeIcon = ArrowDownRight;
  }

  return (
    <div className="bg-white p-4 rounded-xl shadow flex flex-col items-start">
      <h3 className="text-sm text-gray-500">{title}</h3>
      <p className="text-2xl font-semibold">{value}</p>
      {change !== undefined && (
        <div className={`flex items-center text-sm font-medium ${changeColor}`}>
          <ChangeIcon size={14} className="mr-1" />
          {change > 0 ? `+${change.toFixed(2)}` : change.toFixed(2)}
        </div>
      )}
    </div>
  );
};

export default MetricCard;
