// constants/navLinks.js
export const navLinks = [
  { name: "Dashboard", path: "/" },
  { name: "StockView", path: "/stocks" },
  { name: 'Holding Change', path: "/reports/holding-change" },
  {
    name: "Reports",
    children: [
      { name: "Mutual Fund Configuration", path: "/reports/configuration" },
      { name: "Mutual Fund Download URLs", path: "/reports/download-urls" },
      { name: "Volume Report", path: "/reports/volume" }
    ],
  },
];
