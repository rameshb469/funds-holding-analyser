// components/Header.jsx
import React, { useEffect, useRef, useState } from "react";
import { Link, useLocation } from "react-router-dom";

function cx(...classes) {
  return classes.filter(Boolean).join(" ");
}

export default function Header() {
  const location = useLocation();
  const [openDropdown, setOpenDropdown] = useState(null);
  const reportsRef = useRef(null);

  const navLinks = [
    { name: "Dashboard", path: "/" },
    { name: "StockView", path: "/StockView" },
    { name: "Configurations", path: "/reports/configuration" },
    { name: "Download Report", path: "/reports/download-urls" }
    // {
    //   name: "Reports",
    //   children: [
    //     { name: "Configuration", path: "/reports/configuration" },
    //     { name: "Download URLs", path: "/reports/download-urls" },
    //   ],
    // },
  ];

  // Close on click outside
  useEffect(() => {
    function onDocClick(e) {
      if (reportsRef.current && !reportsRef.current.contains(e.target)) {
        setOpenDropdown(null);
      }
    }
    document.addEventListener("mousedown", onDocClick);
    return () => document.removeEventListener("mousedown", onDocClick);
  }, []);

  // Close on Escape
  useEffect(() => {
    function onKey(e) {
      if (e.key === "Escape") setOpenDropdown(null);
    }
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, []);

  return (
    <header className="flex justify-between items-center p-4 bg-white shadow-md sticky top-0 z-40">
      <h1 className="text-xl font-bold">Mutual Fund Dashboard</h1>

      <nav className="flex gap-4 items-center">
        {navLinks.map((link) =>
          link.children ? (
            <div key={link.name} className="relative" ref={reportsRef}>
              <button
                type="button"
                onClick={() =>
                  setOpenDropdown((prev) => (prev === link.name ? null : link.name))
                }
                aria-haspopup="menu"
                aria-expanded={openDropdown === link.name}
                className="text-sm px-4 py-2 rounded bg-gray-100 hover:bg-gray-200 text-gray-800"
              >
                {link.name} ▾
              </button>

              {openDropdown === link.name && (
                <div
                  role="menu"
                  className="absolute top-full left-0 mt-2 w-72 bg-white border shadow-md rounded z-50 overflow-hidden"
                >
                  {link.children.map((child) => (
                    <Link
                      key={child.name}
                      to={child.path}
                      // Use onMouseDown so navigation triggers before button loses focus/closing
                      onMouseDown={() => setOpenDropdown(null)}
                      className={cx(
                        "block px-4 py-2 text-sm",
                        location.pathname === child.path
                          ? "bg-blue-600 text-white"
                          : "hover:bg-gray-100 text-gray-800"
                      )}
                    >
                      {child.name}
                    </Link>
                  ))}
                </div>
              )}
            </div>
          ) : (
            <Link
              key={link.name}
              to={link.path}
              className={cx(
                "text-sm px-4 py-2 rounded",
                location.pathname === link.path
                  ? "bg-blue-600 text-white"
                  : "bg-gray-100 hover:bg-gray-200 text-gray-800"
              )}
            >
              {link.name}
            </Link>
          )
        )}
      </nav>
    </header>
  );
}
