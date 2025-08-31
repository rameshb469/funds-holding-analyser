// constants/navLinks.js (or directly in Header.jsx if local)
export const navLinks = [
    { name: "Dashboard", path: "/" },
    { name: "StockView", path: "/stocks" },
    {
      name: "Reports",
      children: [
        { name: "Mutual Fund Configuration", path: "/reports/configuration" },
        { name: "Mutual Fund Download URLs", path: "/reports/download-urls" },
      ],
    },
  ];
  