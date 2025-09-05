// src/components/SectorIndustryHeatmap.jsx
import React from "react";
import {
  Treemap,
  ResponsiveContainer,
  Tooltip,
} from "recharts";

// ✅ Utility: Build Hierarchy
const buildHierarchy = (data) => {
  const sectorMap = {};

  data.forEach((d) => {
    if (!sectorMap[d.sectorName]) {
      sectorMap[d.sectorName] = {
        name: d.sectorName,
        children: {},
        totalValue: 0,
      };
    }
    const sector = sectorMap[d.sectorName];

    if (!sector.children[d.industryName]) {
      sector.children[d.industryName] = {
        name: d.industryName,
        children: [],
        totalValue: 0,
      };
    }
    const industry = sector.children[d.industryName];

    industry.children.push({
      name: d.stockSymbol,
      size: d.currentValue,
      changePct: d.changePct,
    });

    industry.totalValue += d.currentValue;
    sector.totalValue += d.currentValue;
  });

  return Object.values(sectorMap).map((sector) => ({
    name: sector.name,
    size: sector.totalValue,
    children: Object.values(sector.children).map((ind) => ({
      name: ind.name,
      size: ind.totalValue,
      children: ind.children,
    })),
  }));
};

// Color scale based on % change
const getColor = (pct) => {
  if (pct > 15) return "#004d00";      // very dark green
  if (pct > 5) return "#28a745";       // medium green
  if (pct > 0) return "#66BB6A";       // light green
  if (pct < -15) return "#660000";     // very dark red
  if (pct < -5) return "#dc3545";      // medium red
  if (pct < 0) return "#FF6666";       // light red
  return "#B0BEC5";                    // neutral gray
};

const HeatmapBlock = ({ title, data }) => {
  const treeData = buildHierarchy(data);

  return (
    <div className="bg-white shadow rounded-xl p-4">
      <h2 className="text-lg font-semibold text-gray-700 mb-2">{title}</h2>
      <ResponsiveContainer width="100%" height={450}>
        <Treemap
          data={treeData}
          dataKey="size"
          nameKey="name"
          stroke="#fff"
          content={({ x, y, width, height, name, payload }) => {
            const changePct = payload?.changePct;
            const color =
              changePct !== undefined ? getColor(changePct) : "#ccc";

            return (
              <g>
                <rect
                  x={x}
                  y={y}
                  width={width}
                  height={height}
                  style={{ fill: color, stroke: "#fff" }}
                />
                {width > 60 && height > 20 && (
                  <text
                    x={x + 4}
                    y={y + 16}
                    fontSize={12}
                    fill="#000"
                    fontWeight="bold"
                  >
                    {name}
                  </text>
                )}
              </g>
            );
          }}
        >
          <Tooltip
            formatter={(val, name, entry) => {
              const d = entry?.payload;
              return [
                `Value: ${d?.size?.toLocaleString()} | Change: ${d?.changePct?.toFixed(
                  2
                )}%`,
                d?.name,
              ];
            }}
          />
        </Treemap>
      </ResponsiveContainer>
    </div>
  );
};

const SectorIndustryHeatmap = ({ data }) => {
  const increasing = data.filter((d) => d.changePct > 0);
  const decreasing = data.filter((d) => d.changePct < 0);

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <HeatmapBlock title="📈 Increasing Stocks (by Sector & Industry)" data={increasing} />
      <HeatmapBlock title="📉 Decreasing Stocks (by Sector & Industry)" data={decreasing} />
    </div>
  );
};

export default SectorIndustryHeatmap;
